(ns warmagnet.views.prefs
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [pump.core :refer [assoc-state assoc-in-state e-value]]
            [warmagnet.md5 :refer [md5]]
            [warmagnet.utils :refer [log]]
            [warmagnet.handlers :as handlers]))

(defr Preferences
  :component-will-mount
  (fn [C P S]
    (assoc-state C P))

  :component-will-receive-props
  (fn [C P S next-props]
    (assoc-state C P))

  [C P {:keys [email name] :as S}]

  [:div.col-md-4.col-md-offset-4
   [:h1 "Preferences"]
   [:form.well
    {:role "form" :on-submit #(handlers/save-prefs % S)}

    [:div.pull-right
     [:img {:src (str "http://www.gravatar.com/avatar/"
                      (md5 (.toLowerCase (or email ""))))}]]

    [:div.form-group
     [:label "EMAIL"]
     [:p email]]
    
    [:div.form-group
     [:label {:html-for "name"} "NAME"]
     [:input.form-control {:name "name"
                           :value name
                           :on-change #(assoc-in-state C :name (e-value %))}]]
    
    [:button.btn.btn-primary {:type "submit"} "Save"]]])

