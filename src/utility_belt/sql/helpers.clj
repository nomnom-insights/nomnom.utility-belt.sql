(ns utility-belt.sql.helpers
  (:require [clojure.java.jdbc :as jdbc]))

(defmacro with-transaction
  "Wrapper around jdbc transaction macro"
  [binding & body]
  `(jdbc/with-db-transaction ~binding ~@body))
