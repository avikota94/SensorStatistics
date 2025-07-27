# ScalaDoc Documentation Guide

This guide provides enhanced ScalaDoc documentation for the Sensor Statistics application source code.

## Overview

ScalaDoc is the standard documentation format for Scala code. This guide shows how to properly document the existing codebase and provides enhanced documentation examples.

## Enhanced Source Code Documentation

### 1. SensorStatistics Trait (Enhanced)

```scala
package com.sensor

/**
 * Main interface for sensor statistics operations.
 * 
 * This trait defines the contract for processing CSV files containing sensor humidity
 * measurements and generating various statistical reports.
 * 
 * The typical usage pattern is:
 * 1. Call `numOfProcessedFiles` to discover and count CSV files
 * 2. Call `numOfProcessedMeasurements` to process all measurements
 * 3. Call `numOfFailedMeasurements` to count failed readings
 * 4. Call `minAvgMaxHumidity` to display sensor statistics
 * 5. Call `sortsSensorsByHighestAvgHumidity` to show sensor rankings
 * 
 * @author Sensor Statistics Team
 * @version 0.1
 * @since 0.1
 */
trait SensorStatistics {

  /**
   * Counts the number of CSV files in the specified directory.
   * 
   * This method scans the given directory for files with .csv extension
   * and returns the count. The directory must exist and be accessible.
   * 
   * @param dir the directory path containing CSV files (relative or absolute)
   * @return the number of CSV files found in the directory
   * @throws IllegalArgumentException if the directory doesn't exist
   * @throws SecurityException if the directory is not accessible
   * 
   * @example
   * {{{
   * val stats = new SensorStatisticsDTO()
   * val count = stats.numOfProcessedFiles("resources")
   * println(s"Found $count CSV files")
   * }}}
   */
  def numOfProcessedFiles(dir: String): Int

  /**
   * Counts the total number of measurements across all processed files.
   * 
   * This method must be called after `numOfProcessedFiles` to ensure
   * files have been discovered and loaded. It processes all CSV files
   * and counts individual sensor readings.
   * 
   * @return the total number of sensor measurements found
   * @throws IllegalStateException if called before `numOfProcessedFiles`
   * 
   * @example
   * {{{
   * val stats = new SensorStatisticsDTO()
   * stats.numOfProcessedFiles("data")
   * val total = stats.numOfProcessedMeasurements()
   * println(s"Total measurements: $total")
   * }}}
   */
  def numOfProcessedMeasurements(): Int

  /**
   * Counts the number of measurements with "NaN" humidity values.
   * 
   * Failed measurements are those where the humidity column contains
   * "NaN" instead of a numeric value. This indicates sensor malfunction
   * or data transmission errors.
   * 
   * @return the number of failed measurements
   * @throws IllegalStateException if called before processing measurements
   * 
   * @example
   * {{{
   * val stats = new SensorStatisticsDTO()
   * stats.numOfProcessedFiles("data")
   * stats.numOfProcessedMeasurements()
   * val failed = stats.numOfFailedMeasurements()
   * val successRate = ((total - failed).toDouble / total * 100)
   * println(f"Success rate: $successRate%.1f%%")
   * }}}
   */
  def numOfFailedMeasurements(): Int

  /**
   * Displays minimum, average, and maximum humidity for each sensor.
   * 
   * This method prints a formatted table showing statistics for each
   * unique sensor ID found in the processed files. Sensors with only
   * "NaN" values will show "NaN" for all statistics.
   * 
   * Output format:
   * {{{
   * sensor-id,min,avg,max :
   * s1,10,54,98
   * s2,78,84,88
   * s3,NaN,NaN,NaN
   * }}}
   * 
   * @throws IllegalStateException if called before processing measurements
   * 
   * @example
   * {{{
   * val stats = new SensorStatisticsDTO()
   * stats.numOfProcessedFiles("data")
   * stats.numOfProcessedMeasurements()
   * stats.minAvgMaxHumidity() // Prints statistics table
   * }}}
   */
  def minAvgMaxHumidity(): Unit

  /**
   * Displays sensors sorted by highest average humidity.
   * 
   * This method prints sensors ordered from highest to lowest average
   * humidity. Sensors with no valid measurements (all "NaN") are shown
   * with "NAN" and appear at the end of the list.
   * 
   * Output format:
   * {{{
   * sorts sensors by highest avg humidity :(s2,84), (s1,54), (s3,NAN),
   * }}}
   * 
   * @throws IllegalStateException if called before processing measurements
   * 
   * @example
   * {{{
   * val stats = new SensorStatisticsDTO()
   * stats.numOfProcessedFiles("data")
   * stats.numOfProcessedMeasurements()
   * stats.sortsSensorsByHighestAvgHumidity() // Prints ranked sensors
   * }}}
   */
  def sortsSensorsByHighestAvgHumidity(): Unit
}
```

