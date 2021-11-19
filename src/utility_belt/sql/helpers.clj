(ns utility-belt.sql.helpers
  (:require
    [next.jdbc]
    [utility-belt.sql.model :as model]))


(defmacro with-transaction
  "Wrapper around jdbc transaction macro. Cleans up imports basically"
  [binding & body]
  `(next.jdbc/with-transaction ~binding ~@body))


(defn execute
  ([connection statement]
   (execute connection statement {:mode :kebab-maps}))
  ([connection statement {:keys [mode]}]
   (next.jdbc/execute! connection statement
                       (get model/modes mode))))


(defn transaction?
  "Detects if passed in data source is a transaction or not.
  NOTE: this only works with HikariCP managed connections/transactions and the connection pool
  defined in utility-belt.sql.component.connection-pool"
  [tx-or-conn]
  (-> tx-or-conn bean :transactionIsolation number?))
