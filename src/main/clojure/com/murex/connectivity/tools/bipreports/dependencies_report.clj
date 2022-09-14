(ns com.murex.connectivity.tools.bipreports.dependencies-report
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            )
  (:import (java.io File Writer)
           (clojure.lang Keyword)))

(defn read-file [f]
  (string/split-lines (slurp f))
  )

(defn parse-line [previous line]
  (let [first-letter-index (first 
                             (filter 
                               #(Character/isLetter (nth line %)) 
                               (range)))
        dep-str (subs line first-letter-index)
        parts (string/split dep-str #":")
        dep-info (apply assoc {} (interleave [::groupId ::artifactId ::type ::version ::scope] parts))
        depth (/ first-letter-index 3)
        ]
    (if (= depth 0)
      {::dependant dep-info}
      {::dependant (::dependant previous)
       ::dependency dep-info
       ::path (vec (take (- depth 1) (conj (::path previous)
                    (::dependency previous)
                    )))
       }
      )
    )
  )

(defn g:a [d] (string/join ":" [(::groupId d) (::artifactId d)]))
(defn g:a:v [d] (string/join ":" [(g:a d) (::version d)]))

(def base-headers ["dependency_ga" "dependency_v" "dependency_scope" "dependent_ga"])
(def report-types [{::name "direct-external-deps", ::filter #(nil? (::path %)), ::header base-headers}
 {::name "transitive-external-deps", ::filter #(nil? (::path %)), ::header (conj base-headers "path")}
 ])

(defn dependency->csv-line[d]
  (let [dependant (::dependant d)
        dependency (::dependency d)
        path (::path d)
        ]
    [(g:a dependency)
     (::version dependency)
     (::scope dependency)
     (g:a dependant)
     (string/join "/" (map g:a:v path))
     ]
    )
  )

(defn parse-deps [lines]
  (conj
    (map dependency->csv-line
         (filter ::dependency (reductions parse-line {} lines))
         )
  ["dependency_ga" "dependency_v" "dependency_scope" "dependent_ga" "path"]
  ))

(defn- write-csv [f lines]
  (with-open [writer (io/writer f)]
    (csv/write-csv writer lines)))

(defn generate-report [sourceFile destinationFile]
  (write-csv destinationFile
             (parse-deps (read-file sourceFile))))

; For pretty printing maps.
(set! *print-namespace-maps* false)
(defmethod print-method Keyword [^Keyword k, ^Writer w]
  (if (.getNamespace k)
    (.write w (str "::" (name k)))
    (.write w (str k))))

(defn -main [& args]
  (generate-report (File. (first args))
                   (File. (second args))))