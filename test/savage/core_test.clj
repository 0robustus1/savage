(ns savage.core-test
  (:require [clojure.test :refer :all]
            [savage.core :refer :all]))

(deftest creation-relativity
  (testing "relative creation of a simple rect-svg"
    (let [some-rect (rect :width 12 :height 6 :x 12 :y 6)
          relative-rect (rect :width 12 :height 6 :x 7 :y 6)
          root (svg {:width 64 :height 64} some-rect
                    (left-of-center-from some-rect some-rect 5))]
      (is (some #{relative-rect} (:children root))
          "Contains the relatively created rect")))
  (testing "relative creation should work with negative coordinates"
    (let [some-circle (circle :cx -5 :cy -5 :r 10)
          relative-circle (circle :cx -5 :cy -10 :r 10)
          root (svg {:width 64 :height 64} some-circle
                    (above-center-from some-circle some-circle 5))]
      (is (some #{relative-circle} (:children root))
          "Contains the relatively created circle"))))

(deftest polyline-geometry
  (let [attrs {:points "0,0 10,5 0,10 0,0"}
        shape (polyline :points (:points attrs))]
    (testing "below-center-from should move correctly"
      (let [relative (below-center-from shape shape 5)
            expected-attrs {:points "0,5 10,10 0,15 0,5"}
            expected (polyline :points (:points expected-attrs))]
        (is (= relative expected))))
    (testing "above-center-from should move correctly"
      (let [relative (above-center-from shape shape 5)
            expected-attrs {:points  "0,-5 10,0 0,5 0,-5"}
            expected (polyline :points (:points expected-attrs))]
        (is (= relative expected))))))

(deftest polygon-geometry
  (let [attrs {:points "0,0 10,5 0,10 0,0"}
        shape (polygon :points (:points attrs))
        relative (right-of-center-from shape shape 5)
        expected-attrs {:points "5,0 15,5 5,10 5,0"}
        expected (polygon :points (:points expected-attrs))]
    (testing "relativity should move correctly"
      (is (= relative expected)))))
