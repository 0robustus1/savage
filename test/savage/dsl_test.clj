(ns savage.dsl-test
  (:require [clojure.test :refer :all]
            [savage.dsl :refer :all]
            [savage.svg-structure :as svg]))

(deftest empty-svg
  (testing "that calling svg with mandatory args will yield empty children"
    (let [element (svg {:width 64 :height 64})]
      (is (empty? (:children svg)))))
  (testing "mandatory args will yield those args in result"
    (let [element (svg {:width 64 :height 65})]
      (are [x y] (= x y)
           (get-in element [:attrs :width]) 64
           (get-in element [:attrs :height]) 65)))
  (testing "svg has element-type of svg"
    (let [element (svg {:width 0 :height 0})]
      (is (:element-type element) :svg))))

(deftest create-svg
  (let [some-rect (rect :width 10 :height 5)
        some-circle (circle :cx 5 :cy 5 :r 5)
        some-line (line :x1 0 :x2 10 :y1 0 :y 10)
        root (svg {:width 64 :height 64}
                  some-rect some-circle some-line)]
    (testing "root should contain the structures"
      (is (some #{some-rect} (:children root)) "Contains the rect")
      (is (some #{some-circle} (:children root)) "Contains the circle")
      (is (some #{some-line} (:children root)) "Contains the line"))))

(deftest create-polyline
  (let [attrs {:points "0,0 10,5 0,10 0,0"}
        shape (polyline :points (:points attrs))
        expected (svg/make-shape :polyline [] attrs
                                 :center [5 5]
                                 :bbox {:x 5 :y 5 :width 10 :height 10})]
    (testing "creation should set center correctly"
      (is (= (:center shape) (:center expected))))
    (testing "creation should set bbox correctly"
      (is (= (:bbox shape) (:bbox expected))))))

(deftest create-polygon
  (let [attrs {:points "0,0 10,5 0,10 0,0"}
        shape (polygon :points (:points attrs))
        expected (svg/make-shape :polygon attrs []
                                 :center [5 5]
                                 :bbox {:x 5 :y 5 :width 10 :height 10})]
    (testing "creation should set center correctly"
      (is (= (:center shape) (:center expected))))
    (testing "creation should set bbox correctly"
      (is (= (:bbox shape) (:bbox expected))))))

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
        shape (polygon :points (:points attrs))]
    (testing "right-of-center-from should move correctly"
      (let [relative (right-of-center-from shape shape 5)
            expected-attrs {:points "5,0 15,5 5,10 5,0"}
            expected (polygon :points (:points expected-attrs))]
        (is (= relative expected))))
    (testing "left-of-center-from should move correctly"
      (let [relative (left-of-center-from shape shape 5)
            expected-attrs {:points  "-5,0 5,5 -5,10 -5,0"}
            expected (polygon :points (:points expected-attrs))]
        (is (= relative expected))))))

(deftest spaced-relativity
  (testing "left-from in spaced mode shall work as expected"
    (let [shape (rect :x 10 :y 10 :width 5 :height 5)
          other (rect :width 10 :height 10)
          offset 10
          expected (rect :x -15/2 :y 10 :width 10 :height 10)
          relative (left-from shape other offset :space)]
      (is (= relative expected))))
  (testing "right-from in spaced mode shall work as expected"
    (let [shape (rect :x 10 :y 10 :width 5 :height 5)
          other (rect :width 10 :height 10)
          offset 10
          expected (rect :x 55/2 :y 10 :width 10 :height 10)
          relative (right-from shape other offset :space)]
      (is (= relative expected))))
  (testing "above-from in spaced mode shall work as expected"
    (let [shape (rect :x 10 :y 10 :width 5 :height 5)
          other (rect :width 10 :height 10)
          offset 10
          expected (rect :x 10 :y -15/2 :width 10 :height 10)
          relative (above-from shape other offset :space)]
      (is (= relative expected))))
  (testing "below-from in spaced mode shall work as expected"
    (let [shape (rect :x 10 :y 10 :width 5 :height 5)
          other (rect :width 10 :height 10)
          offset 10
          expected (rect :x 10 :y 55/2 :width 10 :height 10)
          relative (below-from shape other offset :space)]
      (is (= relative expected)))))
