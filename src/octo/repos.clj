(ns octo.repos
  (:require  
    [clj-jgit.porcelain :as jgit :refer [with-repo git-status]]
    [me.raynes.fs :as fs]
    [clojure.java.shell :refer [sh]]
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

(defn clone  
  [user workspace layouts]
  (paginate user  
    (fn [bulk]
      (doseq [{:keys [name git_url]} bulk :let [dest (match-layout workspace name layouts)]]
        (info "Cloning " name " into " dest)
        (sh "git" "clone" git_url dest)
        ))))

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


