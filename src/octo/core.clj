(ns octo.core
  (:gen-class)
  (:require
    [taoensso.timbre :as timbre]
    [octo.config :as config]
    [octo.repos :refer (backup stale)])
  (:use
    [me.raynes.fs :only  [mkdir exists? expand-home]]
    [clojure.java.shell :only [sh]]))

(timbre/refer-timbre)

(def version "0.3.1")

(defn per-repo [[f {:keys [repos]}]]
  (doseq [{:keys [user org] :as repo} repos]
    (info "processing:" (or user org))
    (f repo)))

(defn workspace [[f {:keys [workspace] :as m}]]
  [(partial f workspace) m])

(defn auth [[f {:keys [user token] :as m}]]
  [(partial f (str user ":" token)) m])

(defn c [args]
  (config/load-config (second args)))

(defn -main [& args]
  (try 
    (case (first args)
      "backup" (-> [backup (c args)] workspace auth per-repo)
      "stale" (-> [stale (c args)] auth per-repo)
      "version" (println "octo backup" version)
      nil (println "octo backup" version))
    (catch Exception e
     (error e) 
     (System/exit 1)) 
    ))

