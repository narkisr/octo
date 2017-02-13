(ns octo.config
  (:require 
    [clojure.edn :as edn]))

(defn load-config 
  ([] (load-config "octo.edn"))
  ([c]  (edn/read-string (slurp ))))
