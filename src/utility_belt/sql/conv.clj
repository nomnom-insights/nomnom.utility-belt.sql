(ns utility-belt.sql.conv
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as jdbc.result-set]
            [next.jdbc.prepare :as jdbc.prepare]
            [cheshire.core :as json]
            [clj-time.coerce :as coerce]
            [clojure.tools.logging :as log])
  (:import (clojure.lang IPersistentMap IPersistentVector)
           (org.postgresql.util PGobject)
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
  IPersistentMap
  (set-parameter [value statement idx]
    (.setObject ^PreparedStatement statement idx
                (value-to-json-pgobject value)))
  IPersistentVector
  (set-parameter [value statement idx]
    (.setObject ^PreparedStatement statement idx
                (value-to-json-pgobject value))))
