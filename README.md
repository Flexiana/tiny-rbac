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

With the builder you can define role set with

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
```



## Setup 
### TODO
Add 
> [clojars link and version] 

to dependencies

## Usage

```clojure
(:require 
  [tiny-rbac.builder :as b]
  [tiny-rbac.core :as c])
```

## Project Status
> _complete_  

## Room for Improvement

> To do:
>
> - Release to clojars 
> - Examples and use cases would be nice to provide


## Acknowledgements
> - Many thanks to [Flexiana](https://flexiana.com/) for all the support.


## Contact
> Created by [@g-krisztian](https://github.com/g-krisztian) - feel free to contact me!
