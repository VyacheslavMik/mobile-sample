(ns mobile-sample.pages.feed.core
  (:require [mobile-sample.pages :as pages]
            [mobile-sample.lib.nb :as nb]
            [mobile-sample.layout :as l]
            [mobile-sample.pages.feed.model]
            [re-frame.core :as rf]))

(defn feed []
  (let [{:keys [feed-text]} @(rf/subscribe [:feed/index])
        top-tabs {:top-tabs [{:text "tab1"} {:text "tab2"}]}]
    [l/layout-top-tabs top-tabs
      [nb/view {:style {:padding-top 20}}
        [nb/text feed-text]]]))

(pages/reg-page :feed/index feed)