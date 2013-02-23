(ns ^{:doc "ID3 tags parser"
      :author "Kiran Kulkarni <kk@helpshift.com>"}
  clj-id3.parser
  (:require [clj-id3.utils :as cu]
            [clj-id3.mappings :as cm]
            [clj-id3.fops :as cf]))

(def ^:private header-size 10)
(def ^:private extended-header-size 6)


(defn decode-synchsafe
  "decodes synchsafe big-endian representation of integer
   into actual integer.
   Refer: http://en.wikipedia.org/wiki/Synchsafe"
  [synchsafe-byte-seq]
  {:pre (every? #(< % 0x80) synchsafe-byte-seq)}
  (reduce bit-or (map #(bit-shift-left %1 %2)
                      (reverse synchsafe-byte-seq)
                      (iterate (partial + 7) 0))))


(defn parse-header
  "Parses ID3 Header, returns size, major/minor version.
   Also decodes flags like
   1. Frames are unsynchronized
   2. Extended Header exists
   3. Footer exists
   4. Experimental flag"
  [header]
  {:pre [(= (count header) header-size)]}
  (let [[identifier
         major-version
         minor-version
         flags
         tag-length] (cu/bucket-split header 3 1 1 1 4)]
    (if (and (= (cu/byte-seq->str identifier) "ID3")
             (< major-version 0xFF)
             (<= major-version 4)
             (< minor-version 0xFF)
             (every? #(< % 0x80) tag-length))
       (let [unsynchronized? (cu/not-zero? (bit-and flags 0x80))
             extended-header-exists? (cu/not-zero? (bit-and flags 0x40))
             experimental? (cu/not-zero? (bit-and flags 0x20))
             footer-exists? (cu/not-zero? (bit-and flags 0x10))
             tag-size (decode-synchsafe tag-length)]
         {:major-version major-version
          :minor-version minor-version
          :tag-size tag-size
          :unsynchroized? unsynchronized?
          :extended-header-exists? extended-header-exists?
          :footer-exists? footer-exists?
          :experimental? experimental?})
       (throw (AssertionError. "Does not contain ID3 tag at start of file")))))


(defn parse-extended-header
  "Currently we just return size so that we can drop those many bytes"
  [extended-header]
  {:extended-size (decode-synchsafe (take 4 extended-header))})


(defn decode-text-frame
  "Assumes that given frame is text-frame
   i.e. first byte represents encoding (refer mappings.clj)
   and rest of data is string encoded with that string."
  [{:keys [frame-id frame-data]}]
  (let [encoding-byte (first frame-data)
        encoding (cm/byte->text-encoding encoding-byte "ISO-8859-1")
        payload (filter (complement zero?) (rest frame-data))
        tag-key (cm/frame-id->tag-key frame-id)
        text (cu/byte-seq->str payload :encoding encoding)]
    {tag-key text}))

(def frame-eater
  "Given any input should return nil"
  (constantly nil))

(def frame-id->handler-fn
  "Frame decoding functions for frame-ids.
   frame-eater function is used for those frames
   whose id is not present in this map"
  {"TALB" decode-text-frame
   "TCOM" decode-text-frame
   "TCON" frame-eater
   "TEXT" decode-text-frame
   "TIT2" decode-text-frame
   "TPE1" decode-text-frame
   "TPE2" decode-text-frame
   "TPE3" decode-text-frame
   "TDRC" decode-text-frame
   "TYER" decode-text-frame
   "TRCK" decode-text-frame})


(defn parse-frame-header
  "Parse  a frame-header and returns frame-id and frame-size.
   TODO: Parse flags"
  [header]
  {:pre [(= (count header) header-size)]}
  (let [[frame-id-seq frame-size-seq
         status-flags format-flags] (cu/bucket-split header 4 4 1 1)]
    {:frame-id (cu/byte-seq->str frame-id-seq)
     :frame-size (decode-synchsafe frame-size-seq)
     :status-flags status-flags
     :format-flags format-flags}))


(defn parse-frames
  "Parse frames and return aggregated information"
  [{:keys [major-version minor-version]} byte-seq]
  (loop [byte-seq byte-seq
         info {}]
    (if (or (empty? byte-seq)
            (zero? (first byte-seq)))
      info
      (let [[header data] (cf/read-bytes byte-seq 10)
            frame-header (parse-frame-header header)
            [frame-data rest-byte-seq] (cf/read-bytes data (:frame-size frame-header))
            frame-handler-fn (frame-id->handler-fn (:frame-id frame-header) frame-eater)
            frame-map (assoc frame-header
                        :frame-data frame-data
                        :major-version major-version
                        :minor-version minor-version)]
        (recur rest-byte-seq (merge info (frame-handler-fn frame-map)))))))


(defn- handle-extended-header
  "Currently just drops it"
  [id3-meta byte-seq]
  (if (:extended-header-exists? id3-meta)
    (let [[extended-header rest-data] (cf/read-bytes byte-seq extended-header-size)
          {:keys [extended-size]} (parse-extended-header extended-header)]
      [id3-meta (second (cf/read-bytes rest-data
                                       (- extended-size extended-header-size)))])
    [id3-meta byte-seq]))


(defn parse
  "Parses given byte-seq to check whether it has ID3 information.
   If it does parses it and returns ID3 info. If it does not find "
  [byte-seq]
  (let [[header rest-data] (cf/read-bytes byte-seq header-size)
        id3-meta (parse-header header)
        tag-data (first (cf/read-bytes rest-data (:tag-size id3-meta)))
        [id3-meta rest-data] (handle-extended-header id3-meta tag-data)]
    (parse-frames id3-meta rest-data)))


(defn parse-mp3-file
  [f]
  (if (cf/is-mp3-file? f)
    (parse (cf/lazy-byte-reader f))
    (throw (AssertionError. "Not an MP3 file"))))


(defn parse-directory
  [d]
  (let [dir-file (File. d)
        mp3-files (filter cf/is-mp3-file? (file-seq dir-file))]
    (map (fn [file]
           (try
             (parse (cf/lazy-byte-reader file))
             (catch Throwable t
                 (println "Failed for " (.getPath file) (.getMessage t))
                 nil)))
         mp3-files)))
