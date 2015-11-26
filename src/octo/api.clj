(ns octo.api
  (:require 
   [compojure.core :refer :all]
   [ring.middleware.format :refer (wrap-restful-format)]
   [ring.middleware.format-params :refer (wrap-json-kw-params)]
   [compojure.route :as route]
))

(defroutes app-routes
  (GET "/repos/:user" [user] {})
  (GET "/repos/clone/:user" [user] {})
  (route/not-found "Not Found"))

(def app
  (-> 
   app-routes
   (wrap-json-kw-params)
   (wrap-restful-format :formats [:json-kw :edn :yaml-kw :yaml-in-html]))

)
 
