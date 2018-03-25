(ns octo.config
  (:require
   [octo.provider :refer (match)]
   [clojure.spec.alpha :as s]
   [clojure.edn :as edn]))

(s/def ::org string?)

(s/def ::user string?)

(s/def ::exclude (s/coll-of string?))

(s/def ::layouts (s/coll-of (s/coll-of string?)))

(s/def ::option (s/map-of keyword? string?))

(s/def ::options (s/map-of keyword? ::option))

(s/def ::repos (s/coll-of (s/keys :opt-un [::org ::user ::options] :req-un [::exclude ::layouts])))

(s/def ::github (s/keys :req-un [::repos]))

(s/def ::url string?)

(s/def ::gitblit (s/keys :req-un [::url]))

(def types {:octo.gitblit ::gitblit :octo.github ::github})

(defn load-config
  ([] (load-config "octo.edn"))
  ([c]
   (let [config (edn/read-string (slurp c)) t (types (match config))]
     (if (s/valid? t config)
       config
       (throw (ex-info "Configuration not valid" (s/explain-data ::config config)))))))

;; (s/valid? ::config  (edn/read-string (slurp "octo-auth.edn")))
;; (s/explain-data ::config  (edn/read-string (slurp "octo-auth.edn")))

