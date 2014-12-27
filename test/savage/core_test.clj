(ns savage.core-test
  (:require [clojure.test :refer :all]
            [savage.core :refer :all]))

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
    (testing "should contain the structures"
      (is (some #{some-rect} (:children root)) "Contains the rect")
      (is (some #{some-circle} (:children root)) "Contains the circle")
      (is (some #{some-line} (:children root)) "Contains the line")))
  (testing "relative creation of a simple rect-svg"
    (let [some-rect (rect :width 12 :height 6 :x 12 :y 6)
          relative-rect (rect :width 12 :height 6 :x 7 :y 6)
          root (svg {:width 64 :height 64} some-rect
                   (left-of-center-from some-rect some-rect 5))]
      (is (some #{relative-rect} (:children root))
          "Contains the relatively created rect"))))
