(ns warmagnet.views.gamemap
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [clojure.string :as string]
            [pump.core :refer [assoc-state]]
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

(defn get-map-scale [[x y]]
  (str "scale(" (/ (- (.-clientWidth document/documentElement) 30) x) ")"))

(defn cx [class-map]
  (string/join " " (map #(name (first %)) (filter #(second %) class-map))))

(defr MapDistrict
  [C {:keys [name coordinates borders hovered assoc-hovered]} S]
  (let [this-hovered (= (:name hovered) name)
        attacker-hovered (some #{name} (:borders hovered))
        regular (not-any? true? [this-hovered attacker-hovered])]
    [:div
     {:class (cx {:map-district true
                  :label true
                  :label-success regular
                  :label-info this-hovered
                  :label-danger attacker-hovered})
      :style (clj->js {:left (first coordinates)
                       :top (second coordinates)})
      :on-mouse-enter (fn [C] (assoc-hovered {:name name :borders borders}))
      :on-mouse-leave (fn [C] (assoc-hovered nil))}
     2]))

(defr GameMap
  :component-will-mount (fn [C] (if-not (:name (. C -props)) (xhr-load-map)))
  [C
   {:keys [name map-src regions districts dimensions]}
   {:keys [hovered]}]
  (let [assoc-hovered #(assoc-state C :hovered %)]
    (if-not name
      [:div "No map"]
      [:div.game-map
       {:style (clj->js {:transform (get-map-scale dimensions)})}
       [:img {:src (str "/static/" map-src)}]
       (map (fn [district] [MapDistrict (assoc district
                                          :assoc-hovered assoc-hovered
                                          :hovered hovered)])
            districts)])))
