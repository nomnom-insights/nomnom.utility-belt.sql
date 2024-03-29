(defproject nomnom/utility-belt.sql "1.1.0"
  :description "Tools for working with Postgres (queries, connection pool component, helpers etc)"
  :url "https://github.com/nomnom-insights/nomnom.utility-belt.sql"
  :deploy-repositories {"clojars" {:sign-releases false
                                   :username :env/clojars_username
                                   :password :env/clojars_password}}

  :warn-on-reflection true
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.postgresql/postgresql "42.3.1"]
                 [com.github.seancorfield/next.jdbc "1.2.753"]
                 [cheshire "5.10.1"]
                 [clj-time "0.15.2"]
                 [com.layerware/hugsql "0.5.1"]
                 [com.layerware/hugsql-adapter-next-jdbc "0.5.1" :exclusions [seancorfield/next.jdbc]]
                 [hikari-cp "2.13.0" :exclusions [com.zaxxer/HikariCP]]
                 [com.zaxxer/HikariCP "4.0.3"]]
  :plugins [[lein-cloverage "1.1.1" :exclusions [org.clojure/clojure]]]
  :profiles {:dev
             {:dependencies [[org.clojure/tools.logging "1.1.0"]
                             [ch.qos.logback/logback-classic "1.2.7"]
                             [nomnom/utility-belt "1.3.2"]
                             [com.stuartsierra/component "1.0.0"]]}})
