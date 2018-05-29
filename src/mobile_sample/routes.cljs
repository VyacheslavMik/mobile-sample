(ns mobile-sample.routes)

(def routes
   {:. :index
   :layout :bottom-tabs
   "feed" {:. :feed/index
           ;:layout :top-tabs
           "all" {:. :feed/all
                  :layout :simple
                  "favourites" :feed/favourites
                 }
          } 
   "chat" :chat/index
   "nav" :nav/index})
