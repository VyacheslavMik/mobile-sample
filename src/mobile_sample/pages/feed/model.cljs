(ns mobile-sample.pages.feed.model
  (:require [re-frame.core :as rf]
            [mobile-sample.layout :as l]))

(rf/reg-event-fx
  :feed/index
  (fn [{db :db} [_ phase route]]
    (cond
      (= :init phase)
      {:db (assoc db :feed/index {:feed-text "I'm a feed page!"})}
      (= :deinit phase)
      {})))

(rf/reg-sub
  :feed/index
  (fn [db _]
    (:feed/index db)))

(rf/reg-event-fx
  :feed/favourites
  (fn [{db :db} [_ phase route]]
    (cond
    (= :init phase)
    {:db (assoc db :feed/favourites (for [i (range 30)]
        {:title (str "Item " i)}))}
    (= :deinit phase)
    {})))

(rf/reg-sub
  :feed/favourites
  (fn [db _]
    (:feed/favourites db)))
