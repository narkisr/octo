(ns octo.gitblit.synch
  "Processing gitblit repos"
  (:require
    [clojure.string :refer (replace)]
    [octo.git :as git]
    [octo.common :refer [excluded? files purge]]
    [me.raynes.fs :refer (delete-dir)]
    [taoensso.timbre :as timbre]
    [clojure.core.strint :refer  (<<)]
    [clj-http.client :as client]))

(timbre/refer-timbre)

(defn repos
   "Get gitblit repos"
   [{:keys [url]} auth]
   {:post [(not (empty? %))]}
   (let [basic {:basic-auth auth :insecure true :as :json}]
     (:body (client/get (<< "~{url}/rpc/?req=LIST_REPOSITORIES") basic))))

(defn synch
  [workspace auth {:keys [layouts options exclude] :as m}]
  (let [parent (<< "~{workspace}/sync") bundles (<< "~{parent}/bundles")]
    (info "purging" bundles)
    (delete-dir bundles)
    (doseq [[url {:keys [name]}] (filter (partial excluded? exclude) (repos m auth))
        :let [name' (replace name ".git" "") dest (<< "~{parent}/~{name'}") ]]
         (info "synching" name')
         (git/upclone (subs (str url) 1) dest {})
         (info "mirrored" name')
         (git/bundle parent dest name')
         (info "bundled" name'))
       (let [bs (files bundles "check.edn") total (count bs)
           sources (into #{} (map #(replace (.getName %) ".bundle" "") bs))]
         (spit (<< "~{bundles}/check.edn") (pr-str {:total total}))
         (debug "bundled total of" total)
         (purge sources (files parent "bundles")))))

(comment
  (clojure.pprint/pprint (subs (str (first (keys (repos {:url "https://192.168.121.22:8443/"} "admin:vagrant")))) 1))
  )
