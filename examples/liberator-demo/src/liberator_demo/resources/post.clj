(ns liberator-demo.resources.post
  (:require [liberator.core :refer [defresource by-method]]
            [liberator-demo.acl :refer [role-set acl-post]]
            [liberator-demo.models.posts :as post-model]
            [liberator-demo.models.friends :as friend-model]
            [tiny-rbac.core :as c]
            [liberator-demo.database :as db]))

(defresource resource
  [post-id]
  :available-media-types ["application/json"]
  :handle-ok (fn [_] (post-model/fetch-posts post-id)))

(defresource resource-for
  [user-id post-id]
  :allowed-methods [:post :get :delete :put]
  :initialize-context (fn [_]
                        {:user (db/fetch-one :users user-id)})

  :allowed? (by-method
              {:get    (fn [{:keys [user]}]
                         (let [acl (acl-post user :read)]
                           (and (c/has-permission role-set acl)
                                {:permissions (c/permissions role-set acl)})))
               :post   (fn [{:keys [user]}]
                         (let [acl (acl-post user :comment)]
                           (and (c/has-permission role-set acl)
                                {:permissions (c/permissions role-set acl)})))
               :delete (fn [{:keys [user]}]
                         (let [acl (acl-post user :delete)]
                           (and (c/has-permission role-set acl)
                                {:permissions (c/permissions role-set acl)})))
               :put    (fn [{:keys [user]}]
                         (let [acl (acl-post user :create)]
                           (and (c/has-permission role-set acl)
                                {:permissions (c/permissions role-set acl)})))})

  :available-media-types ["application/json"]

  :handle-ok (fn [{:keys [user]}]
               (let [permissions (c/permissions role-set (:role user) :post :read)
                     friends (friend-model/get-friends-ids (:id user))]
                 {:posts (post-model/fetch-posts permissions friends post-id)})))






