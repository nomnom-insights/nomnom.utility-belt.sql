(ns utility-belt.sql.helpers
  (:require [next.jdbc]))

(defmacro with-transaction
  "Wrapper around jdbc transaction macro. Cleans up imports basically"
  [binding & body]
  `(next.jdbc/with-transaction ~binding ~@body))

(defn execute [connection statement]
  {:pre [(vector? statement)]}
  (next.jdbc/execute! connection statement))
