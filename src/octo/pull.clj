(ns octo.pull
  "pull backup into restore folder"
  (:require
    [octo.push :refer [format-]]
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
  ([workspace]
   (<< "~{workspace}/pull") )
  ([workspace repo]
    (<< "~(parent workspace)/zbackup/~{repo}.zback"))
  ([workspace repo name]
    (<< "~(parent workspace)/repos/~{repo}/~{name}"))
  )

(defn validate [extracted workspace repo]
   (let [{:keys [total]} (read-string (slurp (<< "~{extracted}/check.edn")))
          restored (count (files (<< "~(parent workspace)/repos/~{repo}/")))]
       (debug "validating restoration for" repo)
       (when-not (== total restored)
         (throw (ex-info "Failed to restore found" {:found restored :expected total})))))

(defn restore-bundles
   [workspace repo]
   (let [extracted (file (parent workspace) (.substring workspace 1) "repos" repo "bundles")]
     (info "restoring bundles from" extracted)
     (doseq [bundle (filter #(and (.isFile %) (.endsWith (.getName %) "bundle")) (file-seq extracted))
       :let [name (-> bundle (.getName) (.replace ".bundle" "")) target (parent workspace repo name)]]
       (when (.exists (file target)) (fs/delete-dir target))
       (safe "git" "clone" (.getAbsolutePath bundle) target)
       (with-sh-dir target (safe "git" "fsck" target)))
     (validate extracted workspace repo)
     (fs/delete-dir extracted)))

(defn zbackup-restore
   "Restore an org/repo"
   [workspace password repo]
   (let [base (<< "~(parent workspace repo)/backups/")
         backups (filter #(.isFile %) (file-seq (file base)))
         latest (.getAbsolutePath (last (sort-by #(-> % (.getName) (.replace repo "") parse) backups)))
         target  (<< "~(parent workspace)/tars/~{repo}.tar")]
      (debug "zbackup restore")
      (lazy-mkdir (<< "~(parent workspace)/tars"))
      (safe "/bin/sh" "-c" (<< "zbackup --password-file ~{password} restore ~{latest} > ~{target}"))
      (with-sh-dir (parent workspace)
      (safe "tar" "xf" target))
      (lazy-mkdir (<< "~(parent workspace)/repos"))
      (restore-bundles workspace repo)))

(defn pull
  [workspace {:keys [zbackup rclone] :as m} {:keys [org user]}]
    (lazy-mkdir (parent workspace))
    (let [repo (or org user)]
      (info "synching" repo)
      (rclone-sync  (<< "~(rclone :dest)/~{repo}.zback") (parent workspace repo))
      (info "purging" (<< "~(parent workspace)/repos/~{repo}"))
      (fs/delete-dir (<< "~(parent workspace)/repos/~{repo}"))
      (info "restoring" repo)
      (when (empty? (.list (file (parent workspace repo))))
        (throw (ex-info "zbackup archive empty fix configuration, sync and push" {:repo repo})))
      (zbackup-restore workspace (zbackup :password-file) repo)))
