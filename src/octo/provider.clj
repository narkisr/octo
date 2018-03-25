(ns octo.provider
  (:require
   [clojure.core.strint :refer  (<<)]
   [clojure.set :refer (intersection)]))

(def detection {:octo.gitblit #{:url :password} :octo.github #{:token}})

(defn- resolve-
  "resolve function provided as a symbol with the form of ns/fn"
  [n f]
  (let [n' (name n) f' (name f) space (str n' "." f')]
    (try
      (require (symbol space))
      (ns-resolve (find-ns (symbol space)) (symbol f'))
      (catch java.io.FileNotFoundException e
        (throw  (ex-info (<< "Could not locate function ~{space}/~{f'}") {}))))))

(defn match [m]
  (let [options (map (fn [[k ds]] [k (intersection (into #{} (keys m)) ds)]) detection)]
    (first (first (filter (comp not empty? second) options)))))

(defn find-fn [k m]
  (resolve- (match m) k))
