(ns mobile-sample.pages.chat.model
  (:require [re-frame.core :as rf]
            [mobile-sample.layout :as l]))


(rf/reg-event-fx
  :chat/index
  (fn [{db :db} [_ phase route]]
    (cond
      (= :init phase)
      {:db (assoc db :chat/index {:chat-text "Hello, I'm chat page!"})}
      (= :deinit phase)
      {})))

(rf/reg-sub
  :chat/index
  (fn [db _]
    (:chat/index db)))