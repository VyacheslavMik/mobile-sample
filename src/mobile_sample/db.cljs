(ns mobile-sample.db
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [mobile-sample.lib.rn :as rn]))

(defn clj->string [data]
  (.stringify js/JSON (clj->js data)))

(defn string->clj [data]
  (js->clj (.parse js/JSON data) :keywordize-keys true))

(rf/reg-event-fx
 :dispatch-fx
 (fn [_ [_ effect param]]
   {effect param}))

(defn dispatch-event [event & [data]]
  (when event
    (rf/dispatch (if (vector? event)
                   (conj event data)
                   [event data]))))

(rf/reg-fx
 :clear-async-storage
 (fn [key]
   (.removeItem rn/async-storage key)))

#_(do
    (rf/dispatch [:dispatch-fx :clear-async-storage "location"])
    (rf/dispatch [:dispatch-fx :clear-async-storage "auth"]))

(rf/reg-fx
 :save-async-storage
 (fn [{:keys [key data on-success on-error]}]
   (-> (.setItem rn/async-storage key (clj->string data))
       (.then (fn [] (dispatch-event on-success)))
       (.catch (fn [err] (dispatch-event on-error err))))))

(rf/reg-fx
 :get-async-storage
 (fn [{:keys [key on-success on-error]}]
   (-> (.getItem rn/async-storage key)
       (.then (fn [data] (when data (string->clj data))))
       (.then (fn [data] (dispatch-event on-success data)))
       (.catch (fn [err] (dispatch-event on-error err))))))

(rf/reg-fx
 :multi-remove-async-storage
 (fn [{:keys [keys on-success on-error]}]
   (-> (.multiRemove rn/async-storage (clj->js keys))
       (.then (fn [] (dispatch-event on-success)))
       (.catch (fn [err] (dispatch-event on-error err))))))

(rf/reg-fx
 :multi-get-async-storage
 (fn [{:keys [keys on-success on-error]}]
   (-> (.multiGet rn/async-storage (clj->js keys))
       (.then (fn [data] (when data
                           (reduce (fn [acc [k v]]
                                     (assoc acc (keyword k) (string->clj v)))
                                   {}
                                   data))))
       (.then (fn [data] (dispatch-event on-success data)))
       (.catch (fn [err] (dispatch-event on-error err))))))

(defn insert-by-path [m [k & ks :as path] value]
  (if ks
    (if (int? k)
      (assoc (or m []) k (insert-by-path (get m k) ks value))
      (assoc (or m {}) k (insert-by-path (get m k) ks value)))
    (if (int? k)
      (assoc (or m []) k value) (assoc (or m {}) k value))))

(rf/reg-sub-raw
 :db/get-in
 (fn [db [_ pth]]
   (let [cur (r/cursor db pth)]
     (reaction @cur))))

(rf/reg-event-db
 :db/set-in
 (fn [db [_ path value]]
   (insert-by-path db path value)))
