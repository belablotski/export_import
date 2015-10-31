# export_import

Clojure utility created to facilitate database migration 
(in my case - between environments: create subset of production records to feed a DEV/TEST databases).

The export_import utility (inspired by Oracle exp/imp) exports data from DB on local computer and imports data from local computer into DB. It's also possible to process data in between (there is regex-based replace into committed code). De-facto here is simple Clojure ETL framework, which transforms data on your machine. It can be useful for backup/restore as well.

*Note:* Each record is stored in a separate file. This utility was deigned for huge records, which contain XML documents. But fill free to change serialization format according your specific needs.

## Usage

1. Setup import or export into src/core.clj
2. lein run > log.txt

## License

Copyright © 2015 Aliaksei Belablotski

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
