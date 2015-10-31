(ns export-import.core
  (require [clojure.java.jdbc :as jdbc]
              [clojure.stacktrace :as sttr]
              [clojure.string :as str]))


; DB spec

(def db-spec-source {:classname "com.microsoft.jdbc.sqlserver.SQLServerDriver"
              :subprotocol "sqlserver"
              :subname "//host1:1433;databaseName=Sales;integratedSecurity=false;user=abelablotski;password=123456;"})
              
(def db-spec-target-writeable {:classname "com.microsoft.jdbc.sqlserver.SQLServerDriver"
              :subprotocol "sqlserver"
              :subname "//host2:1433;databaseName=SalesQA;integratedSecurity=false;user=abelablotski;password=123456;"})
              

; DB extract query (should return [id doc datetime])

(def extract-query-web-sales "SELECT [TransactionID] as id, [TransactionXMLDoc] as doc, [CreateDate] as dtm FROM [Sales].[dbo].[WebTransaction] WHERE [CreateDate] >= '2015-07-04 00:00:00.000'")


; export / import

(defn db-extract
  "Extract data from database. Dump each record in a separate file in base-path directory."
  [db-spec query base-path]
  
  (defn write-rec
    "Dump record to disk. Should be in sync with read-rec"
    [path row]
    
    (defn remove-ccn
      [doc]
      (str/replace doc #"([\">])AQAQ(?:[A-Za-z0-9+/]{4}){8,}(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?([\"<])" "$1$2"))		; Same magic, specific to my case
    
    ;(spit (str path "_original") (str (:id row) "\n" (:dtm row) "\n" (:doc row)) :encoding "UTF-8")		; For test only
    (spit path (str (:doc row)) :encoding "UTF-8")							; Write XML only (without CCN information removing)
    ;(spit path (str (:id row) "\n" (:dtm row) "\n" (remove-ccn (:doc row))) :encoding "UTF-8")
    )
  
  (jdbc/with-db-connection [connection db-spec]
    (loop [n 1, rows (jdbc/query connection [query])]
      (let [row (first rows)]
        (if (:id row)
          (do 
            (prn n (:id row))
            (write-rec (str base-path "/" (:id row) ".dump") row)
            (recur (inc n) (rest rows))))))))


(defn db-load
  "Load data into database. Import all files from base-path directory."
  [db-spec table-name base-path]
  
  (defn read-rec
    "Read record from disk. Should be in sync with write-rec"
    [file]
    (let [data (zipmap [:TransactionID :CreateDate :TransactionXMLDoc] (str/split (slurp file :encoding "UTF-8") #"\n" 3))]                   ; 3 fields, 3rd field is text (there is possibility to have \n there)
      (assoc data :BatchID 1, :EtlUser "abelablotski" :LastUpdated nil)))        										; fill other fields (if any) with constants
  
  (loop [n 1, files (file-seq (clojure.java.io/file base-path))]
    (let [file (first files)]
      (when file
        (if (.isDirectory file)
          (do (recur n (rest files)))
          (do (prn n file) (jdbc/insert! db-spec table-name (read-rec file) :transaction? true) (recur (inc n) (rest files))))))))


(defn -main
  []
  ; Import/Export XML docs
  (db-extract db-spec-source extract-query-scoring-20150701 "db_dump")
  (db-load db-spec-target-writeable "[Sales].[dbo].[WebTransaction]" "db_dump")
)
