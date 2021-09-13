(ns tiny-rbac.test
  (:require [clojure.test :refer :all]
            [tiny-rbac.builder :as b]
            [tiny-rbac.core :as c]))

(declare thrown-with-msg?)

(deftest add-resource
  (is (= #{:comment}
         (-> (b/add-resource {} :comment)
             (c/resources)))
      "can add one resource")
  (is (= #{:comment :post}
         (-> (b/add-resource {} [:comment :post])
             (c/resources)))
      "can add multiple resources")
  (is (= :post
         (-> (b/add-resource {} [:comment :post])
             (c/resource :post)))
      "requesting a resource by keyword")
  (is (= #{:post}
         (-> (b/add-resource {} :post)
             (b/add-resource :post)
             c/resources))
      "Cannot duplicate resources")
  (is (= "post"
         (-> (b/add-resource {} [:comment "post"])
             (c/resource "post")))
      "requesting a resource by string")
  (is (= nil
         (-> (b/add-resource {} [:comment :post])
             (c/resource :tag)))
      "nil when resource not found")
  (is (= nil
         (c/resource {} :tag))
      "nil when no resources"))

(deftest delete-resource
  (is (= #{:comment}
         (-> (b/add-resource {} [:comment :post])
             (b/delete-resource :post)
             (c/resources)))
      "can delete one resource")
  (is (= #{:comment}
         (-> (b/add-resource {} [:comment :post :tag])
             (b/delete-resource [:post :tag])
             (c/resources)))
      "can delete multiple resources")
  (is (= #{}
         (-> (b/add-resource {} [:post :tag])
             (b/delete-resource [:post :tag])
             (c/resources)))
      "can delete multiple resources by name")
  (is (= #{}
         (-> (b/add-resource {} [:post :tag])
             (b/delete-resource ::b/all)
             (c/resources)))
      "can delete all resources")
  (is (thrown-with-msg? IllegalArgumentException
                        #"referred resource does not exists"
                        (b/delete-resource {} :comment))
      "Throws an Exception when resource not available")
  (is (thrown-with-msg? IllegalArgumentException
                        #"referred resource does not exists"
                        (-> (b/add-resource {} [:post :comment])
                            (b/delete-resource [:comment :tag])))
      "Throws an Exception when resource not available"))

(deftest add-action
  (is (= #{:read}
         (-> (b/add-resource {} :comment)
             (b/add-action :comment :read)
             (c/actions :comment)))
      "Add an action to resource")
  (is (= #{:read :write}
         (-> (b/add-resource {} :comment)
             (b/add-action :comment [:read :write])
             (c/actions :comment)))
      "Add multiple actions to resource")
  (is (= #{:read :delete :write}
         (-> (b/add-resource {} :comment)
             (b/add-action :comment [:read :write])
             (b/add-action :comment [:read :write :delete])
             (c/actions :comment)))
      "Add actions multiple times to resource")
  (is (= :read
         (-> (b/add-resource {} :comment)
             (b/add-action :comment [:read :write :delete])
             (c/action :comment :read)))
      "Get action by resource and action")
  (is (= nil
         (-> (b/add-resource {} :comment)
             (b/add-action :comment [:write :delete])
             (c/action :comment :read)))
      "Response with nil if action missing from resource")
  (is (thrown-with-msg? IllegalArgumentException
                        #"referred resource does not exists"
                        (b/add-action {} :comment :read))
      "Throws an Exception when resource not available"))

