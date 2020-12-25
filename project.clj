(defproject octo "0.8.2"
  :description "Github backup tool"
  :url "https://github.com/narkisr/octo"
  :license  {:name "Apache License, Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [
          [org.clojure/clojure "1.10.1"]
          [org.clojure/core.incubator "0.1.4"]

          ; repl
          [org.clojure/tools.namespace "1.1.0"]

          ; cli
          [cli-matic "0.3.3"]

          ; filesystem processing
          [me.raynes/fs "1.4.6"]

          ; github access
          [irresponsible/tentacles "0.6.6"]
          [clj-http "3.11.0"]
          [clj-yaml "0.4.0"]

          [org.clojure/tools.trace "0.7.10"]
          [com.taoensso/timbre "4.1.4"]]

  :plugins [
     [lein-cljfmt "0.6.3"]
     [lein-ancient "0.6.15" :exclusions [org.clojure/clojure]]
     [lein-tag "0.1.0"]
     [lein-set-version "0.3.0"]
   ]

  :profiles {
    :dev {
      :source-paths  ["dev"]
      :set-version {
         :updates [
            {:path "src/octo/core.clj" :search-regex #"\"\d+\.\d+\.\d+\""}
            {:path "bin/binary.sh" :search-regex #"\d+\.\d+\.\d+"}
            {:path "README.md" :search-regex #"\d+\.\d+\.\d+"}
          ]}
       :aot [octo.core]
      }
  }


  :aliases {
     "travis" [
        "do" "clean," "compile," "cljfmt" "check"
     ]
  }

  :main octo.core
  )
