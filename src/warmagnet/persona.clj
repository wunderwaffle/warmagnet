(ns warmagnet.persona
  (:require
   [clojure.core.async :refer [go chan >! <!!]]
   [cheshire.core :as json]
   [warmagnet.config :refer [config]]
   [org.httpkit.client :as hk]))


(defn async-form-post [url params result]
	(hk/post url {:form-params params} #(go (>! result %))))

(defn login [token]
	(let [response @(hk/post "https://verifier.login.persona.org/verify"
                            {:form-params {:assertion token :audience (:persona-callback @config)}})]
		(json/decode (:body response) true)))

;(defn login [token] {:status "okay" :email "alexander@solovyov.net"})
