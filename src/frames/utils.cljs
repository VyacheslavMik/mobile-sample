(ns frames.utils
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [goog.crypt.base64 :as base64]))

(defn ^:export decode
  [token]
  (let [segments (s/conform (s/cat :header string? :payload string? :signature string?)
                            (str/split token "."))]
    (if-not (map? segments)
      (throw (js/Error. "invalid token"))
      (let [header (.parse js/JSON (base64/decodeString (:header segments)))
            payload (.parse js/JSON (base64/decodeString (:payload segments)))]
        payload))))
