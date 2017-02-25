(ns octo.pull
  "pull backup into restore folder"
  (:require
    [octo.push :refer [format-]]
    [taoensso.timbre :as timbre]
    [clojure.core.strint :refer  (<<)]
    [octo.common :refer (safe lazy-mkdir rclone-sync)]
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

(defn restore-bundles
   [workspace repo]
   (let [extracted (file (parent workspace) (.substring workspace 1) "repos" repo "bundles") ]
     (info "restoring bundles from" extracted)
     (lazy-mkdir (<< "~(parent workspace)/repos"))
     (doseq [bundle (filter #(.isFile %) (file-seq extracted)) :let
             [name (-> bundle (.getName) (.replace ".bundle" ""))]]
       (info "git" "clone" (.getAbsolutePath bundle) (parent workspace repo name))
       (safe (sh "git" "clone" (.getAbsolutePath bundle) (parent workspace repo name))))))

(defn zbackup-restore
   "Restore an org/repo"
   [workspace password repo]
   (let [base (<< "~(parent workspace repo)/backups/")
         backups (filter #(.isFile %) (file-seq (file base)))
         latest (.getAbsolutePath (first (sort-by #(-> % (.getName) (.replace repo "") parse) backups)))
         target  (<< "~(parent workspace)/tars/~{repo}.tar")]
     (debug "zbackup restore")
     (lazy-mkdir (<< "~(parent workspace)/tars"))
     (safe (sh "/bin/sh" "-c" (<< "zbackup --password-file ~{password} restore ~{latest} > ~{target}")))
     (with-sh-dir (parent workspace)
       (safe (sh "tar" "xf" target)))
     (restore-bundles workspace repo)
     ))

(defn pull
  [workspace {:keys [zbackup rclone] :as m} {:keys [org user]}]
    (lazy-mkdir (parent workspace))
    (let [repo (or org user)]
      (info "synching" repo)
      (rclone-sync  (<< "~(rclone :dest)/~{repo}.zback") (parent workspace repo))
      (info "restoring" repo)
      (zbackup-restore workspace (zbackup :password-file) repo)))
