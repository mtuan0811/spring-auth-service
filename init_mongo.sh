kubectl exec -it --namespace=dev mongodb-57f5964f67-9htp8 -- bash
mongosh "mongodb://mongodb.dev.svc.cluster.local:27017" --username root --authenticationDatabase admin

use dbmain
db.createCollection("accounts")
db.createCollection("roles")
db.createCollection("refreshToken")

db.roles.insert({"name": "ROLE_USER"},{"name": "ROLE_MODERATOR"},{"name": "ROLE_ADMIN"})

kubectl exec -it --namespace=dev redis-master-0 -- bash
redis-cli -h redis-master-0.redis-headless.dev.svc.cluster.local -p 6379
