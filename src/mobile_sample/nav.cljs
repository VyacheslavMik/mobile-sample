(ns mobile-sample.nav
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [mobile-sample.lib.rn :as rn]
            [re-frame.core :as rf]
            [re-frame.db :as rfdb]
            [route-map.core :as rm]
            [mobile-sample.routes :as routes]
            [clojure.string :as str]))

(defonce _history (r/atom []))

(add-watch
 _history
 :match
 (fn [k v old new]
   (rf/dispatch [::navigate (last new)])))

(defn parse-querystring [s]
  (-> (str/replace s #"^\?" "")
      (str/split #"&")
      (->>
       (reduce (fn [acc kv]
                 (let [[k v] (str/split kv #"=" 2)]
                   (let [k (keyword k)
                         v (js/decodeURIComponent v)
                         q (k acc)]
                     (if q
                       (let [q (if (vector? q) q [q])]
                         (assoc acc k (conj q v)))
                       (assoc acc k v)))))
               {}))))

(defn navigate [path & [reset]]
  (when (::navigation-initialized? @rfdb/app-db)
    (.setItem rn/async-storage "location" path))
  (rf/dispatch [::location-changed path]))

(defn init-nav []
  (->
   (.getItem rn/async-storage "location")
   (.then (fn [path]
            (println "restore location " path)
            (rf/dispatch [::navigation-initialized])
            (rf/dispatch [::location-changed (or path "/")])))))


(defn contexts-diff [route old-contexts new-contexts]
  ;; TODO: preserve contexts order
  (let [to-dispose (reduce (fn [acc [ctx-name o-params]]
                             (let [n-params (get new-contexts ctx-name)]
                               (if (= n-params o-params)
                                 acc
                                 (conj acc [ctx-name :deinit o-params]))))
                           [] old-contexts)

        to-dispatch (reduce (fn [acc [ctx-name n-params]]
                              (let [o-params (get old-contexts ctx-name)]
                                (cond
                                  (or (nil? o-params) (not (= n-params o-params)))
                                  (conj acc [ctx-name :init n-params])

                                  :else acc)))
                            to-dispose new-contexts)]
    to-dispatch))

(rf/reg-event-fx
 ::location-changed
 (fn [{db :db} [_ path]]
   (println "location-changed" path)
   (let [[location query] (str/split path #"[?]")
         query (if query (parse-querystring query) query)
         location (str/replace location  #"^.*://" "/")
         match (rm/match location routes/routes)
         prev-route (:route db)

         new-contexts (reduce (fn [acc {c-params :params ctx :context}]
                                (if ctx
                                  (assoc acc ctx c-params)
                                  acc)) {} (:parents match))
         old-contexts (get-in db [:route :contexts])

         ctx-diff (contexts-diff match old-contexts new-contexts)

         route (merge-with merge match {:query query :path path :contexts new-contexts})

         to-dispatch (into ctx-diff
                           (cond
                             (and prev-route route
                                  (not= (:match prev-route) (:match route)))
                             [[(:match prev-route) :deinit prev-route]
                              [(:match route) :init route]]

                             (and prev-route route
                                  (= (:match prev-route) (:match route))
                                  (not= (:params prev-route) (:params route)))
                             [[(:match route) :reinit route]]

                             (and prev-route route
                                  (= (:match prev-route) (:match route))
                                  (not= (:query prev-route) (:query route)))
                             [[(:match route) :query-changed route]]

                             (and (not prev-route) route)
                             [[(:match route) :init route]]

                             (and prev-route (not route))
                             [(:match prev-route) :deinit prev-route]

                             :else []))]
     (if (not match)
       {:db (assoc db :route {:match :not-found :path path})}
       {:db (assoc db :route route)
        :dispatch-n to-dispatch}))))

;; (rf/reg-event-fx
;;  ::location-changed
;;  (fn [{db :db} [_ path]]
;;    (let [[location query] (str/split path #"[?]")
;;          query (if query (parse-querystring query) query)
;;          location (str/replace location  #"^.*://" "/")
;;          match (rm/match location routes/routes)
;;          route (merge-with merge match {:query query :path path})
;;          prev-route (:route db)
;;          to-dispatch (cond-> []
;;                        (and prev-route
;;                             (not (= (:match prev-route)
;;                                     (:match route))))
;;                        (conj [(:match prev-route) :deinit prev-route]
;;                              [(:match route) :init route])

;;                        route
;;                        (conj [(:match route) :init route]))
;;          route (if (not match) {:match :not-found :path path} route)
;;          db (cond-> (assoc db :route route)
;;               prev-route (update :history cons prev-route))]
;;      (println "history" (:history db))
;;      (println "route" route)
;;      (println "prev-route" prev-route)
;;      (cond-> {:db db}
;;        match (assoc :dispatch-n to-dispatch)))))

(rf/reg-event-fx
 ::go-back
 (fn [{db :db} _]
   (println "go-back")))

(defn go-back []
  (when (> (count @_history) 1)
    (swap! _history #(-> % butlast vec))))

(rf/reg-event-db
 ::navigate
 (fn [db [_ route]]
   (assoc db :route route)))

(rf/reg-event-fx
 :navigate
 (fn [_ [_ path]]
   {:navigate path}))

(rf/reg-fx
 :navigate
 (fn [path]
   (navigate path)))

(rf/reg-fx
 :init-nav
 (fn [_] (init-nav)))

(rf/reg-event-db
 ::navigation-initialized
 (fn [db _]
   (assoc db ::navigation-initialized? true)))

#_(rf/reg-event-fx
 :go-back
 (fn [_ [_ _]]
   {:go-back nil}))

#_(rf/reg-fx
 :go-back
 (fn [_]
   (go-back)))

(rf/reg-sub-raw
 :route
 (fn [db [_ _]]
   (let [cur (r/cursor db [:route])]
     (reaction @cur))))

(comment
  (str/replace "tealnet://login?action=pin" #"^.*://" "/")
  (println @_history)
  (do
    (println "---")
    (doseq [h @_history]
      (println (:match h))
      (println (:query h)))
    (println "---"))
  (navigate "/login?action=pin")
  (navigate "/login?action=phone")
  (navigate "/home")
  (go-back)
  )
