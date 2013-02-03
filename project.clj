(defproject octopi "0.1.0-SNAPSHOT"
  :description "A small octopi backup"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [tentacles "0.2.4"] 
                 [ me.raynes/fs "1.4.0"]                 ]
  :plugins [[lein-tarsier "0.10.0"]] 
   :aot  [octopi.core]
  :main octopi.core
  )