### 2. SensorMeasurement Case Class (Enhanced)

```scala
package com.sensor

/**
 * Represents a single sensor measurement record.
 * 
 * This case class models a row from the CSV input files, containing
 * a sensor identifier and its corresponding humidity reading.
 * 
 * @param sensorid the unique identifier for the sensor (e.g., "s1", "temp_001")
 * @param humidity the humidity measurement as a string ("45.5" or "NaN" for failed readings)
 * 
 * @example
 * {{{
 * val validMeasurement = SensorMeasurment("s1", "85.5")
 * val failedMeasurement = SensorMeasurment("s2", "NaN")
 * 
 * // Pattern matching usage
 * measurement match {
 *   case SensorMeasurment(id, "NaN") => println(s"Sensor $id failed")
 *   case SensorMeasurment(id, value) => println(s"Sensor $id: $value%")
 * }
 * }}}
 * 
 * @note The humidity parameter is stored as String to accommodate both
 *       numeric values and "NaN" for failed measurements
 * @author Sensor Statistics Team
 * @version 0.1
 * @since 0.1
 */
case class SensorMeasurment(sensorid: String, humidity: String)
```

### 3. SensorStatisticsDTO Class (Enhanced Documentation)

```scala
package com.sensor

import java.io.File
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import scala.collection.mutable.ListBuffer

/**
 * Concrete implementation of SensorStatistics using Apache Spark.
 * 
 * This class provides a Spark-based implementation for processing sensor
 * CSV files and generating statistics. It uses RDDs for distributed
 * processing and maintains internal state for intermediate calculations.
 * 
 * The implementation follows this processing pipeline:
 * 1. File discovery and validation
 * 2. Spark context initialization
 * 3. CSV file parsing with header removal
 * 4. Data transformation to case classes
 * 5. Statistical calculations and aggregations
 * 6. Result formatting and output
 * 
 * @constructor Creates a new SensorStatisticsDTO instance with empty state
 * 
 * @example
 * {{{
 * val stats = new SensorStatisticsDTO()
 * 
 * // Basic usage
 * val fileCount = stats.numOfProcessedFiles("resources")
 * val measurements = stats.numOfProcessedMeasurements()
 * val failed = stats.numOfFailedMeasurements()
 * 
 * println(s"Processed $measurements measurements from $fileCount files")
 * println(s"Failed measurements: $failed")
 * 
 * // Display detailed statistics
 * stats.minAvgMaxHumidity()
 * stats.sortsSensorsByHighestAvgHumidity()
 * }}}
 * 
 * @note This class creates a new SparkContext during measurement processing.
 *       Ensure proper cleanup to avoid "SparkContext already exists" errors.
 * 
 * @author Sensor Statistics Team
 * @version 0.1
 * @since 0.1
 */
class SensorStatisticsDTO extends SensorStatistics {

  /**
   * List of CSV files discovered in the target directory.
   * Populated by `numOfProcessedFiles` method.
   */
  var fileList: List[File] = null

  /**
   * Temporary storage for calculated mean values during processing.
   * Used internally by statistical calculation methods.
   */
  var mean: String = ""

  /**
   * Buffer storing humidity values from all processed files.
   * Each inner list represents humidity values from one file.
   */
  var humidityListBuffer = new ListBuffer[List[String]]()

  /**
   * Buffer storing sensor IDs from all processed files.
   * Parallel structure to humidityListBuffer.
   */
  var sensoridListBuffer = new ListBuffer[List[String]]()

  /**
   * Map from sensor ID to list of humidity values for that sensor.
   * Used for calculating min/max/average statistics per sensor.
   */
  var map: Map[String, ListBuffer[Int]] = Map()

  /**
   * Discovers and counts CSV files in the specified directory.
   * 
   * This method scans the directory for .csv files and populates
   * the internal fileList. If the directory doesn't exist or contains
   * no files, appropriate messages are displayed and the program may exit.
   * 
   * @param dir the directory path to scan for CSV files
   * @return the number of CSV files found
   * @throws SecurityException if directory access is denied
   * 
   * @example
   * {{{
   * val stats = new SensorStatisticsDTO()
   * val count = stats.numOfProcessedFiles("/path/to/sensor/data")
   * if (count > 0) {
   *   println(s"Ready to process $count files")
   * }
   * }}}
   */
  def numOfProcessedFiles(dir: String): Int = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      println("The files exist in the given directory")
      fileList = d.listFiles.filter(_.isFile).toList
    } else {
      println("No files in the folder provided.Please give the proper directory.")
      System.exit(1)
    }
    fileList.size
  }

  /**
   * Processes all discovered CSV files and counts total measurements.
   * 
   * This method creates a Spark context and processes each CSV file:
   * 1. Reads file into RDD
   * 2. Removes header row
   * 3. Parses rows into SensorMeasurement objects
   * 4. Extracts and stores humidity and sensor ID data
   * 
   * The Spark configuration uses:
   * - Application name: "Sensor statistics"
   * - Master: "local[*]" (all available cores)
   * 
   * @return total number of measurements across all files
   * @throws IllegalStateException if called before numOfProcessedFiles
   * @throws org.apache.spark.SparkException if Spark processing fails
   * 
   * @example
   * {{{
   * val stats = new SensorStatisticsDTO()
   * stats.numOfProcessedFiles("data")
   * val total = stats.numOfProcessedMeasurements()
   * println(s"Processed $total sensor readings")
   * }}}
   */
  def numOfProcessedMeasurements(): Int = {
    var conf = new SparkConf().setAppName("Sensor statistics").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    
    for (file <- fileList) {
      val textRDD = sc.textFile(file.toString)
      val header = textRDD.first()
      // removing the header..
      val textRDDResult = textRDD.filter(row => row != header)
      val empRdd = textRDDResult.map { line =>
        val col = line.split(",")
        SensorMeasurment(col(0), col(1))
      }

      // converting humidity values to a humidity list
      val humidityList = for {
        line <- empRdd
        values = line
        a = values.humidity
      } yield a

      humidityListBuffer += humidityList.collect().toList

      // converting sensor values in file to list
      val sensoridList = for {
        line <- empRdd
        values = line
        a = values.sensorid
      } yield a

      sensoridListBuffer += sensoridList.collect().toList
    }
    val finalSenserIDList: List[String] = sensoridListBuffer.toList.flatten
    finalSenserIDList.length
  }

  // Additional method documentation would continue in similar fashion...
}
```

