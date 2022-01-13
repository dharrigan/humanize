(ns online.harrigan.api.foo.routes
  {:author "David Harrigan"})

(set! *warn-on-reflection* true)

(def ^:private ok 200)

(defn ^:private home
  [app-config]
  (fn [{{{:keys [id]} :query} :parameters :as response}]
    {:status ok :body {:hello id}}))

;; PUBLIC API

(defn routes
  [app-config]
  ["/foo"
   {:get {:handler (home app-config)
          :parameters {:query [:map [:id string?]]}}}])
