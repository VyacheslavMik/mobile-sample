(ns mobile-sample.pages.feed.core
  (:require [mobile-sample.pages :as pages]
            [mobile-sample.lib.nb :as nb]
            [mobile-sample.layout :as l]
            [mobile-sample.pages.feed.model]
            [re-frame.core :as rf]
            [mobile-sample.utils :as u]
            [mobile-sample.flatlist :as fl]))

(defn feed []
  (let [{:keys [feed-text]} @(rf/subscribe [:feed/index])
        top-tabs {:top-tabs [{:text "tab1"} {:text "tab2"}]}]
   ; [l/layout-top-tabs top-tabs
      [nb/view {:style {:padding-top 20}}
        [nb/text feed-text]
        [l/a {:href (u/href :feed :all :favourites)}
          [nb/text "Go to favourites"]]]))

(defn fav-item [item]
  [nb/text (:title item)])

(defn fav []
  (let [favs @(rf/subscribe [:feed/favourites])
        top-tabs {:top-tabs [{:text "tab1"} {:text "tab2"}]}]
      [nb/view {:style {:padding-top 20}}
        [l/a {:href (u/href :feed)}
          [nb/text "Back to feed"]]
        [fl/flat-list {:data favs
                       :key-fn :title
                       :render-fn fav-item
                       }]]))


(pages/reg-page :feed/index feed)

(pages/reg-page :feed/favourites fav)