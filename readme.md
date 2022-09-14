# Parse Dependency Trees file and output CSV in Clojure
Coding session in Clojure for parsing multiple maven dependency:tree files and outputting the result in a csv file.

At Luxoft one of our Maven projects has 944 modules+submodules, and it's important to be able to query for dependencies fast and easy when dealing with security issues, Java 17 upgrade or whatever.

We already have a tool for this, but, for fun, we wanted to see how we might be writing it in Clojure.

Clojure is different from our beloved Java in two big ways: it's functional and it's dynamic typed, which makes the thinking exercise interesting.

