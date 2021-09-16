(ns liberator-demo.models.posts
  (:require [liberator-demo.database :as db]))

(defn public-posts
  [posts]
  (filter #(= :public (:visible %)) posts))

(defn friends-posts
  [friends posts]
  (let [friends (into #{} friends)]
    (filter #(friends (:creator-id %)) posts)))

(defn own-posts
  [user-id posts]
  (filter #(= user-id (:creator-id %)) posts))

(defn visible-posts
  [posts permissions user-id friends]
  (let [friends (into #{} friends)
        visible-posts (for [p permissions]
                        (condp = p
                          :own (own-posts user-id posts)
                          :public (public-posts posts)
                          :friends (friends-posts friends posts)))]
    (->> visible-posts
         (mapcat identity)
         (into #{})
         (sort-by :created-at)
         reverse)))

(defn fetch-posts
  ([permissions user-id friends]
   (visible-posts (db/fetch-all :posts) permissions user-id friends))
  ([permissions user-id friends post-id]
   (visible-posts [(db/fetch-one :posts post-id)] permissions user-id friends)))


(defn new-post [user-id content visibility]
  (db/add-post {:creator-id user-id
                :visible visibility
                :content content}))

(defn update-post
  [post-update]
  (db/update-post post-update))

(defn delete-post [post]
  (db/delete-post post))