(deftest delete-resource-deletes-actions
  (is (= {::c/resources #{}, ::c/actions {}}
         (-> (b/add-resource {} :comment)
             (b/add-action :comment :read)
             (b/delete-resource :comment)))
      "deleting resources removes actions too")
  (is (= {::c/resources #{}, ::c/actions {}}
         (-> (b/add-resource {} [:comment :post])
             (b/add-action :comment :read)
             (b/add-action :post :read)
             (b/delete-resource ::b/all)))
      "deleting resources removes actions too"))

(deftest delete-action
  (is (= #{:tag}
         (-> (b/add-resource {} :comment)
             (b/add-action :comment [:read :tag])
             (b/delete-action :comment :read)
             (c/actions :comment)))
      "deleting action")
  (is (= #{:tag}
         (-> (b/add-resource {} :comment)
             (b/add-action :comment [:read :write :tag])
             (b/delete-action :comment [:read :write])
             (c/actions :comment)))
      "deleting multiple actions")
  (is (thrown-with-msg? IllegalArgumentException
                        #"referred resource does not exists"
                        (b/delete-action {} :comment [:read :write]))
      "Throwing error when resource not defined")
  (is (thrown-with-msg? IllegalArgumentException
                        #"referred action does not exists"
                        (-> (b/add-resource {} :comment)
                            (b/delete-action :comment :read)))
      "Throwing error when action not found")
  (is (= #{}
         (-> (b/add-resource {} :comment)
             (b/add-action :comment [:read :write :tag])
             (b/delete-action :comment ::b/all)
             (c/actions :comment)))
      "deleting all actions"))

(deftest add-role
  (is (= {::c/roles {:poster {}}}
         (b/add-role {} :poster)))
  (is (= {::c/roles {:poster {}
                     :admin     {}}}
         (b/add-role {} [:poster :admin]))))

(deftest add-inheritance
  (is (= #{:poster}
         (-> (b/add-role {} :reader)
             (b/add-role :poster)
             (b/add-inheritance :reader :poster)
             (c/inherit :reader)))
      "Add role as inheritance")
  (is (= #{:poster :admin}
         (-> (b/add-role {} :reader)
             (b/add-role :poster)
             (b/add-role :admin)
             (b/add-inheritance :reader [:poster :admin])
             (c/inherit :reader)))
      "Add roles as inheritance")
  (is (= #{:poster :admin}
         (-> (b/add-role {} :reader)
             (b/add-role :poster)
             (b/add-role :admin)
             (b/add-inheritance :reader [:poster :admin])
             (b/add-inheritance :reader :admin)
             (c/inherit :reader)))
      "add-inheritance does not overwrites given inheritances")
  (is (thrown-with-msg? IllegalArgumentException
                        #"referred role does not exists"
                        (-> (b/add-role {} :reader)
                            (b/add-inheritance :reader :poster)))
      "Add missing role as inheritance")
  (is (thrown-with-msg? IllegalArgumentException
                        #"referred role does not exists"
                        (-> (b/add-role {} :reader)
                            (b/add-role :admin)
                            (b/add-inheritance :reader [:admin :poster])))
      "Add missing role as inheritance")
  (is (= #{:poster}
         (-> (b/add-role {} :poster)
             (b/add-inheritance :reader :poster)
             (c/inherit :reader)))
      "Creating role for only inheritance")
  (is (= #{:poster :admin}
         (-> (b/add-role {} :poster)
             (b/add-role :admin)
             (b/add-inheritance :reader [:poster :admin])
             (c/inherit :reader)))
      "Creating role with multiple inheritances"))

(deftest circular-inheritance
  (is (thrown-with-msg? IllegalArgumentException
                        #"Circular inheritance detected for :reader"
                        (-> (b/add-role {} :reader)
                            (b/add-inheritance :reader :reader)))
      "direct circular inheritance detected")
  (is (thrown-with-msg? IllegalArgumentException
                        #"Circular inheritance detected for :reader"
                        (-> (b/add-role {} :reader)
                            (b/add-role :poster)
                            (b/add-inheritance :reader [:poster :reader])))
      "direct circular inheritance detected")
  (is (thrown-with-msg? IllegalArgumentException
                        #"Circular inheritance detected for :reader"
                        (-> (b/add-role {} :reader)
                            (b/add-inheritance :poster :reader)
                            (b/add-inheritance :reader :poster)))
      "indirect circular inheritance detected")
  (is (thrown-with-msg? IllegalArgumentException
                        #"Circular inheritance detected for :1"
                        (-> (b/add-role {} :1)
                            (b/add-inheritance :2 :1)
                            (b/add-inheritance :3 :2)
                            (b/add-inheritance :4 :3)
                            (b/add-inheritance :1 :4)))
      "indirect circular inheritance detected"))

(deftest add-permission
  (is (= #{::b/all}
         (-> (b/add-resource {} :post)
             (b/add-action :post [:read :write])
             (b/add-role :poster)
             (b/add-permission :poster :post :read ::b/all)
             (c/permissions :poster :post :read)))
      "add single permission")
  (is (= #{:own :friend}
         (-> (b/add-resource {} :post)
             (b/add-action :post [:read :write])
             (b/add-role :poster)
             (b/add-permission :poster :post :read [:own :friend])
             (c/permissions :poster :post :read)))
      "add multiple permission")
  (is (= #{:own :friend}
         (-> (b/add-resource {} :post)
             (b/add-action :post [:read :write])
             (b/add-role :poster)
             (b/add-permission :poster :post :read :own)
             (b/add-permission :poster :post :read :friend)
             (c/permissions :poster :post :read)))
      "add permission multiple times")
  (is (thrown-with-msg? IllegalArgumentException
                        #"referred role does not exists"
                        (-> (b/add-resource {} :post)
                            (b/add-action :post [:read :write])
                            (b/add-permission :poster :post :read ::b/all)))
      "Missing role")
  (is (thrown-with-msg? IllegalArgumentException
                        #"referred action does not exists"
                        (-> (b/add-resource {} :post)
                            (b/add-action :post [:write])
                            (b/add-permission :poster :post :read ::b/all)))
      "Missing action")
  (is (thrown-with-msg? IllegalArgumentException
                        #"referred resource does not exists"
                        (b/add-permission {} :poster :post :read ::b/all))
      "Missing resource"))

(deftest delete-permission
  (is (= #{:friend}
         (-> (b/add-resource {} :post)
             (b/add-action :post [:read :write])
             (b/add-role :poster)
             (b/add-permission :poster :post :read [:own :friend])
             (b/delete-permission :poster :post :read :own)
             (c/permissions :poster :post :read)))
      "delete single permission")
  (is (= #{}
         (-> (b/add-resource {} :post)
             (b/add-action :post [:read :write])
             (b/add-role :poster)
             (b/add-permission :poster :post :read [:own :friend])
             (b/delete-permission :poster :post :read [:own :friend])
             (c/permissions :poster :post :read)))
      "delete multi permission")
  (is (thrown-with-msg? IllegalArgumentException
                        #"referred permission does not exists"
                        (-> (b/add-resource {} :post)
                            (b/add-action :post [:read :write])
                            (b/add-role :poster)
                            (b/add-permission :poster :post :read :own)
                            (b/delete-permission :poster :post :read [:own :friend])))
      "delete missing permission"))

(deftest permission-by-inheritance
  (let [role-set (-> (b/add-resource {} :post)
                     (b/add-action :post [:read :write])
                     (b/add-role :reader)
                     (b/add-role :poster)
                     (b/add-permission :reader :post :read [:own :friend])
                     (b/add-permission :poster :post :write :own)
                     (b/add-inheritance :poster :reader))]
    (is (= #{:own :friend}
           (c/permissions role-set :poster :post :read)))
    (is (= #{:own :friend}
           (c/permissions role-set :reader :post :read)))
    (is (= #{:own}
           (c/permissions role-set :poster :post :write)))
    (is (= #{}
           (c/permissions role-set :reader :post :write)))))

(deftest get-permission-via-map
  (let [role-set (-> (b/add-resource {} :post)
                     (b/add-action :post [:read :write])
                     (b/add-role :reader)
                     (b/add-role :poster)
                     (b/add-permission :reader :post :read [:own :friend])
                     (b/add-permission :poster :post :write :own)
                     (b/add-inheritance :poster :reader))]
    (is (= {::c/resources #{:post},
            ::c/actions   {:post #{:read :write}},
            ::c/inherits  {:poster #{:reader}}
            ::c/roles     {:reader {:post
                                    {:read
                                     #{:own :friend}}}
                           :poster {:post
                                    {:write
                                     #{:own}}}}}
           role-set))
    (is (= #{:own :friend}
           (c/permissions role-set {:role     :poster
                                    :resource :post
                                    :action   :read})))
    (is (= #{:own :friend}
           (c/permissions role-set {:role     :reader
                                    :resource :post
                                    :action   :read})))
    (is (= #{:own}
           (c/permissions role-set {:role     :poster
                                    :resource :post
                                    :action   :write})))
    (is (= #{}
           (c/permissions role-set {:role     :reader
                                    :resource :post
                                    :action   :write})))))

(deftest has-permission
  (let [role-set (-> (b/add-resource {} :post)
                     (b/add-action :post [:read :write])
                     (b/add-role :reader)
                     (b/add-role :poster)
                     (b/add-permission :reader :post :read [:own :friend])
                     (b/add-permission :poster :post :write :own)
                     (b/add-inheritance :poster :reader))]
    (is (true?
          (c/has-permission role-set :reader :post :read))
        "Have own permission")
    (is (false?
          (c/has-permission role-set :reader :post :write))
        "Doesn't have permission")
    (is (true?
          (c/has-permission role-set :poster :post :read))
        "Has inherited permission")
    (is (true?
          (c/has-permission role-set :poster :post :write))
        "Has own permission")
    (is (false?
          (c/has-permission role-set :lurker :post :write))
        "Doesn't have permission with invalid role")
    (is (false?
          (c/has-permission role-set :reader :comment :write))
        "Doesn't have permission on invalid resource")
    (is (false?
          (c/has-permission role-set :reader :post :tag))
        "Doesn't have permission for invalid action")))

(deftest building-role-set
  (let [expected {::c/resources #{:post},
                  ::c/actions   {:post #{:read :write}},
                  ::c/inherits  {:poster #{:reader}}
                  ::c/roles     {:reader {:post {:read #{:own :friend}}}
                                 :poster {:post {:write #{:own}}}}}]
    (is (= expected
           (-> (b/add-resource {} :post)
               (b/add-action :post [:read :write])
               (b/add-role :reader)
               (b/add-role :poster)
               (b/add-permission :reader :post :read [:own :friend])
               (b/add-permission :poster :post :write :own)
               (b/add-inheritance :poster :reader)))
        "Build by code")
    (is (= expected
           (b/init {::c/resources :post
                    ::c/actions   {:post [:read :write]}
                    ::c/inherits  {:poster :reader}
                    ::c/roles     {:reader {:post {:read [:own :friend]}}
                                   :poster {:post {:write :own}}}}))
        "Build from one map")
    (is (= expected
           (-> (b/init {::c/resources :post})
               (b/init {::c/actions {:post [:read :write]}})
               (b/init {::c/roles {:reader {:post {:read #{:own :friend}}}}})
               (b/init {::c/roles {:poster {:post {:write #{:own}}}}})
               (b/init {::c/inherits {:poster :reader}})))
        "Build from multiple maps")))

(deftest delete-resource-deletes-permissions
  (is (= {::c/resources #{},
          ::c/actions   {}
          ::c/roles     {:member {}}}
         (-> (b/add-resource {} :comment)
             (b/add-action :comment :read)
             (b/add-role :member)
             (b/add-permission :member :comment :read ::b/all)
             (b/delete-resource :comment)))
      "deleting resource removes permissions too"))

(deftest delete-action-deletes-permissions
  (is (= {::c/resources #{:comment}
          ::c/actions   {:comment #{:tag}}
          ::c/roles     {:guest {:comment {}}}}
         (-> (b/add-resource {} :comment)
             (b/add-action :comment [:read :tag])
             (b/add-role :guest)
             (b/add-permission :guest :comment :read ::b/all)
             (b/delete-action :comment :read)))
      "deleting action removes permissions"))

(deftest delete-role
  (is (= {::c/resources #{:comment}
          ::c/actions   {:comment #{:read :tag}}
          ::c/roles     {}}
         (-> (b/add-resource {} :comment)
             (b/add-action :comment [:read :tag])
             (b/add-role :guest)
             (b/delete-role :guest)))
      "deleting role removes it from role-set")
  (is (= {::c/resources #{:comment}
          ::c/actions   {:comment #{:read :tag}}
          ::c/inherits  {:admin #{}}
          ::c/roles     {:admin {}}}
         (-> (b/add-resource {} :comment)
             (b/add-action :comment [:read :tag])
             (b/add-role :guest)
             (b/add-inheritance :admin :guest)
             (b/delete-role :guest)))
      "deleting role removes it from inherits"))

(deftest delete-inheritance
  (is (= {::c/resources #{:comment}
          ::c/actions   {:comment #{:read :tag}}
          ::c/roles     {:guest {} :guest2 {} :admin {}}
          ::c/inherits  {:admin #{:guest2}}}
         (-> (b/add-resource {} :comment)
             (b/add-action :comment [:read :tag])
             (b/add-role [:guest :guest2])
             (b/add-inheritance :admin [:guest :guest2])
             (b/delete-inheritance :admin :guest)))
      "deleting inheritance removes it"))