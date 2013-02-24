(ns space-saving.mutable
  (:import (com.clearspring.analytics.stream StreamSummary Counter))
  (:require (space-saving [common :as common])))

(defn create
  "Creates a new summary."
  [& [bins]]
  (StreamSummary. (or bins 1024)))

(defn insert!
  "Insert an item into the summary."
  [^StreamSummary summ item]
  (.offer summ item)
  summ)

(defn- counter-to-clj [^Counter c]
  {:item (.getItem c)
   :count (.getCount c)
   :max-errors (.getError c)})

(defn top-k
  "Returns the top k items."
  [^StreamSummary summ k & {:keys [guarantee]}]
  (common/find-top-k #(map counter-to-clj (.topK summ %)) k guarantee))

(defn frequent
  "Find frequent items given support."
  [^StreamSummary summ support & {:keys [guarantee]}]
  (let [bins (map counter-to-clj (.topK summ (.size summ)))]
    (common/find-frequent bins
                          (* support (reduce + (map :count bins)))
                          guarantee)))
