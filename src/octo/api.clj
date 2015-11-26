(ns octo.api
  (:require 
   [octo.repos :refer (clone list-workspace)]
   [ring.middleware.reload :refer [wrap-reload]]
   [compojure.core :refer :all]
   [ring.middleware.format :refer (wrap-restful-format)]
   [ring.middleware.format-params :refer (wrap-json-kw-params)]
   [compojure.route :as route]
))

(defroutes app-routes
  (GET "/repos/list" [] 
    {:status 200 :body {:repos (list-workspace)}})
  (GET "/repos/clone/:user" [user] 
    (future (clone user))
    {:status 200 :body "starting to clone"})
  (route/files "/" {:root "public/elm"})
  (route/not-found "Not Found"))

(def app
  (-> 
   app-routes
   (wrap-json-kw-params)
   (wrap-restful-format :formats [:json-kw :json :edn :yaml-kw :yaml-in-html])
   (wrap-reload)))
 