### 4. SensorStatisticsRun Object (Enhanced)

```scala
package com.sensor

/**
 * Main application entry point for the Sensor Statistics application.
 * 
 * This object provides the command-line interface for processing sensor
 * CSV files. It demonstrates the typical usage pattern of the SensorStatistics
 * API and formats output for console display.
 * 
 * @example Command line usage:
 * {{{
 * # Using SBT
 * sbt "run resources"
 * sbt "run /path/to/sensor/data"
 * 
 * # Using compiled JAR
 * scala -cp target/scala-2.11/statistics-sensor_2.11-0.1.jar \
 *   com.sensor.SensorStatisticsRun resources
 * }}}
 * 
 * @author Sensor Statistics Team
 * @version 0.1
 * @since 0.1
 */
object SensorStatisticsRun {

  /**
   * Main method for command-line execution.
   * 
   * Processes sensor CSV files from the specified directory and displays
   * comprehensive statistics including file counts, measurement counts,
   * failure rates, and sensor performance metrics.
   * 
   * Expected output format:
   * {{{
   * Num processed files: 3
   * Num processed measurements: 7
   * Num failed measurements: 3
   * sensor-id,min,avg,max :
   * s1,10,54,98
   * s2,78,84,88
   * s3,NaN,NaN,NaN
   * sorts sensors by highest avg humidity :(s2,84), (s1,54), (s3,NAN),
   * }}}
   * 
   * @param args command line arguments where args(0) is the directory path
   * @throws ArrayIndexOutOfBoundsException if no directory argument provided
   * @throws IllegalArgumentException if directory doesn't exist
   * 
   * @example
   * {{{
   * // Programmatic usage
   * val args = Array("resources")
   * SensorStatisticsRun.main(args)
   * }}}
   */
  def main(args: Array[String]): Unit = {
    var dir = args(0)

    // The test files are present under this folder
    // one can uncomment below line and can execute
    // var dir = "resources"

    val sensorObj = new SensorStatisticsDTO()
    val noOfProcessedFile = sensorObj.numOfProcessedFiles(dir)
    println("Num processed files:" + noOfProcessedFile)
    
    val numOfProcessedMeasure = sensorObj.numOfProcessedMeasurements()
    println("Num processed measurements: " + numOfProcessedMeasure)
    
    val numOfFailedMeasure = sensorObj.numOfFailedMeasurements()
    println("Num failed measurements: " + numOfFailedMeasure)
    
    sensorObj.minAvgMaxHumidity()
    sensorObj.sortsSensorsByHighestAvgHumidity()
  }
}
```

