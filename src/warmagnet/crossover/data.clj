(ns warmagnet.crossover.data)

(defn check-transition [game msg]
	true)

(defn game-transition [game {:keys [type user-id] :as msg}]
  (let [game-state (update-in game [:log] conj msg)]
  	(case (keyword type)
    	:join (update-in game-state [:players] assoc user-id (:user-name msg))
    	game-state)))
