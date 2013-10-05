(ns warmagnet.persona
  (:require
   [cheshire.core :as json]
   [warmagnet.config :refer [config]]
   [org.httpkit.client :as hk]))


(defn login [token]
	(let [response @(hk/post "https://verifier.login.persona.org/verify"
                            {:form-params {:assertion token :audience (:persona-callback @config)}})]
		(json/decode (:body response) true)))

;(defn login [token] {:status "okay" :email "alexander@solovyov.net"})
