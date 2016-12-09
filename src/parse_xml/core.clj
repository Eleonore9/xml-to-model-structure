(ns parse-xml.core
  (:require [clojure.java.io :as io]
            [clojure.data.xml :as xml])
  (:gen-class))

(defn parse-xml-model
  "Gets in an xml file. Returns a list of maps
  describing each element of the diagram."
  [xml-file]
  (let [model-xml (slurp (io/file xml-file))
        reader-xml (java.io.StringReader. model-xml)
        model-content (:content (xml/parse reader-xml))]
    (->> model-content
         first
         :content
         (keep (fn [{:keys [tag attrs content]}]
                 (when (not-empty content)
                   (if (not-empty (:content (first content)))
                     {:id (:id attrs)
                      :type :arrow
                      :from (:source attrs)
                      :to (:target attrs)}
                     {:id (:id attrs)
                      :type :box
                      :value (:value attrs)})))))))

(defn create-catalog
  "Create a catalog for the model and differentiate between
   input, function and output."
  ([model-step source target]
   {:witan/name (keyword (:value model-step))
    :witan/version "1.0.0"
    :witan/type :function
    :witan/fn (keyword (str "model/" (:value model-step)))})
  ([model-step target]
   {:witan/name (keyword (:value model-step))
    :witan/version "1.0.0"
    :witan/type :output
    :witan/fn (keyword (str "model/" (:value model-step)))})
  ([model-step]
   {:witan/name (keyword (:value model-step))
    :witan/version "1.0.0"
    :witan/type :input
    :witan/fn (keyword (str "model/" (:value model-step)))
    :witan/params {:src ""}}))

(defn create-pre-model
  [model-elements]
  (let [boxes (filter (fn [{:keys [type]}] (= type :box)) model-elements)
        arrows (filter (fn [{:keys [type]}] (= type :arrow)) model-elements)
        ;; Lookup the value of a box from its ids, and create a keyword:
        lookup-box (fn [id] (keyword (:value (first (filter #(= id (:id %)) boxes)))))
        links {:from (group-by :from arrows) :to (group-by :to arrows)}]

    {:workflow (mapv (fn [{:keys [from to]}]
                       [(lookup-box from) (lookup-box to)])
                     arrows)

     :catalog (mapv (fn [box] (let [f (get (:from links) (:id box))
                                    t (get (:to links) (:id box))]
                                (cond
                                  (and (not-empty f) (not-empty t)) (create-catalog box f t)
                                  (and (empty? f) (not-empty t)) (create-catalog box t)
                                  (and (not-empty f) (empty? t)) (create-catalog box))))
                    boxes)}))

(comment

  (clojure.pprint/pprint (parse-xml-model "dev-resources/test-diagram.xml"))

  (clojure.pprint/pprint
   (create-pre-model (parse-xml-model "dev-resources/test-diagram.xml"))))
