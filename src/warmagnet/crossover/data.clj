(ns warmagnet.crossover.data)

(defn check-transition [game-state msg]
	true)

(defn set-district [game {:keys [user-id district amount] :as msg}]
	(update-in game [:districts district] assoc :user-id user-id :amount amount))

(defn game-transition [game {:keys [type user-id] :as msg}]
  (let [game (update-in game [:log] conj msg)]
  	(case (keyword type)
    	:join (update-in game [:players] conj
                         {:id user-id :name (:user-name msg)})
    	:set-district (set-district game msg)
    	game)))
