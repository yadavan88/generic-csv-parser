# Generic CSV Parser in Scala 3

## Overview
This project provides a **generic CSV parser** in Scala 3 that can read CSV files and map them to case classes using **type-safe generic programming**. It supports various built-in types and allows custom parsing logic for user-defined types.

## Features
- **Generic Parsing**: Uses Scala 3's **Mirror API** and **inline metaprogramming** to derive parsers automatically.
- **Built-in Type Support**:
  - `String`, `Int`, `Long`, `Double`
  - `LocalDate`, `LocalDateTime` (formatted as ISO date/time)
  - `Option[T]` (handles empty values as `None`)
- **Custom Parsing**: Easily extendable for custom types (e.g., enums).
- **Error Handling**: Reports errors with row numbers for easier debugging.

## Run
You can run this sample project using the scala-cli:

```
scala-cli .
```

## Usage

### 1. Define Your Case Class
Create a case class that matches the structure of your CSV file.

```scala
import java.time.LocalDate
import java.time.LocalDateTime

enum LogType:
  case CaptainsLog, FirstOfficerLog, ChiefMedicalOfficerLog, PersonalLog

case class StarLogs(
    starDate: Double,
    logType: LogType,
    crewId: Int,
    crewName: String,
    log: String,
    starfleetDateTime: LocalDateTime,
    earthDate: LocalDate
)
```

### 2. Define Custom Parsing (if needed)
If your case class contains custom types (like enums), define a `CsvParser` for them:

```scala
object StarLogs {
  given CsvParser[LogType] with
    def parse(value: String): LogType = LogType.valueOf(value)
}
```

### 3. Parse a CSV File
Create an instance of `GenericCsvParser` and read a CSV file:

```scala
import java.io.File
import StarLogs.given

@main
def main = {
  val parser = GenericCsvParser[StarLogs]
  val csvData = parser.read(new File("starlog.csv"))

  csvData match {
    case Right(logs) =>
      println(s"Successfully read ${logs.size} logs:")
      logs.foreach(println)
    case Left(error) => println("Error: " + error)
  }
}
```

### 4. Handling Optional Fields
The parser supports `Option[T]`, treating empty fields as `None`:

```scala
case class CrewMember(
    id: Int,
    name: String,
    rank: Option[String] // Can be None if missing in CSV
)
```

## How It Works
- **Generic Parsing:** Uses `Mirror.ProductOf[T]` to extract field types at compile-time.
- **Tuple Conversion:** Converts CSV rows into tuples and then into case class instances.
- **Summoning Parsers:** Automatically finds the appropriate `CsvParser` for each field.
- **scala-csv under the hood:** Uses [scala-csv](https://github.com/tototoshi/scala-csv) library to read the file to List[List[_]] before building the case class

## Extending the Parser
To support a **new type**, define a `CsvParser` instance:

```scala
given CsvParser[Boolean] with
  def parse(value: String): Boolean = value.trim.toBoolean
```

Then, you can use `Boolean` fields in your case classes.

## Error Handling
If a CSV row has a parsing error, it will return `Left("Row X: error message")`, helping you debug issues easily.

