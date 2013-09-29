(ns warmagnet.views.gamemap
  (:require-macros [pump.def-macros :refer [defr]]
                   [warmagnet.macros :refer [cx]])
  (:require [pump.core :refer [assoc-state assoc-in-state e-value]]
            [warmagnet.utils :refer [send-message send-log]]))

(def COLORS ["#f00" "#006400" "#00f" "#cc0" "#f0f" "#0cc"])

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

(defn player-index [game user-id]
  (first (keep-indexed (fn [i x] (if (= (:id x) user-id) i))
                       (:players game))))

(defn player-color [game user-id]
  (COLORS
   (player-index game user-id)))

(defr MapDistrict
  [C {:keys [district dname amount click selected assoc-hovered color]} S]
  (let [{:keys [borders coordinates]} (second district)
        [x y] coordinates]
    [:div
     {:class (cx :map-district true
                 :label true
                 :label-white (nil? selected)
                 :label-primary (= selected :checked)
                 :label-info (= selected :hover)
                 :label-danger (= selected :target))
      :style (clj->js {:left x :top y :color color})
      :on-click (if click (fn [C] (click district)) #())
      :on-mouse-enter #(assoc-hovered district)
      :on-mouse-leave #(assoc-hovered nil)}
     amount]))

(defr Deploy
  :component-will-mount
  (fn [C {:keys [available-troops]} S]
    (assoc-in-state C :to-deploy available-troops))
  
  [C {:keys [available-troops district deploy!]} {:keys [to-deploy]}]
  (let [[dname {:keys [coordinates]}] district
        [x y] (xy-for-popover coordinates)]
    [:div.popover {:style (clj->js {:display "block" :left x :top y})}
     [:div.popover-content
      [:div.input-group
       [:span.input-group-btn
        [:button.btn.btn-success {:on-click #(deploy! district to-deploy)} "Deploy"]]
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
  [C {:keys [attacker attacking defender defending attack!]} S]
  (let [[aname {:keys [coordinates]}] attacker
        [dname dmap] defender
        [x y] (xy-for-popover coordinates)]
   [:div.popover {:style (clj->js {:display "block" :left x :top y})}
    [:div.popover-content
     [:table {:style (clj->js {:width "100%" :text-align "center"})}
      [:thead [:tr [:th (name aname)] [:th (name dname)]]]
      [:tbody [:tr [:td attacking] [:td defending]]]]
     [:div.btn-group
      [:button.btn.btn-warning {:on-click attack!} "Attack"]
      [:button.btn.btn-danger "Blitz"]]]]))

(defr Reinforce
  :component-will-mount (fn [C P S] (assoc-in-state C :transfer 0))

  [C
   {:keys [dst-from troops-from dst-to troops-to reinforce!]}
   {:keys [transfer]}]
  (let [[fname {:keys [coordinates]}] dst-from
        [tname tmap] dst-to
        [x y] (xy-for-popover coordinates)]
    [:div.popover {:style (clj->js {:display "block" :left x :top y})}
     [:div.popover-content
      [:table {:style (clj->js {:width "100%" :text-align "center"})}
       [:thead [:tr [:th (name fname)] [:th (name tname)]]]]
      [:div.input-group
       [:span.input-group-addon (+ troops-from transfer)]
       [:input.form-control
        {:on-change #(assoc-in-state C :transfer (js/parseInt (e-value %)))
         :type "range" :value transfer :min (- 1 troops-from) :max (- troops-to 1)}]
       [:span.input-group-addon (- troops-to transfer)]]
      [:button.btn.btn-block.btn-success {:on-click reinforce!} "Reinforce"]]]))

(defn attack? [[aname {:keys [borders]}] [dname dmap]]
  (let [str-name (name dname)]
      (some #{str-name} borders)))

(def reinforce? attack?)

(defn user-state [game user]
  (let [id-key (keyword (str (:id user)))
        path [:player-state id-key]]
    (get-in game path)))

(defn troops-on [[dname dmap] game-districts]
  (-> game-districts dname :amount))

(defr GameMap
  :component-will-mount (fn [C P] (if-not (:name P) (xhr-load-map)))
  :get-initial-state (fn [] {:state :reinforce})

  [C
   {:keys [game-map game user container-width]}
   {:keys [hovered deploying attacker defender]}]
  (let [{:keys [name map-src regions districts dimensions]} game-map
        {:keys [player-state players turn-by]} game
        game-districts (:districts game)
        game-id (:id game)
        user-id (:id user)
        is-my-turn (= turn-by user-id)
        phase (keyword (:phase (user-state game user)))
   
        clear-popovers #(assoc-state C {:attacker nil :deploying nil :defender nil})
        assoc-hovered #(assoc-in-state C :hovered %)

        dst-deploy #(assoc-in-state C :deploying (if (not= deploying %) % nil))
        dst-attacker #(if (= % attacker)
                        (clear-popovers)
                        (assoc-in-state C :attacker %))
        dst-defender #(if (attack? attacker %)
                        (assoc-in-state C :defender %))
        dst-reinforce #(cond
                        (= % attacker) (clear-popovers)
                        (nil? attacker) (assoc-in-state C :attacker %)
                        (reinforce? attacker %) (assoc-in-state C :defender %))

        deploy! #(send-log game-id {:type :deploy :district (first %1) :amount %2})
        attack! #(assoc-state C {:attacker nil :defender nil :state :reinforce})
        reinforce! #(assoc-state C {:attacker nil :defender nil :state :deploy})

        district-action #(if is-my-turn 
                          (case phase
                            :deploy (if (= % user-id) dst-deploy)
                            :attack (if (= % user-id) dst-attacker dst-defender)
                            :reinforce (if (= % user-id) dst-reinforce)))

        selection-for #(condp = %
                         hovered :hover
                         deploying :checked
                         attacker :checked
                         defender :target
                         (cond
                          (and (= phase :reinforce)
                               attacker (reinforce? attacker %)) :target
                          (and attacker (attack? attacker %)) :target))]

    (if-not name
      [:div "No map"]
      [:div.game-map
       {:style (get-map-style dimensions container-width)}
       [:img {:src (str "/static/" map-src)
              :on-click clear-popovers}]
       (map (fn [district]
              (let [[dname map-district] district
                    gd (dname game-districts)]
               [MapDistrict {:district district
                             :dname dname
                             :selected (selection-for district)
                             :assoc-hovered assoc-hovered
                             :amount (gd :amount)
                             :color (player-color game (gd :user-id))
                             :click (district-action (gd :user-id))}]))
            districts)
       (if deploying
         [Deploy {:available-troops (:supply (user-state game user))
                  :district deploying :deploy! deploy!}])
       (if (and (= phase :attack) attacker defender)
         [Attack {:attacker attacker :attacking (troops-on attacker game-districts)
                  :defender defender :defending (troops-on defender game-districts)
                  :attack! attack!}])
       (if (and (= phase :reinforce) attacker defender)
         [Reinforce {:dst-from attacker :troops-from (troops-on attacker game-districts)
                     :dst-to defender :troops-to (troops-on defender game-districts)
                     :reinforce! reinforce!}])])))
