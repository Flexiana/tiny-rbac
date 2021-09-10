# Tiny RBAC

> Small library for role based access control for clojure

## Table of Contents
* [General Info](#general-information)
* [Technologies Used](#technologies-used)
* [Features](#features)
* [Screenshots](#screenshots)
* [Setup](#setup)
* [Usage](#usage)
* [Project Status](#project-status)
* [Room for Improvement](#room-for-improvement)
* [Acknowledgements](#acknowledgements)
* [Contact](#contact)
<!-- * [License](#license) -->


## General Information
The library has two components 
- Builder
- Core

## Features
### Builder

With the builder you can define role set via

- single map
```clojure
(b/init {:resources :post
         :actions   {:post [:read :write]}
         :roles     {:reader {:permits {:post {:read [:own :friend]}}}
                     :poster {:permits  {:post {:write :own}}
                              :inherits :reader}}})
```
- multiple maps
```clojure
(-> (b/init {:resources :post})
    (b/init {:actions {:post [:read :write]}})
    (b/init {:roles {:reader {:permits {:post {:read #{:own :friend}}}}}})
    (b/init {:roles {:poster {:permits  {:post {:write #{:own}}}
                              :inherits #{:reader}}}}))
```
- code
```clojure
(-> (b/add-resource {} :post)
    (b/add-action :post [:read :write])
    (b/add-role :reader)
    (b/add-role :poster)
    (b/add-permission :reader :post :read [:own :friend])
    (b/add-permission :poster :post :write :own)
    (b/add-inheritance :poster :reader))
```

All examples above providing the same result.

### Core

With the core you can query for

- resource(s)
```clojure
(is (= #{:post}
       (c/resources {...})))

(is (= :post
       (c/resource {...} :post)))
```
- action(s)
```clojure
(is (= #{:read :write}
       (c/actions {...} :post)))

(is (= :read
       (c/action {...} :post :read)))
```
- role(s)
```clojure
(is (= #{:reader :poster}
       (c/roles {...})))

(is (= :reader
       (c/role {...} :reader)))
```
- inheritance
```clojure
(is (= #{:reader}
       (c/inherit {...} :poster)))

(is (= #{}
       (c/inherit {...} :reader)))
```
- permission(s)
```clojure
(is (= #{:own :friend}
       (c/permissions {...} :poster :post :read)))

(is (= :own
       (c/permission {...} :poster :post :read :own)))

(is (= true?
       (c/has-permission {...} :poster :post :write)))

(is (= false?
       (c/has-permission {...} :reader :post :write)))
```

## Setup 
### TODO
Add 
> [clojars link and version] 

to dependencies

## Usage

### Builder

#### Creating a role-set

```clojure
(:require 
  [tiny-rbac.builder :as b])

(def role-set
  (->
      ;; empty role-set
      {}
      
      ;; defining a new resource in the given role-set
      (b/add-resource :post)
      
      ;; defining actions for a resource
      ;; Throws an exception if the given resource not defined
      (b/add-action :post [:read :write])
      
      ;; add an action to the resource
      ;; Throws an exception if the given resource not defined
      (b/add-action :post :tag)
      
      ;; defines roles int the given role-set
      (b/add-role [:reader :guest])
      
      ;; adds role to the given role-set
      (b/add-role :poster)
      
      ;; creates :admin role which inherits its roles from :poster
      ;; Throws an exception if inherited role doesn't exists,
      ;; or in case of cyclic inheritance
      (b/add-inheritance :admin :poster)
      
      ;; Provides permission for :reader role, to :read :own and :friend's :posts
      ;; Throws an exception if
      ;;  :post resource not defined 
      ;;  :post resource has no :read action
      ;;  :reader role doesn't exists 
      (b/add-permission :reader :post :read [:own :friend])

      ;; Provides permission for :poster role, to :write :own :posts
      ;; Throws an exception if
      ;;  :post resource not defined 
      ;;  :post resource has no :write action
      ;;  :poster role doesn't exists 
      (b/add-permission :poster :post :write :own)
      
      ;; Adds :reader as inheritance to :poster
      ;; Throws an exception if inherited :reader role doesn't exists,
      ;; or in case of cyclic inheritance
      (b/add-inheritance :poster :reader)))
```

Every `add-*` functions are supporting bulk and single addition. 

```clojure
(is (= (-> ... 
           (b/add-action ... :read)
           (b/add-action .. :write))
       (b/add-action ... ... [:read :write])))
```

#### Tightening the role-set

```clojure
(:require
  [tiny-rbac.builder :as b])

(def role-set ...)

;; Remove resource(s), with all referring actions and permissions from role-set
;; Throws an Exception if the resource does not exists 
(b/delete-resource role-set :comment)
(b/delete-resource role-set [:post :comment])

;; Remove action(s) from role-set's resource, with all permissions on it
;; Throws an Exception if the resource or the action does not exists 
(b/delete-action role-set :resource :comment)
(b/delete-action role-set :resource [:post :comment])

;; Deleting role(s)
;; Removes role(s) from all inheritances
;; Throws an Exception if the role is missing
(b/delete-role role-set :lurker)                                 ;;TODO
(b/delete-role role-set [:guest :lurker])                        ;;TODO

;; Removing inheritance(s) from role
;; Throws an Exception if the role not inherited from given one
(b/delete-inheritance role-set :admin :lurker)                   ;;TODO
(b/delete-inheritance role-set :admin [:guest :lurker])          ;;TODO

;; Revoking permission(s) from role
;; Throws an Exception if resource, action, role or permission is missing
(b/delete-permission role-set :role :resource :action :permission)
(b/delete-permission role-set :role :resource :action [:permission-1 :permission-2])
```

Calling `delete-*` functions with `::b/all` removes all referred instance. Like:
```clojure
(delete-resource role-set :b/all)
```
results in role-set without resources, actions, or any permissions


### Core
```clojure
(:require 
  [tiny-rbac.builder :as b]
  [tiny-rbac.core :as c])
```



## Project Status
- _almost complete_; 
- see [TODOs](####Tightening-the-role-set)

## Room for Improvement

> To do:
>
> - Implement missing deletion commands
> - Release to clojars 
> - Examples and use cases would be nice to provide


## Acknowledgements
> - Many thanks to [Flexiana](https://flexiana.com/) for all the support.


## Contact
> Created by [@g-krisztian](https://github.com/g-krisztian) - feel free to contact me!
