import com.github.tototoshi.csv.CSVReader
import java.io.File
import scala.util.Using

object CSVTextParser {

  def parseCsv(file: File): Either[String, List[List[String]]] = {
    Using(CSVReader.open(file)) { reader =>
      reader.all()
    }.toEither.left.map(_.getMessage)
  }
}