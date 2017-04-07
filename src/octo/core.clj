(ns octo.core
  (:gen-class)
  (:require
    [taoensso.timbre :as timbre :refer (set-level!)]
    [octo.config :as config]
    [octo.repos :refer (synch stale)]
    [octo.push :refer (push)]
    [octo.pull :refer (pull)])
  (:use
    [me.raynes.fs :only  [mkdir exists? expand-home]]
    [clojure.java.shell :only [sh]]))

(timbre/refer-timbre)

(set-level! :debug)

(def version "0.6.0")

(defn- per-repo [[f {:keys [repos]}]]
  (doseq [{:keys [user org] :as repo} repos]
    (info "processing:" (or user org))
    (f repo)))

(defn- workspace [[f {:keys [workspace] :as m}]]
  [(partial f workspace) m])

(defn- auth [[f {:keys [user token] :as m}]]
  [(partial f (str user ":" token)) m])

(defn- push- [[f {:keys [push] :as m}]]
  [(partial f push) m])

(defn c [args]
  (config/load-config (second args)))

(defn -main [& args]
  (try
    (case (first args)
      "sync" (-> [synch (c args)] workspace auth per-repo)
      "push" (-> [push (c args)] workspace push- per-repo)
      "pull" (-> [pull (c args)] workspace push- per-repo)
      "stale" (-> [stale (c args)] auth per-repo)
      "version" (println "octo backup" version)
      nil (println "octo backup" version))
    (catch Exception e
      (error e)
      (System/exit 1))))

