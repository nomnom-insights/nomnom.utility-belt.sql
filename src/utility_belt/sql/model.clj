(ns utility-belt.sql.model
  (:require [hugsql.core :as hugsql]
            [clojure.tools.logging :as log]
            [next.jdbc.result-set :as result-set]
            [hugsql.adapter.next-jdbc :as next-adapter]))

(defn to-kebab [^String s]
  (.replaceAll s "_" "-"))

(defn as-kebab-maps [rs opts]
  (result-set/as-unqualified-modified-maps rs (assoc opts :qualifier-fn to-kebab :label-fn to-kebab)))

(def modes
  {;; compatible with clojure.java.jdbc
   :java.jdbc {:builder-fn result-set/as-unqualified-lower-maps}
   ;; new style - with qualified keywords
   :next.jdbc {:builder-fn result-set/as-maps}
   ;; like clojure.java.jdbc but column names are converted to kebab-case symbols
   :kebab-maps {:builder-fn as-kebab-maps}})

(defn load-sql-file
  "Loads given SQL file and injects db function definitions into current namespace"
  ([file]
   (load-sql-file file {:mode :java.jdbc}))
  ([file {:keys [mode]}]
   (hugsql/def-db-fns file
     {:adapter (next-adapter/hugsql-adapter-next-jdbc
                (get modes mode))})))

(defn load-sql-file-vec-fns
  "Loads given SQL file and injects debug versions of db functions.
  Functions get a `-sqlvec` suffix and can be used to debug what sort of query
  will be generated without actually running it.
  You can also pass it to JDBC functions for querying if that's your thing."
  [file]
  (hugsql/def-sqlvec-fns file))
