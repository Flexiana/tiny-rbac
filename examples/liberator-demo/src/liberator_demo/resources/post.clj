(ns liberator-demo.resources.post
  (:require [liberator.core :refer [defresource by-method]]
            [liberator-demo.acl :refer [role-set]]
            [tiny-rbac.core :as c]
            [liberator-demo.database :as db]))

(defresource resource
  [post-id]
  :available-media-types ["application/json"]
  :handle-ok (fn [_]
               (if (= "all" post-id)
                 (db/fetch-all :posts)
                 (db/fetch-one :posts post-id))))

(defresource resource-for
  [user-id post-id]
  :initialize-context (by-method
                        {:get    (fn [_]
                                   (let [user (db/fetch-one :users user-id)]
                                     {:user user
                                      :acl  {:resource :post
                                             :action   :read
                                             :role     (:role user)}}))
                         :put    (fn [_]
                                   (let [user (db/fetch-one :users user-id)]
                                     {:user user
                                      :acl  {:resource :post
                                             :action   :create
                                             :role     (:role user)}}))
                         :delete (fn [_]
                                   (let [user (db/fetch-one :users user-id)]
                                     {:user user
                                      :acl  {:resource :post
                                             :action   :delete
                                             :role     (:role user)}}))
                         :post   (fn [_]
                                   (let [user (db/fetch-one :users user-id)]
                                     {:user user
                                      :acl  {:resource :post
                                             :action   :comment
                                             :role     (:role user)}}))})

  :allowed? (fn [{:keys [acl]}]
              (and (c/has-permission role-set acl)
                   {:permissions (c/permissions role-set acl)}))

  :available-media-types ["application/json"]

  :handle-ok (fn [{:keys [user acl permissions]}]
               (let [role (:role user :lurker)]
                 {:permissions permissions
                  :acl acl})))






