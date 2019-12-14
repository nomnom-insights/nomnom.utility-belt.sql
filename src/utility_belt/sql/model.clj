(ns utility-belt.sql.model
  (:require [hugsql.core :as hugsql]
            [next.jdbc.result-set :as result-set]
            [hugsql.adapter.next-jdbc :as next-adapter]))

(defn load-sql-file
  "Loads given SQL file and injects db function definitions into current namespace"
  [file]
  (hugsql/def-db-fns file
    {:adapter (next-adapter/hugsql-adapter-next-jdbc
               {:builder-fn  result-set/as-unqualified-lower-maps})}))

(defn load-sql-file-vec-fns
  "Loads given SQL file and injects debug versions of db functions.
  Functions get a `-sqlvec` suffix and can be used to debug what sort of query
  will be generated without actually running it."
  [file]
  (hugsql/def-sqlvec-fns file))
