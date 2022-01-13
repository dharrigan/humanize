(ns online.harrigan.api.router
  {:author "David Harrigan"}
  (:require
   [muuntaja.core :as m]
   [online.harrigan.api.foo.routes :as foo-api]
   [online.harrigan.api.general.favicon :as favicon-api]
   [online.harrigan.api.general.health :as health-api]
   [online.harrigan.api.home.routes :as home-api]
   [reitit.coercion.malli :as rcm]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   [reitit.spec :as rs]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.cors :refer [wrap-cors]])
  (:import
   [org.eclipse.jetty.server Server]))

(set! *warn-on-reflection* true)

(def ^:private cors-middleware
  [wrap-cors
   :access-control-allow-origin [#".*"]
   :access-control-allow-methods [:delete :get :patch :post :put]])

;;
;;{:coercion :malli,
;; :errors ({:in [:id],
;;           :message "missing required key",
;;           :path [:id],
;;           :schema "[:map {:closed true} [:id string?]]",
;;           :type :malli.core/missing-key,
;;           :value nil}),
;; :humanized {:id ["missing required key"]},
;; :in [:request :query-params],
;; :schema "[:map {:closed true} [:id string?]]",
;; :type :reitit.coercion/request-coercion,
;; :value {})

(defn ^:private encode-error
  [{:keys [errors] :as coercion-failure}]
  (let [{:keys [message path value]} (first errors)]
    (format "What is going on?? There is a '%s'. I was expecting a value for '%s' but got '%s' instead!" message path value)))

(defn ^:private router
  [app-config]
  (ring/router
   [(merge ["/api"]
           (foo-api/routes app-config))
           ;; put other routes here that live under the '/api' endpoint
    (home-api/routes app-config)
    health-api/routes
    favicon-api/routes]
   {:validate rs/validate
    :data {:coercion (rcm/create (assoc rcm/default-options :encode-error encode-error))
           :muuntaja m/instance
           :middleware [cors-middleware
                        muuntaja/format-middleware
                        parameters/parameters-middleware
                        coercion/coerce-exceptions-middleware
                        coercion/coerce-request-middleware
                        coercion/coerce-response-middleware]}}))

;; CLIP Lifecycle Functions

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn start
  [{:keys [runtime-config] :as app-config}]
  (jetty/run-jetty
   (ring/ring-handler (router app-config) (ring/create-default-handler))
   (merge (:jetty runtime-config) {:allow-null-path-info true
                                   :send-server-version? false
                                   :send-date-header? false
                                   :join? false}))) ;; false so that we can stop it at the repl!

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn stop
  [^Server server]
  (.stop server) ; stop is async
  (.join server)) ; so let's make sure it's really stopped!
