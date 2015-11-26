(ns octo.config
  (:require 
    [clojure.edn :as edn]))

(defn load-config []
  (edn/read-string (slurp "octo.edn")))
