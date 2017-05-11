(ns octo.gitblit.push
  "push local backup remotly to the cloud"
 (:require
    [clojure.core.strint :refer  (<<)]
    [octo.common :refer (safe lazy-mkdir rclone-sync)]
    [clojure.java.io :refer (file)]
    [taoensso.timbre :as timbre]
    [clojure.java.shell :refer [sh with-sh-dir]]))

(timbre/refer-timbre)

(defn parent
  ([workspace]
    (<< "~{workspace}/push"))
  ([workspace repo]
      (<< "~(parent workspace)/~{repo}.zback")))

(defn base [workspace repo]
   (<< "~(parent workspace repo)/backups/~{repo}"))

(defn- run-backup
  [source dest password]
  (safe "/bin/sh" "-c" (<< "tar c ~{source} | zbackup backup --password-file ~{password} ~{dest}")))

(defn- init
  [workspace password repo]
  (safe "zbackup" "init" (parent workspace repo)  "--password-file" password))

(def format- "MM-dd-yyyy-HH-MM-ss")

(defn now [] (.format  (java.text.SimpleDateFormat. format- ) (java.util.Date.)))

(defn zbackup-backup
   "Copy git backup into a zbackup based backup"
   [workspace password repo]
   (let [base' (base workspace repo)]
     (when-not (.exists (file (<< "~(parent workspace repo)/backups/")))
       (init workspace password repo))
     (run-backup (<< "~{workspace}/sync/bundles") (<< "~{base'}-~(now)")  password)))

(defn push
  [workspace {:keys [zbackup rclone] :as push} {:keys [user] :as m}]
    (lazy-mkdir (parent workspace))
    (info "zbackup backup" (<< "~{workspace}/sync/bundles"))
    (zbackup-backup workspace (zbackup :password-file) user)
    (info "rclone push up" user)
    (rclone-sync (parent workspace user) (<< "~(rclone :dest)/~{user}.zback")))

