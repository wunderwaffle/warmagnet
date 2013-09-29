(ns warmagnet.views.navbar
  (:require-macros [pump.def-macros :refer [defr]])
  (:require
   [warmagnet.handlers :as handlers]))

(defr SigninButton
  [C {:keys [user]} S]
  (let [handler (if user handlers/persona-sign-out handlers/persona-sign-in)
        text (if user "Sign Out" "Sign In")]
    [:button.btn.btn-success
     {:type "button" :on-click handler}
     text]))

(defr Navbar
  [C {:keys [user]} S]
  [:div.navbar.navbar-inverse.navbar-fixed-top
   [:div.container
    [:div.navbar-header
     [:a.navbar-brand {:href "#"}
      [:img {:src "/static/logo/logo-white-ak-50-nobg.png"}]
      "WarMagnet"]]
     [:div.navbar-collapse.collapse
      (if user
        [:ul.nav.navbar-nav

         [:li [:a {:href "#games"} "Games"]]
         [:li [:a {:href "#games/new"} "New Game"]]
         [:li [:a {:href "#leaderboard"} "Leaderboard"]]
         [:li [:a {:href "#preferences"} "Preferences"]]])
      [:form.navbar-form.navbar-right
       [SigninButton {:user user}]]]]])

