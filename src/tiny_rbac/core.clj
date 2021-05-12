(ns tiny-rbac.core)

(defn grantee
  [permissions {:keys [role resource privilege]}]
  (->> (get permissions role)
       (filter #(#{resource :all} (:resource %)))
       (filter #(some #{privilege :all} (:actions %)))
       first))

(defn has-access
  "Examine if the user is has access to a resource with the provided action.
  If it has, returns anything what is provided in ':acl/roles' corresponding ::over field.
  If isn't then returns \"false\"
  ':acl/roles' is a map keyed by name of :acl/roles.
  'user' is optional, but if it missing you must provide the 'role' field in action.
  'access' defines the role, resource and privilege what needs to be achieved.
  If user is provided, the role will be resolved from it."
  ([permissions user access]
   (cond
     (:roles access) (has-access permissions access)
     (:role access) (has-access permissions access)
     (not (:is_active user)) (has-access permissions (assoc access :role :guest))
     (:roles user) (has-access permissions (assoc access :roles (:roles user)))
     (:role user) (has-access permissions (assoc access :role (:role user)))
     (:is_superuser user) (has-access permissions (assoc access :role :superuser))
     (:is_staff user) (has-access permissions (assoc access :role :staff))
     (:is_active user) (has-access permissions (assoc access :role :member))))
  ([permissions access]
   (if (:role access)
     (if-let [granted (grantee permissions access)]
       (:over granted)
       false)
     (let [result-set (->> (for [role (:roles access)]
                             (grantee permissions (assoc access :role role)))
                           (map :over)
                           (remove nil?))]
       (cond
         (empty? result-set) false
         (apply = result-set) (first result-set)
         (some #{:all} result-set) :all
         :else (into #{} result-set))))))

