(ns warmagnet.persona
  (:require
   [clojure.core.async :refer [go chan >! <!!]]
   [cheshire.core :as json]
   [org.httpkit.client :as hk]))


(defn async-form-post [url params result]
	(hk/post url {:form-params params} #(go (>! result %))))

(defn login [token]
	(let [c (chan)]
		(async-form-post "https://verifier.login.persona.org/verify" {:assertion token :audience "http://localhost:8081"} c)
		(let [response (<!! c)]
			(json/decode (:body response) true))))
