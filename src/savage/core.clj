(ns savage.core
  (:require [savage.svg-structure :as svg]
            [xml-writer.core :as xml]))

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

(defn- xml-form-child
  [child]
  [(:element child) (dissoc child :element :children)
   (if (:children child)
     (map xml-form-child (:children child) []))])

(defn export-svg
  "Exports a svg as xml into a String"
  [svg]
  (xml/emit-sexp-str [:svg (dissoc svg :element :children)
                      (map xml-form-child (:children svg))]))
