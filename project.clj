(defproject nomnom/utility-belt.sql "0.2.3-SNAPSHOT"
  :description "Tools for working with Postgres (queries, connection pool component, helpers etc)"
  :url "https://github.com/nomnom-insights/nomnom.utility-belt.sql"
  :deploy-repositories {"clojars" {:sign-releases false
                                   :username [:gpg :env/clojars_username]
                                   :password [:gpg :env/clojars_password]}}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.postgresql/postgresql "42.2.8"]
                 [org.clojure/java.jdbc "0.7.10"]
                 [cheshire "5.9.0"]
                 [clj-time "0.15.2"]
                 [com.layerware/hugsql "0.5.1"]
                 [hikari-cp "2.9.0"]]
  :plugins [[lein-cloverage "1.1.1" :exclusions [org.clojure/clojure]]]
  :profiles {:dev
             {:dependencies [[org.clojure/tools.logging "0.5.0"]
                             [com.stuartsierra/component "0.4.0"]]}})
