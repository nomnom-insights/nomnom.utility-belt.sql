(ns utility-belt.sql.helpers
  (:require [next.jdbc]))

(defmacro with-transaction
  "Wrapper around jdbc transaction macro. Cleans up imports basically"
  [binding & body]
  `(next.jdbc/with-transaction ~binding ~@body))

;; TODO: Maybe inline this with with settings in `sql.model` and
;; default to as-unaqilified-lower-maps
(defn execute [connection statement]
  {:pre [(vector? statement)]}
  (next.jdbc/execute! connection statement))
