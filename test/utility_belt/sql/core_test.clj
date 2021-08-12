(ns utility-belt.sql.core-test
  (:require
    [clojure.test :refer [deftest is use-fixtures]]
    ;; -- test helpers
    [utility-belt.sql.connection :as connection]
    [utility-belt.sql.conv]
    [utility-belt.sql.people :as people]
    ;; -- actual things under test
    [utility-belt.time :as time]
    [clj-time.coerce :as coerce]))


(def conn (atom nil))


(use-fixtures :each (fn [test-fn]
                      (connection/start! conn)
                      (try
                        (people/setup* @conn)
                        (people/setup-squad* @conn)
                        (people/setup-users* @conn)
                        (people/delete-all* @conn)
                        (test-fn)
                        (finally
                          (connection/stop! conn)))))


(deftest crud-operations
  (is (= [] (people/get-all* @conn)))
  (is (=  [{:name "yest"
            :email "test@test.com"
            :entity-id  #uuid "92731758-98f9-4358-974b-b15c74c917d9"
            :attributes {:bar 1
                         :foo ["a" "b" "c" "92731758-98f9-4358-974b-b15c74c917d9"]}
            :confirmed-at (coerce/to-date-time "2019-06-24")}]
          (people/add* @conn {:name "yest"
                              :email "test@test.com"
                              :entity-id  #uuid "92731758-98f9-4358-974b-b15c74c917d9"
                              :attributes {:bar 1
                                           :foo [:a :b :c #uuid "92731758-98f9-4358-974b-b15c74c917d9"]}
                              :confirmed-at (time/->date-time "2019-06-24")})))
  (is (= [{:name "who"
           :email "dat@test.com"
           :entity-id  #uuid "92731758-98f9-4358-974b-b15c74c917d0"
           :attributes {:bar 1
                        :foo {:ok "dawg"}}
           :confirmed-at (coerce/to-date-time "2018-03-12T00:13:24Z")}]
         (people/add* @conn {:name "who"
                             :email "dat@test.com"
                             :entity-id  #uuid "92731758-98f9-4358-974b-b15c74c917d0"
                             :attributes {:bar 1
                                          :foo {:ok :dawg}}
                             :confirmed-at #inst "2018-03-12T00:13:24Z"})))
  (is (= [{:name "who"
           :email "dat@test.com"
           :entity-id  #uuid "92731758-98f9-4358-974b-b15c74c917d0"
           :attributes {:bar 1
                        :foo {:ok "dawg"}}
           :confirmed-at (coerce/to-date-time "2018-03-12T00:13:24Z")
           :id 2}
          {:name "yest"
           :email "test@test.com"
           :entity-id  #uuid "92731758-98f9-4358-974b-b15c74c917d9"
           :attributes {:bar 1
                        :foo ["a" "b" "c"  "92731758-98f9-4358-974b-b15c74c917d9"]}
           :confirmed-at (coerce/to-date-time "2019-06-24")
           :id 1}]
         (people/get-all* @conn)))
  (is (= [{:name "yest"
           :id 1
           :email "test@test.com"
           :entity-id  #uuid "92731758-98f9-4358-974b-b15c74c917d9"
           :attributes {:bar 1
                        :foo ["a" "b" "c"  "92731758-98f9-4358-974b-b15c74c917d9"]}
           :confirmed-at (coerce/to-date-time "2019-06-24")}]
         (people/get-all* @conn {:email "test@test.com"})))
  (is (= 1
         (people/set-email* @conn {:old-email "test@test.com"
                                   :new-email "test2@test.com"})))
  (is (= 1
         (people/delete* @conn {:email "dat@test.com"})))
  (is (= {:count 1}
         (people/count-all* @conn))))


(deftest array-coercions
  (people/add-user* @conn {:name "yest"})
  (people/add-user* @conn {:name "who"})
  (let [people (people/get-users* @conn)]
    (mapv #(people/add-to-squad* @conn {:name "test" :user-id (:id %)}) people)
    (is (= 1 (count (people/get-squad* @conn {:name "test"}))))
    (is (= [{:name "test"
             :peeps ["yest" "who"]}]
           (people/get-squad-names* @conn {:name "test"})))
    (is (= {:name "test"
            :peeps [{:id 1 :name "yest"}
                    {:id 2 :name "who"}]}
           (-> (people/get-squad* @conn {:name "test"})
               first)))))
