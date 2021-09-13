(ns tiny-rbac.builder
  (:require
    [tiny-rbac.core :as c]))

(defn- con-set
  [new orig]
  (into #{} (concat orig new)))

(defn- valid-resource
  [role-set resource]
  (let [resources (if (= ::all resource)
                    (c/resources role-set)
                    (c/collify resource))]
    (if (some nil? (map #(c/resource role-set %) resources))
      (throw (IllegalArgumentException. "referred resource does not exists"))
      resources)))

(defn- valid-action
  [role-set resource action]
  (let [actions (if (= ::all action)
                  (c/actions role-set resource)
                  (c/collify action))]
    (if (some nil? (map #(c/action role-set resource %) actions))
      (throw (IllegalArgumentException. "referred action does not exists"))
      actions)))

(defn- valid-role [role-set role]
  (let [roles (if (= ::all role)
                (c/roles role-set)
                (c/collify role))]
    (if (some nil? (map #(c/role role-set %) roles))
      (throw (IllegalArgumentException. "referred role does not exists"))
      roles)))

(defn- valid-permission [role-set role resource action permission]
  (let [permissions (if (= ::all permission)
                      (c/permissions role-set role resource action)
                      (c/collify permission))]
    (if (some nil? (map #(c/permission role-set role resource action %) permissions))
      (throw (IllegalArgumentException. "referred permission does not exists"))
      permissions)))

(defn- valid-cyclic-inheritance
  [role-set role inherits]
  (let [inheritances (into #{} (c/collify inherits))]
    (if (inheritances role)
      (throw (IllegalArgumentException. (str "Circular inheritance detected for " role)))
      (doseq [i inheritances]
        (when (c/inherit role-set i)
          (valid-cyclic-inheritance role-set role (c/inherit role-set i)))))))

(defn add-resource
  "Defines a new resource to the role-set"
  [role-set resources]
  (update role-set :resources con-set (c/collify resources)))

(defn delete-resource
  "Deletes a resource from the role-set, if it exist, else throws Exception.
  Removes all actions, and permissions on the given resource"
  [role-set resource]
  (let [resources (valid-resource role-set resource)]
    (reduce (fn [rs res]
              (cond->
                (update rs :resources disj res)
                (:actions rs) (update :actions dissoc res)
                (:roles rs) (update :roles
                                    (fn [role]
                                      (->> (map (fn [role]
                                                  (let [[r _] role]
                                                    (update (apply hash-map role) r dissoc res)))
                                                role)
                                           (into {}))))))
            role-set
            resources)))

(defn add-action
  "Defines a new action on resource
  Throws exception when the resource is missing "
  [role-set resource action]
  (valid-resource role-set resource)
  (update-in role-set [:actions resource] con-set (c/collify action)))

(defn remove-permit-action
  [permission resource action]
  (let [res (key permission)
        permit (val permission)]
    (if (= res resource)
      {res (dissoc permit action)}
      permission)))

(defn remove-role-permit-action
  [permits resource action]
  (into {} (map (fn [p] (remove-permit-action p resource action)) permits)))

(defn remove-roles-action
  [roles resource action]
  (into {} (map (fn [[role permissions]]
                  [role (remove-role-permit-action permissions resource action)]) roles)))

(defn delete-action
  "Deletes an action on resource.
  Throws Exception when the resource or the action is missing"
  [role-set resource action]
  (valid-resource role-set resource)
  (let [actions (valid-action role-set resource action)]
    (reduce (fn [rs ac]
              (cond-> (update-in rs [:actions resource] disj ac)
                      (:roles role-set) (update :roles remove-roles-action resource ac)))
            role-set
            actions)))

(defn add-role
  "Defines a new role to the role-set"
  [role-set role]
  (reduce (fn [rs r] (if-not
                       (get-in rs [:roles r])
                       (assoc-in rs [:roles r] {})
                       rs))
          role-set
          (c/collify role)))

(defn add-inheritance
  "Adds inheritance to a role.
  The parent role should exist, validates for cyclic inheritance.
  On error throws an Exception"
  [role-set role inherits]
  (valid-role role-set inherits)
  (valid-cyclic-inheritance role-set role inherits)
  (-> (update-in role-set [:inherits role] con-set (c/collify inherits))
      (add-role role)))

(defn add-permission
  "Provides a permission for a role.
  Validates for the resource, action and role.
  On error throws an Exception"
  [role-set role resource action permission]
  (valid-resource role-set resource)
  (valid-action role-set resource action)
  (valid-role role-set role)
  (update-in role-set [:roles role resource action] con-set (c/collify permission)))

(defn delete-permission
  "Revokes a permission for a user, for an action on resource
  Validates for the resource, action, role and permission.
  On error throws an Exception"
  [role-set role resource action permission]
  (valid-resource role-set resource)
  (valid-action role-set resource action)
  (valid-role role-set role)
  (let [acc (valid-permission role-set role resource action permission)]
    (reduce (fn [rs ac]
              (update-in rs [:roles role resource action] disj ac))
            role-set
            acc)))

(defn- permit-reducer
  [role-set role permits]
  (reduce (fn [rs [resource permit]]
            (reduce (fn [acc [action permission]]
                      (add-permission acc role resource action permission))
                    rs permit))
          role-set permits))


(defn init
  "Initialize and validates role-set from given map.
  If anything goes wrong throws Exception"
  ([role-set]
   (init {} role-set))
  ([initial-set {:keys [resources actions roles inherits]}]
   (cond-> initial-set
           resources (add-resource resources)
           actions (#(reduce (fn [acc [resource action]]
                               (add-action acc resource action))
                             % actions))
           roles (#(reduce (fn [acc [role permits]]
                             (cond-> (add-role acc role)
                                     permits (permit-reducer role permits)))
                           % roles))
           inherits (#(reduce (fn [rs [role i]]
                                  (add-inheritance rs role i))
                              % inherits)))))

(defn delete-role [role-set role]
  (let [roles (valid-role role-set role)]
    (println roles)
    (reduce (fn [rs r]
              (update rs :roles dissoc r))
            role-set roles)))

