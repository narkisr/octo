(ns octo.gitblit
  "Processing gitblit repos"
  (:require 
    [clojure.core.strint :refer  (<<)]
    [clj-http.client :as client]))

(defn repos 
   "Get gitblit repos" 
   [url auth]
  (:body (client/get (<< "~{url}/rpc/?req=LIST_REPOSITORIES") 
      {:basic-auth auth :insecure true :as :json})))


