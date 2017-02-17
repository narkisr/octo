(defproject octo "0.2.0"
  :description "Github backup tool"
  :url "https://github.com/narkisr/octo-rewind"
  :license  {:name "Apache License, Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-yaml "0.4.0"]
                 [tentacles "0.5.2"]
                 [me.raynes/fs "1.4.6"]
                 [org.clojure/tools.trace "0.7.8"]
                 [com.taoensso/timbre "4.1.4"]]
  :plugins [
     [lein-ancient "0.6.7" :exclusions [org.clojure/clojure]]
     [lein-tag "0.1.0"] [lein-set-version "0.3.0"]]

  :aot [octo.core]
  :main octo.core
  )
