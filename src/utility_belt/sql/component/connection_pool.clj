(ns utility-belt.sql.component.connection-pool
  (:require
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [hikari-cp.core :as hikari]
    [next.jdbc.protocols :as jdbc.protocols]))


(defrecord ConnectionPool [config datasource]
  component/Lifecycle
  (start [this]
    (log/infof "%s connecting=%s %s:%s"
               (:pool-name config)
               (:database-name config)
               (:server-name config)
               (:port-number config))
    (let [pool (hikari/make-datasource config)]
      (assoc this :datasource pool)))
  (stop [this]
    (log/warnf "%s disconnecting=%s %s:%s"
               (:pool-name config)
               (:database-name config)
               (:server-name config)
               (:port-number config))
    (when datasource
      (hikari/close-datasource datasource))
    (assoc this :datasource nil))
  jdbc.protocols/Connectable
  (get-connection [this opts]
    (:datasource this))
  jdbc.protocols/Sourceable
  (get-datasource [this]
    (:datasource this)))


(defn create [config]
  (map->ConnectionPool {:config config}))
