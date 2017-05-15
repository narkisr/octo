(ns octo.github.push
  "push local backup remotely by using zbackup and rclone"
  (:require
    [octo.common.push :as cp]
    [clojure.core.strint :refer  (<<)]
    [taoensso.timbre :as timbre]
    ))

(timbre/refer-timbre)

(defn parent
  ([workspace]
    (<< "~{workspace}/push"))
  ([workspace repo]
      (<< "~(parent workspace)/~{repo}.zback")))

(defn push
  [workspace m {:keys [org user]}]
   (let [repo (or org user)]
    (cp/push m (<< "~{workspace}/sync/~{repo}/bundles") (parent workspace repo) repo)))




