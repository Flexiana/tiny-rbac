(ns tiny-rbac.core)

(defn resources [roleset]
  (:resources roleset))

(defn resource [roleset resource]
  (get (resources roleset) resource))

(defn actions [roleset resource]
  (get-in roleset [:actions resource]))

(defn action [roleset resource action]
  (get (actions roleset resource) action))

(defn inherit
  [roleset role]
  (get-in roleset [:roles role :inherits]))

(defn roles
  [roleset]
  (:roles roleset))

(defn role [roleset role]
  (get (roles roleset) role))

(defn accesses
  ([roleset role resource action]
   (get-in roleset [:roles role resource action])))

(defn access
  ([roleset role resource action access]
   (get (accesses roleset role resource action) access)))