(ns utility-belt.sql.conv
  )
(comment
  (comment
  (:require [clojure.java.jdbc :as jdbc]
            [cheshire.core :as json]
            [clj-time.coerce :as coerce])
  (:import (org.postgresql.util PGobject)
           (java.sql PreparedStatement)
           (org.postgresql.jdbc PgArray)))

(extend-protocol jdbc/IResultSetReadColumn
  org.postgresql.jdbc.PgArray
  (result-set-read-column [val metadata _]
    (vec (.getArray val)))

  ;; When reading JSONB, parse it out to clojure structure
  PGobject
  (result-set-read-column [pgobj metadata idx]
    (let [type  (.getType ^PGobject pgobj)
          value (.getValue ^PGobject pgobj)]
      (case type
        "json" (json/parse-string value true)
        "jsonb" (json/parse-string value true)
        value)))
  ;; When reading timestamp, parse it out to java datetime
  java.sql.Timestamp
  (result-set-read-column [pgobj metadata idx]
    (coerce/to-date-time pgobj)))


(defn value-to-json-pgobject [value]
  (doto (PGobject.)
    (.setType "jsonb")
    (.setValue (json/generate-string value))))

(extend-protocol jdbc/ISQLValue
  ;; When inserting clojure structure convert it to JSONB
  clojure.lang.IPersistentMap
  (sql-value [value] (value-to-json-pgobject value))
  clojure.lang.IPersistentVector
  (sql-value [value] (value-to-json-pgobject value))
  ;; When inserting datetime convert it to sql timestamp
  org.joda.time.DateTime
  (sql-value [value]
    (coerce/to-sql-time value)))

(extend-protocol jdbc/ISQLParameter
  clojure.lang.IPersistentVector
  (set-parameter [v ^PreparedStatement s ^long i]
    ;; Adapted from
    ;; https://github.com/remodoy/clj-postgresql/blob/78a3d11376f0991d92e6faf7d0287ed74df3521e/src/clj_postgresql/types.clj#L133
    ;; Get the current connection and get the parameter's metadata
    ;; so we can inspect the type expected by psql
    (let [conn (.getConnection s)
          meta (.getParameterMetaData s)
          type-name (.getParameterTypeName meta i)]
      ;; if we found a type, remove the underscore so we match a native type
      ;; and create an array of elem-type
      ;; otherwise fallback to original implementation of clojure.java.jdbc
      ;; https://github.com/clojure/java.jdbc/blob/master/src/main/clojure/clojure/java/jdbc.clj#L471
      (if-let [elem-type (when type-name (second (re-find #"^_(.*)" type-name)))]
        (.setObject s i (.createArrayOf conn elem-type (to-array v)))
        (.setObject s i (jdbc/sql-value v))))))
)
