(ns warmagnet.components
  (:require-macros [pump.def-macros :refer [defr]])
  (:require
   [pump.core :refer [react]]
   [warmagnet.handlers :as handlers]))

(defr SigninButton
  {:render (fn [C P S]
             (let [user (:user P)
                   handler (if user handlers/persona-sign-out handlers/persona-sign-in)
                   text (if user "Sign Out" "Sign In")]
               [:button.btn.btn-success
                {:type "button" :on-click handler}
                text]))})

(defr Navbar
  {:render (fn [C P S]
             [:div.navbar.navbar-inverse.navbar-fixed-top
              [:div.container
               [:div.navbar-header
                [:a.navbar-brand {:href "/"} "WarMagnet"]]
               [:div.navbar-collapse.collapse
                [:ul.nav.navbar-nav
                 [:li [:a {:href "#games/new"} "New Game"]]
                 [:li [:a {:href "#leaderboard"} "Leaderboard"]]
                 [:li [:a {:href "#preferences"} "Preferences"]]
                 [:form.navbar-form.navbar-right
                  [SigninButton {:user (:user P)}]]]]]])})

(defr Preferences
  {:render (fn [C user S]
             [:div.col-md-4.col-md-offset-4
              [:h1 "Preferences"]
              [:form.well {:role "form" :on-submit handlers/save-prefs}

               (for [input ["login" "name"]]
                 [:div.form-group
                  [:label {:for input} (.toUpperCase input)]
                  [:input.form-control {:id input :value ((keyword input) user)}]])

               [:button.btn.btn-primary {:type "button"} "Save"]
               ]])})

(defr NewGame
  {:render (fn [C P S]
             [:form {:role "form" :on-submit handlers/new-game}
              [:div.form-group
               [:label {:for "round_time"} "Round time"]
               [:select#round_time.form-control
                [:option {:value "short"} "5 mins"]
                [:option {:value "long"} "24 hours"]]]
              [:div.form-group
               [:label {:for "reinforcement"} "Reinforcements"]
               [:select#reinforcement.form-control
                [:option {:value "adjacent"} "Adjacent"]
                [:option {:value "chained"} "Chained"]
                [:option {:value "unlimited"} "Unlimited"]]]
              [:button.btn.btn-success {:type "submit"} "Create"]])})

(defn tags [tagname values]
  (map (fn [v] [tagname v]) values))

(defr Leaderboard
  {:render (fn [C {:keys [players]} S]
             [:table.table.table-striped
              [:thead [:tr (tags :td ["Name" "Score" "Completed" "Won" "User Rating"])]]
              [:tbody (map (fn [p] [:tr (tags :td [(p :name) (p :score)
                                                   (p :completed) (p :won)
                                                   (p :user-rating)])])
                           players)]])})

(defr GameItem
  {:render (fn [C {:keys [game-map participants round-time reinforcements]} S]
             [:div
              [:img {:src game-map}]
              [:p "Players"]
              [:ul [tags :li (map :name participants)]]
              [:p round-time]
              [:p reinforcements]])})

(defr Profile
  {:render (fn [C P S]
             [:div "Profile!"])})
