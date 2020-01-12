(ns utility-belt.sql.core-test
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

(deftest crud-operations
  (is (= [] (people/get-all* @conn)))
  (is (=  [{:name "yest"
            :email "test@test.com"
            :attributes {:bar 1
                         :foo ["a" "b" "c"]}
            :confirmed-at #inst "2019-06-24"}]
          (people/add* @conn {:name "yest"
                              :email "test@test.com"
                              :attributes {:bar 1
                                           :foo [:a
                                                 :b
                                                 :c]}
                              :confirmed-at (time/->date-time "2019-06-24")})))
  (is (= [{:name "who"
           :email "dat@test.com"
           :attributes
           {:bar 1
            :foo {:ok "dawg"}}
           :confirmed-at #inst "2018-03-12T00:13:24Z"}]
         (people/add* @conn {:name "who"
                             :email "dat@test.com"
                             :attributes {:bar 1
                                          :foo {:ok :dawg}}
                             :confirmed-at #inst "2018-03-12T00:13:24Z"})))
  (is (= [{:name "who"
           :email "dat@test.com"
           :attributes {:bar 1
                        :foo {:ok "dawg"}}
           :confirmed-at #inst  "2018-03-12T00:13:24Z"}
          {:name "yest"
           :email "test@test.com"
           :attributes {:bar 1
                        :foo ["a" "b" "c"]}
           :confirmed-at #inst "2019-06-24"}]
         (people/get-all* @conn)))
  (is (= [{:name "yest"
           :email "test@test.com"
           :attributes {:bar 1
                        :foo ["a" "b" "c"]}
           :confirmed-at #inst "2019-06-24"}]
         (people/get-all* @conn {:email "test@test.com"})))
  (is (= 1
         (people/set-email* @conn {:old-email "test@test.com"
                                   :new-email "test2@test.com"})))
  (is (= 1
         (people/delete* @conn {:email "dat@test.com"})))
  (is (= {:count 1}
         (people/count-all* @conn))))
