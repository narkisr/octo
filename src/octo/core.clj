(ns octo.core 
  (:gen-class)
  (:require 
    [taoensso.timbre :as timbre]
    [octo.config :as config]
    [octo.repos :refer (backup stats)])
  (:use 
    [me.raynes.fs :only  [mkdir exists? expand-home]]
    [clojure.java.shell :only [sh]]))

(timbre/refer-timbre)

(def version "0.2.1")

(defn run [c]
  (let [{:keys [repos workspace user token]} (config/load-config c) auth (str user ":" token)]
     (doseq [{:keys [user org] :as repo} repos] 
        (info "processing the repos of:" (or user org))
        (backup workspace auth repo))))

(defn per-repo [f {:keys [repos]}]
  (doseq [{:keys [user org] :as repo} repos] 
        (info "running through the repos of:" (or user org))
        (f repo)))

(defn with-workspace [f {:keys [workspace]}] 
  (partial f workspace))

(defn with-auth [f {:keys [user token]}]
  (partial f (str user ":" auth)))

(defn -main [& args]
  (case (first args) 
    "backup" (-> (second args) )
    "stats" (stats (second args))
    "version" (println "octo backup" version)
    nil (println "octo backup" version)
    ) 
  )

