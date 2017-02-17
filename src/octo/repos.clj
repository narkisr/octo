(ns octo.repos
  (:require
    [clojure.java.io :refer (file)]
    [octo.git :as git]
    [clojure.tools.trace :as t]
    [tentacles.repos :as repos]
    [tentacles.repos :as repos]
    [taoensso.timbre :as timbre]
    [tentacles.users :as users]))

(timbre/refer-timbre)

(defn check [resp]
  (when (and (map? resp) (not (== (get resp :status -1) 200)))
    (error "Failed to call Github API" (select-keys resp [:status :body]))
    (System/exit 1)))

(defn user-repos [user auth i]
  (repos/repos {:per-page 50 :page i :affiliation "owner" :auth auth}))

(defn org-repos [org auth i]
  (repos/org-repos org {:per-page 50 :page i}))

(def fetchers {:org org-repos :user user-repos})

(defn run-paging [callback fetch]
 (loop [page 1 resp (fetch page)]
   (when (not-empty resp)
      (check resp)
      (callback resp)
      (recur (inc page) (fetch (inc page))))))

(defmulti paginate
  (fn [m auth f] (first (clojure.set/intersection (into #{} (keys m)) #{:user :org}))))

(defmethod paginate :user [{:keys [user]} auth f]
  (run-paging f (partial user-repos user auth)))

(defmethod paginate :org [{:keys [org]} auth f]
  (run-paging f (partial org-repos org auth)))

(defn match-layout
   [path name layouts]
   (if-let [[r dest] (first (filter (fn [[r dest]] (re-find (re-pattern r) name)) layouts))]
     (str path "/" dest "/" name)
     (str path "/" name)))

(defn backup
  [workspace auth {:keys [layouts options] :as m}]
  (paginate m auth
    (fn [bulk]
      (doseq [{:keys [name ssh_url git_url private]} bulk 
        :let [dest (match-layout workspace name layouts)
              op (options (keyword name))
              parent (.getParent (file dest))]]
         (info "backup" name)
         (git/upclone (if private ssh_url git_url) dest op)          
         (info "mirrored" name )
         (git/bundle parent dest name) 
         (info "bundled" name)))))
