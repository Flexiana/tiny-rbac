(ns liberator-demo.resources.post
  (:require [liberator.core :refer [defresource by-method]]
            [liberator-demo.acl :refer [role-set acl-post]]
            [liberator-demo.models.posts :as post-model]
            [liberator-demo.models.comments :as comment-model]
            [liberator-demo.models.users :as user-model]
            [liberator-demo.models.friends :as friend-model]
            [tiny-rbac.core :as acl]))

(defresource resource
  [post-id]
  :available-media-types ["application/json"]
  :handle-ok (fn [_] (map comment-model/comments->>post
                          (post-model/fetch-posts post-id))))

(defresource resource-for-user
  [user-id post-id]
  :allowed-methods [:post :get :delete :put]
  :initialize-context (fn [_]
                        {:user (user-model/fetch-user user-id)})

  :allowed? (by-method
              {:get    (fn [{:keys [user]}]
                         (let [acl (acl-post user :read)]
                           (and (acl/has-permission role-set acl)
                                {:permissions (acl/permissions role-set acl)})))
               :post   (fn [{:keys [user]}]
                         (let [acl (acl-post user :comment)]
                           (and (acl/has-permission role-set acl)
                                {:permissions (acl/permissions role-set acl)})))
               :delete (fn [{:keys [user]}]
                         (let [acl (acl-post user :delete)]
                           (and (acl/has-permission role-set acl)
                                {:permissions (acl/permissions role-set acl)})))
               :put    (fn [{:keys [user]}]
                         (let [acl (acl-post user :create)]
                           (and (acl/has-permission role-set acl)
                                {:permissions (acl/permissions role-set acl)})))})

  :put! (fn [{:keys [request user] :as ctx}]
          (let [content (get-in request [:params "content"])
                visibility (keyword (get-in request [:params "visibility"] "public"))]
            (post-model/new-post user content visibility)))



  :available-media-types ["application/json"]

  :handle-ok (fn [{:keys [user permissions]}]
               (let [friends (friend-model/get-friends-ids (:id user))
                     visible-posts (post-model/fetch-posts permissions friends post-id)
                     posts-with-comments (map comment-model/comments->>post visible-posts)]
                 {:posts posts-with-comments})))






