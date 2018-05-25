(ns mobile-sample.layout
  (:require [mobile-sample.lib.nb :as nb]
            [mobile-sample.lib.rn :as rn]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [clojure.string :as str]))

(def stylesheet (js->clj (aget (js/require "react-native") "StyleSheet") :keywordize-keys true))
(def appbar-height (if rn/is-ios 44 56))
(def statusbar-height (if rn/is-ios 20 0))
(def header-style {:height (+ appbar-height statusbar-height)
                   :justify-content :center
                   :padding-top statusbar-height
                   :background-color "#ffffff"})

(def header-ios-style {:border-bottom-color "rgba(53, 59, 80, 0.1)"
                       :border-bottom-width (:hairlineWidth stylesheet)})

(def header-android-style {:border-bottom-color "rgba(53, 59, 80, 0.1)"
                           :border-bottom-width (:hairlineWidth stylesheet)})

(def header-view-style {:align-items :center
                        :justify-content "space-between"
                        :flex-direction "row"})

(def title-style {:color "#003042"
                  :font-size 16
                  :font-weight "500"})

(defn header-left [prev]
  (when prev
    [nb/view {:style {:flex 0
                      :margin-left 8
                      :margin-right 5}}
     [nb/button {:transparent true
                 :style {:padding 12}
                 :on-press (fn [_]
                             (rf/dispatch [:go-back]))}
      #_[nb/text "<"]
      [nb/material-icon {:name "arrow-back"
                         :style {:font-size 22
                                 :color "#00a984"}}]]]))

(defn header-right [{{prev :prev} :nav right :right :as props}]
  [nb/view {:style {:flex 0
                    :flex-direction "row"}}
   (when (and (not prev) (not right))
     [nb/button {:transparent true
                 :style {:padding 12}}
      [nb/material-icon {:name "search"
                         :size 24
                         :color "#00a984"}]])
   (when right (doall
      (for [btn right]
        (if (fn? btn)
          (btn props)
          [nb/button {:key (pr-str btn)
                      :transparent true
                      :style {:margin-left 10 :padding 12}
                      :on-press #(if (:path btn)
                                   (rf/dispatch [:navigate (:path btn)])
                                   ((:fn btn) props))}
           #_[nb/text "Icon"]
           [nb/material-icon {:name (:icon btn)
                              :style {:font-size 22
                                      :color "#00a984"}}]]))))])

(defn header-body [{{prev :prev} :nav title :title :as props}]
  [nb/view {:style {:flex 1
                    :margin-left (if-not prev 16 0)}}
   (if (empty? title)
     [nb/view {:style {:flex-direction "row"
                       :align-items "center"}}
      #_(when-not prev [nb/app-icon])
      [nb/text {:style (merge title-style
                              (if-not prev {} {:margin-right 71})
                              {:flex 1
                               :text-align "center"})}
       "Mobile Sample"]]
     [nb/text {:style title-style} title])])

(defn header-component [props]
  [nb/view {:style (merge header-style
                          (if rn/is-ios
                            header-ios-style
                            header-android-style))}
   [nb/view {:style header-view-style}
    (if (:left props)
      [(:left props) props]
      [header-left (get-in props [:nav :prev])])
    [header-body props]
    [header-right props]]])

(defn a [params & body]
  (into
   [rn/touchable-opacity
    (merge (dissoc params :href)
           {:on-press (fn [_] (rf/dispatch [:navigate (:href params)]))})]
   body))

(defn layout-tabs [props & body]
  [nb/container
   [header-component props]
   [nb/content
    (map-indexed
     (fn [idx i]
       ^{:key idx}
       [nb/view i])
     body)]
   [nb/footer
    [nb/footer-tab
     (for [tab (:tabs props)]
       ^{:key (pr-str tab)}
        [nb/button
         [a {:href (:path tab)}
          (when (:icon tab)
            [nb/view {:style {:width 30
                              :height 30
                              :margin 0
                              :padding 0
                              :justify-content :center
                              :align-items :center}}
             [rn/image {:source (if (and (:active? tab) (:icon-active tab))
                                  (:icon-active tab)
                                  (:icon tab))}]])
          (when (:text tab)
            [nb/text (:text tab)])]])]]])

(defn layout-top-tabs [props & body]
  [nb/container 
    ;[header-component props]
    [nb/tabs {:initial-page 0}
      (for [tab (:top-tabs props)]
        ^{:key (pr-str tab)}
        [nb/tab {:heading (:text tab)}
          [nb/view
            [nb/text (:text tab)]]])]])


(defn layout [props & body]
  [nb/container
   (if-let [pheader (:header props)]
     pheader
     [nb/header
      [nb/left
       (when-not (= (:left props) false)
         (if (:left props)
           [(:left props) props]
           (when (get-in props [:nav :prev])
             [nb/button {:transparent true}
              #_[nb/text "<"]
              [nb/material-icon {:name "arrow-back"
                                 :style {:font-size 22
                                         :color "#00a984"}}]])))]
      [nb/body
       [nb/title
        (or (:title props) "Page")]]
      [nb/right
       (when (:right props) [(:right props) props])]])
   [nb/content
    (map-indexed
     (fn [idx i]
       ^{:key idx}
       [nb/view i])
     body)]])

(defn path-only [path-with-query]
  (first (str/split path-with-query #"\?")))

(defn default-tabs [db]
  (let [curr-path (path-only (get-in db [:route :path]))]
    (println "default-tabs curr-path: " curr-path)
    (->> [{:path "/feed"
           :icon (:feed nb/icons)
           :icon-active (:feed-active nb/icons)}
          {:path "/chat"
           :icon (:chat nb/icons)
           :icon-active (:chat-active nb/icons)}
          {:path "/nav"
           :icon (:nav nb/icons)
           :icon-active (:nav-active nb/icons)}]
         (remove nil?)
         (map (fn [tab]
                (assoc tab :active? (= (path-only (:path tab)) curr-path)))))))
