(ns octo.repos
  (:require  
    [clj-jgit.porcelain :as jgit :refer [with-repo]]
    [me.raynes.fs :as fs]
    [clojure.edn :as edn]
    [clojure.java.shell :refer [sh]]
    [tentacles.repos :as repos]
    [taoensso.timbre :as timbre]
    [tentacles.users :as users]))


(timbre/refer-timbre)

(defn load-config []
  (edn/read-string (slurp "octo.edn")))

(defn user-repos [name i] 
  (repos/user-repos name {:per-page 50 :page i}))

(defn paginate [user f]
  (loop [page 1 bulk (user-repos user page)]
    (when (not-empty bulk)
      (f bulk)
      (recur (inc page) (user-repos user (inc page))))))

(defn clone  
  [user]
  (let [{:keys [path]} (load-config)]
    (paginate user  
      (fn [bulk]
        (doseq [{:keys [name git_url]} bulk]
          (let [repo-path (str path "/" name)]
            (info "Cloning " name (:out (sh "git" "clone" git_url repo-path)))))))))

(defn repo-remote [f]
  (with-repo (.getPath f)
    (.getString (.getConfig (.getRepository repo)) "remote" "origin" "url")))

(defn list-workspace []
  (let [{:keys [path]} (load-config)]
    (doall (map (partial zipmap [:url :name]) (map (juxt repo-remote (comp :name bean)) (fs/list-dir path))))))


(list-workspace)

