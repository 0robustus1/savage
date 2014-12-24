(ns savage.core
  (:require [savage.svg-structure :as svg :refer [adjust-center]]
            [xml-writer.core :as xml]
            [clojure.string :as s]
            [savage.helper :refer :all]
            [savage.dsl :as dsl]))

(dsl/expose-structures)

;; DSL functions
(defn adjust-position-relatively
  [shape
   {[source-center-x source-center-y] :center}
   [offset-x offset-y] ]
  (adjust-center shape
    [(+ source-center-x offset-x) (+ source-center-y offset-y)]))

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
  [(:element-type child) (:attrs child)
   (if (:children child)
     (map xml-form-child (:children child) []))])

(defn export-svg
  "Exports a svg as xml into a String"
  [svg]
  (xml/emit-sexp-str [:svg (:attrs svg)
                      (map xml-form-child (:children svg))]))
