(ns warmagnet.games
  (:require [clojure.set :refer [subset?]]
  			[warmagnet.db :as db]
  			[warmagnet.utils :as utils]
            [warmagnet.crossover.data :as crossover]))

(def all-games (atom {}))

(def map-file "resources/static/map-classic.json")

(declare execute-log-item)

;; internal api
(defn create-game-state [game]
  (let [map (utils/load-json map-file)]
	{:id (:id game)
	 :options (:data game)
	 :log []
	 :players []
     :map map
     :districts {}
     :player-state {}
     :turn-by nil
	 :watchers #{}}))

(defn replay-game-log [game-state game-log]
	(reduce #(crossover/game-transition %1 %2) game-state game-log))

(defn get-game-state [game]
	(let [game-log (db/get-game-log (:id game))]
		(replay-game-log (create-game-state game) game-log)))

;; public api
(defn create-game [data]
	(let [game (db/new-game data)
		  game-state (create-game-state game)]
			(swap! all-games assoc (:id game) game-state)
			game-state))

(defn load-game [id]
	(let [game (db/get-game id)]
		(if-not (nil? game)
			(let [game-state (get-game-state game)]
				(swap! all-games assoc id game-state)
				game-state))))

(defn get-game [id]
	(let [game (@all-games id)]
		(if (nil? game)
			(load-game id)
			game)))

;; watchers
(defn disj-set [x y] (or (disj x y) #{}))

(defn add-watcher [id user-id]
	(swap! all-games update-in [id :watchers] conj user-id))

(defn remove-watcher [id user-id]
	(if-let [game (@all-games id)]
		(swap! all-games update-in [id :watchers] disj-set user-id)))

(defn get-watchers [id]
	(get-in @all-games [id :watchers]))

;; players
(defn is-player-in-game [id user-id]
	(db/is-user-in-game id user-id))

(defn player-in-state [game-state user-id]
	(some #{user-id} (map :id (:players game-state))))

;; log verificaton
(defn preprocess-game-log-item [id data user-id]
  (if-let [game-state (get-game id)]
    (let [data (assoc data :user-id user-id)]
      (if (and (player-in-state game-state user-id)
               (crossover/check-transition game-state data))
        data))))

;; helpers
(defn is-game-started [game-state]
	(boolean (get game-state [:options :started])))

(defn can-join-game [id user-id]
	(let [game-state (get-game id)]
		(and (not (player-in-state game-state user-id)) (not (is-game-started game-state)))))

(defn make-join-log-item [user]
	{:type "join" :user-id (:id user) :user-name (:name user)})

(defn make-log-message [game-id data]
  {:type "game" :game-id game-id :data data})

(defn has-region [districts region]
  (subset? (into #{} districts) (into #{} (:districts region))))

(defn owns-district
  [[name district] user-id]
  (= (:user-id district) user-id))

; TODO: Fix me - circular reference
(defn log-broadcast [id data]
	(let [game-broadcast (resolve 'warmagnet.app/game-broadcast)]
		(game-broadcast id (make-log-message id data))))

(defn add-log-item [{:keys [id] :as game-state} data]
	(db/add-game-log id data)
	(log-broadcast id data)
	(execute-log-item (crossover/game-transition game-state data) data))

(defn apply-log-item [id data]
	(let [game-state (get-game id)
		  new-state (add-log-item game-state data)]
		(swap! all-games assoc id new-state)))

(defn add-player [id user]
    (db/add-user-to-game id (:id user))
	(apply-log-item id (make-join-log-item user)))

;; game logic
(defn start-game [game-state]
	(let [id (:id game-state)
		  options (assoc (:options game-state) :started true)
		  new-state (assoc game-state :options options)]
		(db/update-game-data id options)
		(add-log-item new-state {:type "start"})))

(defn maybe-start-game [game-state]
	(if (= (get-in game-state [:options :size]) (count (:players game-state)))
		(start-game game-state)
		game-state))

(defn initial-distribute-districts [game-state]
	(let [districts (keys (get-in game-state [:map :districts]))
		  users (map :id (:players game-state))
		  joined (zipmap districts (cycle users))
		  distributor (fn [game-state [district user-id]]
                        (add-log-item game-state {:type "set-district"
                                                  :user-id user-id
                                                  :district district
                                                  :amount 3}))]
      (reduce distributor game-state joined)))

(defn initialize-game [game-state]
  (-> game-state
      (initial-distribute-districts)
      (add-log-item {:type "turn" :user-id (:id (first (:players game-state)))})))

(defn new-turn-supply [game-state user-id]
  (let [districts (->> (get game-state :districts)
                       (filter #(owns-district % user-id))
                       (map first))
        regions (get-in game-state [:map :regions])
        region-supply (fn [game-state [name region]]
                        (if (has-region districts region)
                          (add-log-item game-state {:type "supply"
                                                    :user-id user-id
                                                    :amount (:bonus region)
                                                    :reason name})
                          game-state))]
    (-> game-state
        (add-log-item {:type "supply"
                       :user-id user-id
                       :amount (/ (count districts) 3)
                       :reason (str (count districts) " districts")})
        (#(reduce region-supply % regions)))))

(defn maybe-start-attack [game-state {:keys [user-id]}]
	(let [amount (get-in game-state [:player-state user-id :supply])]
      (if ((fnil zero? 0) amount)
        (add-log-item game-state
                      {:type "phase" :user-id user-id :phase crossover/PHASE-ATTACK}))))

(defn maybe-conquer [game-state {:keys [attack-to user-id]}]
	(let [target-amount (get-in game-state [:districts attack-to :amount])]
		(if (< target-amount 0)
			(add-log-item game-state {:type "conquer" :user-id user-id :district attack-to :amount (- target-amount)}))))

(defn finish-game [{:keys [id turn-by] :as game-state}]
	(db/finish-game id turn-by)
	(add-log-item game-state {:type "finish" :winner turn-by}))

(defn maybe-finish [game-state]
	(let [user-id (:turn-by game-state)
		  districts (:districts game-state)
		  num-conquered (count (filter #(= user-id (:user-id %)) (vals districts)))
		  district-count (count districts)]
		  (println num-conquered district-count)
		  (if (= num-conquered district-count)
		  	(finish-game game-state))))

(defn next-player [game-state user-id]
	(let [players (:players game-state)]
		(or (second (drop-while #(not= user-id (:id %)) players)) (first players))))

(defn next-turn [game-state]
	(let [next-player (next-player game-state (:turn-by game-state))]
		(add-log-item game-state {:type "turn" :user-id (:id next-player)})))

(defn execute-log-item [game-state {:keys [user-id] :as data}]
	(case (keyword (:type data))
		:join (maybe-start-game game-state)
		:start (initialize-game game-state)
	    :turn (new-turn-supply game-state user-id)
		:deploy (maybe-start-attack game-state data)
		:attack (maybe-conquer game-state data)
		:conquer (maybe-finish game-state)
		:reinforce-end (next-turn game-state)
		game-state))
