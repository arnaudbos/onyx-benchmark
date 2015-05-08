(ns onyx.plugin.bench-plugin
  (:require [clojure.core.async :refer [chan >!! <!! close! alts!! timeout]]
            [clojure.data.fressian :as fressian]
            [onyx.peer.pipeline-extensions :as p-ext]))

(def hundred-bytes 
  (into-array Byte/TYPE (range 100)))

(defmethod p-ext/read-batch :generator
  [{:keys [onyx.core/task-map] :as event}]
  (let [batch-size (:onyx/batch-size task-map)]
    {:onyx.core/batch (doall (map (fn [i] {:id (java.util.UUID/randomUUID)
                                           :input :generator
                                           :message {:n i
                                                     ;:data hundred-bytes
                                                     }})
                                  (range batch-size)))}))

(defmethod p-ext/ack-message :generator
  [{:keys [core.async/pending-messages]} message-id]
  ;; We want to go as fast as possible, so we're going
  ;; to ignore message acknowledgment for now.
  )

(defmethod p-ext/retry-message :generator
  [{:keys [core.async/pending-messages core.async/retry-ch]} message-id]
  ;; Same as above.
  )

(defmethod p-ext/pending? :generator
  [{:keys [core.async/pending-messages]} message-id]
  ;; Same as above.
  false)

(defmethod p-ext/drained? :generator
  [event]
  ;; Infinite stream of messages, never drained.
  false)
