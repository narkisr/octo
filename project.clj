(defproject octo "0.8.1"
  :description "Github backup tool"
  :url "https://github.com/narkisr/octo"
  :license  {:name "Apache License, Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [
      [org.clojure/clojure "1.10.1"]

      ; <<
      [org.clojure/core.incubator "0.1.4"]

      [me.raynes/fs "1.4.6"]

      ; gitblit api
      [clj-http "3.10.0"]

      ; github api
      [irresponsible/tentacles "0.6.4"]
      [clj-yaml "0.4.0"]

      ; repl
      [org.clojure/tools.namespace "0.3.1"]

      [org.clojure/tools.reader "1.3.2"]

      [com.taoensso/timbre "4.10.0"]
    ]

  :plugins [
     [io.taylorwood/lein-native-image "0.3.1"]
     [lein-cljfmt "0.6.3"]
     [lein-ancient "0.6.15" :exclusions [org.clojure/clojure]]
     [lein-tag "0.1.0"]
     [lein-set-version "0.3.0"]
   ]

  :profiles {
    :dev {
      :set-version {
         :updates [
            {:path "src/octo/core.clj" :search-regex #"\"\d+\.\d+\.\d+\""}
            {:path "bin/binary.sh" :search-regex #"\d+\.\d+\.\d+"}
            {:path "README.md" :search-regex #"\d+\.\d+\.\d+"}
          ]}
       :aot [octo.core]
      }

     :refresh {
        :repl-options {
          :init-ns user
          :timeout 120000
        }

        :dependencies [[org.clojure/tools.namespace "0.2.10"]
                       [redl "0.2.4"]
                       [org.clojure/tools.trace "0.7.10"]]
        :injections  [(require '[redl core complete])]
        :source-paths  ["dev" "src"]
        :test-paths  []

     }
  }


  :aliases {
     "reloadable" ["with-profile" "refresh" "do" "clean," "repl"]
     "travis" [
        "do" "clean," "compile," "cljfmt" "check," "eastwood"
     ]
  }

  :main octo.core
  )
