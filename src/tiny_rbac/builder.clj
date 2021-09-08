(ns tiny-rbac.builder
  (:require
    [tiny-rbac.core :as c]))

(defn con-set
  [new orig]
  (into #{} (concat orig new)))

(defn valid-resource
  [roleset resource]
  (let [resources (if (= :all resource)
                    (c/resources roleset)
                    (c/collify resource))]
    (if (some nil? (map #(c/resource roleset %) resources))
      (throw (IllegalArgumentException. "referred resource does not exists"))
      resources)))

(defn valid-action
  [roleset resource action]
  (let [actions (if (= :all action)
                  (c/actions roleset resource)
                  (c/collify action))]
    (if (some nil? (map #(c/action roleset resource %) actions))
      (throw (IllegalArgumentException. "referred action does not exists"))
      actions)))

(defn valid-role [roleset role]
  (when (some nil? (map #(c/role roleset %) (c/collify role)))
    (throw (IllegalArgumentException. "referred role does not exists"))))

(defn valid-permission [roleset role resource action permission]
  (let [permissions (if (= :all permission)
                      (c/permissions roleset role resource action)
                      (c/collify permission))]
    (if (some nil? (map #(c/permission roleset role resource action %) permissions))
      (throw (IllegalArgumentException. "referred permission does not exists"))
      permissions)))

(defn valid-cyclic-inheritance
  [roleset role inherits]
  (let [inheritances (into #{} (c/collify inherits))]
    (if (inheritances role)
      (throw (IllegalArgumentException. (str "Circular inheritance detected for " role)))
      (doseq [i inheritances]
        (when (c/inherit roleset i)
          (valid-cyclic-inheritance roleset role (c/inherit roleset i)))))))

(defn add-resource
  [roleset resources]
  (update roleset :resources con-set (c/collify resources)))

(defn delete-resource
  [roleset resource]
  (let [resources (valid-resource roleset resource)]
    (reduce (fn [rs res] (-> (update rs :resources disj res)
                             (update :actions dissoc res)))
            roleset
            resources)))

(defn add-action
  [roleset resource action]
  (valid-resource roleset resource)
  (update-in roleset [:actions resource] con-set (c/collify action)))

(defn delete-action [roleset resource action]
  (valid-resource roleset resource)
  (let [actions (valid-action roleset resource action)]
    (reduce (fn [rs ac]
              (update-in rs [:actions resource] disj ac))
            roleset
            actions)))

(defn add-role
  [roleset role]
  (reduce (fn [rs r] (if-not
                       (get-in rs [:roles r])
                       (assoc-in rs [:roles r] {})
                       rs))
          roleset
          (c/collify role)))

(defn add-inheritance
  [roleset role inherits]
  (valid-role roleset inherits)
  (valid-cyclic-inheritance roleset role inherits)
  (update-in roleset [:roles role :inherits] con-set (c/collify inherits)))

(defn add-permission
  [roleset role resource action permission]
  (valid-resource roleset resource)
  (valid-action roleset resource action)
  (valid-role roleset role)
  (update-in roleset [:roles role :permits resource action] con-set (c/collify permission)))

(defn delete-permission [roleset role resource action permission]
  (valid-resource roleset resource)
  (valid-action roleset resource action)
  (valid-role roleset role)
  (let [acc (valid-permission roleset role resource action permission)]
    (reduce (fn [rs ac]
              (update-in rs [:roles role :permits resource action] disj ac))
            roleset
            acc)))

