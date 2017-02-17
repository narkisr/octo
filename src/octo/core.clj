(ns octo.core 
  (:gen-class)
  (:require 
    [taoensso.timbre :as timbre]
    [octo.config :as config]
    [octo.repos :refer (backup)])
  (:use 
    [me.raynes.fs :only  [mkdir exists? expand-home]]
    [clojure.java.shell :only [sh]]))

(timbre/refer-timbre)

(defn -main [c & args]
  (let [{:keys [repos workspace user token]} (config/load-config c) auth (str user ":" token)]
     (doseq [{:keys [user org] :as repo} repos] 
        (info "Cloning repos from:" (or user org))
        (backup workspace auth repo))))

