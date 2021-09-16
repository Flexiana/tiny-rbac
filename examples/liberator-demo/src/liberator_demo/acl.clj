(ns liberator-demo.acl
  (:require [tiny-rbac.builder :as b]
            [liberator-demo.models.users :as user-model]
            [tiny-rbac.core :as acl]
            [liberator-demo.models.posts :as post-model]))

(def role-set
  (-> {}
      (b/add-resource [:post :comment])
      (b/add-action :post [:read :create :delete :update])
      (b/add-action :comment [:create :modify :delete])
      (b/add-role [:lurker :poster :only-friends])
      (b/add-permission :only-friends :post :read [:own :friends])
      (b/add-inheritance :poster :lurker)
      (b/add-permission :lurker :post :read :public)
      (b/add-permission :poster :post :read [:own :friends])
      (b/add-permission :poster :post :create :own)
      (b/add-permission :poster :post :delete :own)
      (b/add-permission :poster :post :update :own)
      (b/add-permission :poster :comment :create :friends)
      (b/add-permission :poster :comment :modify :own)
      (b/add-permission :poster :comment :delete :own)))

(defn acl-post
  [user action]
  {:resource :post
   :action   action
   :role     (:role user)})

(defn owned-post
  [user action post-id]
  (let [user-id (:id user)
        acl (acl-post user action)
        permissions (acl/permissions role-set acl)
        friends (user-model/get-friends-ids user-id)]
   (first (post-model/fetch-posts permissions user-id friends post-id))))

(defn owned-posts
  [user action]
  (let [user-id (:id user)
        acl (acl-post user action)
        permissions (acl/permissions role-set acl)
        friends (user-model/get-friends-ids user-id)]
    (post-model/fetch-posts permissions user-id friends)))