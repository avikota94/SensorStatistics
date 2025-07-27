# Sensor Statistics - Usage Examples

This document provides comprehensive examples demonstrating various ways to use the Sensor Statistics API.

## Table of Contents

1. [Basic Usage Examples](#basic-usage-examples)
2. [Advanced Processing Examples](#advanced-processing-examples)
3. [Integration Examples](#integration-examples)
4. [Error Handling Examples](#error-handling-examples)
5. [Performance Optimization Examples](#performance-optimization-examples)
6. [Real-World Scenarios](#real-world-scenarios)

---

## Basic Usage Examples

### Example 1: Simple File Processing

```scala
import com.sensor.SensorStatisticsDTO

object BasicExample extends App {
  val stats = new SensorStatisticsDTO()
  
  // Process files in the resources directory
  val fileCount = stats.numOfProcessedFiles("resources")
  println(s"Found $fileCount CSV files")
  
  // Get basic statistics
  val totalMeasurements = stats.numOfProcessedMeasurements()
  val failedMeasurements = stats.numOfFailedMeasurements()
  
  println(s"Total measurements: $totalMeasurements")
  println(s"Failed measurements: $failedMeasurements")
  println(s"Success rate: ${((totalMeasurements - failedMeasurements).toDouble / totalMeasurements * 100).round}%")
  
  // Display detailed statistics
  stats.minAvgMaxHumidity()
  stats.sortsSensorsByHighestAvgHumidity()
}
```

### Example 2: Processing Custom Directory

```scala
import com.sensor.SensorStatisticsDTO
import java.io.File

object CustomDirectoryExample extends App {
  val customPath = "/home/user/sensor-data"
  val stats = new SensorStatisticsDTO()
  
  try {
    val fileCount = stats.numOfProcessedFiles(customPath)
    
    if (fileCount > 0) {
      println(s"Processing $fileCount files from $customPath")
      
      val measurements = stats.numOfProcessedMeasurements()
      val failed = stats.numOfFailedMeasurements()
      
      println(s"Processed $measurements measurements")
      println(s"$failed measurements failed")
      
      // Show sensor statistics
      println("\nSensor Statistics:")
      stats.minAvgMaxHumidity()
      
      println("\nSensor Ranking:")
      stats.sortsSensorsByHighestAvgHumidity()
    } else {
      println("No CSV files found in the specified directory")
    }
  } catch {
    case e: Exception => 
      println(s"Error processing directory: ${e.getMessage}")
  }
}
```

### Example 3: Programmatic Usage with Results Capture

```scala
import com.sensor.{SensorStatisticsDTO, SensorStatistics}
import java.io.{ByteArrayOutputStream, PrintStream}

object CaptureOutputExample extends App {
  def captureOutput[T](block: => T): (T, String) = {
    val baos = new ByteArrayOutputStream()
    val ps = new PrintStream(baos)
    val old = System.out
    
    try {
      System.setOut(ps)
      val result = block
      (result, baos.toString)
    } finally {
      System.setOut(old)
      ps.close()
    }
  }
  
  val stats = new SensorStatisticsDTO()
  stats.numOfProcessedFiles("resources")
  stats.numOfProcessedMeasurements()
  
  // Capture the output of statistics methods
  val (_, minMaxOutput) = captureOutput {
    stats.minAvgMaxHumidity()
  }
  
  val (_, rankingOutput) = captureOutput {
    stats.sortsSensorsByHighestAvgHumidity()
  }
  
  println("Captured Statistics Output:")
  println(minMaxOutput)
  println("\nCaptured Ranking Output:")
  println(rankingOutput)
}
```

---

## Advanced Processing Examples

### Example 4: Batch Processing Multiple Directories

```scala
import com.sensor.SensorStatisticsDTO
import scala.util.{Try, Success, Failure}

object BatchProcessingExample extends App {
  val directories = List(
    "data/2023/january",
    "data/2023/february", 
    "data/2023/march"
  )
  
  case class ProcessingResult(
    directory: String,
    fileCount: Int,
    totalMeasurements: Int,
    failedMeasurements: Int,
    successRate: Double
  )
  
  val results = directories.map { dir =>
    Try {
      val stats = new SensorStatisticsDTO()
      val fileCount = stats.numOfProcessedFiles(dir)
      
      if (fileCount > 0) {
        val total = stats.numOfProcessedMeasurements()
        val failed = stats.numOfFailedMeasurements()
        val successRate = ((total - failed).toDouble / total * 100)
        
        Some(ProcessingResult(dir, fileCount, total, failed, successRate))
      } else {
        None
      }
    } match {
      case Success(result) => result
      case Failure(exception) => 
        println(s"Failed to process $dir: ${exception.getMessage}")
        None
    }
  }.flatten
  
  // Display summary
  println("Batch Processing Summary:")
  println("=" * 50)
  results.foreach { result =>
    println(f"Directory: ${result.directory}")
    println(f"Files: ${result.fileCount}")
    println(f"Measurements: ${result.totalMeasurements}")
    println(f"Success Rate: ${result.successRate}%.1f%%")
    println("-" * 30)
  }
  
  // Overall statistics
  val totalFiles = results.map(_.fileCount).sum
  val totalMeasurements = results.map(_.totalMeasurements).sum
  val totalFailed = results.map(_.failedMeasurements).sum
  val overallSuccessRate = ((totalMeasurements - totalFailed).toDouble / totalMeasurements * 100)
  
  println(f"Overall Summary:")
  println(f"Total Files: $totalFiles")
  println(f"Total Measurements: $totalMeasurements")
  println(f"Overall Success Rate: $overallSuccessRate%.1f%%")
}
```

### Example 5: Custom Statistics with Filtering

```scala
import com.sensor.{SensorStatisticsDTO, SensorMeasurment}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext

class CustomSensorAnalyzer extends SensorStatisticsDTO {
  
  def getHighQualitySensors(threshold: Double = 90.0): List[String] = {
    // This would require modifying the original class to expose internal data
    // For demonstration purposes, showing the concept
    val sensorQuality = Map(
      "s1" -> 85.5,
      "s2" -> 92.3,
      "s3" -> 45.2
    )
    
    sensorQuality.filter(_._2 >= threshold).keys.toList
  }
  
  def getAverageHumidityRange(sensorId: String): Option[(Int, Int, Double)] = {
    // Return (min, max, avg) for a specific sensor
    // This is a conceptual example
    sensorId match {
      case "s1" => Some((10, 98, 54.0))
      case "s2" => Some((78, 88, 84.0))
      case "s3" => None // All NaN values
      case _ => None
    }
  }
}

object CustomAnalysisExample extends App {
  val analyzer = new CustomSensorAnalyzer()
  
  analyzer.numOfProcessedFiles("resources")
  analyzer.numOfProcessedMeasurements()
  
  // Use custom methods
  val highQualitySensors = analyzer.getHighQualitySensors(80.0)
  println(s"High quality sensors (>80% success rate): $highQualitySensors")
  
  // Analyze specific sensors
  List("s1", "s2", "s3").foreach { sensorId =>
    analyzer.getAverageHumidityRange(sensorId) match {
      case Some((min, max, avg)) =>
        println(f"Sensor $sensorId: Range $min-$max, Average $avg%.1f")
      case None =>
        println(s"Sensor $sensorId: No valid data")
    }
  }
}
```

---

## Integration Examples

### Example 6: Spring Boot Integration

```scala
// This is a conceptual example showing how to integrate with Spring Boot
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.{GetMapping, RequestParam, RestController}
import com.sensor.SensorStatisticsDTO

@SpringBootApplication
class SensorStatsApplication

@RestController
class SensorStatsController {
  
  @GetMapping("/api/stats/files")
  def getFileCount(@RequestParam directory: String): Map[String, Any] = {
    try {
      val stats = new SensorStatisticsDTO()
      val count = stats.numOfProcessedFiles(directory)
      Map("directory" -> directory, "fileCount" -> count, "status" -> "success")
    } catch {
      case e: Exception => 
        Map("error" -> e.getMessage, "status" -> "error")
    }
  }
  
  @GetMapping("/api/stats/measurements")
  def getMeasurementStats(@RequestParam directory: String): Map[String, Any] = {
    try {
      val stats = new SensorStatisticsDTO()
      stats.numOfProcessedFiles(directory)
      val total = stats.numOfProcessedMeasurements()
      val failed = stats.numOfFailedMeasurements()
      
      Map(
        "totalMeasurements" -> total,
        "failedMeasurements" -> failed,
        "successRate" -> ((total - failed).toDouble / total * 100),
        "status" -> "success"
      )
    } catch {
      case e: Exception =>
        Map("error" -> e.getMessage, "status" -> "error")
    }
  }
}

object SpringBootExample extends App {
  SpringApplication.run(classOf[SensorStatsApplication], args: _*)
}
```

### Example 7: Akka Streams Integration

```scala
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Source, Sink}
import akka.stream.{ActorMaterializer, IOResult}
import com.sensor.SensorStatisticsDTO
import scala.concurrent.Future
import scala.util.{Success, Failure}

object AkkaStreamsExample extends App {
  implicit val system = ActorSystem("sensor-stats")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher
  
  case class DirectoryProcessingTask(directory: String)
  case class ProcessingResult(directory: String, measurements: Int, failed: Int)
  
  val directories = List(
    "data/batch1",
    "data/batch2", 
    "data/batch3"
  )
  
  val processingFlow = Source(directories)
    .map(DirectoryProcessingTask)
    .mapAsync(2) { task =>
      Future {
        val stats = new SensorStatisticsDTO()
        stats.numOfProcessedFiles(task.directory)
        val total = stats.numOfProcessedMeasurements()
        val failed = stats.numOfFailedMeasurements()
        ProcessingResult(task.directory, total, failed)
      }
    }
    .runWith(Sink.foreach { result =>
      val successRate = ((result.measurements - result.failed).toDouble / result.measurements * 100)
      println(f"${result.directory}: ${result.measurements} measurements, $successRate%.1f%% success")
    })
  
  processingFlow.onComplete {
    case Success(_) => 
      println("All directories processed successfully")
      system.terminate()
    case Failure(exception) => 
      println(s"Processing failed: ${exception.getMessage}")
      system.terminate()
  }
}
```

---

## Error Handling Examples

### Example 8: Comprehensive Error Handling

```scala
import com.sensor.SensorStatisticsDTO
import scala.util.{Try, Success, Failure}
import java.io.File

object ErrorHandlingExample extends App {
  
  sealed trait ProcessingError
  case class DirectoryNotFound(path: String) extends ProcessingError
  case class NoFilesFound(path: String) extends ProcessingError
  case class ProcessingFailed(path: String, cause: Throwable) extends ProcessingError
  
  def safeProcessDirectory(directory: String): Either[ProcessingError, (Int, Int, Int)] = {
    Try {
      val stats = new SensorStatisticsDTO()
      
      // Check if directory exists
      val dir = new File(directory)
      if (!dir.exists() || !dir.isDirectory) {
        return Left(DirectoryNotFound(directory))
      }
      
      val fileCount = stats.numOfProcessedFiles(directory)
      if (fileCount == 0) {
        return Left(NoFilesFound(directory))
      }
      
      val total = stats.numOfProcessedMeasurements()
      val failed = stats.numOfFailedMeasurements()
      
      (fileCount, total, failed)
    } match {
      case Success(result) => Right(result)
      case Failure(exception) => Left(ProcessingFailed(directory, exception))
    }
  }
  
  val testDirectories = List(
    "resources",           // Valid directory
    "nonexistent",        // Non-existent directory
    "empty-directory"     // Empty directory (if exists)
  )
  
  testDirectories.foreach { dir =>
    safeProcessDirectory(dir) match {
      case Right((files, total, failed)) =>
        val successRate = ((total - failed).toDouble / total * 100)
        println(s"✓ $dir: $files files, $total measurements, $successRate%.1f%% success")
        
      case Left(DirectoryNotFound(path)) =>
        println(s"✗ Directory not found: $path")
        
      case Left(NoFilesFound(path)) =>
        println(s"⚠ No CSV files found in: $path")
        
      case Left(ProcessingFailed(path, cause)) =>
        println(s"✗ Processing failed for $path: ${cause.getMessage}")
    }
  }
}
```

### Example 9: Retry Logic with Exponential Backoff

```scala
import com.sensor.SensorStatisticsDTO
import scala.util.{Try, Success, Failure}
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit

object RetryExample extends App {
  
  def withRetry[T](
    operation: () => T,
    maxRetries: Int = 3,
    baseDelay: Duration = 1.second
  ): Try[T] = {
    
    def attempt(retriesLeft: Int, delay: Duration): Try[T] = {
      Try(operation()) match {
        case success @ Success(_) => success
        case Failure(exception) if retriesLeft > 0 =>
          println(s"Operation failed, retrying in ${delay.toSeconds}s... ($retriesLeft retries left)")
          Thread.sleep(delay.toMillis)
          attempt(retriesLeft - 1, delay * 2) // Exponential backoff
        case failure => failure
      }
    }
    
    attempt(maxRetries, baseDelay)
  }
  
  def processWithRetry(directory: String): Try[(Int, Int, Int)] = {
    withRetry(() => {
      val stats = new SensorStatisticsDTO()
      val files = stats.numOfProcessedFiles(directory)
      val total = stats.numOfProcessedMeasurements()
      val failed = stats.numOfFailedMeasurements()
      (files, total, failed)
    })
  }
  
  // Test with potentially flaky directory
  val directory = "resources"
  
  processWithRetry(directory) match {
    case Success((files, total, failed)) =>
      println(s"Successfully processed $directory: $files files, $total measurements")
    case Failure(exception) =>
      println(s"Failed to process $directory after retries: ${exception.getMessage}")
  }
}
```

---

## Performance Optimization Examples

### Example 10: Memory-Optimized Processing

```scala
import com.sensor.SensorStatisticsDTO
import org.apache.spark.{SparkConf, SparkContext}

object OptimizedProcessingExample extends App {
  
  class OptimizedSensorStats extends SensorStatisticsDTO {
    
    // Override to use optimized Spark configuration
    def processWithOptimizedConfig(directory: String): Unit = {
      val conf = new SparkConf()
        .setAppName("Optimized Sensor Statistics")
        .setMaster("local[*]")
        // Memory optimization settings
        .set("spark.sql.adaptive.enabled", "true")
        .set("spark.sql.adaptive.coalescePartitions.enabled", "true")
        .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
        .set("spark.sql.adaptive.skewJoin.enabled", "true")
        // Reduce memory pressure
        .set("spark.sql.execution.arrow.pyspark.enabled", "false")
        .set("spark.sql.adaptive.localShuffleReader.enabled", "true")
      
      // Process with optimized configuration
      numOfProcessedFiles(directory)
      numOfProcessedMeasurements()
      
      println("Processing completed with optimized configuration")
    }
  }
  
  val optimizedStats = new OptimizedSensorStats()
  
  // Monitor memory usage
  val runtime = Runtime.getRuntime
  val maxMemory = runtime.maxMemory() / (1024 * 1024)
  val totalMemory = runtime.totalMemory() / (1024 * 1024)
  val freeMemory = runtime.freeMemory() / (1024 * 1024)
  val usedMemory = totalMemory - freeMemory
  
  println(s"Memory Status Before Processing:")
  println(s"Max Memory: ${maxMemory}MB")
  println(s"Used Memory: ${usedMemory}MB")
  println(s"Free Memory: ${freeMemory}MB")
  
  // Process data
  optimizedStats.processWithOptimizedConfig("resources")
  
  // Check memory after processing
  val usedMemoryAfter = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
  println(s"Used Memory After Processing: ${usedMemoryAfter}MB")
  println(s"Memory Increase: ${usedMemoryAfter - usedMemory}MB")
}
```

### Example 11: Parallel Processing Multiple Datasets

```scala
import com.sensor.SensorStatisticsDTO
import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Success, Failure}
import java.util.concurrent.Executors

object ParallelProcessingExample extends App {
  
  // Create custom thread pool for parallel processing
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(
    Executors.newFixedThreadPool(4)
  )
  
  case class DatasetResult(
    name: String,
    files: Int,
    measurements: Int,
    failed: Int,
    processingTimeMs: Long
  )
  
  def processDatasetAsync(name: String, directory: String): Future[DatasetResult] = {
    Future {
      val startTime = System.currentTimeMillis()
      
      val stats = new SensorStatisticsDTO()
      val files = stats.numOfProcessedFiles(directory)
      val measurements = stats.numOfProcessedMeasurements()
      val failed = stats.numOfFailedMeasurements()
      
      val endTime = System.currentTimeMillis()
      
      DatasetResult(name, files, measurements, failed, endTime - startTime)
    }
  }
  
  val datasets = List(
    ("Q1 Data", "data/q1"),
    ("Q2 Data", "data/q2"),
    ("Q3 Data", "data/q3"),
    ("Q4 Data", "data/q4")
  )
  
  // Process all datasets in parallel
  val futures = datasets.map { case (name, dir) =>
    processDatasetAsync(name, dir)
  }
  
  // Combine all results
  val allResults = Future.sequence(futures)
  
  allResults.onComplete {
    case Success(results) =>
      println("Parallel Processing Results:")
      println("=" * 60)
      
      results.foreach { result =>
        val successRate = ((result.measurements - result.failed).toDouble / result.measurements * 100)
        println(f"${result.name}:")
        println(f"  Files: ${result.files}")
        println(f"  Measurements: ${result.measurements}")
        println(f"  Success Rate: $successRate%.1f%%")
        println(f"  Processing Time: ${result.processingTimeMs}ms")
        println()
      }
      
      // Overall statistics
      val totalFiles = results.map(_.files).sum
      val totalMeasurements = results.map(_.measurements).sum
      val totalFailed = results.map(_.failed).sum
      val totalTime = results.map(_.processingTimeMs).sum
      val overallSuccessRate = ((totalMeasurements - totalFailed).toDouble / totalMeasurements * 100)
      
      println("Overall Summary:")
      println(f"Total Files: $totalFiles")
      println(f"Total Measurements: $totalMeasurements")
      println(f"Overall Success Rate: $overallSuccessRate%.1f%%")
      println(f"Total Processing Time: ${totalTime}ms")
      
      // Shutdown thread pool
      ec.shutdown()
      
    case Failure(exception) =>
      println(s"Parallel processing failed: ${exception.getMessage}")
      ec.shutdown()
  }
  
  // Keep main thread alive until processing completes
  Thread.sleep(10000)
}
```

---

## Real-World Scenarios

### Example 12: IoT Data Processing Pipeline

```scala
import com.sensor.{SensorStatisticsDTO, SensorMeasurment}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object IoTDataPipelineExample extends App {
  
  case class IoTSensorReading(
    sensorId: String,
    humidity: Option[Double],
    timestamp: LocalDateTime,
    deviceStatus: String
  )
  
  class IoTDataProcessor extends SensorStatisticsDTO {
    
    def generateDailyReport(date: String): String = {
      val directory = s"data/iot/$date"
      
      try {
        val files = numOfProcessedFiles(directory)
        val total = numOfProcessedMeasurements()
        val failed = numOfFailedMeasurements()
        val successRate = ((total - failed).toDouble / total * 100)
        
        s"""
        |IoT Sensor Daily Report - $date
        |=====================================
        |Files Processed: $files
        |Total Readings: $total
        |Failed Readings: $failed
        |Success Rate: ${successRate.round}%
        |
        |Sensor Performance:
        """.stripMargin
      } catch {
        case e: Exception =>
          s"Error generating report for $date: ${e.getMessage}"
      }
    }
    
    def detectAnomalies(thresholdSuccessRate: Double = 95.0): List[String] = {
      // This would require access to individual sensor data
      // Conceptual implementation
      List("sensor_001", "sensor_045") // Sensors with low success rates
    }
    
    def generateAlerts(): List[String] = {
      val anomalies = detectAnomalies()
      anomalies.map(sensorId => s"ALERT: Sensor $sensorId has poor data quality")
    }
  }
  
  val processor = new IoTDataProcessor()
  val today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
  
  // Generate daily report
  val report = processor.generateDailyReport(today)
  println(report)
  
  // Check for alerts
  val alerts = processor.generateAlerts()
  if (alerts.nonEmpty) {
    println("ALERTS:")
    alerts.foreach(println)
  } else {
    println("No alerts - all sensors operating normally")
  }
}
```

### Example 13: Quality Control Dashboard Data

```scala
import com.sensor.SensorStatisticsDTO
import scala.collection.mutable

object QualityControlDashboard extends App {
  
  case class SensorMetrics(
    sensorId: String,
    totalReadings: Int,
    failedReadings: Int,
    minHumidity: Option[Double],
    maxHumidity: Option[Double],
    avgHumidity: Option[Double],
    qualityScore: Double
  )
  
  class QualityControlAnalyzer extends SensorStatisticsDTO {
    
    def generateQualityMetrics(): List[SensorMetrics] = {
      // This is a conceptual implementation
      // In reality, you'd need to modify the base class to expose sensor-level data
      
      val mockData = List(
        SensorMetrics("TEMP_001", 1440, 12, Some(45.2), Some(67.8), Some(56.5), 99.2),
        SensorMetrics("TEMP_002", 1440, 45, Some(38.1), Some(72.3), Some(55.2), 96.9),
        SensorMetrics("TEMP_003", 1440, 156, Some(42.7), Some(69.4), Some(54.8), 89.2),
        SensorMetrics("HUM_001", 1440, 8, Some(65.3), Some(89.7), Some(77.5), 99.4),
        SensorMetrics("HUM_002", 1440, 234, Some(58.9), Some(85.2), Some(72.1), 83.8)
      )
      
      mockData
    }
    
    def generateQualityReport(): String = {
      val metrics = generateQualityMetrics()
      val totalSensors = metrics.length
      val highQualitySensors = metrics.count(_.qualityScore >= 95.0)
      val lowQualitySensors = metrics.count(_.qualityScore < 90.0)
      
      val report = new StringBuilder
      report.append("Sensor Quality Control Report\n")
      report.append("=" * 40 + "\n\n")
      
      report.append(f"Total Sensors: $totalSensors\n")
      report.append(f"High Quality (≥95%%): $highQualitySensors\n")
      report.append(f"Low Quality (<90%%): $lowQualitySensors\n\n")
      
      report.append("Individual Sensor Status:\n")
      report.append("-" * 40 + "\n")
      
      metrics.sortBy(-_.qualityScore).foreach { metric =>
        val status = metric.qualityScore match {
          case score if score >= 95.0 => "EXCELLENT"
          case score if score >= 90.0 => "GOOD"
          case score if score >= 80.0 => "FAIR"
          case _ => "POOR"
        }
        
        report.append(f"${metric.sensorId}: ${metric.qualityScore}%.1f%% - $status\n")
        if (metric.avgHumidity.isDefined) {
          report.append(f"  Avg: ${metric.avgHumidity.get}%.1f, ")
          report.append(f"Range: ${metric.minHumidity.get}%.1f - ${metric.maxHumidity.get}%.1f\n")
        }
        report.append(f"  Failed: ${metric.failedReadings}/${metric.totalReadings}\n\n")
      }
      
      report.toString()
    }
  }
  
  val analyzer = new QualityControlAnalyzer()
  
  // Process current data
  analyzer.numOfProcessedFiles("resources")
  analyzer.numOfProcessedMeasurements()
  
  // Generate quality report
  val qualityReport = analyzer.generateQualityReport()
  println(qualityReport)
  
  // Export to file for dashboard consumption
  import java.io.PrintWriter
  val writer = new PrintWriter("quality_report.txt")
  try {
    writer.write(qualityReport)
    println("Quality report exported to quality_report.txt")
  } finally {
    writer.close()
  }
}
```

These examples demonstrate various ways to use the Sensor Statistics API, from basic usage to advanced integration scenarios. Each example includes practical code that can be adapted to specific use cases and requirements.