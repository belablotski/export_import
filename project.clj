(defproject export_import "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
    [org.clojure/java.jdbc "0.3.7"]
    [sqljdbc4/sqljdbc4 "4.0"]]
; :native-path-aa "D:\\Projects2\\_ddl\\sqljdbc_40"
  :jvm-opts ["-Djava.library.path=D:\\Projects2\\_ddl\\sqljdbc_40" "-Xms512m" "-Xmx2348m"]
  :main export-import.core)
