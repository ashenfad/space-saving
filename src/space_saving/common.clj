(ns space-saving.common)

(defn find-top-k
  "Finds the top k given a producer fn."
  [producer k guarantee]
  (let [top (producer (if guarantee (inc k) k))]
    (if guarantee
      (filter #(> (- (:count %) (:max-errors %))
                  (:count (last top)))
              top)
      top)))

(defn find-frequent
  "Finds the frequent items."
  [bins floor guarantee]
  (let [offset (if guarantee #(- (:count %) (:max-errors %)) :count)]
    (take-while #(>= (offset %) floor) bins)))
