(ns liberator-demo.models.users
  (:require [liberator-demo.database :as db]))

(defn fetch-user
  [user-id]
  (db/fetch-one :users user-id))