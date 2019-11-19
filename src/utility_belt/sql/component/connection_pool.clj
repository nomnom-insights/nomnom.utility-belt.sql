(ns utility-belt.sql.component.connection-pool
  (:require [hikari-cp.core :as hikari]
            [clojure.tools.logging :as log]
            [next.jdbc.protocols :as jdbc.protocols]
            [com.stuartsierra.component :as component]))

(defrecord HikariCP [config pool]
  component/Lifecycle
  (start [this]
    (log/infof "%s connecting=%s %s:%s"
               (:pool-name config)
               (:database-name config)
               (:server-name config)
               (:port-number config))
      (let [pool (hikari/make-datasource config)]
      (assoc this :pool pool)))
  (stop [this]
    (log/warnf "%s disconnecting=%s %s:%s"
               (:pool-name config)
               (:database-name config)
               (:server-name config)
               (:port-number config))
    (when pool
      (hikari/close-datasource pool))
    (assoc this :pool nil))
  jdbc.protocols/Connectable
  (get-connection [this opts]
    (:pool this))
  jdbc.protocols/Sourceable
  (get-datasource [this]
    (:pool this))
  )

(defn create [config]
  (map->HikariCP {:config config}))
