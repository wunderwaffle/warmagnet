(defproject warmagnet "0.1.0-SNAPSHOT"
  :clojurescript? true
  :description "war pigs deliver all their madness"
  :url "http://warmagnet.clojurecup.com"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [ring/ring-devel "1.2.0"]
                 [ring/ring-core "1.2.0"]
                 [org.clojure/clojurescript "0.0-1913"]
                 [org.clojure/tools.reader "0.7.8"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]
                 [korma "0.3.0-RC5"]
                 [clj-time "0.6.0"]
                 [cheshire "5.2.0"]
                 [com.taoensso/timbre "2.6.2"]
                 [http-kit "2.1.11"]
                 [pump "0.4.2-SNAPSHOT"]
                 [org.clojure/tools.cli "0.2.4"]]
  :plugins [[lein-ring "0.8.7"]
            [lein-cljsbuild "0.3.3"]]
  :ring {:handler warmagnet.main/app}
  :main warmagnet.main
  :aot [warmagnet.main]
  :cljsbuild
  {:crossovers [warmagnet.crossover]
   :crossover-path "crossover-cljs"
   :crossover-jar true
   :builds [{:id "main"
             :source-paths ["cljs"]
             :compiler {:output-to "resources/static/warmagnet.js"
                        :foreign-libs [{:file "resources/static/react.js"
                                        :provides ["React"]}]
                        :optimizations :whitespace}
             :jar true}
            {:id "min"
             :source-paths ["cljs"]
             :compiler {:output-to "resources/static/warmagnet.min.js"
                        ;; :externs ["resources/externs/react.js"]
                        :foreign-libs [{:file "resources/static/react.js"
                                        :provides ["React"]}]
                        :optimizations :advanced}
             :jar true}]})
