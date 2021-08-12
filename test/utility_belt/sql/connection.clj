(ns utility-belt.sql.connection
  (:require [utility-belt.sql.component.connection-pool :as cp]))

(def connection-spec
  {:pool-name  "test"
   :adapter "postgresql"
   :username (or (System/getenv "PG_USER") "test")
   :password (or (System/getenv "PG_PASSWORD") "password")
   :server-name  (or (System/getenv "PG_HOST") "127.0.0.1")
   :port-number (Integer/parseInt (or (System/getenv "PG_PORT") "5434"))
   :maximum-pool-size 10
   :database-name (or (System/getenv "PG_NAME") "test")})

(defn start! [conn-atom]
  (reset! conn-atom (.start (cp/create connection-spec))))

(defn stop! [conn-atom]
  (.stop @conn-atom))
