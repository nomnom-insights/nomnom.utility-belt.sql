(ns utility-belt.sql.core-test
  (:require [clojure.test :refer :all]
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

(use-fixtures :each (fn [test-fn]
                      (reset! conn (.start (cp/create connection-spec)))
                      (try
                        (setup* @conn)
                        (test-fn)
                        (delete-all* @conn)
                        (finally
                          (.stop @conn)))))

(deftest crud-operations
  (is (= [] (get-all* @conn)))
  (is (=  [{:name "yest" :email "test@test.com" :attributes {:bar 1 :foo ["a" "b" "c"]}}]
          (add* @conn {:name "yest" :email "test@test.com" :attributes {:bar 1 :foo [:a :b :c]}})))
  (is (= [{:name "who" :email "dat@test.com" :attributes {:bar 1 :foo {:ok "dawg"}}}]
         (add* @conn {:name "who" :email "dat@test.com" :attributes {:bar 1 :foo {:ok :dawg}}})))
  (is (= [{:name "who" :email "dat@test.com" :attributes {:bar 1 :foo {:ok "dawg"}}}
          {:name "yest" :email "test@test.com" :attributes {:bar 1 :foo ["a" "b" "c"]}}]
         (get-all* @conn)))
  (is (= [{:name "yest" :email "test@test.com" :attributes {:bar 1 :foo ["a" "b" "c"]}}]
         (get-all* @conn {:email "test@test.com"})))
  (is (= {:next.jdbc/update-count 1}
         (delete* @conn {:email "dat@test.com"})))
  (is (= 1
         (count (get-all* @conn)))))

(deftest transaction-operation
  (helpers/with-transaction [tx @conn]
    (add* tx {:name "yest" :email "test@test.com" :attributes {:bar 1 :foo [:a :b :c]}})
    (add* tx {:name "who" :email "dat@test.com" :attributes {:bar 1 :foo {:ok :dawg}}})
    ;; tx not finished yet so using db-pool no resu
    (is (= 0 (count (get-all* @conn))))
    (is (= 2 (count (get-all* tx)))))
  (is (= 2 (count (get-all* @conn))))
  (helpers/with-transaction [tx @conn]
    (add* tx {:name "yest" :email "test@test.com" :attributes {:bar 1 :foo [:a :b :c]}})
    (try
      (add* tx {:name nil :email "dat@test.com" :attributes {:bar 1 :foo {:ok :dawg}}})
      (catch Exception e)))
  ;; first insert should be cancelled
  (is (= 2 (count (get-all* @conn)))))
