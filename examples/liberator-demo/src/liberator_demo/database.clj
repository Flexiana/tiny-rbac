(ns liberator-demo.database
  (:require [tick.core :as t]))

(defonce _db (atom {:users       [{:id   0
                                   :name "James Bond"
                                   :role :poster}
                                  {:id   1
                                   :name "John Doe"
                                   :role :poster}
                                  {:id   2
                                   :name "Tom Doe"
                                   :role :poster}
                                  {:id   3
                                   :name "Biggus Dickus"
                                   :role :poster}
                                  {:id   4
                                   :name "Clement Mason"
                                   :role :only-friends}]
                    :friendships [{:who-id  0
                                   :whom-id 1
                                   :status  :active}
                                  {:who-id  2
                                   :whom-id 1
                                   :status  :pending}
                                  {:who-id  4
                                   :whom-id 1
                                   :status  :active}
                                  {:who-id  0
                                   :whom-id 2
                                   :status  :active}]
                    :posts       [{:id         0
                                   :creator-id 1
                                   :content    "I thought Christmas only comes once a year"
                                   :created-at 1631614711
                                   :visible    :friends}
                                  {:id         1
                                   :creator-id 3
                                   :content    "Incontinentia! please forgive me!"
                                   :created-at 1631615592
                                   :visible    :public}
                                  {:id         2
                                   :creator-id 2
                                   :content    "Just hangin' all day with @dick & @harry"
                                   :created-at 1631615592
                                   :visible    :public}]
                    :comments    [{:id         0
                                   :creator-id 0
                                   :post-id    0
                                   :content    "Was that a Christmas joke?"
                                   :created-at 1631693159}]}))

(defn fetch-one
  [resource id]
  (some->> (get @_db resource)
           (filter #(= id (str (:id %))))
           first))

(defn fetch-all
  [resource]
  (get @_db resource))

(defn fetch-comments-by-post-id
  [post-id]
  (->> (:comments @_db)
       (filter #(= post-id (:post-id %)))
       (sort-by :created-at)))

(defn next-id
  [resource]
  (->> (get @_db resource)
       (map :id)
       (apply max)
       inc))

(defn add-post
  [post]
  (let [new-post (assoc post :created-at (t/long (t/now)) :id (next-id :posts))]
    (swap! _db update :posts conj new-post)))

(defn- update
  [c e]
  (-> (remove #(= (:id e) (:id %)) c)
      (conj e)))

(defn update-post [post-update]
  (swap! _db update :posts update post-update))





