# generic-csv-parser
A generic parser to read from CSV to a case class in Scala 3

Given a path to the CSV file and a required case class T, this converts it into List[T].

This uses the [scala-csv](https://github.com/tototoshi/scala-csv) under the hood to read csv as Seq[Seq[_]] and uses Scala 3 Mirror to convert into case class

**Note: This is still work in progress!**

# How to run the sample?

Running is very simple, just use the command:
```
scala-cli .
```

# Usage

You can create the required case class that are required for mapping the csv. Only the basic types such as String, Int, Long, Double, LocalDate and LocalDateTime are now supported out of the box. For other types, it is possible to provide a parser as given instance and it uses it.

Here is an example:

```
enum LogType:
    case CaptainsLog, FirstOfficerLog, ChiefMedicalOfficerLog, PersonalLog

case class StarLogs(starDate: Double, logType: LogType, crewId: Int, crewName: String, log: String, starfleetDateTime: LocalDateTime, earthDate: LocalDate)

object StarLogs {
    given CsvParser[LogType] with
        def parse(value: String): LogType = LogType.valueOf(value)
}

val parser = GenericCsvParser[StarLogs]
val csvData: Either[String, List[StarLogs]] = parser.read(new File("starlog.csv"))  
```

In this case, we provided a custom given instance to map the enum LogType.
