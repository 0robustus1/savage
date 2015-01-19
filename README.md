# savage

[![Build Status](https://travis-ci.org/0robustus1/savage.svg?branch=master)](https://travis-ci.org/0robustus1/savage)

**s**a**v**a**g**e is a dsl for svg. A dsl (domain specific language) takes a
domain and allows the developer to write a program/definition with words and
syntax from that domain. As clojure provides a real nice macro system (as only
Lisp's make it possible) it is quite easy to build complex domain specific
languages here. However this dsl is a little different.  It does not only allow
you to write a *svg* in clojure code but also allows you to make reference
which were not possible before (because a svg-document does not really have a
sense of variables and objects).

## Usage

There are basically two versions of using savage. The one is a vectorized dsl
(which is the preferred version of using savage) and the other one is a set of
functions to create a svg. They are basically equivalent, as the vectorized dsl
is translated into function calls to these functions.

In order to use the vectorized-version you might want to require
`'savage.core`.  To use the functional version requiring
`'savage.svg-structure` is the way to go.

### Vectorized DSL

`use` `'savage.core`.

- A basic example, which will place a rectangle into the center of the canvas.

  ```clojure
  (make-svg {:width 64 :height 64}
    [:rect :x 22 :width 20 :y 22 :height 20 :fill "black"])
  ```

- Place a circle relative to a rectangle. The `:by 25`
  is optional and will default to zero.

  ```clojure
  (make-svg {:width 64 :height 64}
    (let [rectangle [:rect :x 22 :width 20 :y 22 :height 20 :fill "black"]]
      [:left-of-center-from rectangle [:circle :r 5] :by 25]))
  ```

  - Additionally one can use `:above-center-from`, `:below-center-from` and
    `:right-of-center-from`.


### Functional API

`use` `'savage.svg-structure`.

- A basic example, which will place a rectangle into the center of the canvas.

  ```clojure
  (make-svg {:width 64 :height 64}
    (rect :x 22 :width 20 :y 22 :height 20 :fill "black"))
  ```

- Place a circle relative to a rectangle. The `:by 25`
  is optional and will default to zero.

  ```clojure
  (svg {:width 64 :height 64}
    (rect :x 22 :width 20 :y 22 :height 20 :fill "black")
    (left-of-center-from (rect :x 22 :width 20 :y 22 :height 20 :fill "black")
      (:circle :r 5) 25]))
  ```

  - Additionally one can use `above-center-from`, `below-center-from` and
    `right-of-center-from`.

## License

Copyright © 2014 Tim Reddehase

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
