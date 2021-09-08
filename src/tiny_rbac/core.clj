(ns tiny-rbac.core)

(defn collify
  [x]
  (if (coll? x) x [x]))

(defn resources
  [roleset]
  (:resources roleset))

(defn resource
  [roleset resource]
  (get (resources roleset) resource))

(defn actions
  [roleset resource]
  (get-in roleset [:actions resource]))

(defn action
  [roleset resource action]
  (get (actions roleset resource) action))

(defn inherit
  [roleset role]
  (get-in roleset [:roles role :inherits]))

(defn roles
  [roleset]
  (:roles roleset))

(defn role
  [roleset role]
  (get (roles roleset) role))

(defn permissions
  ([roleset {:keys [role resource action]}]
   (permissions roleset role resource action #{}))
  ([roleset role resource action]
   (permissions roleset role resource action #{}))
  ([roleset role resource action acc]
   (->> (let [inherit (inherit roleset role)]
          (cond-> (into acc (collify (get-in roleset [:roles role :permits resource action])))
                  inherit (into (mapcat identity
                                        (for [i (collify inherit)]
                                          (permissions roleset i resource action acc))))))
        (filter some?)
        (into #{}))))

(defn permission
  ([roleset {:keys [role resource action permission]}]
   (permission roleset role resource action permission))
  ([roleset role resource action permission]
   (get (permissions roleset role resource action) permission)))

(defn has-permission
  ([roleset {:keys [role resource action]}]
   (has-permission roleset role resource action))
  ([roleset role resource action]
   (not-empty (permissions roleset role resource action))))