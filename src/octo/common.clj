(ns octo.common
  "Common reusable functions"
  (:require
   [clojure.set :refer (difference)]
   [clojure.java.shell :refer [sh]]
   [me.raynes.fs :refer [delete-dir]]
   [clojure.java.io :refer (file)]
   [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defn safe-output [{:keys [out err exit]}]
  (when-not (empty? out)
    (debug out))
  (when-not (= exit 0)
    (error err exit)
    (throw (ex-info err {:code exit}))))

(def safe (comp safe-output sh))

(defn lazy-mkdir [dir]
  (when-not (.exists (file dir))
    (.mkdirs (file dir))))

(defn rclone-sync
  "Push/Pull changes to a remote/local backup"
  [source dest]
  (safe "rclone" "sync" source dest))

(defn files [d & exclude]
  (remove (fn [f] ((into #{} exclude) (.getName f))) (.listFiles (file d))))

(defn purge
  "delete files that exist in dest but not in source names set"
  [sources dest]
  (doseq [f (filter #(not (sources (.getName %))) dest)]
    (debug "clearing non existing remote" f)
    (delete-dir f)))

(defn excluded? [es {:keys [name]}]
  (empty? (first (filter (fn [e] (= e name)) es))))

