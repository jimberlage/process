(ns ticker.iex
  (:require [clj-http.client :as client]))

(defn get-stock [ticker-symbol]
  (client/get
    (str "https://api.iextrading.com/1.0/" ticker-symbol "/chart/1y")
    {:accept :json}))
