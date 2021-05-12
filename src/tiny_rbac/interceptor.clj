(ns tiny-rbac.interceptor
  (:require
    [tiny-rbac.adapters.reitit :as acl]))

(defn acl-restrict
  ([]
   (acl-restrict {}))
  ([m]
   {:enter (fn [{acl :acl/access-map :as ctx}]
             (acl/is-allowed ctx (merge m acl)))
    :leave (fn [{query                 :query
                 {{user-id :id} :user} :session-data
                 owner-fn              :owner-fn
                 :as                   state}]
             (if owner-fn
               (assoc state :query (owner-fn query user-id))
               state))}))

