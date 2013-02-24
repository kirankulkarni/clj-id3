# clj-id3

A Clojure library designed to parse ID3v2 tags from mp3 files.

## Installation

clj-id3 is available as a Maven artifact via [clojars](https://clojars.org/org.clojars.kiran/clj-id3)

For leiningen, add

    [org.clojars.kiran/clj-id3 "0.1.0"]

## Usage

Parsing functions are available in `clj-id3.parser` namespace

    (use 'clj-id3.parser)

Parsing an mp3 file using file-path

    (parse-mp3-file "<path to mp3 file>")

Parsing an mp3 file using File object

    (parse-mp3-file <File object>)

Parsing a directory

    (parse-directory "<path of directory>")

## Parsed Information

Library will parse ID3 and will collect following information or
a subset of it available in Tag

| Frame-ID | Interpretation   | Information                            |
| -------- |:----------------:|:--------------------------------------:|
| TIT2     | title            | Title of the mp3 file                  |
| TPE1     | artist           | Lead Performer(s)                      |
| TPE2     | album-artist     | Band/Orchestra/Music By                |
| TPE3     | performer        | Conductor/ Performer refinement        |
| TCOM     | composer         | Original Composer                      |
| TEXT     | lyricist         | Lyricist/ Text writer                  |
| TALB     | album            | Album Name                             |
| TDRC     | recording-date   | Recording Date                         |
| TDRL     | release-date     | Release Date                           |
| TYER     | year             | Year of the album                      |
| TRCK     | track            | Track Number in the group (e.g. 2/21)  |
| TCOM     | genre            | Genre of song (if it is part of ID3v1) |



## License

Copyright © 2013

Distributed under the Eclipse Public License, the same as Clojure.
