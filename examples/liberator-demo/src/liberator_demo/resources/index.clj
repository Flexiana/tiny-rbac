(ns liberator-demo.resources.index
  (:require [liberator.core :refer [defresource]]
            [hiccup.core :refer [html]]))

(defresource resource []
  :available-media-types ["text/html"]
  :handle-ok (html [:html
                    [:h1 "Welcome to tiny RBAC liberator demo"]
                    [:div
                     [:h2 "Testing with postman"]
                     [:p "Because it's just a quick example, there are no tests included."]
                     [:table
                      [:thead
                       [:tr
                        [:th "method"]
                        [:th "path"]
                        [:th "result"]]]
                      [:tbody
                       [:tr
                        [:td "GET"]
                        [:td "localhost:3000"]
                        [:td "welcome screen"]]
                       [:tr
                        [:td "GET"]
                        [:td "localhost:3000/user/{{user-id}}/posts"]
                        [:td "All posts and comments which visible for the user"]]
                       [:tr
                        [:td "GET"]
                        [:td "localhost:3000/user/{{user-id}}/posts/{{post-id}}"]
                        [:td "One post and comments if it's visible for the user"]]
                       [:tr
                        [:td "PUT"]
                        [:td "localhost:3000/user/{{user-id}}/posts"]
                        [:td "Creates a post if the user has the ability"]]
                       [:tr
                        [:td "PATCH"]
                        [:td "localhost:3000/user/{{user-id}}/posts/{{post-id}}"]
                        [:td "Updates a post if the user has the ability"]]
                       [:tr
                        [:td "DELETE"]
                        [:td "localhost:3000/user/{{user-id}}/posts/{{post-id}}"]
                        [:td "Deletes a post if the user has the ability"]]]]
                     [:p "When a user is not permitted to make an action, by its role or on a given post, then the response status is 403: Forbidden"]
                     [:h3 "Creating and updating a post"]
                     [:p "From postman the body should be " [:code "x-www-form-urlencoded"] "."]
                     [:p "The used fields are:"]
                     [:ul
                      [:li "content"]
                      [:li "visible"]]
                     [:p "If the visible parameter is not set, then it's defaults to " [:code "public"] "."]]]))