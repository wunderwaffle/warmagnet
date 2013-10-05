(ns warmagnet.views.prefs
  (:require-macros [pump.macros :refer [defr]])
  (:require [pump :refer [e-value]]
            [warmagnet.md5 :refer [md5]]
            [warmagnet.utils :refer [log]]
            [warmagnet.handlers :as handlers]))

(defr Preferences
  :component-will-mount
  (fn [C P S]
    (swap! C merge P))

  :component-will-receive-props
  (fn [C P S next-props]
    (swap! C merge P))

  [C P {:keys [email name] :as S}]

  [:div.col-md-6.col-md-offset-2
   (if-not (:name P)
     [:div.container
       [:h1 "Welcome"]
       [:p.lead "Thanks for signing up! Please set your user name to start playing."]]

     [:h1 "Preferences"])

   [:form.well
    {:role "form" :on-submit #(handlers/save-prefs % S)}

    [:div.pull-right
     [:img {:src (str "http://www.gravatar.com/avatar/"
                      (md5 (.toLowerCase (or email ""))))}]]

    [:div.form-group
     [:label "Email"]
     [:p email]]

    [:div.form-group
     [:label {:html-for "name"} "Name"]
     [:input.form-control {:name "name"
                           :value name
                           :on-change #(swap! C assoc :name (e-value %))}]]
    [:button.btn.btn-primary {:type "submit"} "Save"]]])

