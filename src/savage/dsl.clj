(ns savage.dsl
  (:require [savage.helper :refer :all]
            [savage.svg-structure :as svg]
            [clojure.string :as s]))

;; Helper from Structures
(defmacro expose
  [a-ns & fn-list]
  `(do
     ~@(for [name fn-list]
         `(def ~name ~(symbol (str a-ns "/" name))) )))

(defmacro expose-structures []
  `(expose savage.dsl
          ~'rect ~'circle ~'ellipse ~'line ~'polyline ~'polygon ~'svg))

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

(defn rect
  "Represents a svg-rect shape. This shape has :x, :y, :width and :height as
  mandatory keys. If, however, :x or :y are not supplied, they'll default to
  0."
  [& rest]
  (let [attrs (apply hash-map rest)
        x (or (:x attrs) 0)
        y (or (:y attrs) 0)
        width (:width attrs)
        height (:height attrs)]
    (svg/->Rect :rect [] attrs
            [x y]
            {:x x :y y :width width :height height})))

(defn circle
  "Represents a svg-circle shape. This shape has :cx, :cy and :r as mandatory
  keys. If, however, :cx or :cy are not supplied they'll default to 0."
  [& rest]
  (let [attrs (apply hash-map rest)
        x (or (:cx attrs) 0)
        y (or (:cy attrs) 0)
        width (* (:r attrs) 2)
        height width]
    (svg/->Circle :circle [] attrs
              [x y]
              {:x x :y y :width width :height height})))

(defn ellipse
  "Represents a svg-ellipse shape. This shape has :cx, :cy, :rx and :ry as
  mandatory keys. If, however, :cx or :cy are not supplied they'll default to
  0."
  [& rest]
  (let [attrs  (apply hash-map rest)
        x (or (:cx attrs) 0)
        y (or (:cy attrs) 0)
        width (* (:rx attrs) 2)
        height (* (:ry attrs) 2)]
    (svg/->Ellipse :ellipse [] attrs
               [x y]
               {:x x :y y :width width :height height})))

(defn line
  "Represents a svg-line shape. This shape has :x1, :x2, :y1 and :y2 as
  mandatory keys. If however any of the coordinates is not supplied, it will
  default to 0."
  [& rest]
  (let [attrs  (apply hash-map rest)
        min-x (min (attrs :x1 0) (attrs :x2 0))
        min-y (min (attrs :y1 0) (attrs :y2 0))
        width (- (attrs :x2 0) (attrs :x1 0))
        height (- (attrs :y2 0) (attrs :y1 0))
        x (+ min-x (/ width 2))
        y (+ min-y (/ height 2))]
    (svg/->Line :line [] attrs
            [x y]
            {:x x :y y :width width :height height})))

(defn polyline
  "Represents a svg-polyline shape. This shape has :points as mandatory attrs
  key."
  [& rest]
  (let [attrs  (apply hash-map rest)
        points (:points attrs)
        point-l-fn (fn [point-s] (map num (s/split point-s ",\\s*")))
        points-l (map point-l-fn (s/split points "\\s+"))
        [max-x min-x] (max-min (use-nth 0 points-l))
        [max-y min-y] (max-min (use-nth 1 points-l))
        width (- max-x min-x)
        height (- max-y min-y)
        x (+ min-x (/ width 2))
        y (+ min-y (/ height 2))]
    (svg/->Polyline :polyline [] attrs
                [x y]
                {:x x :y y :width width :height height})))

(defn polygon
  "Polygon is not really supported yet."
  [& rest]
  :none)
