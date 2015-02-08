(ns savage.core-test
  (:require [clojure.test :refer :all]
            [savage.core :refer :all]
            [savage.dsl :as dsl]))

(deftest percentages-in-elements
  (let [svg-width 64
        svg-height 64
        svg-attrs {:width svg-width :height svg-height}]
    (testing "creating a rect with a width percentage works"
      (let [fifty% {:dimension :width :percentage 50}
            svg (make-svg svg-attrs
                          [:rect :width fifty% :height 5])
            rect (-> svg :children first)]
        (is (= (/ svg-width 2) (-> rect :attrs :width)))))
    (testing "creating a rect with a y percentage works"
      (let [ten% {:dimension :height :percentage 10}
            svg (make-svg svg-attrs
                          [:rect :width 10 :height 10 :y ten%])
            rect (-> svg :children first)]
        (is (= (/ svg-height 10) (-> rect :attrs :y)))))))

(deftest svg-dsl-element
  (testing "creating svg with rect"
    (let [svg-attrs {:width 64 :height 64}
          svg (make-svg svg-attrs
                        [:rect :width 5 :height 10])
          manual-svg (dsl/svg svg-attrs (dsl/rect :width 5 :height 10))]
      (is (= svg manual-svg))))
  (testing "creating svg with circle"
    (let [svg-attrs {:width 64 :height 64}
          svg (make-svg svg-attrs
                        [:circle :r 5])
          manual-svg (dsl/svg svg-attrs (dsl/circle :r 5))]
      (is (= svg manual-svg))))
  (testing "creating svg with ellipse"
    (let [svg-attrs {:width 64 :height 64}
          svg (make-svg svg-attrs
                        [:ellipse :rx 5 :ry 10])
          manual-svg (dsl/svg svg-attrs (dsl/ellipse :rx 5 :ry 10))]
      (is (= svg manual-svg))))
  (testing "creating svg with line"
    (let [svg-attrs {:width 64 :height 64}
          svg (make-svg svg-attrs
                        [:line :x2 10 :y2 10])
          manual-svg (dsl/svg svg-attrs (dsl/line :x2 10 :y2 10))]
      (is (= svg manual-svg))))
  (testing "creating svg with polyline"
    (let [svg-attrs {:width 64 :height 64}
          svg (make-svg svg-attrs
                        [:polyline :points "1,2 3,4"])
          manual-svg (dsl/svg svg-attrs (dsl/polyline :points "1,2 3,4"))]
      (is (= svg manual-svg))))
  (testing "creating svg with polygon"
    (let [svg-attrs {:width 64 :height 64}
          svg (make-svg svg-attrs
                        [:polygon :points "1,2 3,4"])
          manual-svg (dsl/svg svg-attrs (dsl/polygon :points "1,2 3,4"))]
      (is (= svg manual-svg)))))

