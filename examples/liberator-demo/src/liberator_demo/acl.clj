(ns liberator-demo.acl
    (:require [tiny-rbac.builder :as b]))

(def role-set
  (-> {}
      (b/add-resource [:post :comment])
      (b/add-action :post [:read :create :delete :comment])
      (b/add-action :comment [:read :modify :delete])
      (b/add-role [:lurker :poster :only-friends])
      (b/add-permission :only-friends :post :read :friends)
      (b/add-inheritance :poster :lurker)
      (b/add-permission :lurker :post :read :all)
      (b/add-permission :lurker :comment :read :all)
      (b/add-permission :poster :post :create :all)
      (b/add-permission :poster :post :delete :own)
      (b/add-permission :poster :post :comment :friend)
      (b/add-permission :poster :comment :modify :own)
      (b/add-permission :poster :comment :delete :own)))

(defn acl-post
    [user action]
    {:resource :post
     :action   action
     :role     (:role user)})