(ns octo.core 
  (:require 
    [octo.jetty :refer (start restart)]
    [octo.api :refer (app)]
    [octo.repos :refer (clone load-config)]
    )
  (:use 
    ring.adapter.jetty
    [me.raynes.fs :only  [mkdir exists? expand-home]]
    [clojure.java.shell :only [sh]]))

(defn -main [user action] 
    (case action
      "clone" (clone user)
      "push" identity
      "server" (run-jetty {:port 8080})
    ))

(restart app) 
