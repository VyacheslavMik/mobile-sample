(ns frames.flash
  (:require [re-frame.core :as rf]))

(rf/reg-event-db
 ::flash
 (fn [db [_ status message]]
   (let [id (keyword (str (.getTime (js/Date.))))]
     (js/setTimeout #(rf/dispatch [::remove-flash id]) 3000)
     (assoc-in db [:flash id] {:st status :msg message}))))

(rf/reg-event-fx
 ::remove-flash
 (fn [{db :db} [_ id]]
   {:db (update db :flash dissoc id)}))

(rf/reg-fx
 :flash/flash
 (fn [{:keys [status message]}]
   (rf/dispatch [::flash status message])))

(rf/reg-sub
 ::flashes
 (fn [db _]
   (:flash db)))
