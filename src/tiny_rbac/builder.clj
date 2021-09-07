(ns tiny-rbac.builder
  (:require
    [tiny-rbac.core :as c]))

(defn collify
  [x]
  (if (and (not (map? x)) (coll? x)) x [x]))

(defn con-set
  [new orig]
  (into #{} (concat orig new)))

(defn add-resource
  [roleset resources]
  (update roleset :resources con-set (collify resources)))

(defn delete-resource
  [roleset resource]
  (let [resources (if (= :all resource)
                    (c/resources roleset)
                    (collify resource))]
    (if (some nil? (map #(c/resource roleset %) resources))
      (throw (IllegalArgumentException. "referred resource does not exists"))
      (reduce (fn [rs res]
                (-> (update rs :resources disj res)
                    (update :actions dissoc res)))
              roleset
              resources))))

(defn add-action
  [roleset resource action]
  (if (c/resource roleset resource)
    (update-in roleset [:actions resource] con-set (collify action))
    (throw (IllegalArgumentException. "referred resource does not exists"))))

(defn delete-action [roleset resource action]
  (let [actions (if (= :all action)
                  (c/actions roleset resource)
                  (collify action))]
    (when-not (c/resource roleset resource)
      (throw (IllegalArgumentException. "referred resource does not exists")))
    (when (some nil? (map #(c/action roleset resource %) actions))
      (throw (IllegalArgumentException. "referred action does not exists")))
    (reduce (fn [rs ac]
              (update-in rs [:actions resource] disj ac))
            roleset
            actions)))

(defn add-role
  [roleset role]
  (let [roles (collify role)]
    (reduce (fn [rs r]
              (if-not (get-in rs [:roles r])
                (assoc-in rs [:roles r] {})
                rs))
            roleset
            roles)))

(defn check-cyclic-inheritance
  [roleset role inherits]
  (let [inheritances (into #{} (collify inherits))]
    (if (inheritances role)
      (throw (IllegalArgumentException. (str "Circular inheritance detected for " role)))
      (doseq [i inheritances]
        (when (c/inherit roleset i)
          (check-cyclic-inheritance roleset role (c/inherit roleset i)))))))

(defn add-inheritance
  [roleset role inherits]
  (when (some nil? (map #(c/role roleset %) (collify inherits)))
    (throw (IllegalArgumentException. "referred role does not exists")))
  (check-cyclic-inheritance roleset role inherits)
  (let [inheritances (collify inherits)]
    (update-in roleset [:roles role :inherits] con-set inheritances)))

(defn add-access
  [roleset role resource action access]
  (when-not (c/resource roleset resource)
    (throw (IllegalArgumentException. "referred resource does not exists")))
  (when-not (c/action roleset resource action)
      (throw (IllegalArgumentException. "referred action does not exists")))

  (when-not (c/role roleset role)
    (throw (IllegalArgumentException. "referred role does not exists")))
  (let [acc (collify access)]
    (update-in roleset [:roles role resource action] con-set acc)))


