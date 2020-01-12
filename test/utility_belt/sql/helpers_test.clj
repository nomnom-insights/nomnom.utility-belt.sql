(ns utility-belt.sql.helpers-test
  (:require [clojure.test :refer :all]
            ;; -- test helpers
            [utility-belt.sql.connection :as connection]
            [utility-belt.sql.people :as people]
            ;; -- actual things under test
            [utility-belt.time :as time]
            [utility-belt.sql.conv]
            [utility-belt.sql.helpers :as helpers]
            [utility-belt.sql.model :as model]))

(def conn (atom nil))

(use-fixtures :each (fn [test-fn]
                      (connection/start! conn)
                      (try
                        (people/setup* @conn)
                        (people/delete-all* @conn)
                        (test-fn)
                        (finally
                          (connection/stop! conn)))))

(deftest raw-execute
  (people/add* @conn {:name "yest"
                      :email "test@test.com"
                      :attributes {:bar 1
                                   :foo [:a :b :c]}
                      :confirmed-at #inst "2019-02-03"})
  (is (= [{:attributes {:bar 1 :foo ["a" "b" "c"]}
           :confirmed-at #inst "2019-02-03"
           :email "test@test.com"
           :name "yest"}]
         (map
          #(dissoc %
                   :id
                   :created-at
                   :updated-at)
          (helpers/execute @conn ["select * from people"])))))

(deftest transaction-operation
  (helpers/with-transaction [tx @conn]
    (people/add* tx {:name "yest"
                     :email "test@test.com"
                     :attributes {:bar 1
                                  :foo [:a :b :c]}
                     :confirmed-at #inst "2019-02-03"})
    (people/add* tx {:name "who"
                     :email "dat@test.com"
                     :attributes {:bar 1
                                  :foo {:ok :dawg}}
                     :confirmed-at #inst "2019-02-03"})
    (testing  "tx not finished yet so using db-pool no results should be reutnred"
      (is (= 0 (count (people/get-all* @conn)))))
    (testing "when reading via transaction, we get the correct result"
      (is (= 2 (count (people/get-all* tx))))))
  (testing "tx is done, we can read via normal conn:"
    (is (= 2 (count (people/get-all* @conn)))))

  (testing "failing within transaction - should not add rows if an exception is thrown during transaction"
    (helpers/with-transaction [tx @conn {:rollback-only true}]
      (people/add* tx {:name "yest"
                       :email "test@test.com"
                       :confirmed-at #inst "2019-02-03"
                       :attributes {:bar 1
                                    :foo [:a :b :c]}})
      (try
        (people/add* tx {:name nil
                         :email "dat@test.com"
                         :attributes {:bar 1
                                      :foo {:ok :dawg}}})
        (catch Exception e
          (is (= "Parameter Mismatch: :confirmed-at parameter data not found."
                 (ex-message e))))))
    (testing "No new data should be inserted"
      (is (= 2 (count (people/get-all* @conn)))))))
