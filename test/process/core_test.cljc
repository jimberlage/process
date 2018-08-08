(ns process.core-test
  (:require #?(:clj  [clojure.core.async :as ca :refer [<! <!! >!! >! go]]
               :cljs [cljs.core.async :as ca :refer [<! >!] :refer-macros [go]])
            #?(:clj  [clojure.test :refer [deftest is testing]]
               :cljs [cljs.test :refer [deftest is testing] :refer-macros [async]])
            [process.core :as p :include-macros true]))

#?(:clj
   (defmacro async
     "async macro, like cljs.test/async, for clojure.  Makes cross-platform
     stuff easier."
     [done & body]
     `(let [done-ch# (ca/chan 1)
            ~done #(>!! done-ch# :done)]
        ~@body
        (<!! done-ch#)
        nil)))

(defn- timed-into
  "Like clojure.core.async/into, except it will time out if the result cannot be obtained quickly enough.
  Returns an array like [:ok, coll] or [:timeout], so you may have to destructure it."
  [coll channel ms]
  (let [result (ca/chan 1)]
    (go
      (let [coll-channel (ca/into coll channel)
            timeout (ca/timeout ms)]
        (>! result (ca/alt!
                     coll-channel ([coll] [:ok coll])
                     timeout      [:timeout]))))
    result))

(deftest process-error-test-0
  (testing "Thrown errors are returned on the error channel."
    (async done
      (go
        (let [exception (ex-info "foo" {:bar :baz})
              channels (p/process _ (throw exception))
              result (<! (timed-into [] (:err channels) 1000))]
          (is (= (ex-data exception) (some-> result second first ex-data)))
          (done))))))

(deftest process-error-test-1
  (testing "Non-exceptions placed on the error channel are returned."
    (async done
      (go
        (let [obj :foo
              channels (p/process channels (ca/>! (:err channels) obj))
              result (<! (timed-into [] (:err channels) 1000))]
          (is (= [:ok [obj]] result))
          (done))))))

(deftest process-out-test-0
  (testing "All outputs are returned on the out channel."
    (async done
      (go
        (let [expected (range 10)
              channels (p/process channels (doseq [x expected] (>! (:out channels) x)))
              result (<! (timed-into [] (:out channels) 1000))]
          (is (= [:ok expected] result))
          (done))))))

(deftest process-in-test-0
  (testing "All inputs are handled correctly."
    (async done
      (go
        (let [expected (range 10)
              channels (p/process channels (loop []
                                             (when-let [x (<! (:in channels))]
                                               (>! (:out channels) x)
                                               (recur))))]
          (doseq [x expected]
            (>! (:in channels) x))
          (ca/close! (:in channels))
          (let [result (<! (timed-into [] (:out channels) 1000))]
            (is (= [:ok expected] result)))
          (done))))))

(deftest process>-error-test-0
  (testing "Thrown errors are returned on the error channel."
    (async done
      (go
        (let [exception (ex-info "foo" {:bar :baz})
              channels (p/process> _ (throw exception))
              result (<! (timed-into [] (:err channels) 1000))]
          (is (= (ex-data exception) (some-> result second first ex-data)))
          (done))))))

(deftest process>-error-test-1
  (testing "Non-exceptions placed on the error channel are returned."
    (async done
      (go
        (let [obj :foo
              channels (p/process> channels (ca/>! (:err channels) obj))
              result (<! (timed-into [] (:err channels) 1000))]
          (is (= [:ok [obj]] result))
          (done))))))

(deftest process>-out-test-0
  (testing "All outputs are returned on the out channel."
    (async done
      (go
        (let [expected (range 10)
              channels (p/process> channels (doseq [x expected] (>! (:out channels) x)))
              result (<! (timed-into [] (:out channels) 1000))]
          (is (= [:ok expected] result))
          (done))))))

(deftest process>-in-test-0
  (testing "Writing to the input channel raises an error."
    (async done
      (go
        (let [channels (p/process> channels (loop []
                                              (when-let [x (<! (:in channels))]
                                                (>! (:out channels) x)
                                                (recur))))]
          (is (false? (>! (:in channels) :foo)))
          (done))))))
