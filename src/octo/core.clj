(ns octo.core 
  (:require 
    [taoensso.timbre :as timbre]
    [octo.jetty :refer (start restart)]
    [octo.config :as config]
    [octo.api :refer (app)]
    [octo.repos :refer (clone)])
  (:use 
    ring.adapter.jetty
    [me.raynes.fs :only  [mkdir exists? expand-home]]
    [clojure.java.shell :only [sh]]))

(timbre/refer-timbre)

(defn -main [action] 
    (case action
      "clone" 
      (let [{:keys [repos workspace]} (config/load-config)]
        (doseq [{:keys [user layouts]} repos] 
          (info "Cloning repos for: " user)
          (clone user workspace layouts)))
      "push" identity
      "server" (run-jetty app {:port 8080})
    ))

;; (restart app) 
