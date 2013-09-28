(ns warmagnet.persona
  (:require
   [clojure.core.async :refer [go chan >! <!!]]
   [cheshire.core :as json]
   [org.httpkit.client :as hk]))


(defn async-form-post [url params result]
	(hk/post url {:form-params params} #(go (>! result %))))

(defn login [token]
	(let [response @(hk/post "https://verifier.login.persona.org/verify"
                            {:form-params {:assertion token :audience "http://localhost:3000"}})]
		(json/decode (:body response) true)))

;(defn login [token] {:status "okay" :email "alexander@solovyov.net"})
