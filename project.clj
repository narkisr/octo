(defproject octo "0.6.0"
  :description "Github backup tool"
  :url "https://github.com/narkisr/octo-rewind"
  :license  {:name "Apache License, Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-yaml "0.4.0"]
                 [tentacles "0.5.2"]
                 [clojure-future-spec "1.9.0-alpha14"]
                 [org.clojure/core.incubator "0.1.3"]
                 [me.raynes/fs "1.4.6"]
                 [clj-http "3.1.0"]
                 [org.clojure/tools.trace "0.7.8"]
                 [com.taoensso/timbre "4.1.4"]]
  :plugins [
     [lein-ancient "0.6.7" :exclusions [org.clojure/clojure]]
     [lein-tag "0.1.0"] [lein-set-version "0.3.0"]]

  :profiles {
    :dev {
      :set-version {
         :updates [
            {:path "src/octo/core.clj" :search-regex #"\"\d+\.\d+\.\d+\""}
            {:path "bin/binary.sh" :search-regex #"\d+\.\d+\.\d+"}
            {:path "README.md" :search-regex #"\d+\.\d+\.\d+"}
          ]}
      }
  }
  :aot [octo.core]
  :main octo.core
  )
