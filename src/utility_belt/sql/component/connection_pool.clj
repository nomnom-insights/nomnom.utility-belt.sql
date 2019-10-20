(ns utility-belt.sql.component.connection-pool
  (:require [hikari-cp.core :as hikari]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

;; To be accepted as a db-spec (for JDBC)
;; component has to have a datasource key
(defrecord HikariCP [config datasource]
  component/Lifecycle
  (start [this]
    (log/infof "%s connecting=%s %s:%s"
               (:pool-name config)
               (:database-name config)
               (:server-name config)
               (:port-number config))
    ;; we can't pass arbitrary config so...
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
    (assoc this :datasource nil)))

(defn create [config]
  (map->HikariCP {:config config}))
