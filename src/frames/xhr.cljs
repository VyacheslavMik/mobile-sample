(ns frames.xhr
  (:require [re-frame.core :as rf]
            [clojure.string :as str]
            [frames.flash :as flash]))

(defn param-value [k v]
  (if (vector? v)
    (->> v (map name) (map str/trim) (str/join (str "&" (name k) "=" )))
    v))

(defn to-query [params]
  (->> (if (and (vector? params) (some (comp not vector?) (take-nth 2 params)))
         (partition-all 2 params)
         params)
       (mapv (fn [[k v]] (str (name k) "=" (param-value k v))))
       (str/join "&")))

(defn fetch [db {:keys [uri headers params disable-flash] :as opts} & [acc]]
  (let [headers (if (:Transaction-Meta headers)
                  (let [headers (update headers :Transaction-Meta assoc :author (:current-profile db))]
                    (update headers :Transaction-Meta #(js/encodeURIComponent (.stringify js/JSON (clj->js %)))))
                  headers)
        headers (merge {"Accept" "application/json"
                        "Content-Type" "application/json"
                        "Authorization" (str "Bearer " (get-in db [:auth :id_token]))}
                       (or headers {}))
        fetch-opts (-> (merge {:method "get" :mode "cors"} opts)
                       (dissoc :uri :headers :success :error :params)
                       (assoc :headers headers))
        fetch-opts (cond
                     (:body opts)
                     (assoc fetch-opts
                            :body
                            (if (= (get headers "Content-Type") "application/json")
                              (.stringify js/JSON (clj->js (:body opts)))
                              (:body opts)))
                     :else fetch-opts)
        fetch-opts (update fetch-opts :headers
                           (fn [h] (if (= (get h "Content-Type") "multipart/form-data")
                                     (dissoc h "Content-Type")
                                     h)))
        url (str (get-in db [:config :base-url]) uri (when params (str "?" (to-query params))))]
    (-> (js/fetch url (clj->js fetch-opts))
        (.then
         (fn [resp]
           (if-not (.-ok resp)
             (do
               (.log js/console "Fetch resp error:" resp)
               (if-not disable-flash
                 (rf/dispatch [::flash/flash :danger (str "Request error: " (:statusText resp))]))
               (rf/dispatch [::xhr-fail opts])
               (.then (.json resp) (fn [d] [d resp])))
             (if-not (= 204 (.-status resp))
               (.then (.json resp) (fn [d] [d resp]))
               ["" resp]))))
        (.then
         (fn [[data resp]]
           (let [res {:request opts
                      :response resp
                      :data (cond
                              (array? data) (js->clj data :keywordize-keys true)
                              (object? data) (js->clj data :keywordize-keys true)
                              :else data)}]
             (if acc
               (conj acc res)
               res))))
        (.catch
         (fn [err]
           (.log js/console "Fetch error:" err)
           (if-not disable-flash
             (rf/dispatch [::flash/flash :danger (str "Request error: " err)]))
           (rf/dispatch [::xhr-fail opts]))))
    ))

(rf/reg-fx
 :json/fetch-with-db
 (fn [[db {:keys [success error cb] :as opts}]]
   (.then
    (fetch db opts)
    (fn [{:keys [response data] :as res}]
      (if (< (.-status response) 299)
        (do
          (rf/dispatch [::xhr-success opts])
          (when success
            (rf/dispatch (conj success res)))
          (when cb (cb res)))
        (if error
          (rf/dispatch (conj error res))
          (rf/dispatch [::flash/flash :danger (str "Request error: " (.-status response))])))))))

(defn mk-entry-opts [entry]
  {:uri (str "/" (:resourceType entry) "/" (:id entry))
   :method "put"
   :body entry})

(rf/reg-fx
 :json/fetch-bundle-with-db
 (fn [[db {:keys [success error bundle] :as opts}]]
   (.then
    (->> (:entry bundle) (map :resource)
         (reduce (fn [acc e]
                   (.then acc (fn [r] (fetch db (mk-entry-opts e) r))))
                 (.resolve js/Promise [])))
    (fn [res]
      (if (every? #(.-ok (:response %)) res)
        (do
          (rf/dispatch [::xhr-success opts])
          (rf/dispatch (conj success res)))
        (do
          (rf/dispatch [::xhr-fail opts])
          (rf/dispatch (conj error res))))))))

(rf/reg-fx
 :json/fetch-all-with-db
 (fn [[db {:keys [success error bundle] :as opts}]]
   (.then
    (.all js/Promise
          (mapv #(fetch db %) bundle))
    (fn [res]
      (if (every? #(.-ok (:response %)) res)
        (do
          (rf/dispatch [::xhr-success opts])
          (rf/dispatch (conj success res)))
        (do
          (rf/dispatch [::xhr-fail opts])
          (rf/dispatch (conj error res))))))))

(rf/reg-event-db
 ::xhr-fail
 (fn [db [_ arg]]
   (assoc-in db [:xhr :status] "fail")))

(rf/reg-event-fx
 ::xhr-success
 (fn [{db :db} [_ arg]]
   (let [db (update-in db [:xhr :requests ] dissoc (keyword (:uri arg)))
         db (assoc-in db [:xhr :status] "success")
         db (if (empty? (get-in db [:xhr :requests ]))
              (assoc-in db [:xhr :status] "success")
              db)]
     {:db db})))

(rf/reg-event-fx
 :fetch-with-db
 (fn [{db :db} [_ arg]]
   {:db (-> db
            (assoc-in [:xhr :status] "pending")
            (assoc-in [:xhr :requests (keyword (:uri arg))] "pending"))
    :json/fetch-with-db [db arg]}))

(rf/reg-event-fx
 :xhr-fetch-with-db
 (fn [{db :db} [_ arg]]
   {:db (-> db
            (assoc-in [:xhr :status] "pending")
            (assoc-in [:xhr :requests (keyword (:uri arg))] "pending"))
    :xhr/fetch-with-db [db arg]}))

(rf/reg-event-fx
 :fetch-bundle-with-db
 (fn [{db :db} [_ arg]]
   {:json/fetch-bundle-with-db [db arg]}))

(rf/reg-event-fx
 :fetch-all-with-db
 (fn [{db :db} [_ arg]]
   {:db (-> db
            (assoc-in [:xhr :status] "pending")
            (assoc-in [:xhr :requests (keyword (or (:uri arg) "/"))] "pending"))
    :json/fetch-all-with-db [db arg]}))


(rf/reg-fx
 :xhr/fetch
 #(rf/dispatch [:xhr-fetch-with-db %]))
(rf/reg-fx
 :json/fetch
 #(rf/dispatch [:fetch-with-db %]))
(rf/reg-fx
 :json/fetch-all
 #(rf/dispatch [:fetch-all-with-db %]))
(rf/reg-fx
 :json/bundle
 #(rf/dispatch [:fetch-bundle-with-db %]))

(rf/reg-sub
 :xhr/status
 (fn [db _]
   (get-in db [:xhr :status])))

(rf/reg-event-fx
 ::save->db
 (fn [{db :db} [_ status event path formatter {data :data :as resp}]]
   (merge {:db (assoc-in db path (cond-> {:status status}
                                   (= status :succeed) (assoc :data (formatter data))
                                   (= status :failure) (assoc :error resp)))}
          (when event {:dispatch (conj event resp)}))))

(rf/reg-fx
 :json/fetch->db
 #(rf/dispatch [:json/fetch->db %]))

(rf/reg-event-fx
 :json/fetch->db
 (fn [{db :db} [_ {:keys [formatter success error path] :as params :or {formatter identity}}]]
   {:db (assoc-in db (conj path :status) :loading)
    :json/fetch (-> params
                    (dissoc :path :formatter)
                    (assoc
                     :success [::save->db :succeed success path formatter]
                     :error   [::save->db :failure error path formatter]))}))