(deftest svg-full-dsl-example
  (let [offset 51/2
        background [:rect :id "background"
                    :width 64 :height 64 :fill "black" :rx 16]

        top-k [:circle :cx 32 :cy 13/2 :r 4 :stroke-width 0 :fill "white"]
        top-j [:circle :cx 32 :cy 81/2 :r 4 :stroke-width 0 :fill "white"]
        top-h [:circle :cx 12 :cy 47/2 :r 4 :stroke-width 0 :fill "white"]
        top-l [:circle :cx 52 :cy 47/2 :r 4 :stroke-width 0 :fill "white"]

        bottom-k [:below-of top-k [:circle :cx 32 :r 4
                  :stroke-width 0 :fill "white"] :by 17]
        bottom-j [:below-of top-j [:circle :cx 32 :r 4
                  :stroke-width 0 :fill "white"] :by 17]
        bottom-h [:below-of top-h [:circle :cx 12 :r 4
                  :stroke-width 0 :fill "white"] :by 17]
        bottom-l [:below-of top-l [:circle :cx 52 :r 4
                  :stroke-width 0 :fill "white"] :by 17]

        k-k [:line :from top-k :to bottom-k]
        j-j [:line :from top-j :to bottom-j]
        h-h [:line :from top-h :to bottom-h]
        l-l [:line :from top-l :to bottom-l]

        bottom-k-l [:line :from bottom-k :to bottom-l]
        bottom-l-j [:line :from bottom-l :to bottom-j]
        bottom-j-h [:line :from bottom-j :to bottom-h]
        bottom-h-k [:line :from bottom-h :to bottom-k]

        top-k-l [:line :from top-k :to top-l]
        top-l-j [:line :from top-l :to top-j]
        top-j-h [:line :from top-j :to top-h]
        top-h-k [:line :from top-h :to top-k]
        svg (make-svg
              {:width 64 :height 64}
              background top-k top-j top-h top-l
              bottom-k bottom-j bottom-h bottom-l
              [:default-attrs {:stroke "white" :stroke-width "1.5"}
               k-k j-j h-h l-l
               bottom-k-l bottom-l-j bottom-j-h bottom-h-k
               top-k-l top-l-j top-j-h top-h-k])]
    (is (= (export-svg svg) (export-svg #savage.svg_structure.SVG{:element-type :svg, :attrs {:viewBox "0 0 64 64", :xmlns "http://www.w3.org/2000/svg", :width 64, :height 64}, :children (
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :rect, :children [], :attrs {:y 0, :rx 16, :fill black, :width 64, :id background, :x 0, :height 64}, :center [0 0], :bbox {:x 0, :y 0, :width 64, :height 64}}
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :circle, :children [], :attrs {:r 4, :fill white, :stroke-width 0, :cx 32, :cy 13/2}, :center [32 13/2], :bbox {:x 32, :y 13/2, :width 8, :height 8}}
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :circle, :children [], :attrs {:r 4, :fill white, :stroke-width 0, :cx 32, :cy 81/2}, :center [32 81/2], :bbox {:x 32, :y 81/2, :width 8, :height 8}}
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :circle, :children [], :attrs {:r 4, :fill white, :stroke-width 0, :cx 12, :cy 47/2}, :center [12 47/2], :bbox {:x 12, :y 47/2, :width 8, :height 8}}
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :circle, :children [], :attrs {:r 4, :fill white, :stroke-width 0, :cx 52, :cy 47/2}, :center [52 47/2], :bbox {:x 52, :y 47/2, :width 8, :height 8}}
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :circle, :children [], :attrs {:r 4, :fill white, :stroke-width 0, :cx 32, :cy 47/2}, :center [32 47/2], :bbox {:x 32, :y 47/2, :width 8, :height 8}}
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :circle, :children [], :attrs {:r 4, :fill white, :stroke-width 0, :cx 32, :cy 115/2}, :center [32 115/2], :bbox {:x 32, :y 115/2, :width 8, :height 8}}
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :circle, :children [], :attrs {:r 4, :fill white, :stroke-width 0, :cx 12, :cy 81/2}, :center [12 81/2], :bbox {:x 12, :y 81/2, :width 8, :height 8}}
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :circle, :children [], :attrs {:r 4, :fill white, :stroke-width 0, :cx 52, :cy 81/2}, :center [52 81/2], :bbox {:x 52, :y 81/2, :width 8, :height 8}}
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :line, :children [], :attrs {:stroke white, :stroke-width 1.5, :y1 13/2, :x1 32, :y2 47/2, :x2 32}, :center [32 15N], :bbox {:x 32, :y 15N, :width 0, :height 17N}}
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :line, :children [], :attrs {:stroke white, :stroke-width 1.5, :y1 81/2, :x1 32, :y2 115/2, :x2 32}, :center [32 49N], :bbox {:x 32, :y 49N, :width 0, :height 17N}}
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :line, :children [], :attrs {:stroke white, :stroke-width 1.5, :y1 47/2, :x1 12, :y2 81/2, :x2 12}, :center [12 32N], :bbox {:x 12, :y 32N, :width 0, :height 17N}}
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :line, :children [], :attrs {:stroke white, :stroke-width 1.5, :y1 47/2, :x1 52, :y2 81/2, :x2 52}, :center [52 32N], :bbox {:x 52, :y 32N, :width 0, :height 17N}}
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :line, :children [], :attrs {:stroke white, :stroke-width 1.5, :y1 47/2, :x1 32, :y2 81/2, :x2 52}, :center [42 32N], :bbox {:x 42, :y 32N, :width 20, :height 17N}}
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :line, :children [], :attrs {:stroke white, :stroke-width 1.5, :y1 81/2, :x1 52, :y2 115/2, :x2 32}, :center [22 49N], :bbox {:x 22, :y 49N, :width -20, :height 17N}}
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :line, :children [], :attrs {:stroke white, :stroke-width 1.5, :y1 115/2, :x1 32, :y2 81/2, :x2 12}, :center [2 32N], :bbox {:x 2, :y 32N, :width -20, :height -17N}}
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :line, :children [], :attrs {:stroke white, :stroke-width 1.5, :y1 81/2, :x1 12, :y2 47/2, :x2 32}, :center [22 15N], :bbox {:x 22, :y 15N, :width 20, :height -17N}}
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :line, :children [], :attrs {:stroke white, :stroke-width 1.5, :y1 13/2, :x1 32, :y2 47/2, :x2 52}, :center [42 15N], :bbox {:x 42, :y 15N, :width 20, :height 17N}}
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :line, :children [], :attrs {:stroke white, :stroke-width 1.5, :y1 47/2, :x1 52, :y2 81/2, :x2 32}, :center [22 32N], :bbox {:x 22, :y 32N, :width -20, :height 17N}}
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :line, :children [], :attrs {:stroke white, :stroke-width 1.5, :y1 81/2, :x1 32, :y2 47/2, :x2 12}, :center [2 15N], :bbox {:x 2, :y 15N, :width -20, :height -17N}}
                                                                                                                                                                    #savage.svg_structure.Shape{:element-type :line, :children [], :attrs {:stroke white, :stroke-width 1.5, :y1 47/2, :x1 12, :y2 13/2, :x2 32}, :center [22 -2N], :bbox {:x 22, :y -2N, :width 20, :height -17N}})})))))
