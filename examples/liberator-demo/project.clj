(defproject liberator-demo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler liberator-demo.core/handler}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [liberator "0.15.2"]
                 [compojure "1.6.2"]
                 [javax.servlet/servlet-api "2.5"]
                 [ring/ring-core "1.9.4"]
                 [tiny-rbac "0.1.0-SNAPSHOT"]]
  :repl-options {:init-ns liberator-demo.core})
