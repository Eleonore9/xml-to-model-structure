(ns parse-xml.core-test
  (:require [clojure.test :refer :all]
            [parse-xml.core :refer :all]))

(deftest parse-xml-model-test
  (testing "The fn can identify all elements of the xml diagram"
    (is (= (parse-xml-model "dev-resources/test-model.xml")
           '({:id "2" :type :box :value "in"}
             {:id "3" :type :arrow :from "2" :to "4"}
             {:id "4" :type :box :value "fn"}
             {:id "5" :type :arrow :from "4" :to "6"}
             {:id "6" :type :box :value "out"})))))
