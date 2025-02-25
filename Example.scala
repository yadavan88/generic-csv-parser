import java.time.LocalDateTime
import java.time.LocalDate
import java.io.File

enum LogType:
    case CaptainsLog, FirstOfficerLog, ChiefMedicalOfficerLog, PersonalLog

case class StarLogs(starDate: Double, logType: LogType, crewId: Int, crewName: String, log: String, starfleetDateTime: LocalDateTime, earthDate: LocalDate)

object StarLogs {
    given CsvParser[LogType] with
        def parse(value: String): LogType = LogType.valueOf(value)
}

@main
def main = {
    
    import StarLogs.given
    println("started parsing... ")

    val parser = GenericCsvParser[StarLogs]
    println("created parser... ")

    val csvData = parser.read(new File("starlog.csv"))

    csvData match {
        case Right(logs) => 
            println(s"read ${logs.size} lines from csv")
            println(logs)
        case Left(error) => println("Error occurred during csv reading: " + error)
    }

}
