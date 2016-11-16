(ns subdb-api.core
  #?@ (:cljs 
       [(:require-macros [cljs.core.async.macros :refer [go]])
        (:require [cljs-http.client :as cljs-http]
                  [cljs.core.async :refer [<!]])]
       :clj
       [(:require [clj-http.client :as clj-http])])
  )

(def hostname "http://sandbox.thesubdb.com")
(def options
  {:agent false
   :headers {"Content-Length" 0
             "User-Agent" "SubDB/1.0 (CljSubdb/0.0.1; http://github.com/broose/subdb-api)" 
             }})
           
#? (:cljs
    (defn- sync [method]
      #(go (let [response (<! (method hostname %))]
             response))))
(def method-funcs #? (:cljs
                      {:get  (sync cljs-http/get) 
                       :post (sync cljs-http/post)}
                      :clj
                      {:get  #(clj-http/get  hostname %)
                       :post #(clj-http/post hostname %)}))
 
(defn greet []
  #? (:clj (println "Hey, clj dude!")
      :cljs (println "Hullo, cljs pal!")))

(defmulti perform (fn [method _] (identity method)))
(defmethod perform :get [_ qparams]
  ((:get method-funcs) (assoc options
                              :query-params qparams
                              )))
(defmethod perform :post [_ params]
  ((:post method-funcs) params))


(defn avail-langs []
  (perform :get {:action "languages"}))

(defn search 
  ([hash] (search hash false))
  ([hash versions?]
   (perform :get (merge {:action "search"
                         :hash hash
                         }
                        (when versions?
                          {:versions true})
                        ))))

(defn download [hash lang]
  (perform :get {:action "download"
                 :hash hash
                 :language lang
                 }))


;; FIXME doesn't work
#_ (defn upload [hash path]
  (perform :post {:multipart [;; Add hash
                              {:content-disposition "form-data"
                               :name "file"
                               :content (clojure.java.io/file path)}]}))


;; TODO hash func
