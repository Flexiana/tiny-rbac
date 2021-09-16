(ns liberator-demo.core
  (:require [ring.middleware.params :refer [wrap-params]]
            [liberator-demo.resources.index :as index]
            [liberator-demo.resources.post :as post]
            [compojure.core :refer [defroutes ANY context]]))

(defroutes app
  (ANY "/" [] (index/resource))
  (context "/user/:user-id" [user-id]
    (ANY "/posts" [] (post/posts user-id))
    (ANY "/posts/:post-id" [post-id] (post/post user-id post-id))))

(def handler
  (-> app
      wrap-params))