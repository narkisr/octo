(ns octo.common.pull
  "pull backup into restore folder"
  (:require
   [clojure.string :refer (replace-first)]
   [octo.common.push :refer [format-]]
   [taoensso.timbre :as timbre]
   [clojure.core.strint :refer  (<<)]
   [octo.common :refer (safe lazy-mkdir rclone-sync files)]
   [clojure.edn :refer (read-string)]
   [me.raynes.fs :as fs]
   [clojure.java.io :refer (file)]
   [taoensso.timbre :as timbre]
   [clojure.java.shell :refer [sh with-sh-dir]]))

(timbre/refer-timbre)

(defn parse [s] (.parse (java.text.SimpleDateFormat. format-) s))

(defn parent
  ([to id]
   (<< "~{to}/zbackup/~{id}.zback"))
  ([to id name]
   (<< "~{to}/repos/~{id}/~{name}")))

(defn validate [extracted to id]
  (let [{:keys [total]} (read-string (slurp (<< "~{extracted}/check.edn")))
        restored (count (files (<< "~{to}/repos/~{id}/")))]
    (debug "validating restoration for" id)
    (when-not (== total restored)
      (throw (ex-info "Failed to restore found" {:found restored :expected total})))))

(defn restore-bundles
  [to id]
  (info "to is" to)
  (let [extracted (file to (.replace (.substring to 1) "pull" "sync") id "bundles")]
    (info "restoring bundles from" extracted)
    (doseq [bundle (filter #(and (.isFile %) (.endsWith (.getName %) "bundle")) (file-seq extracted))
            :let [name (-> bundle (.getName) (.replace ".bundle" "")) target (parent to id name)]]
      (when (.exists (file target)) (fs/delete-dir target))
      (safe "git" "clone" (.getAbsolutePath bundle) target)
      (with-sh-dir target (safe "git" "fsck" target)))
    (validate extracted to id)
    (fs/delete-dir extracted)))

(defn zbackup-restore
  "Restore an org/repo"
  [to password id]
  (let [base (<< "~(parent to id)/backups/")
        backups (filter #(.isFile %) (file-seq (file base)))
        latest (.getAbsolutePath (last (sort-by #(-> % (.getName) (.replace id "") parse) backups)))
        target  (<< "~{to}/tars/~{id}.tar")]
    (debug "zbackup restore")
    (lazy-mkdir (<< "~{to}/tars"))
    (safe "/bin/sh" "-c" (<< "zbackup --password-file ~{password} restore ~{latest} > ~{target}"))
    (debug "extracting into" target)
    (with-sh-dir to
      (safe "tar" "xf" target))
    (lazy-mkdir (<< "~{to}/repos"))
    (restore-bundles to id)))

(defn pull
  [{:keys [zbackup rclone] :as m} to id]
  (lazy-mkdir to)
  (info "synching" id)
  (rclone-sync  (<< "~(rclone :dest)/~{id}.zback") (parent to id))
  (info "purging" (<< "~{to}/repos/~{id}"))
  (fs/delete-dir (<< "~{to}/repos/~{id}"))
  (info "restoring" id)
  (when (empty? (.list (file (parent to id))))
    (throw (ex-info "zbackup archive is empty, fix configuration, sync and push again." {:id id})))
  (zbackup-restore to (zbackup :password-file) id))
