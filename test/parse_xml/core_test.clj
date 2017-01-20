(ns parse-xml.core-test
  (:require [clojure.test :refer :all]
            [parse-xml.core :refer :all]))

(deftest parse-xml-model-test
  (testing "The fn can identify all elements of the xml diagram"
    (is (= (set (parse-xml-model "dev-resources/test-diagram.xml"))
           (set '({:id "2", :type :box, :value "in"}
                  {:id "3", :type :box, :value "fn"}
                  {:id "4", :type :box, :value "out"}
                  {:id "5", :type :arrow, :from "2", :to "3"}
                  {:id "6", :type :arrow, :from "3", :to "4"}))))
    (is (= (set (parse-xml-model "dev-resources/test-diagram2.xml"))
           (set '({:id "2" :type :box :value "input"}
                  {:id "4" :type :box :value "step"}
                  {:id "6" :type :box :value "output"}
                  {:id "3" :type :arrow :from "2" :to "4"}
                  {:id "5" :type :arrow :from "4" :to "6"}))))))

(deftest create-pre-model-test
  (testing "The pre-model contains valid workflow and catalog"
    (let [parsed-xml (parse-xml-model "dev-resources/test-diagram.xml")
          pre-model (create-pre-model parsed-xml)]
      (is (= pre-model
             {:catalog [{:witan/name :in
                         :witan/version "1.0.0"
                         :witan/type :input
                         :witan/fn :model/in
                         :witan/params {:src ""}}
                        {:witan/name :fn
                         :witan/version "1.0.0"
                         :witan/type :function
                         :witan/fn :model/fn}
                        {:witan/name :out
                         :witan/version "1.0.0"
                         :witan/type :output
                         :witan/fn :model/out}]
              :workflow [[:in :fn] [:fn :out]]})))))
