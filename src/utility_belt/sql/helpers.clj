(ns utility-belt.sql.helpers
  (:require [next.jdbc]
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
