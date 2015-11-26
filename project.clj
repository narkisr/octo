(defproject octo "0.1.0"
  :description "A small octo backup tool"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [tentacles "0.4.0"] 
                 [me.raynes/fs "1.4.6"]                 
                 ; ring
                 [clj-yaml "0.4.0"]
                 [org.clojure/data.json "0.2.2" ]
                 [ring-middleware-format "0.5.0"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [ring "1.3.2"]
                 [compojure "1.3.4" :exclusions  [ring/ring-core]]]

  :plugins [[lein-tarsier "0.10.0"]] 
  :aot  [octo.core]
  :main octo.core
  )
