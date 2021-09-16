(ns liberator-demo.resources.post
  (:require [liberator.core :refer [defresource by-method]]
            [liberator-demo.acl :refer [role-set acl-post owned-post]]
            [liberator-demo.models.posts :as post-model]
            [liberator-demo.models.comments :as comment-model]
            [liberator-demo.models.users :as user-model]
            [tiny-rbac.core :as acl]))

(defresource posts
  [user-id]
  :available-media-types ["application/json"]
  :allowed-methods [:get :put]
  :initialize-context (fn [_]
                        {:user (user-model/fetch-user user-id)})

  :allowed? (by-method
              {:get (fn [{:keys [user]}]
                      (let [acl (acl-post user :read)]
                        (and (acl/has-permission role-set acl)
                             {:permissions (acl/permissions role-set acl)})))
               :put (fn [{:keys [user]}]
                      (let [acl (acl-post user :create)]
                        (acl/has-permission role-set acl)))})

  :put! (fn [{:keys [request]}]
          (let [content (get-in request [:params "content"])
                visible (keyword (get-in request [:params "visible"] "public"))]
            (post-model/new-post user-id content visible)))

  :handle-ok (fn [{:keys [user permissions]}]
               (let [friends (user-model/get-friends-ids (:id user))
                     visible-posts (post-model/fetch-posts permissions user-id friends)
                     posts-with-comments (map comment-model/comments->>post visible-posts)]
                 {:posts posts-with-comments})))

(defresource post
  [user-id post-id]
  :available-media-types ["application/json"]
  :allowed-methods [:post :get :delete]
  :initialize-context (fn [_] {:user (user-model/fetch-user user-id)})

  :allowed? (by-method
              {:get    (fn [{:keys [user]}]
                         (when-let [post (owned-post user :read post-id)]
                           (println post)
                           {:post post}))
               :post   (fn [{:keys [user]}]
                         (when-let [post (owned-post user :update post-id)]
                           {:post post}))
               :delete (fn [{:keys [user]}]
                         (when-let [post (owned-post user :delete post-id)]
                           {:post post}))})

  :post! (fn [{:keys [request post]}]
           (let [visible (get-in request [:params "visible"])
                 content (get-in request [:params "content"])
                 post-update (cond-> post
                                     visible (assoc :visible visible)
                                     content (assoc :content content))]
             (post-model/update-post post-update)))

  :delete! (fn [{:keys [post]}]
             (post-model/delete-post post))

  :handle-ok (fn [{:keys [post]}]
               {:posts [post]}))






