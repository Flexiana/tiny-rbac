;(ns tiny-rbac.builder.permissions-test
;  (:require
;    [clojure.test :refer :all]
;    [tiny-rbac.builder :refer [add-actions
;                               override-actions
;                               remove-resource]]))
;
;(defn test-permissions
;  [expected actual]
;  (is (= expected (-> actual
;                      :right
;                      :acl/available-permissions))))
;
;(deftest permissions-builder
;  (test-permissions {"posts" [:send :update :delete :read], "comments" [:send]}
;                    (-> {}
;                      (add-actions {"posts" :read})
;                      (add-actions {"posts" :delete})
;                      (add-actions {"posts" :update})
;                      (add-actions {"posts" :send})
;                      (add-actions {"comments" :send})
;                      (add-actions {"comments" :send})
;                      (add-actions {"comments" :send})))
;  (test-permissions {"comments" [:send]}
;                    (->
;                      {}
;                      (add-actions {"posts" :read})
;                      (add-actions {"posts" :delete})
;                      (add-actions {"posts" :update})
;                      (add-actions {"posts" :send})
;                      (add-actions {"comments" :send})
;                      (add-actions {"comments" :send})
;                      (add-actions {"comments" :send})
;                      (remove-resource "posts")))
;  (test-permissions {"posts" [:blow-up], "comments" [:send]}
;                    (->
;                      {}
;                      (add-actions {"posts" :read})
;                      (add-actions {"posts" :delete})
;                      (add-actions {"posts" :update})
;                      (add-actions {"posts" :send})
;                      (add-actions {"comments" :send})
;                      (add-actions {"comments" :send})
;                      (add-actions {"comments" :send})
;                      (override-actions {"posts" :blow-up}))))
