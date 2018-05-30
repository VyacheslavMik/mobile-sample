(ns mobile-sample.pages.sample.core
  (:require [mobile-sample.pages :as pages]
            [mobile-sample.lib.nb :as nb]
            [mobile-sample.lib.rn :as rn]
            [mobile-sample.layout :as l]
            [mobile-sample.pages.sample.model]
            [re-frame.core :as rf]))

(defn sample []
  (let [db @(rf/subscribe [:sample/index])
        {:keys [sample-text tabs]} db]
    (println "[sample] db " db)
    [nb/text sample-text]
    #_[l/layout-tabs {:tabs (l/default-tabs db)}
     [nb/view {:style {:padding-top 20}}
      [nb/text sample-text]]]))

(pages/reg-page :sample/index sample)
