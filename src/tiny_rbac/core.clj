(ns tiny-rbac.core)

(defn- get-grantee
  [user-permissions {:keys [resource privilege]}]
  (some->> user-permissions
           (filter #(#{resource :all} (:resource %)))
           (filter #(some #{privilege :all} (:actions %)))
           first))

(defn- resolve-inherited
  [permissions {role :role :as access}]
  (let [user-permissions (get permissions role)
        test-permission (if (map? user-permissions)
                          (:permissions user-permissions)
                          user-permissions)
        granted (get-grantee test-permission access)
        inherited (:inherit user-permissions)]
    (cond
      granted granted
      inherited (resolve-inherited permissions (assoc access :role inherited))
      :else false)))

(defn- grantee
  [permissions {role :role :as access}]
  (let [user-permissions (get permissions role)]
    (if (map? user-permissions)
      (resolve-inherited permissions access)
      (get-grantee user-permissions access))))

(defn- user->access
  [user access]
  (cond
    (not (:is_active user)) (assoc access :role :guest)
    (:roles user) (assoc access :roles (:roles user))
    (:role user) (assoc access :role (:role user))
    (:is_superuser user) (assoc access :role :superuser)
    (:is_staff user) (assoc access :role :staff)
    (:is_active user) (assoc access :role :member)))

(defn- resolve-single-role
  [permissions access]
  (if-let [granted (grantee permissions access)]
    (:over granted)
    false))

(defn- resolve-multi-role
  [permissions access]
  (let [result-set (->> (for [role (:roles access)]
                          (grantee permissions (assoc access :role role)))
                        (map :over)
                        (remove nil?))]
    (cond
      (empty? result-set) false
      (apply = result-set) (first result-set)
      (some #{:all} result-set) :all
      :else (into #{} result-set))))

(defn has-access
  "Examine if the user is has access to a resource with the provided action.
  If it has, returns anything what is provided in ':acl/roles' corresponding ::over field.
  If isn't then returns \"false\"
  ':acl/roles' is a map keyed by name of :acl/roles.
  'user' is optional, but if it missing you must provide the 'role' field in action.
  'access' defines the role, resource and privilege what needs to be achieved.
  If user is provided, the role will be resolved from it."
  ([permissions user access]
   (if (or (:roles access) (:role access))
     (has-access permissions access)
     (has-access permissions (user->access user access))))
  ([permissions access]
   (if (:role access)
     (resolve-single-role permissions access)
     (resolve-multi-role permissions access))))

