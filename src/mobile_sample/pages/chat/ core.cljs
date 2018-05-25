(ns mobile-sample.pages.chat.core
  (:require [mobile-sample.pages :as pages]
            [mobile-sample.lib.nb :as nb]
            [mobile-sample.layout :as l]
            [mobile-sample.pages.chat.model]
            [re-frame.core :as rf]))


(defn chat []
  (let [{:keys [chat-text]} @(rf/subscribe [:chat/index])]
    [nb/view {:style {:padding-top 20}}
      [nb/text chat-text]]))

(pages/reg-page :chat/index chat)