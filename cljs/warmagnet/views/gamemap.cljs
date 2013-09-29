(ns warmagnet.views.gamemap
  (:require-macros [pump.def-macros :refer [defr]]
                   [warmagnet.macros :refer [cx]])
  (:require [pump.core :refer [assoc-in-state e-value]]
            [warmagnet.utils :refer [send-message]]))

(defn xhr [url callback]
  (let [xhr (js/XMLHttpRequest.)]
    (.open xhr "GET" url true)
    (aset xhr "onreadystatechange"
          #(if (= 4 (.-readyState xhr))
             (callback (.-responseText xhr))))
    (.send xhr)))

(defn xhr-load-map []
  (xhr "/static/map-classic.json"
       #(let [json (.parse js/JSON %)
              data (js->clj json :keywordize-keys true)
              message {:type :map-received :data data}]
          (send-message message))))

(defn get-map-style [[x y] container-width]
  (let [padding 30
        scale (/ (- container-width padding) x)
        str-scale (str "scale(" scale ")")
        width (* x scale)
        height (* y scale)]
    (clj->js {:-webkit-transform str-scale
              :transform str-scale
              :width width
              :height height})))

(defr MapDistrict
  [C {:keys [district hovered assoc-hovered]} S]
  (let [[dname {:keys [borders coordinates]}] district
        [x y] coordinates
        str-name (name dname)

        [hname hmap] hovered
        this-hovered (= dname hname)
        attacker-hovered (some #{str-name} (:borders hmap))
        irregular (or this-hovered attacker-hovered)]
    [:div
     {:class (cx :map-district true
                 :label true
                 :label-success (not irregular)
                 :label-info this-hovered
                 :label-danger attacker-hovered)
      :style (clj->js {:left x :top y})
      :on-mouse-enter (fn [C] (assoc-hovered district))
      :on-mouse-leave (fn [C] (assoc-hovered nil))}
     2]))

(defr Deploy
  :component-will-mount
  (fn [C {:keys [available-troops]} S]
    (assoc-in-state C :to-deploy (/ available-troops 2)))
  
  [C {:keys [available-troops]} {:keys [to-deploy]}]
  [:div.popover {:style (clj->js {:display "block"})}
   [:div.popover-content
    [:div.input-group
     [:span.input-group-btn
      [:button.btn.btn-success "Deploy"]]
     [:input.form-control
      {:on-change #(if-let [v (js/parseInt (e-value %))]
                     (if (<= 1 v 40)
                       (assoc-in-state C :to-deploy (e-value %))))
       :type "number" :value to-deploy :min 1 :max available-troops}]]
    [:div.input-group
     [:span.input-group-addon "1"]
     [:input.form-control
      {:on-change #(assoc-in-state C :to-deploy (e-value %))
       :type "range" :value to-deploy :min 1 :max available-troops}]
     [:span.input-group-addon available-troops]]]])

(defr Attack
  [C {:keys [attacking defending]} S]
  [:div.popover {:style (clj->js {:display "block" :top "240px"})}
   [:div.popover-content
    [:table {:style (clj->js {:width "100%" :text-align "center"})}
     [:thead [:tr [:th (:name attacking)] [:th (:name defending)]]]
     [:tbody [:tr [:td (:troops attacking)] [:td (:troops defending)]]]]
    [:div.btn-group
     [:button.btn.btn-default "Cancel"]
     [:button.btn.btn-warning "Attack"]
     [:button.btn.btn-danger "Blitz"]]]])

(defr GameMap
  :component-will-mount (fn [C] (if-not (:name (. C -props))
                                  (xhr-load-map)))
  [C
   {:keys [name map-src regions districts dimensions container-width]}
   {:keys [hovered]}]
  (let [assoc-hovered #(assoc-in-state C :hovered %)]
    (if-not name
      [:div "No map"]
      [:div.game-map.row
       {:style (get-map-style dimensions container-width)}
       [:img {:src (str "/static/" map-src)}]
       (map (fn [district] [MapDistrict {:district district
                                         :assoc-hovered assoc-hovered
                                         :hovered hovered}])
            districts)
       [Deploy {:available-troops 40}]
       [Attack {:attacking {:name "Ukraine" :troops 142}
                :defending {:name "Germany" :troops 30}}]])))
