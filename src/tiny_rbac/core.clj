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

(defn accesses
  [roleset role resource action]
  (->> (collify role)
       (reduce (fn [acc role]
                 (let [inherit (inherit roleset role)]
                   (cond-> (into acc (collify (get-in roleset [:roles role resource action])))
                           inherit (into (mapcat identity
                                                 (for [i (collify inherit)]
                                                   (accesses roleset i resource action)))))))
               #{})
       (filter some?)
       (into #{})))

(defn access
  [roleset role resource action access]
  (get (accesses roleset role resource action) access))