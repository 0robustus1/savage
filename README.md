# savage

[![Build Status](https://travis-ci.org/0robustus1/savage.svg?branch=master)](https://travis-ci.org/0robustus1/savage)
[![Project Dependencies](https://www.versioneye.com/user/projects/54d65c2d3ca08495310006c7/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54d65c2d3ca08495310006c7)
[![Clojars Version](https://img.shields.io/badge/Clojars--Version-0.0.7-blue.svg)](http://clojars.org/savage)

[![Clojars Project](http://clojars.org/savage/latest-version.svg)](http://clojars.org/savage)

**s**a**v**a**g**e is a dsl for svg. A dsl (domain specific language) takes a
domain and allows the developer to write a program/definition with words and
syntax from that domain. As clojure provides a real nice macro system (as only
Lisp's make it possible) it is quite easy to build complex domain specific
languages here. However this dsl is a little different.  It does not only allow
you to write a *svg* in clojure code but also allows you to make reference
which were not possible before (because a svg-document does not really have a
sense of variables and objects).

If you want to read a little bit more about the road we went on to create this
project you should take a look at this [article][scale-your-crazy] on my blog.

[scale-your-crazy]: https://rightsrestricted.com/2015/02/08/scale-your-crazy/

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
      [:left-of rectangle [:circle :r 5] :by 25]))
  ```

  - Additionally to `:left-of` one can use `:above-of`, `:below-of` and
    `:right-of`.

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
       [:left-of rectangle [:circle :r 2] :by 5]
       [:left-of rectangle [:circle :r 2] :by 10]
       [:left-of rectangle [:circle :r 2] :by 17]
       [:left-of rectangle [:circle :r 2] :by 25]]))
  ```

- Combine two relativity calls. The target shape (in this case
  `[:circle :r 2]`) only needs to be provided once.

  ```clojure
  (make-svg {:width 64 :height 64}
    [:left-of [:rect :width 10 :height 22 :x 25] [:circle :r 2] :by 5
     :and
     :above-of [:rect :x 15 :y 25 :width 10 :height 10] :by 5]
       [:left-of rectangle [:circle :r 2] :by 10]
       [:left-of rectangle [:circle :r 2] :by 17]
       [:left-of rectangle [:circle :r 2] :by 25])
  ```

- Provide percentage values (relative to the SVG-elements `width` and `height`
  attributes) instead of absolute ones. Currently this is only supported for
  the direct shape vectors (e.g. `:rect`, `:circle`, ...) and not for
  relationships or even `:default-attrs`.

  ```clojure
  (make-svg {:width 64 :height 64}
    [:rect
    :width {:dimension :width :percentage 10}
    :height {:dimension :height :percentage 20}
    :x {:dimension :width :percentage 40}
    :y {:dimension :height :percentage 30}])
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

Copyright © 2014-2015 Tim Reddehase

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
