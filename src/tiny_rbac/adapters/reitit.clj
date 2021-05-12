(ns tiny-rbac.adapters.reitit
  (:require
    [clojure.string :as str]
    [tiny-rbac.core :refer [has-access]]))

(def action-mapping
  {:get    :read
   :post   :create
   :put    :update
   :delete :delete})

(defn- ->resource
  ([uri prefix]
   (->> (if prefix (str/replace uri (re-pattern prefix) "") uri)
        (re-find #"\w+")))
  ([uri]
   (re-find #"\w+" uri)))

(defn- user->role
  [u]
  (when
    (or (:users/is_active u) (:is_active u))
    (or (:role u) (:users/role u))))

(defn is-allowed
  "Checks if the user is able to do an action on a resource.
  Returns xiana/ok when it is, and extends [:response-data :acl] with the :over of ownership check.
  When the user has no access, returns xiana/error or executes ((:or-else access) ctx) if it's provided.
  If any key is missing from 'access' it's resolved like:
  - role from user
  - resource from URI (/users/ -> \"users\")
  - and privilege from request method:

  |req:    | action: |
  |------- |---------|
  |:get    | :read   |
  |:post   | :create |
  |:put    | :update |
  |:delete | :delete |"
  ([{{user :user}             :session-data
     roles                    :acl/roles
     {method :request-method} :request
     :as                      ctx}
    {:keys [role privilege resource prefix] :as access}]
   (let [pr (or privilege (action-mapping method))
         res (name (or resource (->resource (get-in ctx [:request :uri]) prefix)))
         role (keyword (or role (user->role user)))
         result (has-access roles user {:resource  res
                                        :role      role
                                        :privilege pr})]
     (cond result (-> (assoc-in ctx [:response-data :acl] result)
                      (assoc-in [:response-data :acl-resource] (keyword res)))
           (:or-else access) ((:or-else access) ctx)
           :else (assoc ctx :response {:status 401 :body "Authorization error"}))))
  ([{{user :user} :session-data http-request :request :as ctx}]
   (let [resource (->resource (:uri http-request))
         privilege (action-mapping (:request-method http-request))]
     (is-allowed ctx {:resource resource :privilege privilege :role (:role user)}))))
