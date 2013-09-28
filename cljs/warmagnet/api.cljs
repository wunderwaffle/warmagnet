(ns warmagnet.api)

(def ws-url "ws://localhost:3000/ws")
(def ws (js/WebSocket. ws-url))
