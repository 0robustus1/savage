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
  ([base-attrs]
  (let [attrs (into rect-default-attrs base-attrs)
        x (or (:x attrs) 0)
        y (or (:y attrs) 0)
        width (:width attrs)
        height (:height attrs)
        center [(+ x (/ width 2)) (+ y (/ height 2))]]
    (svg/make-shape :rect attrs [] :center center
                    :bbox {:x x :y y :width width :height height})))
  ([key val & rest]
   (rect (apply hash-map key val rest))))

(def circle-default-attrs {:cx 0 :cy 0})

(defn circle
  "Represents a svg-circle shape. This shape has :cx, :cy and :r as mandatory
  keys. If, however, :cx or :cy are not supplied they'll default to 0."
  ([base-attrs]
  (let [attrs (into circle-default-attrs base-attrs)
        center-x (or (:cx attrs) 0)
        center-y (or (:cy attrs) 0)
        width (* (:r attrs) 2)
        height width
        x (- center-x (:r attrs))
        y (- center-y (:r attrs))]
    (svg/make-shape :circle attrs [] :center [center-x center-y]
              :bbox {:x x :y y :width width :height height})))
  ([key val & rest]
   (circle (apply hash-map key val rest))))

(def ellipse-default-attrs {:cx 0 :cy 0})

(defn ellipse
  "Represents a svg-ellipse shape. This shape has :cx, :cy, :rx and :ry as
  mandatory keys. If, however, :cx or :cy are not supplied they'll default to
  0."
  ([base-attrs]
  (let [attrs  (into ellipse-default-attrs base-attrs)
        x (or (:cx attrs) 0)
        y (or (:cy attrs) 0)
        width (* (:rx attrs) 2)
        height (* (:ry attrs) 2)]
    (svg/make-shape :ellipse attrs [] :center [x y]
               :bbox {:x x :y y :width width :height height})))
  ([key val & rest]
   (ellipse (apply hash-map key val rest))))

(def line-default-attrs {:x1 0 :x2 0 :y1 0 :y2 0})

(defn line
  "Represents a svg-line shape. This shape has :x1, :x2, :y1 and :y2 as
  mandatory keys. If however any of the coordinates is not supplied, it will
  default to 0."
  ([base-attrs]
  (let [attrs (into line-default-attrs base-attrs)
        [max-x min-x] (max-min [(attrs :x1 0) (attrs :x2 0)])
        [max-y min-y] (max-min [(attrs :y1 0) (attrs :y2 0)])
        width (- max-x min-x)
        height (- max-y min-y)
        center-x (+ min-x (/ width 2))
        center-y (+ min-y (/ height 2))]
    (svg/make-shape :line attrs [] :center [center-x center-y]
            :bbox {:x min-x :y min-y :width width :height height})))
  ([key val & rest]
   (line (apply hash-map key val rest))))

(defn polyline
  "Represents a svg-polyline shape. This shape has :points as mandatory attrs
  key."
  ([attrs]
  (let [jig (svg/make-shape :polyline attrs [])
        points (svg/extracted-points jig)
        [center-x center-y] (svg/positional-center jig)
        [width height] (svg/dimensions jig)
        x (- center-x (/ width 2))
        y (- center-y (/ height 2))]
    (assoc jig
           :center [center-x center-y]
           :bbox {:x x :y y :width width :height height})))
  ([key val & rest]
   (polyline (apply hash-map key val rest))))

(defn polygon
  "Represents a svg-polygon shape. This shape has :points as mandatory attrs
  key."
  ([attrs]
  (let [jig (svg/make-shape :polygon attrs [])
        points (svg/extracted-points jig)
        [center-x center-y] (svg/positional-center jig)
        [width height] (svg/dimensions jig)
        x (- center-x (/ width 2))
        y (- center-y (/ height 2))]
    (assoc jig
           :center [center-x center-y]
           :bbox {:x x :y y :width width :height height})))
  ([key val & rest]
   (polygon (apply hash-map key val rest))))

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

(defn left-of
  "Returns redefined target for adjusted relative position. Redefines by
  applying the offset to the x-axis of source (decreases the x-value)."
  ([source target x-offset]
   (left-of source target x-offset :center))
  ([source target x-offset positioning]
   (adjust-spaced-position-relatively source target x-offset positioning :width -1)))

(defn right-of
  "Returns redefined target for adjusted relative position. Redefines by
  applying the offset to the x-axis of source (increases the x-value)."
  ([source target x-offset]
   (right-of source target x-offset :center))
  ([source target x-offset positioning]
   (adjust-spaced-position-relatively source target x-offset positioning :width 1)))

(defn above-of
  "Returns redefined target for adjusted relative position. Redefines by
  applying the offset to the y-axis of source (decreases the y-value)."
  ([source target y-offset]
   (above-of source target y-offset :center))
  ([source target y-offset positioning]
   (adjust-spaced-position-relatively source target y-offset positioning :height -1)))

(defn below-of
  "Returns redefined target for adjusted relative position. Redefines by
  applying the offset to the y-axis of source (increases the y-value)."
  ([source target y-offset]
   (below-of source target y-offset :center))
  ([source target y-offset positioning]
   (adjust-spaced-position-relatively source target y-offset positioning :height 1)))
