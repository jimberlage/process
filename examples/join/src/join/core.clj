(ns join.core
  (:require [clojure.core.async :as async :refer [>! <! <!! go]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [process.core :as p])
  (:import [java.io BufferedReader])
  (:gen-class))

(defn pipe-stdin [in]
  (go
    (with-open [rdr (BufferedReader. *in*)]
      (doseq [line (line-seq rdr)]
        (>! in line)))
    (async/close! in)))

(defn split [s]
  (str/split s #"\s+"))

(defn read-in []
  (p/process chs
    (loop []
      (when-let [input (<! (:in chs))]
        (doseq [line (str/split-lines input)]
          (>! (:out chs) (split line)))
        (recur)))))

(defn read-file [filename]
  (p/process> chs
    (with-open [rdr (io/reader filename)]
      (doseq [line (line-seq rdr)]
        (>! (:out chs) (split line))))))

(defn reduce-lines [out]
  (async/reduce #(assoc %1 (first %2) (rest %2)) (sorted-map) out))

(defn join-maps [m1 m2]
  (reduce-kv (fn [result key fields]
               (if-let [other-fields (get m2 key)]
                 (conj result (concat [key] fields other-fields))
                 result))
             []
             m1))

(defn join-chs [file-chs other-file-chs]
  (let [errors (->> (async/merge [(:err file-chs) (:err other-file-chs)] p/default-buffer-size)
                    (async/into [])
                    (<!!))]
    (if (empty? errors)
      (let [file-lines (reduce-lines (:out file-chs))
            other-file-lines (reduce-lines (:out other-file-chs))
            result (join-maps (<!! file-lines) (<!! other-file-lines))]
        (doseq [line result]
          (println (str/join " " line))))
      (throw (ex-info "Error(s) occurred." {:errors errors})))))

(defn -main
  "Like unix join, but way dumber.  You can't pass in options because I don't feel like taking the time to code that into an example."
  [& args]
  (case (count args)
    1 (let [file-chs (read-file (first args))
            stdin-chs (read-in)]
        (pipe-stdin (:in stdin-chs))
        (join-chs stdin-chs file-chs))
    2 (join-chs (read-file (first args)) (read-file (second args)))
      (throw (ex-info "Too few/many arguments." {}))))
