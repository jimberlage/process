(ns ^:figwheel-no-load ticker.dev
  (:require
    [ticker.core :as core]
    [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

(core/init!)
