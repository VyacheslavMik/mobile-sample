(ns mobile-sample.pages.sample.model
  (:require [re-frame.core :as rf]
            [mobile-sample.layout :as l]))

(rf/reg-event-fx
 :sample/index
 (fn [{db :db} [_ phase route]]
   (cond
     (= :init phase)
     (let [tabs (l/default-tabs db)]
       {:db (assoc db :sample/index {:tabs tabs
                                     :sample-text "This is a sample text"})})

     (= :deinit phase)
     {})))

(rf/reg-sub
 :sample/index
 (fn [db _]
   (:sample/index db)))
