(ns octo.core 
  (:require 
    [tentacles.repos :as repos]
    ;;wa; [clojure.edn :as edn]
    [tentacles.users :as users]
    [octo.api :refer (app)]
    )
  (:use 
    ring.adapter.jetty
    [me.raynes.fs :only  [mkdir exists? expand-home]]
    [clojure.java.shell :only  [sh]]))

(defn user-repos [name i] (repos/user-repos name {:per-page 50 :page i}))

(defn paginate [user f]
  (loop [page 1 bulk (user-repos user page)]
    (when (not-empty bulk)
      (f bulk)
      (recur (inc page) (user-repos user (inc page))))))


(defn clone  
   [path exclude bulk]
  (doseq [{:keys [name git_url]} bulk]
    (let [repo-path (str path "/" name)]
      (when-not (contains? exclude name)
        (println "Cloning " name (:out (sh "git" "clone" git_url repo-path))) 
        ))))

(defn load-config  
   []
   {}
   #_((edn/read-string (slurp "octo.edn"))))

(defn -main [user action] 
  (let [{:keys [path users] :as config} (load-config)]
    (case action
      "clone" 
         (doseq [{:keys [name exclude]} users] (paginate name (clone path exclude)))
      "push" identity
      "server" (run-jetty {:port 8080})
    )))
