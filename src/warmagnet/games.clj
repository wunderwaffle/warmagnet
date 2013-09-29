(ns warmagnet.games
  (:require
            [cheshire.core :as json]

  			[warmagnet.db :as db]
  			[warmagnet.crossover.data :as crossover]
            ))

(def all-games (atom {}))

;; internal api
(defn create-game-state [game]
	{:id (:id game)
	 :options (json/decode (:data game) true)
	 :log []
	 :players {}
	 :watchers #{}})

(defn replay-game-log [game-state game-log]
	(reduce #(crossover/game-transition %1 %2) game-state game-log))

(defn get-game-state [game]
	(let [game-log (->> (db/get-game-log (:id game))
						(mapv #(json/decode (:data %) true)))]
		(replay-game-log (create-game-state game) game-log)))

(defn make-join-log-item [user]
	{:type "join" :user-id (:id user) :user-name (:name user)})

;; public api
(defn create-game [data]
	(let [game (db/new-game (json/encode data))]
		(create-game-state game [])))

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
		(db/add-game-log id (:type data) (json/encode data))
		(swap! all-games assoc id (crossover/game-transition game-state data))))

;; watchers
(defn add-watcher [id user-id]
	(swap! all-games update-in [id :watchers] conj user-id))

(defn remove-watcher [id user-id]
	(swap! all-games update-in [id :watchers] disj user-id))

(defn get-watchers [id]
	(get-in @all-games [id :watchers]))

;; game state
(defn process-game-log-item [data user-id]
  (assoc data :user-id user-id))
