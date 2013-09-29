(ns warmagnet.views.navbar
  (:require-macros [pump.def-macros :refer [defr]]
                   [warmagnet.macros :refer [cx]])
  (:require
   [warmagnet.handlers :as handlers]))

(defr SigninButton
  [C {:keys [user]} S]
  (let [handler (if user handlers/persona-sign-out handlers/persona-sign-in)
        text (if user "Sign Out" "Sign In")]
    [:button.btn.btn-success
     {:type "button" :on-click handler}
     text]))

(defn nav-item [name route current]
  [:li {:class (cx :active (= route current))}
   [:a {:href (str "#" route)} name]])

(defr Navbar
  [C {:keys [user route]} S]
  [:div.navbar.navbar-inverse.navbar-fixed-top
   [:div.container
    [:div.navbar-header
     [:a.navbar-brand {:href "#"}
      [:img {:src "/static/logo/logo-white-ak-50-nobg.png"}]
      "WarMagnet"]]
     [:div.navbar-collapse.collapse

      (if user
        [:ul.nav.navbar-nav
         (nav-item "My Games" "games" route)
         (nav-item "New Game" "games/new" route)
         (nav-item "Browse" "browse" route)
         (nav-item "Leaderboard" "leaderboard" route)
         (nav-item "Preferences" "preferences" route)])

      [:form.navbar-form.navbar-right
       [SigninButton {:user user}]]]]])

