(ns utility-belt.sql.model
  (:require [hugsql.core :as hugsql]
            [hugsql.adapter.clojure-java-jdbc :as adapter]))

(defn load-sql-file
  "Loads given SQL file and injects db function definitions into current namespace"
  [file]
  (hugsql/def-db-fns file
    {:adapter (adapter/hugsql-adapter-clojure-java-jdbc)}))

(defn load-sql-file-vec-fns
  "Loads given SQL file and injects debug versions of db functions.
  Functions get a `-sqlvec` suffix and can be used to debug what sort of query
  will be generated without actually running it."
  [file]
  (hugsql/def-sqlvec-fns file))
