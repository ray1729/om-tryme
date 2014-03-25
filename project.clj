(defproject om-tryme "0.1.0-SNAPSHOT"
  :description "Getting started with OM"
  :url "https://github.com/ray1729/om-tryme"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2173"]
                 [org.clojure/core.async "0.1.278.0-76b25b-alpha"]
                 [secretary "1.1.0"]
                 [om "0.5.3"]]

  :plugins [[lein-cljsbuild "1.0.2"]]

  :source-paths ["src"]

  :cljsbuild {
    :builds [{:id "om-tryme"
              :source-paths ["src"]
              :compiler {
                :output-to "om_tryme.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
