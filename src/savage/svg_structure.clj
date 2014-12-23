(ns savage.svg-structure)

(def restricted-keys [:element :children :center :bb-box])

(defn raw-attrs
  [element]
  (apply dissoc element restricted-keys) )

(defn svg
  "Defines a root svg element as a clojure structure"
  [width height children]
  {:element :svg :width width :height height :children children} )

(defn rect
  "Defines Representation of SVG rectangle"
  [& attrs]
  (assoc (apply array-map attrs) :element :rect) )

(defn circle
  "Defines Representation of SVG circle"
  [& attrs]
  (assoc (apply array-map attrs) :element :circle) )

(defn ellipse
  "Defines Representation of SVG ellipse"
  [& attrs]
  (assoc (apply array-map attrs) :element :ellipse) )

(defn line
  "Defines Representation of SVG line"
  [& attrs]
  (assoc (apply array-map attrs) :element :line) )

(defn polyline
  "Defines Representation of SVG polyline"
  [& attrs]
  (assoc (apply array-map attrs) :element :polyline) )

(defn polygon
  "Defines Representation of SVG polygon"
  [& attrs]
  (assoc (apply array-map attrs) :element :polygon) )
