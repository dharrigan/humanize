(ns online.harrigan.api.general.favicon
  {:author "David Harrigan"})

(set! *warn-on-reflection* true)

(def ^:private ok 200)

;; PUBLIC API

(def routes
  ["/favicon.ico"
   {:get {:handler (constantly {:status ok :body "I'm an API, not a website!"})}}])
