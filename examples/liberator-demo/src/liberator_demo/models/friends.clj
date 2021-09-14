(ns liberator-demo.models.friends
  (:require [liberator-demo.database :as db]))

(defn get-friends-ids
  [user-id]
  (->> (db/fetch-all :friendships)
       (filter #(and
                  (or (= user-id (:who-id %))
                      (= user-id (:whom-id %)))
                  (= :active (:status %))))
       (map (fn [x] [(:who-id x) (:whom-id x)]))
       flatten))