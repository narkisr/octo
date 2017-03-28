(ns octo.common
  "Common reusable functions"
  (:require 
    [clojure.java.shell :refer [sh]]
    [clojure.java.io :refer (file)]
    [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defn safe-output [{:keys [out err exit]}]
   (when-not (empty? out) (debug out))
   (when-not (= exit 0)
     (error err exit)
     (throw (ex-info err {:code exit}))))

(def safe (comp safe-output sh))

(defn lazy-mkdir [dir]
   (when-not (.exists (file dir) )
     (.mkdirs (file dir))))

(defn rclone-sync
   "Push/Pull changes to a remote/local backup"
   [source dest]
   (safe "rclone" "sync" source dest))

(defn folder-count [d & exclude]
  (count (remove (fn [f] ((into #{} exclude) (.getName f))) (.listFiles (file d)))))
