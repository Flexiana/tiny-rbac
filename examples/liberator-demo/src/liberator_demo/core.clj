(ns liberator-demo.core
  (:require [ring.middleware.params :refer [wrap-params]]
            [liberator-demo.resources.index :as index]
            [liberator-demo.resources.post :as post]
            [compojure.core :refer [defroutes ANY]]))

(defroutes app
  (ANY "/" [] (index/resource))
  (ANY "/posts/:post-id" [post-id] (post/resource post-id))
  (ANY "/posts/:user-id/:post-id" [user-id post-id] (post/resource-for-user user-id post-id)))

(def handler
  (-> app
      wrap-params))