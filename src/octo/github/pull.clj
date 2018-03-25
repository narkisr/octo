(ns octo.github.pull
  "pull backup into restore folder"
  (:require
   [octo.common.pull :as cp]
   [taoensso.timbre :as timbre]
   [clojure.core.strint :refer  (<<)]))

(timbre/refer-timbre)

(defn pull
  [workspace {:keys [zbackup rclone] :as m} {:keys [org user]}]
  (let [repo (or org user)]
    (cp/pull m (<< "~{workspace}/pull") repo)))
