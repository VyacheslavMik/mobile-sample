(ns mobile-sample.pages.nav.core
  (:require [mobile-sample.pages :as pages]
            [mobile-sample.lib.nb :as nb]
            [mobile-sample.layout :as l]
            [mobile-sample.pages.nav.model]
            [re-frame.core :as rf]))

(defn nav []
  (let [{:keys [nav-text]} @(rf/subscribe [:nav/index])]
    [l/layout {}
      [nb/view {:style {:padding-top 20}}
        [nb/text nav-text]]]))


(pages/reg-page :nav/index nav)