(ns octo.jetty
  "Core api components"
  (:require 
    [ring.adapter.jetty :refer (run-jetty)] 
    ))

(def jetty (atom nil))

(defn start [app]
   (println "Starting jetty")
   (reset! jetty
      (run-jetty app {:port 8080 :join? false})))

(defn stop []
  (when @jetty 
    (println "Stopping jetty")
    (.stop @jetty)
    (reset! jetty nil)))

(defn restart [app]
  (stop)
  (start app)
  )
