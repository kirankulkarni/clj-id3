(ns ^{:doc "Utilities"
      :author "Kiran Kulkarni <kk@helpshift.com>"}
  clj-id3.utils
  )

(defn bucket-split
  "Splits a collection in given buckets
   (bucket-split '(0 1 2 3 4 5 6 7 8 9) 4 3 1 2) => [(0 1 2 3) (4 5 6) 7 (8 9)]"
  [coll & bucket-sizes]
  (first (reduce (fn [[acc coll] n]
                   [(if (= n 1)
                      (conj acc (first coll))
                      (conj acc
                            (take n coll)))
                    (drop n coll)])
                 [[] coll]
                 bucket-sizes)))

(defn byte-seq->str
  [byte-seq & {:keys [encoding] :or {encoding "ISO-8859-1"}}]
  (when (seq byte-seq)
    (String. (into-array Byte/TYPE byte-seq) encoding)))


(defn str->byte-seq
  [string]
  (seq (.getBytes string)))


(def not-zero? (complement zero?))
