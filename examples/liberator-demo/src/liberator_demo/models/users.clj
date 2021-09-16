(ns liberator-demo.models.users
  (:require [liberator-demo.database :as db]))

(defn fetch-user
  [user-id]
  (db/fetch-one :users user-id))

(defn get-friends-ids
  [user-id]
  (disj (->> (db/fetch-all :friendships)
             (filter #(and
                        (or (= user-id (:who-id %))
                            (= user-id (:whom-id %)))
                        (= :active (:status %))))
             (map (fn [x] [(:who-id x) (:whom-id x)]))
             flatten
             (into #{}))
        user-id))