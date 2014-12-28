(ns savage.svg-structure
  (:require [savage.helper :refer :all]
            [clojure.string :as s]
            [clojure.template :refer [apply-template]]))

(defrecord SVG [element-type attrs children])
(defn width [svg-structure] (:width (:attrs svg-structure)))
(defn height [svg-structure] (:height (:attrs svg-structure)))

(defprotocol AdjustGeometricalMetadata
  (update-center-from-bbox [this])
  (update-bbox-from-center [this])
  (adjust-center [this center])
  (adjust-bbox [this bbox])
  (update-geometrical-attrs [this]))

(defprotocol GeometricalData
  (dimensions [this])
  (bbox-center [this]))

(defprotocol PointsHandling
  (extracted-points [this])
  (map-points [this callback])
  (positional-center [this]))

(defmacro defsvg-structure
  [name & opts+specs]
  `(defrecord ~name [~'element-type ~'children ~'attrs ~'center ~'bbox]
     ~@opts+specs))

(defmacro extend-types
  "Extends multiple types with the same
  protocol/method definition-combination."
  [types & forms]
  `(do
     ~@(for [type types]
         `(extend-type ~type ~@forms))))

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
        update-bbox-from-center update-geometrical-attrs))
  (adjust-bbox [this center]
    (-> (assoc this :bbox bbox)
        update-center-from-bbox update-geometrical-attrs))
  (update-geometrical-attrs [this]
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
        update-bbox-from-center update-geometrical-attrs))
  (adjust-bbox [this center]
    (-> (assoc this :bbox bbox)
        update-center-from-bbox update-geometrical-attrs))
  (update-geometrical-attrs [this]
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
        update-bbox-from-center update-geometrical-attrs))
  (adjust-bbox [this center]
    (-> (assoc this :bbox bbox)
        update-center-from-bbox update-geometrical-attrs))
  (update-geometrical-attrs [this]
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
        update-bbox-from-center update-geometrical-attrs))
  (adjust-bbox [this center]
    (-> (assoc this :bbox bbox)
        update-center-from-bbox update-geometrical-attrs))
  (update-geometrical-attrs [this]
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
        update-bbox-from-center (update-geometrical-attrs)))
  (adjust-bbox [this center]
    (-> (assoc this :bbox bbox)
        update-center-from-bbox update-geometrical-attrs))
  (update-geometrical-attrs
    [this]
    (let [[center-x center-y] (:center this)
          [old-x old-y] (positional-center this)
          x-offset (- center-x old-x)
          y-offset (- center-y old-y)
          join (fn [[x y]] (str (+ x x-offset) "," (+ y y-offset)))]
      (assoc-record
        this :attrs
        (assoc (:attrs this) :points (s/join " " (map-points this join)))))))

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
        update-bbox-from-center update-geometrical-attrs))
  (adjust-bbox [this center]
    (-> (assoc this :bbox bbox)
        update-center-from-bbox update-geometrical-attrs))
  (update-geometrical-attrs [this] this))

(extend-types
  [Polyline Polygon]
  GeometricalData
  (dimensions [this]
    (let [points (extracted-points this)
          width (apply - (max-min (use-nth 0 points)))
          height (apply - (max-min (use-nth 1 points)))]
      [width height]))
  (bbox-center [this]
    [(:x (:bbox this)) (:y (:bbox this))])
  PointsHandling
  (extracted-points
    [this]
    (let [as-points (fn [p] (map as-num (s/split p #"\s*,\s*")))]
      (->> (s/split (get-in this [:attrs :points] "") #"\s+")
           (map as-points))))
  (map-points
    [this callback]
    (->> (extracted-points this) (map callback)))
  (positional-center
    [this]
    (let [points (extracted-points this)
          [min-x max-x] (min-max (use-nth 0 points))
          [min-y max-y] (min-max (use-nth 1 points))]
      [(+ min-x (/ (- max-x min-x) 2))
       (+ min-y (/ (- max-y min-y) 2))])))
