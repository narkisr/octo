(ns octo.core 
  (:gen-class)
  (:require 
    [taoensso.timbre :as timbre]
    [octo.config :as config]
    [octo.repos :refer (backup)])
  (:use 
    [me.raynes.fs :only  [mkdir exists? expand-home]]
    [clojure.java.shell :only [sh]]))

(timbre/refer-timbre)

(def version "0.2.1")

(defn run [c]
  (let [{:keys [repos workspace user token]} (config/load-config c) auth (str user ":" token)]
     (doseq [{:keys [user org] :as repo} repos] 
        (info "backup the repos of:" (or user org))
        (backup workspace auth repo))))

(defn -main [& args]
  (case (first args) 
    "backup" (run (second args))
    "version" (println "octo backup" version)
    nil (println "octo backup" version)
    ) 
  )

