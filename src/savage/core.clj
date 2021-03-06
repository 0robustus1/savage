(ns savage.core
  (:require [xml-writer.core :as xml]
            [clojure.core.match :refer [match]]
            [clojure.string :as s]
            [savage.helper :refer :all]
            [savage.svg-structure :refer [unfold-percentages]]
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
  (#{:above-of :below-of :left-of :right-of} key))

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
  [svg [key :as form]]
  (if (vector? form)
    (when (svg-key? key)
      (cond
        (svg-cons? key) (let [raw-attrs (apply hash-map (rest form))
                              attrs (unfold-percentages svg raw-attrs)]
                          ((svg-sym key) attrs))
        (svg-rel? key) (let [[base target & [:by offset]] (rest form)]
                         ((svg-sym key) (svg-apply svg base) (svg-apply svg target)
                          (or offset 0)))))
    form))

(defn- svg-apply-specials
  [svg-apply form]
  (match
    form
    [:line :from source :to target & attrs]
    (let [[x1 y1] (:center (svg-apply source))
          [x2 y2] (:center (svg-apply target))]
      (into [:line :x1 x1 :y1 y1 :x2 x2 :y2 y2] attrs))
    [:default-attrs attrs & forms]
    (for [form forms]
      (let [svg-cons (svg-apply form)]
        (assoc svg-cons :attrs (into attrs (:attrs svg-cons)))))
    [(rel :guard svg-rel?) source target
     :and
     (o-rel :guard svg-rel?) o-source]
      [o-rel o-source [rel source target]]
    [(rel :guard svg-rel?) source target :by n
     :and
     (o-rel :guard svg-rel?) o-source]
      [o-rel o-source [rel source target :by n]]
    [(rel :guard svg-rel?) source target
     :and
     (o-rel :guard svg-rel?) o-source :by n]
      [o-rel o-source [rel source target] :by n]
    [(rel :guard svg-rel?) source target :by n
     :and
     (o-rel :guard svg-rel?) o-source :by o-n]
      [o-rel o-source [rel source target :by n] :by o-n]
    :else form))

(defn- svg-apply
  [svg form]
  (or
    (when-let [key (and (vector? form) (first form))]
      (->> form
          (svg-apply-specials (partial svg-apply svg))
          (svg-apply-fn-call svg)))
    form))

(defn- svg-children
  [svg [form & _rest-forms :as forms]]
  (flatten (if (vector? form)
             (map (partial svg-apply svg) forms)
             (svg-apply svg forms))))

(defn make-svg
  [svg-attrs & forms]
  (let [svg (dsl/svg svg-attrs)]
    (assoc svg :children (svg-children svg forms))))
