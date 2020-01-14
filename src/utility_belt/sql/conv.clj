(ns utility-belt.sql.conv
  (:require [next.jdbc.result-set :as jdbc.result-set]
            [next.jdbc.prepare :as jdbc.prepare]
            [cheshire.core :as json]
            [clj-time.coerce :as coerce])
  (:import (clojure.lang IPersistentMap IPersistentVector)
           (org.postgresql.util PGobject)
           (org.joda.time DateTime)
           (java.util Date)
           (java.sql PreparedStatement)
           (org.postgresql.jdbc PgArray)))

(extend-protocol jdbc.result-set/ReadableColumn
  PgArray
  (result-read-column-by-index [val _meta _idx]
    (vec (.getArray val)))
  PGobject
  (read-column-by-index [val _meta _idx]
    (let [type  (.getType ^PGobject val)
          value (.getValue ^PGobject val)]
      (case type
        "json" (json/parse-string value true)
        "jsonb" (json/parse-string value true)
        value))))

(defn value-to-json-pgobject [value]
  (doto (PGobject.)
    (.setType "jsonb")
    (.setValue (json/generate-string value))))

(extend-protocol jdbc.prepare/SettableParameter
  Date
  (set-parameter [value statement idx]
    (.setTimestamp ^PreparedStatement statement idx
                   (coerce/to-sql-time value)))
  DateTime
  (set-parameter [value statement idx]
    (.setTimestamp ^PreparedStatement statement idx
                   (coerce/to-sql-time value)))
  IPersistentMap
  (set-parameter [value statement idx]
    (.setObject ^PreparedStatement statement idx
                (value-to-json-pgobject value)))
  IPersistentVector
  (set-parameter [value statement idx]
    (.setObject ^PreparedStatement statement idx
                (value-to-json-pgobject value))))
