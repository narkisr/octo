(ns octo.gitblit.pull
  "pull backup into restore folder"
  (:require
   [octo.common.pull :as cp]
   [taoensso.timbre :as timbre]
   [clojure.core.strint :refer  (<<)]))

(timbre/refer-timbre)

(defn pull
  [workspace m {:keys [user]}]
  (cp/pull m (<< "~{workspace}/pull") user))
