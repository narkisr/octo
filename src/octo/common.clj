(ns octo.common
  "Common reusable functions"
  (:require 
    [clojure.java.shell :refer [sh]]
    [clojure.java.io :refer (file)]
    [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defn safe [{:keys [out err exit]}]
   (when-not (empty? out) (debug out))
   (when-not (= exit 0)
     (error err exit)
     (throw (ex-info err {:code exit}))))

(defn lazy-mkdir [dir]
   (when-not (.exists (file dir) )
     (.mkdirs (file dir))))

(defn rclone-sync
   "Push/Pull changes to a remote/local backup"
   [source dest]
   (safe (sh "rclone" "sync" source dest)))
