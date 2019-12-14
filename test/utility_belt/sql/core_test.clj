(ns utility-belt.sql.core-test
  (:require [clojure.test :refer :all]
            [utility-belt.time :as time]
            [utility-belt.sql.conv]
            [utility-belt.sql.component.connection-pool :as cp]
            [utility-belt.sql.helpers :as helpers]
            [utility-belt.sql.model :as model]))

(def connection-spec
  {:pool-name  "test"
   :adapter "postgresql"
   :username (or (System/getenv "PG_USER") "utility_belt")
   :password (or (System/getenv "PG_PASSWORD") "password")
   :server-name  (or (System/getenv "PG_HOST") "127.0.0.1")
   :port-number (Integer/parseInt (or (System/getenv "PG_PORT") "5434"))
   :maximum-pool-size 10
   :database-name (or (System/getenv "PG_DB") "utility_belt_test")})

(model/load-sql-file "utility_belt/sql/people.sql")

(def conn (atom nil))

(defn start-conn! []
  (reset! conn (.start (cp/create connection-spec))))

(defn stop-conn! []
  (.stop @conn))

(defn reset-db! []
  (start-conn!)
  (next.jdbc/execute! @conn ["drop table people"])
  (stop-conn!))

(use-fixtures :each (fn [test-fn]
                      (start-conn!)
                      (try
                        (setup* @conn)
                        (delete-all* @conn)
                        (test-fn)
                        (finally
                          (stop-conn!)))))

(deftest crud-operations
  (is (= [] (get-all* @conn)))
  (is (=  [{:name "yest"
            :email "test@test.com"
            :attributes {:bar 1
                         :foo ["a" "b" "c"]}
            :confirmed_at #inst "2019-06-24"}]
          (add* @conn {:name "yest"

                       :email "test@test.com"

                       :attributes {:bar 1
                                    :foo [:a
                                          :b
                                          :c]}
                       :confirmed_at (time/->date-time "2019-06-24")})))
  (is (= [{:name "who"
           :email "dat@test.com"
           :attributes
           {:bar 1
            :foo {:ok "dawg"}}
           :confirmed_at #inst "2018-03-12T00:13:24Z"}]
         (add* @conn {:name "who"
                      :email "dat@test.com"
                      :attributes {:bar 1
                                   :foo {:ok :dawg}}
                      :confirmed_at (time/->date-time "2018-03-12T00:13:24Z")})))
  (is (= [{:name "who"
           :email "dat@test.com"
           :attributes {:bar 1
                        :foo {:ok "dawg"}}
           :confirmed_at #inst  "2018-03-12T00:13:24Z"}
          {:name "yest"
           :email "test@test.com"
           :attributes {:bar 1
                        :foo ["a" "b" "c"]}
           :confirmed_at #inst "2019-06-24"}]
         (get-all* @conn)))
  (is (= [{:name "yest"
           :email "test@test.com"
           :attributes {:bar 1
                        :foo ["a" "b" "c"]}
           :confirmed_at #inst "2019-06-24"}]
         (get-all* @conn {:email "test@test.com"})))
  (is (= {:next.jdbc/update-count 1}
         (delete* @conn {:email "dat@test.com"})))
  (is (= 1
         (count (get-all* @conn)))))

(deftest transaction-operation
  (helpers/with-transaction [tx @conn]
    (add* tx {:name "yest"
              :email "test@test.com"
              :attributes {:bar 1
                           :foo [:a
                                 :b
                                 :c]}

              :confirmed_at #inst "2019-02-03"})
    (add* tx {:name "who"
              :email "dat@test.com"
              :attributes {:bar 1
                           :foo {:ok :dawg}}
              :confirmed_at #inst "2019-02-03"})
    (testing  "tx not finished yet so using db-pool no results should be reutnred"
      (is (= 0 (count (get-all* @conn)))))
    (is (= 2 (count (get-all* tx)))))
  (is (= 2 (count (get-all* @conn))))
  (testing "failing within transaction - should not aad rows if an exception is throwna"
    (try
      (helpers/with-transaction [tx @conn]
        (add* tx {:name "yest"
                  :email "test@test.com"
                  :confirmed_at #inst "2019-02-03"
                  :attributes {:bar 1
                               :foo [:a :b :c]}})
        (add* tx {:name nil
                  :email "dat@test.com"
                  :attributes {:bar 1
                               :foo {:ok :dawg}}}))
      (catch Exception e
        (is (= "Parameter Mismatch: :confirmed_at parameter data not found."
               (ex-message e)))))
    (testing "first insert should be cancelled"
      (is (= 2 (count (get-all* @conn)))))))
