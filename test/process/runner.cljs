(ns process.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            process.core-test))

(doo-tests 'process.core-test)
