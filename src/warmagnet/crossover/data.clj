(ns warmagnet.crossover.data)

(def PHASE-DEPLOY "deploy")
(def PHASE-ATTACK "attack")
(def PHASE-REINFORCE "reinforce")

;; check
(defn check-deploy [game {:keys [user-id district amount]}]
	(let [turn-by (:turn-by game)
		  player-state (get-in game [:player-state user-id])
		  phase (:phase player-state)
		  supply (:supply player-state 0)
		  district-owner (get-in game [:districts district :user-id])
		  delta (- supply amount)]
		  (and (= turn-by user-id) (= phase PHASE-DEPLOY) (= district-owner user-id) (>= delta 0))))

(defn check-attack [game {:keys [user-id attack-from attack-to amount]}]
	(let [turn-by (:turn-by game)
		  player-state (get-in game [:player-state user-id])
		  phase (:phase player-state)
		  source (get-in game [:districts attack-from])
		  source-supply (:amount source)
		  source-district (get-in game [:map :districts (keyword attack-from)])
		  source-connects-target (boolean (some #{attack-to} (:borders source-district)))
		  amount-valid (> source-supply amount)]
		  (and (= turn-by user-id) (= phase PHASE-ATTACK) amount-valid source-connects-target)
		))

(defn check-attack-end [game {:keys [user-id]}]
	(let [turn-by (:turn-by game)
  		  player-state (get-in game [:player-state user-id])
		  phase (:phase player-state)]
		  (and (= turn-by user-id) (= phase PHASE-ATTACK))))

(defn check-transition [game msg]
	(case (keyword (:type msg))
		:deploy (check-deploy game msg)
		:attack (check-attack game msg)
		:attack-end (check-attack-end game msg)
		false))

;; set
(defn set-district [game {:keys [user-id district amount] :as msg}]
	(update-in game [:districts district] assoc :user-id user-id :amount amount))

(defn handle-deploy [game {:keys [user-id district amount]}]
	(-> game
		(update-in [:player-state user-id :supply] (fnil - 0) amount)
		(update-in [:districts district :amount] (fnil + 0) amount)))

(defn handle-attack [game {:keys [user-id attack-from attack-to amount]}]
	(-> game
		(update-in [:districts attack-from :amount] (fnil - 0) amount)
		(update-in [:districts attack-to :amount] (fnil - 0) amount)))

(defn game-transition [game {:keys [type user-id] :as msg}]
  (let [game (update-in game [:log] conj msg)]
  	(case (keyword type)
    	:join (update-in game [:players] conj
                         {:id user-id :name (:user-name msg)})
    	:set-district (set-district game msg)
        :supply (update-in game [:player-state user-id :supply]
                           (fnil + 0) (:amount msg))
    	:conquer (set-district game msg)
    	:turn (-> game
    			  (assoc :turn-by user-id)
    			  (assoc-in [:player-state user-id :phase] PHASE-DEPLOY))
    	:phase (assoc-in game [:player-state user-id :phase] (:phase msg))
    	:deploy (handle-deploy game msg)
    	:attack (handle-attack game msg)
    	:attack-end (assoc-in game [:player-state user-id :phase] PHASE-REINFORCE)
    	:reinforce-end (assoc-in game [:player-state user-id :phase] nil)
    	game)))
