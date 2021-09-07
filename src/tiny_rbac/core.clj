(ns tiny-rbac.core)

(defn resources [roleset]
  (:resources roleset))

(defn resource [roleset resource]
  (get-in roleset [:resources resource]))

(defn actions [roleset resource]
  (get-in roleset [:actions resource]))

(defn action [roleset resource action]
  (get-in roleset [:actions resource action]))

(defn inherit
  [roleset role]
  (get-in roleset [:roles role :inherits]))

(defn role [roleset role]
  (get-in roleset [:roles role]))

(defn access
  ([roleset role resource action]
   (get-in roleset [:roles role resource action])))