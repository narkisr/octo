(ns octo.gitblit.push
  "push local backup remotly to the cloud"
  (:require
   [octo.common.push :as cp]
   [clojure.core.strint :refer  (<<)]
   [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defn parent
  ([workspace]
   (<< "~{workspace}/push"))
  ([workspace repo]
   (<< "~(parent workspace)/~{repo}.zback")))

(defn push
  [workspace m {:keys [user]}]
  (cp/push m (<< "~{workspace}/sync/~{user}") (parent workspace user) user))
