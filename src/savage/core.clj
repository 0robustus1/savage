(ns savage.core
  (:require [xml-writer.core :as xml]
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

(defn- svg-rel?
  [key]
  (#{:above-from :below-from :left-from :right-from} key))

(defn- svg-cons?
  [key]
  (#{:rect :circle :ellipse :line :polyline :polygon} key))

(defn- svg-key?
  [key]
  (or (svg-rel? key) (svg-cons? key)))

(defn- svg-sym
  [key]
  (find-var (symbol (str 'savage.dsl "/" (name key)))))

(defn- svg-apply
  [form]
  (or
    (when-let [key (and (vector? form) (first form))]
      (when (svg-key? key)
        (cond
          (svg-cons? key) (apply (svg-sym key) (rest form))
          (svg-rel? key) (let [[base target & [:by offset]] (rest form)]
                           ((svg-sym key) (svg-apply base) (svg-apply target)
                            (or offset 0))))))
    form))

(defn- svg-children
  [[form & _rest-forms :as forms]]
  (if (vector? form)
    (map svg-apply forms)
    (svg-apply forms)))

(defn make-svg
  [svg-attrs & forms]
  (apply dsl/svg svg-attrs (svg-children forms)))
