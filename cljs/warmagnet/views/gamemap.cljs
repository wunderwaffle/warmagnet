(ns warmagnet.views.gamemap
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [warmagnet.utils :refer [send-message]]))

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

(defr MapDistrict
  {:render (fn [C {:keys [name coordinates borders]} S]
             [:div.map-district {:style (clj->js {:left (first coordinates)
                                                  :top (second coordinates)})}
              2])})

(defr GameMap
  {:component-will-mount (fn [C] (if-not (:name (. C -props)) (xhr-load-map)))
   :render (fn [C {:keys [name map-src regions districts dimensions]} S]
             (if-not name
               [:div "No map"]
               [:div.game-map
                [:img {:src (str "/static/" map-src)}]
                (map (fn [district] [MapDistrict district]) districts)]))})
