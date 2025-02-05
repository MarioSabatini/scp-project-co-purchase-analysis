package example
import org.apache.spark.sql.SparkSession
import java.nio.file.{Files, Paths}
import org.apache.spark.RangePartitioner
import org.apache.spark.HashPartitioner
import org.apache.spark.storage.StorageLevel

object CoPurchaseAnalysis {
  def main(args: Array[String]): Unit = {

    // Crea una sessione Spark
    val spark = SparkSession
      .builder()
      .appName("Co-Purchase Analysis")
      .getOrCreate()
    // Ottieni il contesto Spark
    val sc = spark.sparkContext

    val outputDirectory = args(0) + "results"

    // Leggi i dati da un file CSV
    val filePath = args(0) + "order_products.csv"
    val rdd = sc.textFile(filePath)

    // Mappa per ottenere coppie di prodotti
    val coPurchases = rdd
      .map(line => line.split(","))
      .map(arr => (arr(0), arr(1))) // (order_id, product_id)
      .groupByKey() // Raggruppa per order_id
      .flatMap { case (orderId, productIds) =>
        val products = productIds.toArray.sorted // Ordina i prodotti
        for {
          i <- products.indices
          j <- i + 1 until products.length
        } yield {
          // Poiché i prodotti sono già ordinati, non è necessario controllare l'ordine
          ((products(i), products(j)), 1)
        }
      }

    val coPurchasesPart =
      coPurchases.partitionBy(new HashPartitioner(100))
    // Riduci per sommare i conteggi delle coppie
    val result = coPurchasesPart
      .reduceByKey(_ + _) // Somma i conteggi delle coppie
      .map { case ((p1, p2), count) => (p1, p2, count) }

    result.saveAsTextFile(outputDirectory)
    spark.stop()
  }
}
