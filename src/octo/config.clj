(ns octo.config
  (:require 
    [clojure.spec :as s]
    [clojure.edn :as edn]))


(s/def ::org string?)

(s/def ::user string?)

(s/def ::exclude (s/coll-of string?))

(s/def ::layouts (s/coll-of (s/coll-of string?)))

(s/def ::option (s/map-of keyword? string?))

(s/def ::options (s/map-of keyword? ::option))

(s/def ::repos (s/coll-of (s/keys :opt-un [::org ::user ::options] :req-un [::exclude ::layouts])))

(s/def ::config (s/keys :req-un [::repos]))


(defn load-config 
  ([] (load-config "octo.edn"))
  ([c] 
    (let [config (edn/read-string (slurp c))]
      (if (s/valid? ::config config)
         config
        (throw (ex-info "Configuration not valid" (s/explain-data ::config config))))  
      )))

;; (s/valid? ::config  (edn/read-string (slurp "octo-auth.edn")))
;; (s/explain-data ::config  (edn/read-string (slurp "octo-auth.edn")))
 
