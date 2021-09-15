(ns liberator-demo.resources.index
  (:require [liberator.core :refer [defresource]]
            [hiccup.core :refer [html]]))

(defresource resource []
  :available-media-types ["text/html"]
  :handle-ok (html [:html
                    [:h1 "Welcome to tiny RBAC liberator demo"]
                    [:h2 "Available links"]
                    [:div
                     [:p "\"/\" this page"]
                     [:p "\"/user/{user-id}/posts\" see all posts as user"]
                     [:p "\"/user/{user-id}/posts/{post-id}\" see a post as user."]
                     [:p "users at start-up are available with user-id from 0 to 4"]
                     [:p "posts with post-id from 0 to 2"]
                     [:p "friends are 0-1, 0-2, 4-1"]
                     [:p "the user with id 4 can see only his friend's posts"]
                     [:p "the post with id 0 is only visible for friends of user-id 0"]]]))