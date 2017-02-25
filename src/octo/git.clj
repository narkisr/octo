(ns octo.git
  (:require
    [octo.common :refer (safe)]
    [clojure.java.io :refer (file)]
    [taoensso.timbre :as timbre]
    [clojure.java.shell :refer [sh with-sh-dir]]))

(timbre/refer-timbre)

(defmulti with-opts
  (fn [op & args]
    (keyword (first (clojure.set/intersection (into #{} args) #{"clone" "fetch"})))))

(defmethod with-opts :clone  [op & args]
  (if (and op (contains? op :branch))
      (apply sh (concat args ["-b" (op :branch) "--single-branch"] ))
      (apply sh args)))

(defmethod with-opts :fetch [op & args]
   (if (and op (contains? op :branch))
      (apply sh (concat args [(op :branch)]))
      (apply sh args)))

(defn upclone
  "update or clone repo"
  [url dest op]
  (if-not (.exists (file dest))
    (safe (with-opts op "git" "clone" "--mirror" url dest))
    (with-sh-dir dest
      (safe (with-opts op "git" "fetch" "origin")))))

(defn is-empty?
  "checks if a repo is empty"
  [dest]
  (nil? (first (filter #(.isFile %) (file-seq (file (str dest "/objects")))))))

(defn bundle
   "Create a bundle file from the repo"
   [parent dest name]
   (when-not (.exists (file parent "bundles"))
     (.mkdirs (file parent "bundles")))
   (if (is-empty? dest)
     (warn "repository under" dest "is empty!, skipping bundle")
     (with-sh-dir dest
       (safe (sh "git" "--git-dir" dest  "bundle" "create" (str parent "/bundles/" name ".bundle") "--all")))))
