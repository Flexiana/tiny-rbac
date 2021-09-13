(ns tiny-rbac.core)

(defn collify
  [x]
  (if (coll? x) x [x]))

(defn resources
  "Get all resources from the role-set"
  [role-set]
  (:resources role-set #{}))

(defn resource
  "Get given resource from the role-set"
  [role-set resource]
  (get (resources role-set) resource))

(defn actions
  "Get all actions for given resource"
  [role-set resource]
  (get-in role-set [:actions resource] #{}))

(defn action
  "Get given action for resource"
  [role-set resource action]
  (get (actions role-set resource) action))

(defn inherit
  "Get role parents"
  [role-set role]
  (get-in role-set [:inherits role] #{}))

(defn roles
  "Get all roles from the role-set"
  [role-set]
  (:roles role-set #{}))

(defn role
  "Get given role from the role-set"
  [role-set role]
  (get (roles role-set) role))

(defn permissions
  "Get permissions provided for a role for a resource and action"
  ([role-set {:keys [role resource action]}]
   (permissions role-set role resource action #{}))
  ([role-set role resource action]
   (permissions role-set role resource action #{}))
  ([role-set role resource action acc]
   (->> (let [inherit (inherit role-set role)]
          (cond-> (into acc (collify (get-in role-set [:roles role :permits resource action])))
                  inherit (into (mapcat identity
                                        (for [i (collify inherit)]
                                          (permissions role-set i resource action acc))))))
        (filter some?)
        (into #{}))))

(defn permission
  "Get given permission for a role for a resource and action"
  ([role-set {:keys [role resource action permission]}]
   (permission role-set role resource action permission))
  ([role-set role resource action permission]
   (get (permissions role-set role resource action) permission)))

(defn has-permission
  "Checks if a role has any permission for a resource and action"
  ([role-set {:keys [role resource action]}]
   (has-permission role-set role resource action))
  ([role-set role resource action]
   (not (empty? (permissions role-set role resource action)))))