(ns tiny-rbac.core)

(defn collify
  [x]
  (if (coll? x) x [x]))

(defn resources
  [role-set]
  (:resources role-set))

(defn resource
  [role-set resource]
  (get (resources role-set) resource))

(defn actions
  [role-set resource]
  (get-in role-set [:actions resource]))

(defn action
  [role-set resource action]
  (get (actions role-set resource) action))

(defn inherit
  [role-set role]
  (get-in role-set [:roles role :inherits]))

(defn roles
  [role-set]
  (:roles role-set))

(defn role
  [role-set role]
  (get (roles role-set) role))

(defn permissions
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
  ([role-set {:keys [role resource action permission]}]
   (permission role-set role resource action permission))
  ([role-set role resource action permission]
   (get (permissions role-set role resource action) permission)))

(defn has-permission
  ([role-set {:keys [role resource action]}]
   (has-permission role-set role resource action))
  ([role-set role resource action]
   (not (empty? (permissions role-set role resource action)))))