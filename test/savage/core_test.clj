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
           (get-in element [:attrs :height]) 65))))

(deftest create-with-no-errors
  (testing "relative creating of a simple rect-svg"
    (let [some-rect (rect :width 12 :height 6)
          res (svg {:width 64 :height 64} some-rect
                   (left-of-center-from some-rect some-rect 5))]
      (is (:element-type res) :svg))))
