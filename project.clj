(defproject nomnom/utility-belt.sql "0.3.0-SNAPSHOT"
  :description "Tools for working with Postgres (queries, connection pool component, helpers etc)"
  :url "https://github.com/nomnom-insights/nomnom.utility-belt.sql"
  :deploy-repositories {"clojars" {:sign-releases false
                                   :username :env/clojars_username
                                   :password :env/clojars_password}}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.postgresql/postgresql "42.2.8"]
                 [seancorfield/next.jdbc "1.0.10"]

                 [cheshire "5.9.0"]
                 [clj-time "0.15.2"]
                 [com.layerware/hugsql "0.5.1"]
                 [com.layerware/hugsql-adapter-next-jdbc "0.5.1"]
                 [hikari-cp "2.9.0"]]
  :plugins [[lein-cloverage "1.1.1" :exclusions [org.clojure/clojure]]]
  :profiles {:dev
             {:dependencies [[org.clojure/tools.logging "0.5.0"]
                             [com.stuartsierra/component "0.4.0"]]}})
