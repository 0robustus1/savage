(ns savage.core-test
  (:require [clojure.test :refer :all]
            [savage.core :refer :all]
            [savage.dsl :as dsl]))

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
