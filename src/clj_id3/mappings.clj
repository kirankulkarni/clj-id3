(ns ^{:doc "Different mappings"
      :author "Kiran Kulkarni <kk@helpshift.com>"}
  clj-id3.mappings
  )


(def byte->text-encoding
  {0 "ISO-8859-1"
   1 "UTF-16"
   2 "UTF-16"
   3 "UTF-8"})

(def frame-id->tag-key
  {"TALB" :album
   "TCOM" :composer
   "TCON" :genre
   "TEXT" :lyricist
   "TIT2" :title
   "TPE1" :artist
   "TPE2" :album-artist
   "TPE3" :performer
   "TDRC" :recording-date
   "TDRL" :release-date
   "TYER" :year
   "TRCK" :track})
