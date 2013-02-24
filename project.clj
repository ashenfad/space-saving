(defproject space-saving "0.1.0-SNAPSHOT"
  :description "The 'SpaceSaving' stream counting algorithm for Clojure"
  :url "https://github.com/ashenfad/space-saving"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev {:dependencies [[bigml/sampling "2.1.0"]]}}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [ashenfad.com.clearspring.analytics/stream "2.3.0-SNAPSHOT"]])
