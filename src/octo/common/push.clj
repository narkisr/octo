(ns octo.common.push
  "push local backup remotely by using zbackup and rclone"
  (:require
    [me.raynes.fs :as fs :refer (base-name)]
    [clojure.core.strint :refer  (<<)]
    [octo.common :refer (safe lazy-mkdir rclone-sync)]
    [clojure.java.io :refer (file)]
    [taoensso.timbre :as timbre]
    [clojure.java.shell :refer [sh with-sh-dir]]))

(timbre/refer-timbre)

(defn- run-backup
  [source dest password]
  (debug "source is" source)
  (safe "/bin/sh" "-c" (<< "tar c ~{source} | zbackup backup --password-file ~{password} ~{dest}")))

(defn- init
  [password dest]
  (safe "zbackup" "init" dest  "--password-file" password))

(def format- "MM-dd-yyyy-HH-MM-ss")

(defn now [] (.format  (java.text.SimpleDateFormat. format- ) (java.util.Date.)))

(defn zbackup-backup
   "Copy git backup into a zbackup based backup using backup id"
   [password source dest id]
   (when-not (.exists (file (<< "~{dest}/backups/")))
     (init password dest))
   (run-backup source  (<< "~{dest}/backups/~{id}-~(now)")  password))

(defn push
  [{:keys [zbackup rclone] :as m} source dest id]
    (lazy-mkdir dest)
    (info "zbackup backup" dest)
    (zbackup-backup (zbackup :password-file) source dest id)
    (info "rclone push" dest "into" (<< "~(rclone :dest)/~(base-name dest)"))
    (rclone-sync dest (<< "~(rclone :dest)/~(base-name dest)")))
