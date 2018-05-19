(ns mobile-sample.pages.feed.core
  (:require [mobile-sample.pages :as pages]
            [mobile-sample.lib.nb :as nb]
            [mobile-sample.layout :as l]
            [mobile-sample.pages.feed.model]
            [re-frame.core :as rf]))

(defn feed []
  (let [{:keys [feed-text]} @(rf/subscribe [:feed/index])]
    [l/layout {}
      [nb/view {:style {:padding-top 20}}
        [nb/text feed-text]]]))

(pages/reg-page :feed/index feed)