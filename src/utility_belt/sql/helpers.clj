(ns utility-belt.sql.helpers
  (:require [next.jdbc]))

(defmacro with-transaction
  "Wrapper around jdbc transaction macro"
  [binding & body]
  `(next.jdbc/with-transaction ~binding ~@body))
