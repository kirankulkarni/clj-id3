(ns ^{:doc "Utilities"
      :author "Kiran Kulkarni <kk@helpshift.com>"}
  clj-id3.utils
  )


(defn bucket-split
  "Splits given sequence in given bucket-sizes"
  [coll & bucket-sizes]
  {:pre [(seq bucket-sizes)]}
  (let [coll-vector (vec coll)
        bucket-indices (reduce #(conj %1 (+ (last %1) %2)) [0] bucket-sizes)]
    (map #(if (= (- %2 %1) 1)
             (get coll-vector %1)
             (subvec coll-vector %1 %2))
         bucket-indices
         (rest bucket-indices))))


(defn byte-seq->str
  [byte-seq & {:keys [encoding] :or {encoding "ISO-8859-1"}}]
  (when (seq byte-seq)
    (String. (into-array Byte/TYPE byte-seq) encoding)))


(defn str->byte-seq
  [string]
  (seq (.getBytes string)))


(def not-zero? (complement zero?))
