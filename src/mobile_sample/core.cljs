(ns mobile-sample.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [re-frame.loggers :as rf.log]
            [mobile-sample.db]
            [mobile-sample.nav]
            [mobile-sample.pages :as pages]
            [mobile-sample.pages.sample.core]
            [mobile-sample.pages.chat.core]
            [mobile-sample.pages.feed.core]
            [mobile-sample.pages.nav.core]
            [mobile-sample.lib.nb :as nb]
            [mobile-sample.lib.rn :as rn]
            [mobile-sample.layout :as l]
            [mobile-sample.nav :as nav]))

(def warn (js/console.warn.bind js/console))
(rf.log/set-loggers!
 {:warn (fn [& args]
          (cond
            (= "re-frame: overwriting" (first args)) nil
            :else (apply warn args)))})

(set! (.-ignoredYellowBox js/console) (clj->js ["Remote debugger"]))

(defn not-found [{path :path :as nav}]
  (fn [_]
    [l/layout {:title "Page not found"}
     [nb/text (str "Page: " (pr-str nav) " not found")]]))

(rf/reg-event-fx :not-found (fn [_] {}))

(defn route-not-found [params]
  [l/layout {:title "Route not found"}
   [nb/text (str "Route for " (:path params) " not found")]])

(pages/reg-page :not-found route-not-found)

(defn debug-footer [route]
  [nb/view {:flex-direction :row
            :height 18}
   [rn/touchable-opacity {:on-press (fn [_]
                                      (rf/dispatch [::nav/go-back]))}
    [nb/text {:style {:font-size 8
                      :color :black
                      :padding-left 10}}
     "<"]]
   [rn/touchable-opacity {:on-press (fn [_]
                                      (rf/dispatch [:dispatch-fx :clear-async-storage "location"])
                                      (rf/dispatch [:dispatch-fx :clear-async-storage "auth"])
                                      (rf/dispatch [:core-init]))}
    [nb/text {:style {:font-size 8
                      :color :black
                      :padding-left 10}}
     "R"]]
   [nb/text {:style {:font-size 8
                     :text-align :right
                     :flex 1
                     :padding-right 10}}
    (:path route)]])

(defn content [route]
  (println "Content called with route: " route)
  (println "Route match: " (:match route))
  (let [c (get @pages/pages (:match route))]
    [nb/view {:style {:flex 1}}
     (if c
       [l/layout-tabs {:tabs (l/default-tabs {:route route})}
         [c]]
       [not-found @route])]))

(defn app-root []
  (let [route (rf/subscribe [:route])]
    (fn []
      (when @route
        [nb/style-provider {:style (nb/get-theme)}
         [nb/view {:style {:flex 1
                           :flex-direction :column
                           }}
          [content @route]
          [debug-footer @route]]]))))

(rf/reg-event-fx
 :core-init
 (fn [coef [_]]
   {:db {:config {:base-url "example.com"}}
    :get-async-storage {:key "auth"
                        :on-success ::init}}))

(rf/reg-event-fx
 ::init
 (fn [{db :db} [_ auth]]
   {:db (assoc db :auth auth)
      :navigate "/"}))

(defn init []
  (rf/dispatch-sync [:core-init])
  (rn/register "mobileSample" app-root))
