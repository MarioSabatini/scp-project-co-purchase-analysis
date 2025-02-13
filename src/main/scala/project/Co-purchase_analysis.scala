package example
import org.apache.spark.sql.SparkSession
import java.nio.file.{Files, Paths}
import org.apache.spark.HashPartitioner
import org.apache.spark.storage.StorageLevel
import org.apache.spark.SparkConf

object CoPurchaseAnalysis {
  def main(args: Array[String]): Unit = {
  
    // Sessione Spark
    val spark = SparkSession
      .builder()
      .appName("Co-Purchase Analysis")
      .getOrCreate()
    val sc = spark.sparkContext

    // calcolo del numero di partizioni in base al numero di core e di nodi nel cluster
    val numNodes = sc.getConf.get("spark.executor.instances")
    val numCores = sc.getConf.get("spark.executor.cores")
    val partitions = (numNodes.toInt * numCores.toInt * 3)
   

    val outputDirectory = args(0) + "results"

    // Leggiamo i dati dal file CSV
    val filePath = args(0) + "order_products.csv"

    // Tempo di esecuzione
    val startTime = System.currentTimeMillis()

    val rdd = sc.textFile(filePath)
    .map(line => line.split(","))
    .map(arr => (arr(0), arr(1)))
    .partitionBy(new HashPartitioner(partitions))
 
   
    val coPurchases = rdd
    .groupByKey() // Raggruppiamo per order_id
    .flatMap { case (orderId, productIds) =>
      val products = productIds.toList 
      for {
        i <- products
        j <- products if i < j
      } yield {
        ((i, j), 1)
      }
    }
    
    val coPurchasesPart =
     coPurchases .partitionBy(new HashPartitioner(partitions))
   
    // Riduciamo per sommare i conteggi delle coppie
    val result = coPurchasesPart
      .reduceByKey(_ + _)
      .map { case ((p1, p2), count) => (p1, p2, count) }
    // Riduciamo ad una sola partizione e salviamo il file
    val singleResult = result.repartition(1)
    singleResult.saveAsTextFile(outputDirectory)
   
    val endTime = System.currentTimeMillis()
    val executionTime = endTime - startTime
    // Stampiamo il tempo di esecuzione
    println(s"Tempo di esecuzione: ${executionTime} ms")
    spark.stop()
  }
}
