(ns parse-xml.core
  (:require [clojure.java.io :as io]
            [clojure.data.xml :as xml]
            [clojure.string :as s]
            [taoensso.timbre :as timbre :refer [info]]
            [clojure.java.shell :refer [sh with-sh-dir]])
  (:gen-class))

;; Process the XML diagram
(defn clean-text
  "Try and anticipate special characters in the diagram boxes."
  [text]
  (-> text
      (s/trim)
      (s/lower-case)
      (s/replace #"<(\w+)>" " ")
      (s/trim)
      (s/replace #"[.()_]" "-")
      (s/replace #" " "-")))

(defn parse-xml-model
  "Gets in an xml file. Returns a list of maps
  describing each element of the diagram."
  [xml-file]
  (info "Parsing the XML from" xml-file)
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
                      :value (clean-text (:value attrs))})))))))

;; Work out the structure of the model
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
  "Gets in a list of maps containing info for each step of the model diagram.
   Returns a map containing the model workflow and the model catalog."
  [model-elements]
  (let [boxes (filter (fn [{:keys [type]}] (= type :box)) model-elements)
        arrows (filter (fn [{:keys [type]}] (= type :arrow)) model-elements)
        ;; Lookup the value of a box from its ids, and create a keyword:
        lookup-box (fn [id] (keyword (:value (first (filter #(= id (:id %)) boxes)))))
        links {:from (group-by :from arrows) :to (group-by :to arrows)}
        ;; Look for disconnected arrows
        disc-arrows (keep #(when (or (nil? (:from %)) (nil? (:to %))) %) arrows)]

    ;; When arrows are disconnected we can't create a model.
    (when (not-empty disc-arrows)
      (let [msg (str "!DANGER! The flowchart has " (count disc-arrows)
                     " disconnected arrows.\nYou CANNOT proceed with creating a model. Go fix your diagram first!\n")]
        (throw (Exception. msg))))

    (info "Creating the workflow and catalog")
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

;; Create the model in a new project directory
(defn create-model-project
  "Create a Leiningen project for the model.
   Takes in a directory where the project will be created,
   and the name for this new project."
  [project-dirpath project-name]
  (let [src-path (io/file project-dirpath project-name "src/"
                          (s/replace project-name #"-" "_"))
        test-path (io/file project-dirpath project-name "test/"
                           (s/replace project-name #"-" "_"))]
    (info "Creating a new Clojure project" project-name "at" project-dirpath "...")
    (sh "lein" "new" "app" project-name :dir (io/file project-dirpath))
    (info "Creating a 'model.clj' namespace to define the model.")
    (sh "touch" "model.clj" :dir src-path)
    (info "Creating a 'schemas.clj' namespace to define the data schemas.")
    (sh "touch" "schemas.clj" :dir src-path)
    (info "Creating a 'model_test.clj' namespace to validate the model.")
    (sh "touch" "model_test.clj" :dir test-path)
    (sh "mkdir" (str test-path "/acceptance/"))
    (info "Creating a 'workspace_test.clj' namespace to test the model can be run.")
    (sh "touch" "workspace_test.clj" :dir (io/file test-path "acceptance"))))

(defn -main
  [xml-model-diagram]
  (-> (parse-xml-model xml-model-diagram)
      create-pre-model
      clojure.pprint/pprint))
