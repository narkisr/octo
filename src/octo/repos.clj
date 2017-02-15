(ns octo.repos
  (:require
    [clj-jgit.porcelain :as jgit :refer [with-repo git-status]]
    [me.raynes.fs :as fs]
    [clojure.java.shell :refer [sh with-sh-dir]]
    [tentacles.repos :as repos]
    [taoensso.timbre :as timbre]
    [tentacles.users :as users]))

(timbre/refer-timbre)

(defn user-repos [name i]
  (repos/user-repos name {:per-page 50 :page i}))

(defn paginate [user f]
  (loop [page 1 bulk (user-repos user page)]
    (when (not-empty bulk)
      (f bulk)
      (recur (inc page) (user-repos user (inc page))))))

(defn match-layout
   [path name layouts]
   (if-let [[r dest] (first (filter (fn [[r dest]] (re-find (re-pattern r) name)) layouts))]
     (str path "/" dest "/" name)
     (str path "/" name)))

(defn safe [{:keys [out err exit]}]
   (when-not (empty? out) (debug out))
   (when-not (= exit 0)
     (error err exit)
     (throw (ex-info err {:code exit}))))

(defn with-opts [f op & args]
    (if (and op (contains? op :branch))
      (apply sh  (concat args ["-b" (op :branch) "--single-branch"] ))
      (apply sh args)))

(defn clone
  [user workspace layouts options]
  (paginate user
    (fn [bulk]
      (doseq [{:keys [name git_url]} bulk
              :let [dest (match-layout workspace name layouts) op (options (keyword name))]]
        (info "checking" name)
        (if-not (.exists (clojure.java.io/file dest))
          (safe (with-opts sh op "git" "clone" "--mirror" git_url dest))
          (with-sh-dir dest
            (safe (sh "git" "remote" "update" "--prune"))))
        (info "mirrored" name )
        (with-sh-dir dest
          (safe (sh "git" "--git-dir" dest  "bundle" "create" (str name ".bundle") "--all")))
        (info "bundled" name)))))

(defn repo-origin [f]
  (with-repo (.getPath f)
    (.getString (.getConfig (.getRepository repo)) "remote" "origin" "url")))

(defn status-changed? [f]
  (with-repo (.getPath f)
    (not (every? empty? (vals (git-status repo))))))

(defn list-workspace [workspace]
   (doall
     (map (partial zipmap [:status-changed :url :name])
       (map (fn [path] [(status-changed? path) (repo-origin path) (.getName path)]) (fs/list-dir workspace)))))


