(defproject join "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [process "0.1.0-SNAPSHOT"]]
  :main ^:skip-aot join.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
