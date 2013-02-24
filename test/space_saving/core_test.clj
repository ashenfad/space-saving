(ns space-saving.core-test
  (:use clojure.test)
  (:require (bigml.sampling [random :as random])
            (space-saving [mutable :as mut]
                          [immutable :as immut])))

(defn- make-data [size seed]
  (let [rnd (java.util.Random. (hash seed))
        d1-pop (int (* size 0.985))
        d1 (map #(long (* 100000 %))
                (repeatedly d1-pop #(.nextGaussian rnd)))
        d2 (map #(long (+ 36 (* 10 %)))
                (repeatedly (- size d1-pop) #(.nextGaussian rnd)))]
    (random/shuffle! (concat d1 d2) rnd)))

(deftest summaries
  (let [data (make-data 100000 7)
        summ1 (reduce mut/insert! (mut/create 2000) data)
        summ2 (reduce immut/insert (immut/create 2000) data)]
    (is (= (mut/top-k summ1 8)
           (immut/top-k summ2 8)
           '({:item 33, :count 65, :max-errors 1}
             {:item 39, :count 64, :max-errors 27}
             {:item 37, :count 60, :max-errors 0}
             {:item 35, :count 60, :max-errors 24}
             {:item 36, :count 60, :max-errors 6}
             {:item 29, :count 59, :max-errors 31}
             {:item 34, :count 59, :max-errors 27}
             {:item 38, :count 59, :max-errors 18})))
    (is (= (mut/frequent summ1 0.0003 :guarantee true)
           (immut/frequent summ2 0.0003 :guarantee true)
           '({:item 33, :count 65, :max-errors 1}
             {:item 39, :count 64, :max-errors 27}
             {:item 37, :count 60, :max-errors 0}
             {:item 35, :count 60, :max-errors 24}
             {:item 36, :count 60, :max-errors 6})))))
