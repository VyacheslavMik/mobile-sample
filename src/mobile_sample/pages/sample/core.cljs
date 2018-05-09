(ns mobile-sample.pages.sample.core
  (:require [mobile-sample.pages :as pages]
            [mobile-sample.lib.nb :as nb]
            [mobile-sample.lib.rn :as rn]
            [mobile-sample.layout :as l]
            [mobile-sample.pages.sample.model]
            [re-frame.core :as rf]))

(defn sample []
  (let [{:keys [sample-text tabs]} @(rf/subscribe [:sample/index])]
    [l/layout-tabs {:tabs tabs}
     [nb/view {:style {:padding-top 20}}
      [nb/text sample-text]]]))

(pages/reg-page :sample/index sample)
