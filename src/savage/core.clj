(ns savage.core
  (:require [xml-writer.core :as xml]
            [clojure.core.match :refer [match]]
            [clojure.string :as s]
            [savage.helper :refer :all]
            [savage.dsl :as dsl]))

(defn- us-format
  "Basically the same as 'format' but enforce US-Locale"
    ^String  [fmt & args]
    (String/format java.util.Locale/US fmt (to-array args)))

(defn- filter-for-decimal
  "Traverses the map and returns every fractional/rational
  number as a decimal-string otherwise as integer-string."
  [coll]
  (let [filter-fn
        (fn [[key val]]
          [key (cond
                 (ratio? val) (us-format "%.3f" (bigdec val))
                 (decimal? val) (us-format "%d" val)
                 :else val)])]
    (into (empty coll) (map filter-fn coll))))

(defn- xml-form-child
  "Traverses the children-tree and returns xml-write syntax."
  [child]
  [(:element-type child) (filter-for-decimal (:attrs child))
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

(declare svg-apply)

(defn- svg-rel?
  [key]
  "Tests whether the key matches a svg-relativity function."
  (#{:above-from :below-from :left-from :right-from} key))

(defn- svg-cons?
  "Tests whether the key matches a svg-construct/structure."
  [key]
  (#{:rect :circle :ellipse :line :polyline :polygon} key))

(defn- svg-key?
  "Tests whether key is a known svg-function."
  [key]
  (or (svg-rel? key) (svg-cons? key)))

(defn- svg-sym
  "Retrieves var svg-symbol in the savage.dsl namespace for a given key."
  [key]
  (find-var (symbol (str 'savage.dsl "/" (name key)))))

(defn- svg-apply-fn-call
  "Applies a svg-function call to vector-construct form."
  [[key :as form]]
  (if (vector? form)
    (when (svg-key? key)
      (cond
        (svg-cons? key) (apply (svg-sym key) (rest form))
        (svg-rel? key) (let [[base target & [:by offset]] (rest form)]
                         ((svg-sym key) (svg-apply base) (svg-apply target)
                          (or offset 0)))))
    form))

(defn- svg-apply-specials
  [form]
  (match
    form
    [:line :from source :to target & attrs]
    (let [[x1 y1] (:center (svg-apply source))
          [x2 y2] (:center (svg-apply target))]
      (into [:line :x1 x1 :y1 y1 :x2 x2 :y2 y2] attrs))
    :else form))

(defn- svg-apply
  [form]
  (or
    (when-let [key (and (vector? form) (first form))]
      (-> form svg-apply-specials svg-apply-fn-call))
    form))

(defn- svg-children
  [[form & _rest-forms :as forms]]
  (if (vector? form)
    (map svg-apply forms)
    (svg-apply forms)))

(defn make-svg
  [svg-attrs & forms]
  (apply dsl/svg svg-attrs (svg-children forms)))
