(ns liberator-demo.models.posts
  (:require [liberator-demo.database :as db]))

(defn fetch-posts
  ([post-id]
   (if (= "all" post-id)
     (db/fetch-all :posts)
     (db/fetch-one :posts post-id)))
  ([permissions friends post-id]
   (let [posts (if (= "all" post-id)
                 (db/fetch-all :posts)
                 (db/fetch-one :posts post-id))
         permissions (conj permissions :friends)
         friends (into #{} friends)
         public-posts (filter #(= :public (:visible %)) posts)
         visible-posts (for [p permissions]
                         (condp = p
                           :all public-posts
                           :friends (filter #(friends (:creator-id %)) posts)))]
     (->> visible-posts
          (mapcat identity)
          dedupe
          (sort-by :created-at)))))