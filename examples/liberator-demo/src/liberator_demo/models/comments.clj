(ns liberator-demo.models.comments
  (:require [liberator-demo.database :as db]))

(defn comments->>post
  [post]
  (assoc post :comments (db/fetch-comments-by-post-id (:id post))))
