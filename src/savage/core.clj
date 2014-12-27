(ns savage.core
  (:require [savage.svg-structure :as svg :refer [adjust-center]]
            [xml-writer.core :as xml]
            [clojure.string :as s]
            [savage.helper :refer :all]
            [savage.dsl :as dsl]))

(dsl/expose-structures)

;; DSL functions
(defn adjust-position-relatively
  "Returns a redefined shape by repositioning it according to a source and an
  offset. It utilizes the virtual center-representation of a svg-shape."
  [shape
   {[source-center-x source-center-y] :center}
   [offset-x offset-y] ]
  (adjust-center shape
    [(+ source-center-x offset-x) (+ source-center-y offset-y)]))

(defn left-of-center-from
  "Returns redefined target for adjusted relative position. Redefines by
  applying the offset to the x-axis of source (decreases the x-value)."
  [source target x-offset]
  (adjust-position-relatively target source [(* -1 x-offset) 0]))

(defn right-of-center-from
  "Returns redefined target for adjusted relative position. Redefines by
  applying the offset to the x-axis of source (increases the x-value)."
  [source target x-offset]
  (adjust-position-relatively target source [x-offset 0]))

(defn above-center-from
  "Returns redefined target for adjusted relative position. Redefines by
  applying the offset to the y-axis of source (decreases the y-value)."
  [source target y-offset]
  (adjust-position-relatively target source [0 (* -1 y-offset)]))

(defn below-center-from
  "Returns redefined target for adjusted relative position. Redefines by
  applying the offset to the y-axis of source (increases the y-value)."
  [source target y-offset]
  (adjust-position-relatively target source [0 y-offset]))

(defn- xml-form-child
  "Traverses the children-tree and returns xml-write syntax."
  [child]
  [(:element-type child) (:attrs child)
   (if (:children child)
     (map xml-form-child (:children child) []))])

(defn export-svg
  "Exports a svg as xml into a String."
  [svg]
  (xml/emit-sexp-str [:svg (:attrs svg)
                      (map xml-form-child (:children svg))]))

(defn eval-export
  "Evaluates the input-file and write resulting xml to output-file."
  [input-file output-file]
  (let [res (load-file input-file)
        xml (export-svg res)]
    (spit output-file xml)
    res))
