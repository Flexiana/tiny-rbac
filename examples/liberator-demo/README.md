# liberator-demo tor tiny RBAC

Example project to demonstrate tiny-RBAC library capabilities

## Usage

Start the application with
> * lein ring server
>* open http://localhost:3000/

## Description

### Resources

The example has two resources:

* posts
* comments

### Roles

#### Posts

| Role | can read post | can create post | update post | delete post | 
|------------------|--------------------|-----|-----|-----|
| **lurker**       | public             | No  | -   | -   |
| **poster**       | public own friends | Yes | own | own |
| **friends-only** | own friends        | No  | -   | -   |

#### Comments

_**Comment actions aren't implemented yet**_

| Role | can read comment | can create comment | update comment | delete comment | 
|------------------|-------------------|-----------------|-----|-----|
| **lurker**       | for visible posts | No              | -   | -   |
| **poster**       | for visible posts | on friends post | own | own |
| **friends-only** | for visible posts | No              | -   | -   |

### Users

| id | name | role |
|---|----------------|--------------|
| 0 | James Bond     | poster       |
| 1 | John Doe       | poster       |
| 2 | Tom Doe        | poster       |
| 3 | Biggus Dickus  | poster       |
| 4 | Clement Manson | only-friends |

### Friendships

| id | 0 | 1 | 2 | 3 | 4 | 
|----|---|---|---|---|---|
| 0  |   | X | X |   |   |
| 1  | X |   |   |   | X |
| 2  | X |   |   |   |   |
| 3  |   |   |   |   |   |
| 4  |   | X |   |   |   |



## Testing with postman

## License

Copyright © 2021 [g-krisztian](https://github.com/g-krisztian)

This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0 which
is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary Licenses when the conditions for such
availability set forth in the Eclipse Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your option) any later version, with the GNU
Classpath Exception which is available at https://www.gnu.org/software/classpath/license.html.
