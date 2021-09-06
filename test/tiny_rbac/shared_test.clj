(ns tiny-rbac.shared-test
  (:require [clojure.test :refer :all]
            [tiny-rbac.builder :as b]
            [tiny-rbac.core :as c]))

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
             (b/delete-resource :all)
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
  (is (= {:resources #{}, :actions {}}
         (-> (b/add-resource {} :comment)
             (b/add-action :comment :read)
             (b/delete-resource :comment)))
      "deleting resources removes actions too")
  (is (= {:resources #{}, :actions {}}
         (-> (b/add-resource {} [:comment :post])
             (b/add-action :comment :read)
             (b/add-action :post :read)
             (b/delete-resource :all)))
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
             (b/delete-action :comment :all)
             (c/actions :comment)))
      "deleting all actions"))

(deftest add-role
  (is (= {:roles {:poster {}}}
         (b/add-role {} :poster)))
  (is (= {:roles {:poster {}
                  :admin  {}}}
         (b/add-role {} [:poster :admin])))
  (is (= {:poster {:post #{:read}}}
         (-> (b/add-resource {} :post)
             (b/add-action :post [:read :write])
             (b/add-role :poster :post :read)
             :roles)))
  (is (= {:poster {:post #{:read :write}}}
         (-> (b/add-resource {} :post)
             (b/add-action :post [:read :write])
             (b/add-role :poster :post [:read :write])
             :roles)))
  (is (thrown-with-msg? IllegalArgumentException
                        #"referred action does not exists"
                        (-> (b/add-resource {} :post)
                            (b/add-role :poster :post :read))))
  (is (thrown-with-msg? IllegalArgumentException
                        #"referred resource does not exists"
                        (b/add-role {} :poster :post :read))))