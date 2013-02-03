(ns octopi.core 
  (:require 
    [tentacles.repos :as repos]
    [tentacles.users :as users])
  (:use 
    [me.raynes.fs :only  [mkdir exists? expand-home]]
    [clojure.java.shell :only  [sh]]))

(def backup-path (expand-home "~/.github_bac"))

(defn user-repos [name i] (repos/user-repos name {:per-page 50 :page i}))

(defn paginate [user f]
  (loop [page 1 bulk (user-repos user page)]
    (when (not-empty bulk)
      (f bulk)
      (recur (inc page) (user-repos user (inc page))))))

(defn backup [bulk]
  (doseq [{:keys [name git_url]} bulk]
    (let [repo-path (str backup-path "/" name)]
      (if-not (exists? repo-path)
        (println "Mirroring " name (:out (sh "git" "clone" "--mirror" git_url repo-path))) 
        (println "Updating " name (:out (sh "git" "remote" "update" :dir repo-path)))))))

(defn -main [name] 
  (mkdir backup-path)
  (paginate name backup))
