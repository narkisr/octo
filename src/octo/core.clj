(ns octo.core
  (:gen-class)
  (:require
    [tentacles.repos :as repos]
    [taoensso.timbre :as timbre :refer (set-level!)]
    [octo.config :as config]
    [octo.provider :refer (find-fn)]
    )
  (:use
    [me.raynes.fs :only  [mkdir exists? expand-home]]
    [clojure.java.shell :only [sh]]))

(timbre/refer-timbre)

(set-level! :debug)

(defn version []
  (let [current  "0.8.0" last-version (:name (last (sort-by :name (repos/tags "narkisr" "octo"))))]
    (if-not (= current last-version )
      (println "octo backup" current ",latest version is" last-version "please upgrade")
      (println "octo backup" current))))

(defn- run
  "Running through repos list if exists else just pass m"
  [[f {:keys [repos] :as m}]]
  (if repos
     (doseq [{:keys [user org] :as repo} repos]
       (info "processing:" (or user org))
       (f repo))
     (f m)))

(defn- workspace [[f {:keys [workspace] :as m}]]
  [(partial f workspace) m])

(defn- auth [[f {:keys [user token password] :as m}]]
  [(partial f (str user ":" (or token password))) m])

(defn- push- [[f {:keys [push] :as m}]]
  [(partial f push) m])

(defn c [args]
  (config/load-config (second args)))

(defn match [[k m]]
  [(find-fn k m) m])

(defn -main [& args]
  (try
    (case (first args)
      "sync" (-> [:synch (c args)] match workspace auth run)
      "push" (-> [:push (c args)] match workspace push- run)
      "pull" (-> [:pull (c args)] match workspace push- run)
      "stale" (-> [:stale (c args)] match auth run)
      "version" (version)
      nil (version))
    (catch Exception e
      (error e)
      (System/exit 1))))

