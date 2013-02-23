(ns ^{:doc "File operations"
      :author "Kiran Kulkarni <kk@helpshift.com>"}
  clj-id3.fops
  (:require [clojure.java.io :as io])
  (:import [java.io IOException]))


(defn lazy-byte-reader
  "Opens the file and creates a lazy-sequence of the bytes"
  [file]
  (letfn [(byte-reader
            [rdr]
            (lazy-seq
              (let [byte (.read rdr)]
                (if (>= byte 0)
                  (cons (short (bit-and byte 0xFF)) (byte-reader rdr))
                  (do (. rdr close) nil)))))]
    (byte-reader (io/reader file))))



(defn read-bytes
  "Reads given number of bytes from file and returns byte-seq remaining byte-seq
   Throws IOException if unable to read given number of bytes"
  [byte-seq size & {:keys [throw-exception] :or {throw-exception true}}]
  (let [bytes (take size byte-seq)
        bytes-count (count bytes)]
    (if (or (= bytes-count size)
            (not throw-exception))
      [bytes (drop size byte-seq)]
      (throw (IOException. "Reached EOF prematurely")))))
