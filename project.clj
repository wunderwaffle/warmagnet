(defproject warmagnet "0.1.0-SNAPSHOT"
  :description "war pigs deliver all their madness"
  :url "http://warmagnet.clojurecup.com"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.async "0.1.222.0-83d0c2-alpha"]
                 [compojure "1.1.5"]
                 [ring/ring-core "1.2.0"]
                 [org.clojure/tools.reader "0.7.6"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]
                 [korma "0.3.0-RC5"]
                 [com.cemerick/friend "0.1.5"]
                 [clj-time "0.6.0"]
                 [cheshire "5.2.0"]
                 [com.taoensso/timbre "2.6.1"]
                 [http-kit "2.1.10"]
                 [pump "0.1.0"]
                 [org.clojure/clojurescript "0.0-1877"]]
  :plugins [[lein-ring "0.8.7"]
            [lein-cljsbuild "0.3.3"]]
  :ring {:handler warmagnet.main/app}
  :main warmagnet.main
  :aot [warmagnet.main]
  :cljsbuild {:builds [{:id "main"
                        :source-paths ["cljs"]
                        :compiler {:output-to "resources/static/warmagnet.js"
                                   :externs ["resources/externs/react.js"]
                                   :optimizations :whitespace}}]})
