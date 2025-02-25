import scala.io.Source
import java.io.File
import scala.deriving.Mirror
import scala.compiletime.{erasedValue, summonInline}
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset
import scala.util.Try

class GenericCsvParser[T <: Product] {

  inline def read(file: File)(using m: Mirror.ProductOf[T]): Either[String, List[T]] = {
    CSVTextParser.parseCsv(file) match {
      case Right(rows) =>
        val header = rows.head
        println(s"Header: ${header.mkString(", ")}")
        val dataRows = rows.tail

        val parsedRows = dataRows.zipWithIndex
          .foldLeft[Either[String, List[T]]](Right(List.empty)) { case (acc, (row, index)) =>
            for {
              list <- acc
              obj <- fromCsvRow[T](row).left.map(err => s"Row ${index + 1}: $err")
            } yield obj :: list
          }
          .map(_.reverse)

        parsedRows
      case Left(err) => Left(s"CSV Parsing Error: $err")
    }
  }

  inline def tupleFromCsv[T <: Tuple](values: List[String], parsers: List[CsvParser[?]]): T =
    values.zip(parsers).map { case (v, parser) =>
      parser.asInstanceOf[CsvParser[Any]].parse(v)
    } match {
      case list => Tuple.fromArray(list.toArray).asInstanceOf[T]
    }

  inline def fromCsvRow[A](row: List[String])(using m: Mirror.ProductOf[A]): Either[String, A] = {
    Try {
      val parsers = summonParsers[m.MirroredElemTypes]
      require(
        row.length == parsers.length,
        s"Number of columns in CSV (${row.length}) does not match the number of fields in case class (${parsers.length})"
      )
      val tuple = tupleFromCsv[m.MirroredElemTypes](row, parsers)
      m.fromProduct(tuple)
    }.toEither.left.map(_.getMessage)
  }

  inline def summonParsers[T <: Tuple]: List[CsvParser[?]] =
    inline erasedValue[T] match
      case _: (t *: ts)  => summonInline[CsvParser[t]] :: summonParsers[ts]
      case _: EmptyTuple => Nil

}

trait CsvParser[T]:
  def parse(value: String): T

object CsvParser:
  given CsvParser[String] with
    def parse(value: String): String = value.trim

  given CsvParser[Int] with
    def parse(value: String): Int = value.trim.toInt

  given CsvParser[Long] with
    def parse(value: String): Long = value.trim.toLong

  given CsvParser[Double] with
    def parse(value: String): Double = value.trim.toDouble

  given CsvParser[LocalDate] with
    def parse(value: String): LocalDate =
      LocalDate.parse(value.trim, DateTimeFormatter.ISO_DATE)

  given CsvParser[LocalDateTime] with
    def parse(value: String): LocalDateTime =
      val valueTrimmed = value.trim
      if valueTrimmed.endsWith("Z") then
        LocalDateTime
          .parse(valueTrimmed.dropRight(1), DateTimeFormatter.ISO_DATE_TIME)
          .atOffset(ZoneOffset.UTC)
          .toLocalDateTime
      else LocalDateTime.parse(valueTrimmed, DateTimeFormatter.ISO_DATE_TIME)

  given optionCsvParser[T](using parser: CsvParser[T]): CsvParser[Option[T]] with
    def parse(value: String): Option[T] =
      val trimmed = value.trim
      if trimmed.isEmpty then None else Some(parser.parse(trimmed))    