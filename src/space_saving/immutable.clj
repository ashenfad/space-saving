(ns space-saving.immutable
  (:require (space-saving [common :as common])))

(defn create
  "Creates a new summary."
  [& [bins]]
  {:locs {} :edges {} :bins [] :max-bins (or bins 1024)})

(def ^:private new-bin {:count 1 :max-errors 0})

(defn- no-jump [{:keys [locs edges bins] :as summ} new-bin index]
  (let [new-count (:count new-bin)
        old-count (dec new-count)
        edges (if (edges new-count)
                edges
                (assoc edges new-count index))]
    (assoc summ
      :locs (assoc locs (:item new-bin) index)
      :edges (if (= index (edges old-count))
                (let [lesser-bin (get bins (inc index))]
                  (if (and lesser-bin (= (:count lesser-bin) old-count))
                    (assoc edges old-count (inc index))
                    (dissoc edges old-count)))
                edges)
      :bins (assoc bins index new-bin))))

(defn- jump-left [{:keys [locs edges bins] :as summ} new-bin index]
  (let [new-count (:count new-bin)
        old-count (dec new-count)
        edge-index (edges old-count)
        edges (if (edges new-count)
                edges
                (assoc edges new-count edge-index))
        edge-bin (nth bins edge-index)]
    (assoc summ
      :locs (assoc locs (:item edge-bin) index (:item new-bin) edge-index)
      :edges (assoc edges old-count (inc edge-index))
      :bins (assoc bins index edge-bin edge-index new-bin))))

(defn- update-summ [{:keys [bins] :as summ} new-bin index]
  (let [next-bin (get bins (dec index))]
    (if (and next-bin (> (:count new-bin) (:count next-bin)))
      (jump-left summ new-bin index)
      (no-jump summ new-bin index))))

(defn- replace-bin [{:keys [count max-errors] :as bin} item]
  (assoc bin :item item :count (inc count) :max-errors count))

(defn insert
  "Inserts an item into the summary."
  [{:keys [locs edges bins max-bins] :as summ} item]
  (if-let [index (locs item)]
    (let [bin (nth bins index)]
      (update-summ summ (assoc bin :count (inc (:count bin))) index))
    (if (>= (count bins) max-bins)
      (let [index (dec (count bins))
            min-bin (nth bins index)]
        (update-summ (assoc summ :locs (dissoc locs (:item min-bin)))
                    (replace-bin min-bin item)
                    index))
      (assoc summ
        :locs (assoc locs item (count bins))
        :edges (if (get edges 1) edges (assoc edges 1 (count bins)))
        :bins (conj bins (assoc new-bin :item item))))))

(defn top-k
  "Returns the top k items."
  [summ k & {:keys [guarantee]}]
  (common/find-top-k #(take % (:bins summ)) k guarantee))

(defn frequent
  "Find frequent items given support."
  [summ support & {:keys [guarantee]}]
  (let [bins (:bins summ)]
    (common/find-frequent bins
                          (* support (reduce + (map :count bins)))
                          guarantee)))
