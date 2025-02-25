# generic-csv-parser
A generic parser to read from CSV to a case class in Scala 3

Given a path to the CSV file and a required case class T, this converts it into List[T].

This uses the [scala-csv](https://github.com/tototoshi/scala-csv) under the hood to read csv as Seq[Seq[_]] and uses Scala 3 Mirror to convert into case class



# How to run?

Running is very simple, just use the command:
```
scala-cli .
```
