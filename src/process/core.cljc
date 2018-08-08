(ns process.core
  (:require #?(:clj  [clojure.core.async :as async :refer [<! >!]]
               :cljs [cljs.core.async :as async :refer [<! >!] :include-macros true])))

(def default-buffer-size 1000)

(defn make-channels [opts]
  (let [channels (->> [:in :out :err]
                      (map (fn [channel-name]
                             [channel-name
                              (async/chan (get-in opts [:buffer-size channel-name] default-buffer-size))]))
                      (into {}))]
    (doseq [channel-name (get opts :close-immediately [])]
      (async/close! (get channels channel-name)))
    channels))

(defn close-outputs [channels]
  (doseq [channel-name [:out :err]]
    (async/close! (get channels channel-name))))

(defn- parse-opts
  "Gets an optional map from the body passed into a macro."
  [body]
  (if (and (>= (count body) 2) (map? (first body)))
    [(first body) (rest body)]
    [{} body]))

(defn- process*
  "Shared code for running some statements as an asynchronous process."
  [env opts channels-binding body]
  (let [cljs? (boolean (:ns env))]
    `(let [~channels-binding (make-channels ~opts)]
       (~(if cljs? 'cljs.core.async/go 'clojure.core.async/go)
         (try
           ~@body
           (catch ~(if cljs? :default `Throwable) error#
             (~(if cljs? 'cljs.core.async/>! 'clojure.core.async/>!)
               (:err ~channels-binding)
               error#))
           (finally
             (close-outputs ~channels-binding))))
       ~channels-binding)))

(defmacro process
  "Runs the given function with :in, :out, and :err channels.

  You can run it like this:
  (process channels (do-some-work channels))
  or pass in a map of options.

  For example,
  (process
    channels
    {:close-immediately #{:err}}
    (do-some-work channels))
  will close the :err channel before the body is executed.

  To change the buffer size for a channel from the default, specify a :buffer-size.

  For example,
  (process
    channels
    {:buffer-size {:out 1}}
    (do-some-work channels))
  will create the :out channel with (async/chan 1)."
  [channels-binding & body]
  (let [[opts body] (parse-opts body)]
    (process* &env opts channels-binding body)))

(defmacro process>
  "Shorthand for
  (process
    channels
    (merge-with conj {:close-immediately #{:in}}) opts)
    (do-some-work channels)).

  Use this if you don't need to provide input to the running process."
  [channels-binding & body]
  (let [[opts body] (parse-opts body)
        opts (merge-with conj {:close-immediately #{:in}} opts)]
    (process* &env opts channels-binding body)))
