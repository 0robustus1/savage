(ns savage.dsl
  (:require [savage.helper :refer :all]
            [savage.svg-structure :as svg :refer [adjust-center]]
            [clojure.string :as s]))

(defn svg
  "Creates a svg. Needs mandatory :width and :height arguments in the
  attrs-map. Children should be svg-shape/structure forms."
  [attrs & children]
  (svg/->SVG :svg
             (assoc attrs
                    :xmlns "http://www.w3.org/2000/svg"
                    :viewBox (str 0 " "
                                  0 " "
                                  (:width attrs) " "
                                  (:height attrs)))
             children))

(def rect-default-attrs {:x 0 :y 0})

(defn rect
  "Represents a svg-rect shape. This shape has :x, :y, :width and :height as
  mandatory keys. If, however, :x or :y are not supplied, they'll default to
  0."
  [& rest]
  (let [attrs (into rect-default-attrs (apply hash-map rest))
        x (or (:x attrs) 0)
        y (or (:y attrs) 0)
        width (:width attrs)
        height (:height attrs)]
    (svg/make-shape :rect attrs [] :center [x y]
                    :bbox {:x x :y y :width width :height height})))

(def circle-default-attrs {:cx 0 :cy 0})

(defn circle
  "Represents a svg-circle shape. This shape has :cx, :cy and :r as mandatory
  keys. If, however, :cx or :cy are not supplied they'll default to 0."
  [& rest]
  (let [attrs (into circle-default-attrs (apply hash-map rest))
        x (or (:cx attrs) 0)
        y (or (:cy attrs) 0)
        width (* (:r attrs) 2)
        height width]
    (svg/make-shape :circle attrs [] :center [x y]
              :bbox {:x x :y y :width width :height height})))

(def ellipse-default-attrs {:cx 0 :cy 0})

(defn ellipse
  "Represents a svg-ellipse shape. This shape has :cx, :cy, :rx and :ry as
  mandatory keys. If, however, :cx or :cy are not supplied they'll default to
  0."
  [& rest]
  (let [attrs  (into ellipse-default-attrs (apply hash-map rest))
        x (or (:cx attrs) 0)
        y (or (:cy attrs) 0)
        width (* (:rx attrs) 2)
        height (* (:ry attrs) 2)]
    (svg/make-shape :ellipse attrs [] :center [x y]
               :bbox {:x x :y y :width width :height height})))

(def line-default-attrs {:x1 0 :x2 0 :y1 0 :y2 0})

(defn line
  "Represents a svg-line shape. This shape has :x1, :x2, :y1 and :y2 as
  mandatory keys. If however any of the coordinates is not supplied, it will
  default to 0."
  [& rest]
  (let [attrs (into line-default-attrs (apply hash-map rest))
        min-x (min (attrs :x1 0) (attrs :x2 0))
        min-y (min (attrs :y1 0) (attrs :y2 0))
        width (- (attrs :x2 0) (attrs :x1 0))
        height (- (attrs :y2 0) (attrs :y1 0))
        x (+ min-x (/ width 2))
        y (+ min-y (/ height 2))]
    (svg/make-shape :line attrs [] :center [x y]
            :bbox {:x x :y y :width width :height height})))

(defn polyline
  "Represents a svg-polyline shape. This shape has :points as mandatory attrs
  key."
  [& rest]
  (let [attrs  (apply hash-map rest)
        jig (svg/make-shape :polyline attrs [])
        points (svg/extracted-points jig)
        [x y] (svg/positional-center jig)
        [width height] (svg/dimensions jig)]
    (assoc jig
           :center [x y]
           :bbox {:x x :y y :width width :height height})))

(defn polygon
  "Represents a svg-polygon shape. This shape has :points as mandatory attrs
  key."
  [& rest]
  (let [attrs  (apply hash-map rest)
        jig (svg/make-shape :polygon attrs [])
        points (svg/extracted-points jig)
        [x y] (svg/positional-center jig)
        [width height] (svg/dimensions jig)]
    (assoc jig
           :center [x y]
           :bbox {:x x :y y :width width :height height})))

(defn- adjust-position-relatively
  "Returns a redefined shape by repositioning it according to a source and an
  offset. It utilizes the virtual center-representation of a svg-shape."
  [{[target-center-x target-center-y] :center :as shape}
   {[source-center-x source-center-y] :center}
   [offset-x offset-y axis]]
  (adjust-center
    shape
    (cond
      (= axis :x) [(+ source-center-x offset-x) target-center-y]
      (= axis :y) [target-center-x (+ source-center-y offset-y)])))

(defn- spaced-offset
  "Calculates the spaced offset based on two dimension-value.
  Usually they represent either height or width of source
  and target."
  [base-offset dimension other-dimension]
  (+ base-offset (/ dimension 2) (/ other-dimension 2)))

(defn- adjust-spaced-position-relatively
  [source target base-offset positioning dimension sign]
  (let [offset (cond
                 (= positioning :center) base-offset
                 (= positioning :space)
                 (+ base-offset
                    (/ (-> source :bbox dimension) 2)
                    (/ (-> target :bbox dimension) 2)))
        center (cond
                 (= dimension :width) [(* sign offset) 0 :x]
                 (= dimension :height) [0 (* sign offset) :y])]
    (adjust-position-relatively target source center)))

(defn left-from
  "Returns redefined target for adjusted relative position. Redefines by
  applying the offset to the x-axis of source (decreases the x-value)."
  ([source target x-offset]
   (left-from source target x-offset :center))
  ([source target x-offset positioning]
   (adjust-spaced-position-relatively source target x-offset positioning :width -1)))

(defn right-from
  "Returns redefined target for adjusted relative position. Redefines by
  applying the offset to the x-axis of source (increases the x-value)."
  ([source target x-offset]
   (right-from source target x-offset :center))
  ([source target x-offset positioning]
   (adjust-spaced-position-relatively source target x-offset positioning :width 1)))

(defn above-from
  "Returns redefined target for adjusted relative position. Redefines by
  applying the offset to the y-axis of source (decreases the y-value)."
  ([source target y-offset]
   (above-from source target y-offset :center))
  ([source target y-offset positioning]
   (adjust-spaced-position-relatively source target y-offset positioning :height -1)))

(defn below-from
  "Returns redefined target for adjusted relative position. Redefines by
  applying the offset to the y-axis of source (increases the y-value)."
  ([source target y-offset]
   (below-from source target y-offset :center))
  ([source target y-offset positioning]
   (adjust-spaced-position-relatively source target y-offset positioning :height 1)))
