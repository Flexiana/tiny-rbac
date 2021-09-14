(ns liberator-demo.resources.index
  (:require [liberator.core :refer [defresource]]
            [liberator-demo.database :refer [_db]]))

(defresource resource []
  :available-media-types ["text/html"]
  :handle-ok (hiccup.core/html [:html
                                [:h1 "Welcome to tiny RBAC liberator demo"]
                                [:h2 "Available links"]
                                [:p "\"/\" this page"]
                                [:p "\"/posts/{post-id}\" see a post"]
                                [:p "\"/posts/{user-id}\"{post-id}\" see a post as user"]]))