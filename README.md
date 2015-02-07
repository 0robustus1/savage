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

Instead of building a macro-based DSL (which would transform custom symbols
into normal function-calls which will then create an internal svg-structure),
i've decided to go a more classic way - for clojure at least - by
creating a vectorized dsl. This basically means that one will build vectors
which will represent either svg-element constructs or expressions about that
svg-element construct.

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
      [:left-from rectangle [:circle :r 5] :by 25]))
  ```

  - Additionally to `:left-from` one can use `:above-from`, `:below-from` and
    `:right-from`.

Additionally there are the expressions about actual svg element, which
will allow us to place a rectangle relative to a circle or something like that.

- Draw a line between two other shapes:

  ```clojure
  (make-svg {:width 64 :height 64}
    (let [rectangle [:rect :x 22 :width 20 :y 22 :height 20 :fill "black"]
          circle [:circle :x 53 :y :5 :r 13]]
      [:line :from rectangle :to circle]))
  ```

- Provide default attributes to multiple shapes at once.

  ```clojure
  (make-svg {:width 64 :height 64}
    (let [rectangle [:rect :x 22 :width 20 :y 22 :height 20 :fill "black"]]
      [:default-attrs {:stroke "white" :stroke-width "1.5"}
       [:left-from rectangle [:circle :r 2] :by 5]
       [:left-from rectangle [:circle :r 2] :by 10]
       [:left-from rectangle [:circle :r 2] :by 17]
       [:left-from rectangle [:circle :r 2] :by 25]]))
  ```

- Combine two relativity calls. The target shape (in this case
  `[:circle :r 2]`) only needs to be provided once.

  ```clojure
  (make-svg {:width 64 :height 64}
    [:left-from [:rect :width 10 :height 22 :x 25] [:circle :r 2] :by 5
     :and
     :above-from [:rect :x 15 :y 25 :width 10 :height 10] :by 5]
       [:left-from rectangle [:circle :r 2] :by 10]
       [:left-from rectangle [:circle :r 2] :by 17]
       [:left-from rectangle [:circle :r 2] :by 25])
  ```

## Supported SVG Elements

Currently savage does not yet support every SVG-Element, as we want to provide
the special handling capabilities for every element that is integrated into
the DSL. The currently supported Elements are:

- Shapes:
  - rect
  - circle
  - ellipse
  - line
  - polyline
  - polygon

Support for the following is planned in the very near future:

- Text:
  - text
  - tspan
  - tref
- Paths:
  - path

Support for these elements is currently not planned, but might be
available in the future:

- Text:
  - textPath
- Definitions:
  - marker

## License

Copyright © 2014 Tim Reddehase

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
