(ns join.core
  (:require [clojure.core.async :as async :refer [>! <!!]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [process.core :as p])
  (:gen-class))

(defn read-file [filename]
  (p/process>
    channels
    (with-open [rdr (io/reader filename)]
      (doseq [line (line-seq rdr)]
        (>! (:out channels) (str/split line #"\s+"))))))

(defn reduce-lines [out]
  (async/reduce
    (fn [result line]
      (assoc result (first line) (rest line)))
    {}
    out))

(defn -main
  "Like unix join, but way dumber.  You can't pass in options because I don't feel like taking the time to code that into an example."
  [& args]
  (let [[filename other-filename] args
        {file-out :out file-err :err} (read-file filename)
        {other-file-out :out other-file-err :err} (read-file other-filename)
        errors (<!! (async/into [] (async/merge file-err other-file-err p/default-buffer-size)))]
    (if (empty? errors)
      (let [file-lines (reduce-lines file-out)
            other-file-lines (reduce-lines other-file-out)
            result (merge-with concat (<!! file-lines) (<!! other-file-lines))]
        (doseq [[key fields] result]
          (println (str/join (conj fields key) " "))))
      (throw (ex-info "Error(s) occurred." {:errors errors})))))