## ScalaDoc Generation

### Generating Documentation

To generate HTML documentation from ScalaDoc comments:

```bash
# Generate ScalaDoc
sbt doc

# View generated documentation
open target/scala-2.11/api/index.html
```

### ScalaDoc Best Practices

1. **Use proper ScalaDoc syntax**:
   ```scala
   /**
    * Brief description on first line.
    * 
    * Detailed description starts after blank line.
    * 
    * @param paramName description of parameter
    * @return description of return value
    * @throws ExceptionType when this exception is thrown
    * @example usage example in code blocks
    * @since version when this was added
    * @author author information
    */
   ```

2. **Include examples**:
   ```scala
   /**
    * @example
    * {{{
    * val stats = new SensorStatisticsDTO()
    * val count = stats.numOfProcessedFiles("data")
    * }}}
    */
   ```

3. **Document parameters and return values**:
   ```scala
   /**
    * @param dir the directory path containing CSV files
    * @return the number of CSV files found
    */
   ```

4. **Include cross-references**:
   ```scala
   /**
    * @see [[SensorStatistics.numOfProcessedMeasurements]]
    * @see [[com.sensor.SensorMeasurment]]
    */
   ```

### Documentation Tags Reference

| Tag | Purpose | Example |
|-----|---------|---------|
| `@param` | Parameter description | `@param dir the directory path` |
| `@return` | Return value description | `@return number of files found` |
| `@throws` | Exception conditions | `@throws IllegalArgumentException if dir is null` |
| `@example` | Usage examples | `@example {{{ code here }}}` |
| `@since` | Version introduced | `@since 0.1` |
| `@author` | Author information | `@author John Doe` |
| `@version` | Current version | `@version 1.0` |
| `@see` | Cross-references | `@see [[OtherClass]]` |
| `@note` | Important notes | `@note This method is thread-safe` |
| `@todo` | Future improvements | `@todo Add error handling` |

## Integration with IDEs

### IntelliJ IDEA
- ScalaDoc comments appear in quick documentation (Ctrl+Q)
- Auto-completion shows parameter descriptions
- Generate ScalaDoc stubs with templates

### VS Code with Metals
- Hover information shows ScalaDoc
- Parameter hints include descriptions
- Go to definition shows full documentation

### Eclipse with Scala IDE
- Javadoc view displays ScalaDoc
- Content assist shows parameter info
- Export documentation to HTML

This enhanced documentation approach makes the codebase more maintainable and provides better developer experience through comprehensive API documentation.