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
  (testing "The function creates a map containing a workflow and a catalog"
    (let [pre-model (create-pre-model (parse-xml-model "dev-resources/test-diagram5.xml"))]
      (is (= (set (keys pre-model)) #{:workflow :catalog}))
      (is (= pre-model
             {:workflow [[:input-dataset :group-by-year]
                         [:group-by-year :output-new-dataset]]
              :catalog [{:witan/name :input-dataset :witan/version "1.0.0"
                         :witan/type :input :witan/fn :model/input-dataset :witan/params {:src ""}}
                        {:witan/name :group-by-year :witan/version "1.0.0"
                         :witan/type :function :witan/fn :model/group-by-year}
                        {:witan/name :output-new-dataset :witan/version "1.0.0"
                         :witan/type :output :witan/fn :model/output-new-dataset}]}))))
  (testing "An exception is thrown when the model diagram is broken"
    (is (thrown? java.lang.Exception (create-pre-model
                                      (parse-xml-model "dev-resources/test-diagram5-broken.xml"))))
    (is (thrown-with-msg? java.lang.Exception
                          #"!DANGER! The flowchart has 1 disconnected arrows.\nYou CANNOT proceed with creating a model. Go fix your diagram first!\n"
                          (create-pre-model
                           (parse-xml-model
                            "dev-resources/test-diagram5-broken.xml"))))))
