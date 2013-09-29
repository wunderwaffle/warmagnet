(ns warmagnet.games
  (:require
  			[warmagnet.db :as db]
  			[warmagnet.utils :as utils]
  			[warmagnet.crossover.data :as crossover]
            ))

(def all-games (atom {}))

(def map-file "resources/static/map-classic.json")

;; internal api
(defn create-game-state [game]
  (let [map (utils/load-json map-file)]
	{:id (:id game)
	 :options (:data game)
	 :log []
	 :players {}
         :map map
	 :watchers #{}}))

(defn replay-game-log [game-state game-log]
	(reduce #(crossover/game-transition %1 %2) game-state game-log))

(defn get-game-state [game]
	(let [game-log (db/get-game-log (:id game))]
		(replay-game-log (create-game-state game) game-log)))

(defn make-join-log-item [user]
	{:type "join" :user-id (:id user) :user-name (:name user)})

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

(defn add-log [id data]
	(when-let [game-state (get-game id)]
		(db/add-game-log id data)
		(swap! all-games assoc id (crossover/game-transition game-state data))))

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
(defn add-player [id user-id]
    (db/add-user-to-game id user-id))

(defn is-player-in-game [id user-id]
	(db/is-user-in-game id user-id))

;; game state
(defn player-in-state [game-state user-id]
	(contains? (:players game-state) user-id))

(defn process-game-log-item [id data user-id]
	(if-let [game-state (get-game id)]
		(let [new-data (assoc data :user-id user-id)]
			(if (and (player-in-state game-state user-id) (crossover/check-transition game-state data))
	  			new-data))))
