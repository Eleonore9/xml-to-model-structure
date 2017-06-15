# `xml-to-model-structure`

## Background
Creating a model for Witan currently requires Clojure coding skills and a lot of manual step.
My aim is to automate some steps, and enable some customisation by non Clojure coders via the browser.
This project is an intermediary step where the repetitive steps are automated.

## Aim
The aim for the minimal viable product (MVP) is to create a command-line tool that will take three arguments:
* a path to model diagram as a XML file (as a string),
* a path for the directory where you want your model project to be created (as a string),
* a name for your model project (as a string).

## Features
All of the project code lives in the `core.clj` namespace.
This code:
1) Parses the XML to understand the steps of the model,
2) Creates a "pre-model" which is a data structure with the model workflow (steps of the model) and catalog (metadata on those steps).
3) Creates a new Clojure project using the [`witan-model-template`](https://github.com/MastodonC/witan-model-template)
Note: This template creates a minimal model structure, but the tests wouldn't pass as bits are missing
4) Update the new model projects with info from the "pre-model" generated from the model diagram
Note: The model workflow and catalog are added into a `model.clj` namespace. Ultimately empty functions should be added into the `core.clj` namespace and their names should be added inside the "model library" in the `model/clj` namespace.

## New features
To reach the MVP stage the remaining features are:
* generating the empty functions in `core.clj` namespace
* list the function names in the model library in `model.clj` namespace
* generate empty schemas for the functions input and output
* generate the test to check the model works in `workspace_test.clj` namespace (something like:
```Clojure
 (is (:output-projections result))
```
Note: Use empty maps fot the inputs and empty maps for the schemas. Checks all tests pass.

## Usage
The MVP of this project is to ultimately be used on the command-line.
It is currently used from the repl.
You can use it as follows:
0) A working Clojure environment is needed!
1) Clone the [Github repository](https://github.com/MastodonC/xml-to-model-structure) for this project
2) Clone the [Github repository](https://github.com/MastodonC/witan-model-template) for the "witan model template" project
3) From your command-line `cd` into `witan-model-template` and run `lein install`
4) From your repl in the `xml-to-model-structure` project, run something like:
```Clojure
(-main "dev-resources/test-diagram5.xml" "/home/user/Documents/" "my-model")
```
