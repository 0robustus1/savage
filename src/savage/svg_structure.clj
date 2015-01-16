(ns savage.svg-structure
  (:require [savage.helper :refer :all]
            [clojure.string :as s]
            [clojure.template :refer [apply-template]]))

(def shape-hierarchy
  (-> (make-hierarchy)
      (derive :rect ::shape)
      (derive :circle ::shape)
      (derive :ellipse ::shape)
      (derive :line ::shape)
      (derive ::points-based ::shape)
      (derive :polyline ::points-based)
      (derive :polygon ::points-based)))

(defrecord Shape [element-type children attrs center bbox])
(defrecord SVG [element-type attrs children])

(defn width [svg-structure] (:width (:attrs svg-structure)))
(defn height [svg-structure] (:height (:attrs svg-structure)))

(defn make-shape
  [element-type attrs children & {:keys [center bbox]}]
  (->Shape element-type
    (or children '()) (or attrs {})
    (or center [0 0]) (or bbox {:x 0 :y 0 :width 0 :height 0})))

(defmulti dimensions
  :element-type
  :hierarchy #'shape-hierarchy)

(defmethod dimensions :rect
  [this]
  [(-> this :attrs :width) (-> this :attrs :height)])

(defmethod dimensions :circle
  [this]
  (let [width-height (* (-> this :attrs :r) 2)]
    [width-height width-height]))

(defmethod dimensions :ellipse
  [this]
  (let [width (* (-> this :attrs :rx) 2)
        height (* (-> this :attrs :ry) 2)]
    [width height]))

(defmethod dimensions :line
  [this]
  (let [width (- (-> this :attrs :x2) (-> this :attrs :x1))
        height (- (-> this :attrs :y2) (-> this :attrs :y1))]
    [width height]))

(defmulti extracted-points
  :element-type
  :hierarchy #'shape-hierarchy)

(defmethod extracted-points ::points-based
  [this]
  (let [as-points (fn [p] (map as-num (s/split p #"\s*,\s*")))]
    (->> (s/split (get-in this [:attrs :points] "") #"\s+")
         (map as-points))))

(defmulti map-points
  :element-type
  :hierarchy #'shape-hierarchy)

(defmethod map-points ::points-based
  [this callback]
  (->> (extracted-points this) (map callback)))

(defmulti positional-center
  :element-type
  :hierarchy #'shape-hierarchy)

(defmethod positional-center ::points-based
  [this]
  (let [points (extracted-points this)
        [min-x max-x] (min-max (use-nth 0 points))
        [min-y max-y] (min-max (use-nth 1 points))]
    [(+ min-x (/ (- max-x min-x) 2))
     (+ min-y (/ (- max-y min-y) 2))]))

(defmethod dimensions ::points-based
  [this]
  (let [points (extracted-points this)
        width (apply - (max-min (use-nth 0 points)))
        height (apply - (max-min (use-nth 1 points)))]
    [width height]))

(defmulti update-center-from-bbox
  :element-type
  :hierarchy #'shape-hierarchy)

(defmethod update-center-from-bbox ::shape
  [this]
  (assoc this :center
         [(+ (-> this :bbox :x) (/ (-> this :bbox :width) 2))
          (+ (-> this :bbox :y) (/ (-> this :bbox :height) 2))]))

(defmulti update-bbox-from-center
  :element-type
  :hierarchy #'shape-hierarchy)

(defmethod update-bbox-from-center ::shape
  [this]
  (let [[x y] (:center this)
        [width height] (dimensions this)]
      (assoc this :bbox {:x x :y y :width width :height height})))

(defmulti update-geometrical-attrs
  :element-type
  :hierarchy #'shape-hierarchy)

(defmethod update-geometrical-attrs :rect
  [this]
  (let [bbox (:bbox this)]
    (assoc-record this :attrs
                  :width (:width bbox) :height (:height bbox)
                  :x (:x bbox) :y (:y bbox))))

(defmethod update-geometrical-attrs :circle
  [this]
  (let [bbox (:bbox this)]
    (assoc-record this :attrs
                  :cx (:x bbox) :cy (:y bbox)
                  :r (/ (:width bbox) 2))))

(defmethod update-geometrical-attrs :ellipse
  [this]
  (let [bbox (:bbox this)]
    (assoc-record this :attrs
                  :cx (:x bbox) :cy (:y bbox)
                  (:rx (/ (:width bbox) 2) :ry (/ (:height bbox) 2)))))

(defmethod update-geometrical-attrs :line
  [this]
  (let [bbox (:bbox this)]
    (assoc-record this :attrs
                  :x1 (- (:x bbox) (/ (:width bbox) 2))
                  :x2 (+ (:x bbox) (/ (:width bbox) 2))
                  :y1 (- (:y bbox) (/ (:height bbox) 2))
                  :y2 (- (:y bbox) (/ (:height bbox) 2)))))

(defmethod update-geometrical-attrs ::points-based
  [this]
  (let [[center-x center-y] (:center this)
        [old-x old-y] (positional-center this)
        x-offset (- center-x old-x)
        y-offset (- center-y old-y)
        join (fn [[x y]] (str (+ x x-offset) "," (+ y y-offset)))]
    (assoc-record
      this :attrs :points
      (s/join " " (map-points this join)))))

(defmulti adjust-center
  :element-type
  :hierarchy #'shape-hierarchy)

(defmethod adjust-center ::shape
  [this center]
  (-> (assoc this :center center)
      update-bbox-from-center update-geometrical-attrs))

(defmulti adjust-bbox
  :element-type
  :hierarchy #'shape-hierarchy)

(defmethod adjust-bbox ::shape
  [this bbox]
  (-> (assoc this :bbox bbox)
      update-center-from-bbox update-geometrical-attrs))

(defmulti bbox-center
  :element-type
  :hierarchy #'shape-hierarchy)

(defmethod bbox-center ::shape
  [this]
  [(-> this :bbox :x) (-> this :bbox :y)])
