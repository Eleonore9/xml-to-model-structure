(defproject parse-xml "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [witan.workspace-api "0.1.22"]
                 [com.taoensso/timbre "4.8.0"]
                 [me.raynes/fs "1.4.6"]]
  :main ^:skip-aot parse-xml.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
