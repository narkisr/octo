(ns octo.core 
  (:gen-class)
  (:require 
    [taoensso.timbre :as timbre]
    [octo.config :as config]
    [octo.repos :refer (clone)])
  (:use 
    [me.raynes.fs :only  [mkdir exists? expand-home]]
    [clojure.java.shell :only [sh]]))

(timbre/refer-timbre)

(defn -main [c & args]
  (let [{:keys [repos workspace]} (config/load-config c)]
     (doseq [{:keys [user layouts options]} repos] 
        (info "Cloning repos from:" user)
        (clone user workspace layouts options))))

