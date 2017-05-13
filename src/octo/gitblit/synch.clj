(ns octo.gitblit.synch
  "Processing gitblit repos"
  (:require
    [clojure.string :refer (replace)]
    [octo.common.synch :as cs]
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
  [workspace auth m]
   (cs/synch workspace auth (merge m {
      :parent (<< "~{workspace}/sync") :bundles (<< "~{workspace}/sync/bundles")
      :repos (repos m auth) :f (fn [[url {:keys [name]}]] [(replace name ".git" "") url])
     })))

(comment
  (clojure.pprint/pprint (subs (str (first (keys (repos {:url "https://192.168.121.22:8443/"} "admin:vagrant")))) 1))
  )
