(ns octo.github.synch
  (:require
   [octo.common.synch :as cs]
   [clojure.core.strint :refer  (<<)]
   [clojure.pprint :refer (print-table)]
   [clojure.set :refer (intersection)]
   [tentacles.repos :as repos]
   [taoensso.timbre :refer (refer-timbre)]))

(refer-timbre)

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
  (first
   (intersection (into #{} (keys m)) #{:user :org})))

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
  [workspace auth m]
  (letfn [(repo-url-pair [{:keys [name ssh_url clone_url private]}]
            [name (if private ssh_url clone_url)])]
    (let [id ((identifier m) m)
          parent (<< "~{workspace}/sync/~{id}")
          bundles (<< "~{parent}/bundles")]
      (cs/synch workspace auth
                (merge m {:parent parent :bundles bundles :repos (paginate m auth) :f repo-url-pair})))))

(defn stale
  [auth m]
  (let [repos (paginate m auth)]
    (info "Forks:")
    (print-table [:name :pushed_at] (filter :fork repos))
    (info "Least updated:")
    (print-table [:name :pushed_at] (take 20 (sort-by :pushed_at repos)))))

