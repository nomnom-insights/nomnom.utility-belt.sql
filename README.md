# utility-belt.sql

[![Clojars Project](https://img.shields.io/clojars/v/nomnom/utility-belt.sql.svg)](https://clojars.org/nomnom/utility-belt.sql)

[![CircleCI](https://circleci.com/gh/nomnom-insights/nomnom.utility-belt.sql.svg?style=svg)](https://circleci.com/gh/nomnom-insights/nomnom.utility-belt.sql)

> A set of utilities for working with JDBC (SQL) data stores

Consist of:

- a connection pool component
- set of utils extending JDBC protocols for reading/writing pg values (mostly JSON and datetimes)
- helpers for writing SQL queries

Built on top of:

- [Cheshire](https://github.com/dakrone/cheshire) for JSON
- [HugSQL](https://www.hugsql.org) for SQL queries
- [clj-time](https://github.com/clj-time/clj-time) for datetime handling
- [hikari-cp](https://github.com/brettwooldridge/HikariCP) and the fantastic [hikari-cp Clojure wrapper](https://github.com/tomekw/hikari-cp) for the connection pooling functionality

Mostly used with Postgres and H2, but should work with anything that's supported by JDBC.

## Usage


```clojure
(def db-pool
  (component/start (utility-belt.sql.component.connection-pool/create config))
```

Then use can use the running component as an argument passed to HugSQL functions or as the connection to `clojure.tools.jdbc`

## Configuration

### Production

Postgres configuration, with [Aero](https://github.com/juxt/aero):


```clojure
{ :pg {:auto-commit true
      :pool-name  "my-cool-service"
      :adapter "postgresql"
      :username #or [#env PG_USER "service_name"]
      :password #or [#env PG_PASSWORD "password"]
      :server-name #or [#env PG_HOST "127.0.0.1"]
      :port-number #long #or [#env PG_PORT "5432"]
      :maximum-pool-size  #long #or [#env PG_MAX_POOL_SIZE 2]
      :database-name #or [#env PG_NAME "service_name"]}}

```



Note: `utility-belt.sql` comes with all required dependencies for communication with Postgres (including a connection pool, PG adapter and SQL query interface).


### Tests

Using H2 for in-memory or file based DB:

```clojure
{:adapter "h2" :url "jdbc:h2:mem:"}
```

Dependencies, in `:dev` Lein profile:

```clojure
  :profiles {:dev {:resource-paths ["dev-resources"]
                   :dependencies [[com.h2database/h2 "1.4.196"]]}}
```

#### Running utility-belt.sql test locally

1. start test Postgres instance `./script/test-postgres`
2. run tests via `lein test` or in the repl via your test runner of choice


## Defining SQL queries and functions

We're using [HugSQL](https://hugsql.org) for defining SQL queries and turning them into functions.

`utility-belt.sql.model` namespace provides a helper which makes it easy to load these SQL files:

file: `some.model.sql`

```sql

-- :name get-all*
select *  from some_table where team_uuid =  :team-uuid

```

file: `some_model.clj`

```clojure
(ns some.model
  :require [utility-belt.sql.model :as sql.model]))

(sql.model/load-sql-file "app/some/model.sql")

;; will pull in `get-all*` into current ns
```

###  Debugging queries

HugSQL has a handy functionality of creating functions returning
query vectors (can be used with `clojure.java.jdbc` !). They are also
useful for debugging:

```clojure

(require '[utility-belt.sql.model :as sm])

(sm/load-sql-file-vec-fns "some.file.sql")
;; now, if some.file.sql defined get-all* fn we can do:

(get-all*-sqlvec {:team-uuid "abcdef"})

;; and it will return

[ "SELECT * from some_table where team_uuid = ? ", "abcdef"]

```

# Authors

<sup>In alphabetical order</sup>

- [Afonso Tsukamoto](https://github.com/AfonsoTsukamoto)
- [Łukasz Korecki](https://github.com/lukaszkorecki)
- [Marketa Adamova](https://github.com/MarketaAdamova)
