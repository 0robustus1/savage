(ns savage.helper)

(defn as-num
  "Evaluates a string as number. Does not include safety checks."
  [number]
  (Integer/parseInt number))

(defn max-min
  "Returns the [max min] combination of a list of numbers"
  [list]
  [(apply max list) (apply min list)])

(defn min-max
  "Returns the [min max] combination of a list of numbers"
  [list]
  (reverse (max-min list)))

(defn use-nth
  "Returns a list containing the nth-element of each inner list"
  [index list-of-lists]
  (reduce (fn [res inner-list]
            (cons (nth inner-list index) res)) [] list-of-lists))

(defn assoc-record
  "Assocs a member inside of a record like 'assoc' would do"
  ([record member key val]
  (assoc-in record [member key] val))
  ([record member key val & kvs]
   (let [record (assoc-record record member key val)]
     (if kvs
       (recur record member (first kvs) (second kvs) (nnext kvs))
       record))))
