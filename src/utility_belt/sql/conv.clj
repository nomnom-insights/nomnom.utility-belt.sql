(ns utility-belt.sql.conv
  (:require
    [cheshire.core :as json]
    [clj-time.coerce :as coerce]
    [next.jdbc.prepare :as jdbc.prepare]
    [next.jdbc.result-set :as jdbc.result-set])
  (:import
    (clojure.lang
      IPersistentMap
      IPersistentVector)
    (java.sql
      PreparedStatement
      Timestamp)
    (java.util
      Date)
    (org.joda.time
      DateTime)
    (org.postgresql.jdbc
      PgArray)
    (org.postgresql.util
      PGobject)))


(defn pgobject-json-to-value [val]
  (let [type (.getType ^PGobject val)
        value (.getValue ^PGobject val)]
    (case type
      "json" (json/parse-string value true)
      "jsonb" (json/parse-string value true)
      value)))


(extend-protocol jdbc.result-set/ReadableColumn
  PgArray
  (read-column-by-index [val _meta _idx]
    (vec (.getArray val)))
  PGobject
  (read-column-by-index [val _meta _idx]
    (pgobject-json-to-value val))
  Timestamp
  (read-column-by-index [val _meta _idx]
    (coerce/to-date-time val)))


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
  (set-parameter [v ^PreparedStatement s ^long i]
    (let [conn (.getConnection s)
          meta (.getParameterMetaData s)
          type-name (.getParameterTypeName meta i)]
      (if-let [elem-type (when type-name (second (re-find #"^_(.*)" type-name)))]
        (.setObject s i (.createArrayOf conn elem-type (to-array v)))
        (.setObject ^PreparedStatement s i
                    (value-to-json-pgobject v))))))
