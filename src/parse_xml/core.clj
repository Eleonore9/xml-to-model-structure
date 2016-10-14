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

(defn create-pre-model
  ([model-step source target]
   {:workflow (map (fn [t] [(keyword (:value model-step)) (keyword (:value t))]) target)
    :catalog {:witan/name (keyword (:value model-step))
              :witan/version "1.0.0"
              :witan/type :function
              :witan/fn (keyword (str "model/" (:value model-step)))}})
  ([model-step target]
   {:workflow (map (fn [t] [(keyword (:value model-step)) (keyword (:value t))]) target)
    :catalog {:witan/name (keyword (:value model-step))
              :witan/version "1.0.0"
              :witan/type :output
              :witan/fn (keyword (str "model/" (:value model-step)))}})
  ([model-step]
   {:catalog {:witan/name (keyword (:value model-step))
              :witan/version "1.0.0"
              :witan/type :input
              :witan/fn (keyword (str "model/" (:value model-step)))
              :witan/params {:src ""}}}))

(defn add-model-metadata
  [model-elements]
  (let [boxes (filter (fn [{:keys [type]}] (= type :box)) model-elements)
        arrows (filter (fn [{:keys [type]}] (= type :arrow)) model-elements)
        links {:from (group-by :from arrows) :to (group-by :to arrows)}]
    (mapv (fn [box] (let [f (get (:from links) (:id box))
                          t (get (:to links) (:id box))]
                      (cond
                        (and (not-empty f) (not-empty t)) (create-pre-model box f t)
                        (and (empty? f) (not-empty t)) (create-pre-model box t)
                        (and (not-empty f) (empty? t)) (create-pre-model box))))
          boxes)))

(defn create-workflow
  [model-metadata]
  (vec (keep :workflow model-metadata)))

(defn create-catalog
  [model-metadata]
  (vec (keep :catalog model-metadata)))

(comment

  (clojure.pprint/pprint (parse-xml-model "dev-resources/test-model.xml"))

  (add-model-metadata (parse-xml-model "dev-resources/test-model.xml")))
