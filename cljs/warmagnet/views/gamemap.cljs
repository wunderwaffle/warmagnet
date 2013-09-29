(ns warmagnet.views.gamemap
  (:require-macros [pump.def-macros :refer [defr]]
                   [warmagnet.macros :refer [cx]])
  (:require [pump.core :refer [assoc-in-state]]
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
  [C {:keys [name coordinates borders hovered assoc-hovered]} S]
  (let [this-hovered (#{name} (:name hovered))
        attacker-hovered (some #{name} (:borders hovered))
        irregular (or this-hovered attacker-hovered)]
    [:div
     {:class (cx :map-district true
                 :label true
                 :label-success (not irregular)
                 :label-info this-hovered
                 :label-danger attacker-hovered)
      :style (clj->js {:left (first coordinates)
                       :top (second coordinates)})
      :on-mouse-enter (fn [C] (assoc-hovered {:name name :borders borders}))
      :on-mouse-leave (fn [C] (assoc-hovered nil))}
     2]))

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
       (map (fn [district] [MapDistrict (assoc district
                                          :assoc-hovered assoc-hovered
                                          :hovered hovered)])
            districts)])))
