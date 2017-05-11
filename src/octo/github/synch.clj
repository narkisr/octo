(ns octo.github.synch
  (:require
    [me.raynes.fs :refer (delete-dir)]
    [octo.common :refer (files purge excluded?)]
    [clojure.core.strint :refer  (<<)]
    [clojure.pprint :refer (print-table)]
    [clojure.java.io :refer (file)]
    [octo.git :as git]
    [clojure.tools.trace :as t]
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

(defn run-paging [fetch]
 (loop [res [] page 1 resp (fetch page)]
   (if (empty? resp)
       res
      (do
        (check resp)
        (recur (into res resp) (inc page) (fetch (inc page)))))))

(defn identifier [m]
  (first (clojure.set/intersection (into #{} (keys m)) #{:user :org})))

(defmulti paginate
  (fn [m auth] (identifier m)))

(defmethod paginate :user [{:keys [user]} auth]
  (run-paging (partial user-repos user auth)))

(defmethod paginate :org [{:keys [org]} auth]
  (run-paging (partial org-repos org auth)))

(defn match-layout
   [path name layouts]
   (if-let [[r dest] (first (filter (fn [[r dest]] (re-find (re-pattern r) name)) layouts))]
     (str path "/" dest "/" name)
     (str path "/" name)))

(defn synch
  [workspace auth {:keys [layouts options exclude] :as m}]
  (let [id ((identifier m) m) parent (<< "~{workspace}/repos/~{id}") bundles (<< "~{parent}/bundles")]
     (info "purging" bundles)
     (delete-dir bundles)
     (doseq [{:keys [name ssh_url git_url private]} (filter (partial excluded? exclude) (paginate m auth))
        :let [dest (<< "~{parent}/~{name}") op ((or options {}) (keyword name))]]
         (info "synching" name)
         (git/upclone (if private ssh_url git_url) dest op)
         (info "mirrored" name )
         (git/bundle parent dest name)
         (info "bundled" name))
     (let [bs (files bundles "check.edn") total (count bs)
           sources (into #{} (map #(.replace (.getName %) ".bundle" "") bs))]
       (spit (<< "~{bundles}/check.edn") (pr-str {:total total}))
       (debug "bundled total of" total)
       (purge sources (files parent "bundles")))))


(defn stale
  [auth m]
  (let [repos (paginate m auth)]
    (info "Forks:")
    (print-table [:name :pushed_at] (filter :fork repos))
    (info "Least updated:")
    (print-table [:name :pushed_at] (take 20 (sort-by :pushed_at repos)))))

