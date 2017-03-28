(ns octo.push
  "push local backup remotely by using zbackup and rclone"
  (:require
    [taoensso.timbre :as timbre]
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
     (run-backup (<< "~{workspace}/repos/~{repo}/bundles") (<< "~{base'}-~(now)")  password)))

(defn push
  [workspace {:keys [zbackup rclone] :as m} {:keys [org user]}]
    (lazy-mkdir (parent workspace))
    (let [repo (or org user)]
      (info "zbackup backup" repo)
      (zbackup-backup workspace (zbackup :password-file) repo)
      (info "rclone push up" repo)
      (rclone-sync (parent workspace repo) (<< "~(rclone :dest)/~{repo}.zback"))))

