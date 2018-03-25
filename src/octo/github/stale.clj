(ns octo.github.stale
  (:require
   [clojure.pprint :refer (print-table)]
   [taoensso.timbre :as timbre]
   [octo.github.synch :refer (paginate)]))

(timbre/refer-timbre)

(defn stale
  [auth m]
  (let [repos (paginate m auth)]
    (info "Forks:")
    (print-table [:name :pushed_at] (filter :fork repos))
    (info "Least updated:")
    (print-table [:name :pushed_at] (take 20 (sort-by :pushed_at repos)))))

