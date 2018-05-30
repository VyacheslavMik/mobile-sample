(ns mobile-sample.utils
    (:require [clojure.string :as str]))

(defn href [& parts]
  (let [l (last parts)
        qp? (and l (map? l))]
    (apply str
           (concat
            (when-not (str/starts-with? (name (or (first parts) "")) "/")
              ["/"])
            [(str/join "/" (concat
                            (map name (butlast parts))
                            (when (and l (not qp?))
                              [(name l)])))
             (when qp?
               "?")]
            (when qp?
              (str/join "&"
                        (map (fn [[k v]] (str (name k) "=" (if (keyword? v) (name v) v))) l)))))))