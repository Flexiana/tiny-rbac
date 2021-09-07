(ns tiny-rbac.core)

(defn resources [roleset]
  (:resources roleset))

(defn resource [roleset resource]
  (->> (resources roleset)
       (filter (hash-set resource))
       first))

(defn actions [roleset resource]
  (get-in roleset [:actions resource]))

(defn action [roleset resource action]
  (-> roleset
      :actions
      (get resource)
      (get action)))

(defn inherit
  [roleset role]
  (get-in roleset [:roles role :inherits]))

(defn role [roleset role]
  (get-in roleset [:roles role]))

(defn access
  ([roleset role resource action]
   (get-in roleset [:roles role resource action])))