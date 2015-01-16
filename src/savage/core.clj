(ns savage.core
  (:require [xml-writer.core :as xml]
            [clojure.string :as s]
            [savage.helper :refer :all]
            [savage.dsl :as dsl]))

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
