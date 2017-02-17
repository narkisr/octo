(ns octo.git
  (:require 
    [clojure.java.io :refer (file)]
    [taoensso.timbre :as timbre]
    [clojure.java.shell :refer [sh with-sh-dir]]))

(timbre/refer-timbre)

(defn safe [{:keys [out err exit]}]
   (when-not (empty? out) (debug out))
   (when-not (= exit 0)
     (error err exit)
     (throw (ex-info err {:code exit}))))

(defn with-opts [f op & args]
    (if (and op (contains? op :branch))
      (apply sh  (concat args ["-b" (op :branch) "--single-branch"] ))
      (apply sh args)))

(defn upclone 
  "update or clone repo"
  [url dest op]
  (if-not (.exists (file dest))
    (safe (with-opts sh op "git" "clone" "--mirror" url dest))
    (with-sh-dir dest 
      (safe (sh "git" "remote" "update" "--prune")))))

(defn bundle 
   "Create a bundle file from the repo" 
   [parent dest name]
   (when-not (.exists (file (str parent "/bundles")))
     (.mkdirs (file (str parent "/bundles"))))
   (with-sh-dir dest
     (safe (sh "git" "--git-dir" dest  "bundle" "create" (str parent "/bundles/" name ".bundle") "--all"))))
