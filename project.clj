(defproject process "0.1.0"
  :description "A library for running code like a Unix process, with attached input, output, and error streams."
  :url "https://github.com/jimberlage/process"
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [org.clojure/core.async "0.4.474"]]
  :aliases {"test-all" ["do"
                        "clean,"
                        "test,"
                        "doo" "node" "node-test" "once,"
                        "doo" "chrome-headless" "browser-test" "once"]}
  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-doo "0.1.10"]]
  :cljsbuild {:builds {"browser-test" {:source-paths ["src" "test"]
                                       :compiler     {:main       process.runner
                                                      :output-dir "target/out"
                                                      :output-to  "target/process.js"}}
                       "node-test"    {:source-paths ["src" "test"]
                                       :compiler     {:main       process.runner
                                                      :output-dir "target/out"
                                                      :output-to  "target/process.js"
                                                      :target     :nodejs}}}})
