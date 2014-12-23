(ns savage.core
  (:require [savage.svg-structure :as svg]
            [xml-writer.core :as xml]
            [clojure.string :as s]))

;; Helper from Structures
(defmacro expose
  [a-ns & fn-list]
  `(do
     ~@(for [name fn-list]
         `(def ~name ~(symbol (str a-ns "/" name))) )))

(def svg-el "Alias for svg-structure/svg" svg/svg)
; (def circle "Alias for svg-structure/circle" svg/circle)
(expose savage.svg-structure
        rect circle ellipse line polyline polygon)

;; DSL functions
(defn svg
  "Creates a svg"
  [width height & children]
  (svg-el width height children) )

(defn update-center
  "Updates the center according to the bounding box"
  [{bb-box :bb-box :as target}]
  (assoc target :center [(+ (:x bb-box) (/ (:width bb-box) 2))
                         (+ (:y bb-box) (/ (:height bb-box) 2)) ]))

(defn max-min
  [a-list]
  [(apply max a-list) (apply min a-list)])

(defn use-nth
  [index list-of-lists]
  (reduce (fn [inner-list res]
          (cons (nth index inner-list) res))))

(defn dimensions
  [element]
  (case (:element element)
    :rect
    [(:width element) (:height element)]
    :circle
    [(* (:r element) 2) (* (:r element) 2)]
    :ellipse
    [(* (:rx element) 2) (* (:ry element) 2)]
    :line
    [(- (:x2 element) (:x1 element)) (- (:y2 element) (:y1 element))]
    :polyline
    (let [points (:points element)
          points-l (map (fn [point-s]
                          (map num (s/split point-s ",\\s*")) )
                        (s/split points "\\s+") )]
      [(apply - (max-min (use-nth 0 points-l)))
       (apply - (max-min (use-nth 1 points-l))) ])
    :polygon
    [0 0] ))

(defn update-bb-box
  "Updates the bounding box according to the center"
  [{[x y] :center :as target}]
  (let [[width height] (dimensions target)]
    (assoc target :bb-box {:x x :y y :width width :height height}) ))

(defn update-pos-attrs
  "Updates the positional attributes based on the bounding box"
  [{bb-box :bb-box element-type :element :as element}]
  (case element-type
    :rect
    (assoc element
           :width (:width bb-box) :height (:height bb-box)
           :x (:x bb-box) :y (:y bb-box))
    :circle
    (assoc element
           :cx (:x bb-box) :cy (:y bb-box)
           :r (/ (:width bb-box) 2))
    :ellipse
    (assoc element
           :cx (:x bb-box) :cy (:y bb-box)
           :rx (/ (:width bb-box) 2) :ry (/ (:height bb-box) 2))
    :line
    (assoc element
           :x1 (- (:x bb-box) (/ (:width bb-box) 2))
           :x2 (+ (:x bb-box) (/ (:width bb-box) 2))
           :y1 (- (:y bb-box) (/ (:height bb-box) 2))
           :y2 (- (:y bb-box) (/ (:height bb-box) 2)))
    :polyline
    element
    :polygon
    element))

(defn adjust-center-to
  ([[x y :as center] target]
   (-> (assoc target :center center) update-bb-box update-pos-attrs) ))

(defn adjust-bb-box-to
  ([bb-box target]
   (-> (assoc target :bb-box bb-box) update-center update-pos-attrs) ))

(defn adjust-position-relatively
  [target
   {[source-center-x source-center-y] :center :as source}
   [offset-x offset-y] ]
  (adjust-center-to
    [(+ source-center-x offset-x) (+ source-center-y offset-y)]
    target ))

(defn left-of-center-from
  [source target x-offset]
  (adjust-position-relatively target source [(* -1 x-offset) 0]))

(defn right-of-center-from
  [source target x-offset]
  (adjust-position-relatively target source [x-offset 0]))

(defn above-center-from
  [source target y-offset]
  (adjust-position-relatively target source [0 y-offset]))

(defn below-center-from
  [source target y-offset]
  (adjust-position-relatively target source [0 (* -1 y-offset)]))

(defn- xml-form-child
  [child]
  [(:element child) (svg/raw-attrs child)
   (if (:children child)
     (map xml-form-child (:children child) []))])

(defn export-svg
  "Exports a svg as xml into a String"
  [svg]
  (xml/emit-sexp-str [:svg (svg/raw-attrs svg)
                      (map xml-form-child (:children svg))]))
