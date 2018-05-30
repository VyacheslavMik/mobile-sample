(ns mobile-sample.flatlist
  (:require [reagent.core :as r]
            [mobile-sample.lib.rn :as rn]))


(deftype Item [value]
  IEncodeJS
  (-clj->js [x] (.-value x))
  (-key->js [x] (.-value x))
  IEncodeClojure
  (-js->clj [x _] (.-value x)))

(defn- wrap-key-fn [f]
  (fn [data index] (f data index)))

(defn- to-js-array
  "Converts a collection to a JS array (but leave content as is)"
  [coll]
  (let [arr (array)]
    (doseq [x coll]
      (.push arr x))
    arr))

(defn- wrap-data [o]
  (Item. (to-js-array o)))

(defn- wrap-render-fn [f]
  (fn [data]
    (r/as-element (f (.-item data) (.-index data) (.-separators data)))))

(defn flat-list
  [{:keys [data render-fn key-fn] :as props}]
  [rn/flat-list (merge props {:data (wrap-data data)
                              :keyExtractor (wrap-key-fn key-fn)
                              :renderItem   (wrap-render-fn render-fn)})])