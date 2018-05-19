(ns mobile-sample.pages.nav.model
  (:require [re-frame.core :as rf]
            [mobile-sample.layout :as l]))

(rf/reg-event-fx
  :nav/index
  (fn [{db :db} [_ phase route]]
    (cond
      (= :init phase)
      {:db (assoc db :nav/index {:nav-text "I'm a nav page!"})}
      (= :deinit phase)
      {})))

(rf/reg-sub
  :nav/index
  (fn [db _]
    (:nav/index db)))