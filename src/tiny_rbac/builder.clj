(ns tiny-rbac.builder
  (:require
    [tiny-rbac.builder.permissions :as abp]
    [tiny-rbac.builder.roles :as abr]))

(defn init
  [this config]
  (->
    this
    (abp/init (:acl/permissions config))
    (abr/init (:acl/roles config))))
