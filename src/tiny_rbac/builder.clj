(ns tiny-rbac.builder
  (:require
    [tiny-rbac.core :as c]))

(defn con-set
  [new orig]
  (into #{} (concat orig new)))

(defn valid-resource
  [role-set resource]
  (let [resources (if (= :all resource)
                    (c/resources role-set)
                    (c/collify resource))]
    (if (some nil? (map #(c/resource role-set %) resources))
      (throw (IllegalArgumentException. "referred resource does not exists"))
      resources)))

(defn valid-action
  [role-set resource action]
  (let [actions (if (= :all action)
                  (c/actions role-set resource)
                  (c/collify action))]
    (if (some nil? (map #(c/action role-set resource %) actions))
      (throw (IllegalArgumentException. "referred action does not exists"))
      actions)))

(defn valid-role [role-set role]
  (when (some nil? (map #(c/role role-set %) (c/collify role)))
    (throw (IllegalArgumentException. "referred role does not exists"))))

(defn valid-permission [role-set role resource action permission]
  (let [permissions (if (= :all permission)
                      (c/permissions role-set role resource action)
                      (c/collify permission))]
    (if (some nil? (map #(c/permission role-set role resource action %) permissions))
      (throw (IllegalArgumentException. "referred permission does not exists"))
      permissions)))

(defn valid-cyclic-inheritance
  [role-set role inherits]
  (let [inheritances (into #{} (c/collify inherits))]
    (if (inheritances role)
      (throw (IllegalArgumentException. (str "Circular inheritance detected for " role)))
      (doseq [i inheritances]
        (when (c/inherit role-set i)
          (valid-cyclic-inheritance role-set role (c/inherit role-set i)))))))

(defn add-resource
  [role-set resources]
  (update role-set :resources con-set (c/collify resources)))

(defn delete-resource
  [role-set resource]
  (let [resources (valid-resource role-set resource)]
    (reduce (fn [rs res] (-> (update rs :resources disj res)
                             (update :actions dissoc res)))
            role-set
            resources)))

(defn add-action
  [role-set resource action]
  (valid-resource role-set resource)
  (update-in role-set [:actions resource] con-set (c/collify action)))

(defn delete-action [role-set resource action]
  (valid-resource role-set resource)
  (let [actions (valid-action role-set resource action)]
    (reduce (fn [rs ac]
              (update-in rs [:actions resource] disj ac))
            role-set
            actions)))

(defn add-role
  [role-set role]
  (reduce (fn [rs r] (if-not
                       (get-in rs [:roles r])
                       (assoc-in rs [:roles r] {})
                       rs))
          role-set
          (c/collify role)))

(defn add-inheritance
  [role-set role inherits]
  (valid-role role-set inherits)
  (valid-cyclic-inheritance role-set role inherits)
  (update-in role-set [:roles role :inherits] con-set (c/collify inherits)))

(defn add-permission
  [role-set role resource action permission]
  (valid-resource role-set resource)
  (valid-action role-set resource action)
  (valid-role role-set role)
  (update-in role-set [:roles role :permits resource action] con-set (c/collify permission)))

(defn delete-permission [role-set role resource action permission]
  (valid-resource role-set resource)
  (valid-action role-set resource action)
  (valid-role role-set role)
  (let [acc (valid-permission role-set role resource action permission)]
    (reduce (fn [rs ac]
              (update-in rs [:roles role :permits resource action] disj ac))
            role-set
            acc)))

