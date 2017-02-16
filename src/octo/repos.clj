(ns octo.repos
  (:require
    [clj-jgit.porcelain :as jgit :refer [with-repo git-status]]
    [me.raynes.fs :as fs]
    [clojure.java.shell :refer [sh with-sh-dir]]
    [clojure.java.io :refer (file)]
    [tentacles.repos :as repos]
    [tentacles.repos :as repos]
    [taoensso.timbre :as timbre]
    [tentacles.users :as users]))

(timbre/refer-timbre)

(defn check-resp [{:keys [status ] :as bulk}]
  (when (and (not (nil? status)) (not (== status 200)))
    (error "Failed to call Github API" (select-keys bulk [:status :body]))
    (System/exit 1))
   bulk 
  )

(defn user-repos [user auth i]
  (if auth 
    (repos/repos {:per-page 50 :page 2 :affiliation "owner" :auth (str user ":" auth)})
    (repos/user-repos user {:per-page 50 :page i})))

(defn org-repos [org auth i]
  (repos/user-repos org {:per-page 50 :page i}))

(def fetchers {:org org-repos :user user-repos})

(defn run-paging [callback fetch]
 (loop [page 1 bulk (fetch page)]
   (when (not-empty bulk)
      (callback (check-resp bulk))
      (recur (inc page) (fetch (inc page))))))

(defmulti paginate
  (fn [m f] (first (clojure.set/intersection (into #{} (keys m)) #{:user :org}))))

(defmethod paginate :user [{:keys [user auth]} f] 
  (run-paging f (partial user-repos user auth)))

(defmethod paginate :org [{:keys [org]} f] 
  (run-paging f (partial org-repos org)))

(defn match-layout
   [path name layouts]
   (println path name layouts)
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
  [workspace {:keys [layouts options] :as m}]
  (paginate m
    (fn [bulk]
      (doseq [{:keys [name git_url]} bulk :let [
          dest (match-layout workspace name layouts)
          op (options (keyword name))
          parent (.getParent (file dest))]]
          (info "checking" name)
          (if-not (.exists (file dest))
            (safe (with-opts sh op "git" "clone" "--mirror" git_url dest))
            (with-sh-dir dest
              (safe (sh "git" "remote" "update" "--prune"))))
          (info "mirrored" name )
          (when-not (.exists (file (str parent "/bundles")))
            (.mkdirs (file (str parent "/bundles"))))
          (with-sh-dir dest
            (safe (sh "git" "--git-dir" dest  "bundle" "create" (str parent "/bundles/" name ".bundle") "--all")))
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


