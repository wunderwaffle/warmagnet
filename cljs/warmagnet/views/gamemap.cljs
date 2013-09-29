(ns warmagnet.views.gamemap
  (:require-macros [pump.def-macros :refer [defr]]
                   [warmagnet.macros :refer [cx]])
  (:require [pump.core :refer [assoc-state assoc-in-state e-value]]
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

(defn xy-for-popover [[x y]]
  [(- x 240) (+ y 24)])

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
  [C {:keys [district hovered assoc-hovered click]} S]
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
      :on-click (fn [C] (click district))
      :on-mouse-enter (fn [C] (assoc-hovered district))
      :on-mouse-leave (fn [C] (assoc-hovered nil))}
     2]))

(defr Deploy
  :component-will-mount
  (fn [C {:keys [available-troops]} S]
    (assoc-in-state C :to-deploy (/ available-troops 2)))
  
  [C {:keys [available-troops district deploy]} {:keys [to-deploy]}]
  (let [[dname {:keys [coordinates]}] district
        [x y] (xy-for-popover coordinates)]
    [:div.popover {:style (clj->js {:display "block" :left x :top y})}
     [:div.popover-content
      [:div.input-group
       [:span.input-group-btn
        [:button.btn.btn-success {:on-click #(deploy to-deploy)} "Deploy"]]
       [:input.form-control
        {:on-change #(if-let [v (js/parseInt (e-value %))]
                       (if (<= 1 v available-troops)
                         (assoc-in-state C :to-deploy (e-value %))))
         :type "number" :value to-deploy :min 1 :max available-troops}]]
      [:div.input-group
       [:span.input-group-addon "1"]
       [:input.form-control
        {:on-change #(assoc-in-state C :to-deploy (e-value %))
         :type "range" :value to-deploy :min 1 :max available-troops}]
       [:span.input-group-addon available-troops]]]]))

(defr Attack
  [C {:keys [attacker attacking defender defending attack]} S]
  (let [[aname {:keys [coordinates]}] attacker
        [dname dmap] defender
        [x y] (xy-for-popover coordinates)
        ]
   [:div.popover {:style (clj->js {:display "block" :left x :top y})}
    [:div.popover-content
     [:table {:style (clj->js {:width "100%" :text-align "center"})}
      [:thead [:tr
               [:th (name aname)]
               [:th (if dname (name dname) "")]]]
      [:tbody [:tr [:td attacking] [:td (if dname defending "")]]]]
     [:div.btn-group
      [:button.btn.btn-warning {:on-click attack} "Attack"]
      [:button.btn.btn-danger "Blitz"]]]]))

(defr GameMap
  :component-will-mount (fn [C] (if-not (:name (. C -props))
                                  (xhr-load-map)))
  :get-initial-state #(identity {:state :deploy})
  [C
   {:keys [name map-src regions districts dimensions container-width]}
   {:keys [hovered deploying attacker defender state]}]
  (let [clear-popovers #(assoc-state C {:attacker nil :deploying nil :defender nil})
        assoc-hovered #(assoc-in-state C :hovered %)
        click-deploying #(assoc-in-state C :deploying (if (not= deploying %) % nil))
        deploy #(assoc-state C {:deploying nil :state :attack})
        attack #(assoc-state C {:attacker nil :defender nil :state :deploy})
        assoc-attacker #(assoc-in-state C :attacker %)
        assoc-defender #(assoc-in-state C :defender %)

        district-action #(case state
                           :deploy click-deploying
                           :attack (if (= % attacker) clear-popovers
                                       (if (nil? attacker)
                                         assoc-attacker
                                         assoc-defender)))]
    (if-not name
      [:div "No map"]
      [:div.game-map
       {:style (get-map-style dimensions container-width)}
       [:img {:src (str "/static/" map-src)
              :on-click clear-popovers}]
       (map (fn [district] [MapDistrict {:district district
                                         :assoc-hovered assoc-hovered
                                         :click (district-action district)
                                         :hovered hovered}])
            districts)
       (if deploying [Deploy {:available-troops 40 :district deploying :deploy deploy}])
       (if attacker [Attack {:attacker attacker :attacking 142
                             :defender defender :defending 30
                             :attack attack}])])))
