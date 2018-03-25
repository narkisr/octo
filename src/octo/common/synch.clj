(ns octo.common.synch
  (:require
   [octo.git :as git]
   [octo.common :refer [excluded? files purge]]
   [me.raynes.fs :refer (delete-dir)]
   [taoensso.timbre :as timbre]
   [clojure.core.strint :refer (<<)]))

(timbre/refer-timbre)

(defn synch
  [workspace auth {:keys [options exclude parent bundles repos f] :as m}]
  (info "purging" bundles)
  (delete-dir bundles)
  (doseq [[name url] (map f (filter (partial excluded? exclude) repos))
          :let [dest (<< "~{parent}/~{name}") op ((or options {}) (keyword name))]]
    (info "synching" name url dest op)
    (git/upclone url dest op)
    (info "mirrored" name)
    (git/bundle parent dest name)
    (info "bundled" name))
  (let [bs (files bundles "check.edn") total (count bs)
        sources (into #{} (map #(.replace (.getName %) ".bundle" "") bs))]
    (spit (<< "~{bundles}/check.edn") (pr-str {:total total}))
    (debug "bundled total of" total)
    (purge sources (files parent "bundles"))))
