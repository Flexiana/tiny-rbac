(ns liberator-demo.resources.index
  (:require [liberator.core :refer [defresource]]
            [liberator-demo.database :refer [_db]]))

(defresource resource []
  :available-media-types ["text/html"]
  :handle-ok (hiccup.core/html [:html
                                [:h1 "Welcome to tiny RBAC liberator demo"]
                                [:h2 "Available links"]
                                [:div
                                 [:p "\"/\" this page"]
                                 [:p "\"/posts/{post-id}\" see a post"]
                                 [:p "\"/posts/{user-id}/{post-id}\" see a post as user"]
                                 [:p "users at start-up are available with user-id from 0 to 3"]
                                 [:p "posts with post-id from 0 to 2"]
                                 [:p "user 0 and 1, and user 0 and 2 are friends"]]]))