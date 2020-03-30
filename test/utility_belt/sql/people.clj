(ns utility-belt.sql.people
  (:require
    [utility-belt.sql.model :as model]))


(model/load-sql-file "utility_belt/sql/people.sql" {:mode :kebab-maps})
