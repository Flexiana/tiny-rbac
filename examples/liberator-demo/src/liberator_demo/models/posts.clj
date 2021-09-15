(ns liberator-demo.models.posts
  (:require [liberator-demo.database :as db]))

(defn fetch-posts
  [permissions friends post-id]
  (let [posts (if (= :all post-id)
                (db/fetch-all :posts)
                [(db/fetch-one :posts (str post-id))])
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
         (sort-by :created-at))))

(defn new-post [user content visibility]
  (db/add-post {:creator-id (:id user)
                :visible visibility
                :content content}))
