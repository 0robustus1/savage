(ns savage.svg-structure
  (:require [savage.helper :refer :all]
            [clojure.string :as s]
            [clojure.template :refer [apply-template]]))

(def restricted-keys [:element :children :center :bb-box])

(defn raw-attrs
  [element]
  (apply dissoc element restricted-keys) )

(defrecord SVG [element-type attrs children])
(defn width [svg-structure] (:width (:attrs svg-structure)))
(defn height [svg-structure] (:height (:attrs svg-structure)))

(defprotocol AdjustGeometricalMetadata
  (update-center-from-bbox [this])
  (update-bbox-from-center [this])
  (adjust-center [this center])
  (adjust-bbox [this bbox]))

(defprotocol AdjustPosition
  (update-pos-attrs [this]))

(defprotocol AdjustPositionAndSize
  (update-pos-attrs [this]))

(defprotocol GeometricalData
  (dimensions [this])
  (bbox-center [this]))

(defmacro defsvg-structure
  [name & opts+specs]
  `(defrecord ~name [~'element-type ~'children ~'attrs ~'center ~'bbox]
     ~@opts+specs))

(defsvg-structure Rect
  AdjustGeometricalMetadata
  (update-center-from-bbox [this]
    (assoc this :center [(+ (:x bbox) (/ (:width bbox) 2))
                         (+ (:y bbox) (/ (:height bbox) 2)) ]))
  (update-bbox-from-center [this]
    (let [[x y] center
          [width height] (dimensions this)]
      (assoc this :bbox {:x x :y y :width width :height height}) ))
  (adjust-center [this center]
    (-> (assoc this :center center)
        update-bbox-from-center update-pos-attrs))
  (adjust-bbox [this center]
    (-> (assoc this :bbox bbox)
        update-center-from-bbox update-pos-attrs))
  AdjustPositionAndSize
  (update-pos-attrs [this]
    (assoc-record this :attrs
                  :width (:width bbox) :height (:height bbox)
                  :x (:x bbox) :y (:y bbox)))
  GeometricalData
  (dimensions [this]
    [(:width attrs) (:height attrs)])
  (bbox-center [this]
    [(:x bbox) (:y bbox)])
  )

(defsvg-structure Circle
  AdjustGeometricalMetadata
  (update-center-from-bbox [this]
    (assoc this :center [(+ (:x bbox) (/ (:width bbox) 2))
                         (+ (:y bbox) (/ (:height bbox) 2)) ]))
  (update-bbox-from-center [this]
    (let [[x y] center
          [width height] (dimensions this)]
      (assoc this :bbox {:x x :y y :width width :height height}) ))
  (adjust-center [this center]
    (-> (assoc this :center center)
        update-bbox-from-center update-pos-attrs))
  (adjust-bbox [this center]
    (-> (assoc this :bbox bbox)
        update-center-from-bbox update-pos-attrs))
  AdjustPositionAndSize
  (update-pos-attrs [this]
    (assoc-record this :attrs
                  :cx (:x bbox) :cy (:y bbox)
                  :r (/ (:width bbox) 2)))
  GeometricalData
  (dimensions [this]
    (let [width-height (* (:r attrs) 2)]
      [width-height width-height]))
  (bbox-center [this]
    [(:x bbox) (:y bbox)])
  )

(defsvg-structure Ellipse
  AdjustGeometricalMetadata
  (update-center-from-bbox [this]
    (assoc this :center [(+ (:x bbox) (/ (:width bbox) 2))
                         (+ (:y bbox) (/ (:height bbox) 2)) ]))
  (update-bbox-from-center [this]
    (let [[x y] center
          [width height] (dimensions this)]
      (assoc this :bbox {:x x :y y :width width :height height}) ))
  (adjust-center [this center]
    (-> (assoc this :center center)
        update-bbox-from-center update-pos-attrs))
  (adjust-bbox [this center]
    (-> (assoc this :bbox bbox)
        update-center-from-bbox update-pos-attrs))
  AdjustPositionAndSize
  (update-pos-attrs [this]
    (assoc-record this :attrs
                  :cx (:x bbox) :cy (:y bbox)
                  (:rx (/ (:width bbox) 2) :ry (/ (:height bbox) 2))))
  GeometricalData
  (dimensions [this]
    (let [width (* (:rx attrs) 2)
          height (* (:ry attrs) 2)]
      [width height]))
  (bbox-center [this]
    [(:x bbox) (:y bbox)])
  )

(defsvg-structure Line
  AdjustGeometricalMetadata
  (update-center-from-bbox [this]
    (assoc this :center [(+ (:x bbox) (/ (:width bbox) 2))
                         (+ (:y bbox) (/ (:height bbox) 2)) ]))
  (update-bbox-from-center [this]
    (let [[x y] center
          [width height] (dimensions this)]
      (assoc this :bbox {:x x :y y :width width :height height}) ))
  (adjust-center [this center]
    (-> (assoc this :center center)
        update-bbox-from-center update-pos-attrs))
  (adjust-bbox [this center]
    (-> (assoc this :bbox bbox)
        update-center-from-bbox update-pos-attrs))
  AdjustPositionAndSize
  (update-pos-attrs [this]
    (assoc-record this :attrs
                  :x1 (- (:x bbox) (/ (:width bbox) 2))
                  :x2 (+ (:x bbox) (/ (:width bbox) 2))
                  :y1 (- (:y bbox) (/ (:height bbox) 2))
                  :y2 (- (:y bbox) (/ (:height bbox) 2))))
  GeometricalData
  (dimensions [this]
    (let [width (- (:x2 attrs) (:x1 attrs))
          height (- (:y2 attrs) (:y1 attrs))]
      [width height]))
  (bbox-center [this]
    [(:x bbox) (:y bbox)])
  )

(defsvg-structure Polyline
  AdjustGeometricalMetadata
  (update-center-from-bbox [this]
    (assoc this :center [(+ (:x bbox) (/ (:width bbox) 2))
                         (+ (:y bbox) (/ (:height bbox) 2)) ]))
  (update-bbox-from-center [this]
    (let [[x y] center
          [width height] (dimensions this)]
      (assoc this :bbox {:x x :y y :width width :height height}) ))
  (adjust-center [this center]
    (-> (assoc this :center center)
        update-bbox-from-center update-pos-attrs))
  (adjust-bbox [this center]
    (-> (assoc this :bbox bbox)
        update-center-from-bbox update-pos-attrs))
  GeometricalData
  (dimensions [this]
    (let [points (:points attrs)
          point-l-fn (fn [point-s] (map num (s/split point-s ",\\s*")))
          points-l (map point-l-fn (s/split points "\\s+"))
          width (apply - (max-min (use-nth 0 points-l)))
          height (apply - (max-min (use-nth 1 points-l)))]
      [width height]))
  (bbox-center [this]
    [(:x bbox) (:y bbox)])
  )

(defsvg-structure Polygon
  AdjustGeometricalMetadata
  (update-center-from-bbox [this]
    (assoc this :center [(+ (:x bbox) (/ (:width bbox) 2))
                         (+ (:y bbox) (/ (:height bbox) 2)) ]))
  (update-bbox-from-center [this]
    (let [[x y] center
          [width height] (dimensions this)]
      (assoc this :bbox {:x x :y y :width width :height height}) ))
  (adjust-center [this center]
    (-> (assoc this :center center)
        update-bbox-from-center update-pos-attrs))
  (adjust-bbox [this center]
    (-> (assoc this :bbox bbox)
        update-center-from-bbox update-pos-attrs))
  GeometricalData
  (dimensions [this]
    (let [points (:points attrs)
          point-l-fn (fn [point-s] (map num (s/split point-s ",\\s*")))
          points-l (map point-l-fn (s/split points "\\s+"))
          width (apply - (max-min (use-nth 0 points-l)))
          height (apply - (max-min (use-nth 1 points-l)))]
      [width height]))
  (bbox-center [this]
    [(:x bbox) (:y bbox)])
  )
