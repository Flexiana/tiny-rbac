(ns tiny-rbac.builder
  (:require
    [tiny-rbac.core :as c]))

(defn collify
  [x]
  (if (and (not (map? x)) (coll? x)) x [x]))

(defn con-set
  [new orig]
  (into #{} (concat orig new)))

(defn check-resource
  [roleset resource]
  (let [resources (if (= :all resource)
                    (c/resources roleset)
                    (collify resource))]
    (if (some nil? (map #(c/resource roleset %) resources))
      (throw (IllegalArgumentException. "referred resource does not exists"))
      resources)))

(defn check-action
  [roleset resource action]
  (let [actions (if (= :all action)
                  (c/actions roleset resource)
                  (collify action))]
    (if (some nil? (map #(c/action roleset resource %) actions))
      (throw (IllegalArgumentException. "referred action does not exists"))
      actions)))

(defn check-role [roleset role]
  (when (some nil? (map #(c/role roleset %) (collify role)))
    (throw (IllegalArgumentException. "referred role does not exists"))))

(defn check-cyclic-inheritance
  [roleset role inherits]
  (let [inheritances (into #{} (collify inherits))]
    (if (inheritances role)
      (throw (IllegalArgumentException. (str "Circular inheritance detected for " role)))
      (doseq [i inheritances]
        (when (c/inherit roleset i)
          (check-cyclic-inheritance roleset role (c/inherit roleset i)))))))

(defn add-resource
  [roleset resources]
  (update roleset :resources con-set (collify resources)))

(defn delete-resource
  [roleset resource]
  (let [resources (check-resource roleset resource)]
    (reduce (fn [rs res] (-> (update rs :resources disj res)
                             (update :actions dissoc res)))
            roleset
            resources)))

(defn add-action
  [roleset resource action]
  (check-resource roleset resource)
  (update-in roleset [:actions resource] con-set (collify action)))

(defn delete-action [roleset resource action]
  (check-resource roleset resource)
  (let [actions (check-action roleset resource action)]
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
          (collify role)))

(defn add-inheritance
  [roleset role inherits]
  (check-role roleset inherits)
  (check-cyclic-inheritance roleset role inherits)
  (update-in roleset [:roles role :inherits] con-set (collify inherits)))

(defn add-access
  [roleset role resource action access]
  (check-resource roleset resource)
  (check-action roleset resource action)
  (check-role roleset role)
  (update-in roleset [:roles role resource action] con-set (collify access)))



(defn check-access [roleset role resource action access]
  (let [accesses (if (= :all access)
                   (c/accesses roleset role resource action)
                   (collify access))]
    (if (some nil? (map #(c/access roleset role resource action %) accesses))
      (throw (IllegalArgumentException. "referred action does not exists"))
      accesses)))


(defn delete-access [roleset role resource action access]
  (check-resource roleset resource)
  (check-action roleset resource action)
  (check-role roleset role)
  (let [acc (check-access roleset role resource action access)]
    (reduce (fn [rs ac]
              (update-in rs [:roles role resource action] disj ac))
            roleset
            acc)))

