# Sensor Statistics API Documentation

## Overview

The Sensor Statistics application is a Scala-based Apache Spark application designed to process CSV files containing sensor humidity measurements. It provides comprehensive statistics including file processing counts, measurement analysis, and sensor ranking capabilities.

## Project Information

- **Name**: statistics-sensor
- **Version**: 0.1
- **Scala Version**: 2.11.8
- **Apache Spark Version**: 1.6.0
- **SBT Version**: 1.2.8

## Dependencies

```scala
libraryDependencies += Seq(
  "org.apache.spark" %% "spark-core" % "1.6.0",
  "org.apache.spark" %% "spark-sql" % "1.6.0",
  "org.scalatest" %% "scalatest" % "2.2.2" % Test
)
```

## Package Structure

```
com.sensor/
├── SensorStatistics.scala          # Main trait interface
├── SensorStatisticsDTO.scala       # Implementation class
├── SensorStatisticsRun.scala       # Application entry point
└── SensorMeasurement               # Case class for data modeling
```

---

## Core Components

### 1. SensorMeasurement Case Class

**Location**: `com.sensor.SensorStatisticsRun.scala`

```scala
case class SensorMeasurment(sensorid: String, humidity: String)
```

#### Description
A case class representing a single sensor measurement record.

#### Parameters
- `sensorid: String` - Unique identifier for the sensor
- `humidity: String` - Humidity measurement value (can be "NaN" for failed measurements)

#### Usage Example
```scala
val measurement = SensorMeasurment("s1", "85")
val failedMeasurement = SensorMeasurment("s2", "NaN")
```

---

### 2. SensorStatistics Trait

**Location**: `com.sensor.SensorStatistics.scala`

#### Description
Main interface defining the contract for sensor statistics operations.

#### Public Methods

##### `numOfProcessedFiles(dir: String): Int`
**Purpose**: Count the number of CSV files in the specified directory.

**Parameters**:
- `dir: String` - Directory path containing CSV files

**Returns**: `Int` - Number of files found

**Example**:
```scala
val stats: SensorStatistics = new SensorStatisticsDTO()
val fileCount = stats.numOfProcessedFiles("resources")
println(s"Found $fileCount files")
```

##### `numOfProcessedMeasurements(): Int`
**Purpose**: Count total number of measurements across all processed files.

**Returns**: `Int` - Total measurement count

**Example**:
```scala
val measurementCount = stats.numOfProcessedMeasurements()
println(s"Processed $measurementCount measurements")
```

##### `numOfFailedMeasurements(): Int`
**Purpose**: Count measurements with "NaN" humidity values.

**Returns**: `Int` - Number of failed measurements

**Example**:
```scala
val failedCount = stats.numOfFailedMeasurements()
println(s"Failed measurements: $failedCount")
```

##### `minAvgMaxHumidity(): Unit`
**Purpose**: Display minimum, average, and maximum humidity for each sensor.

**Output Format**:
```
sensor-id,min,avg,max :
s1,10,54,98
s2,78,84,88
s3,NaN,NaN,NaN
```

**Example**:
```scala
stats.minAvgMaxHumidity()
// Prints statistics to console
```

##### `sortsSensorsByHighestAvgHumidity(): Unit`
**Purpose**: Display sensors sorted by highest average humidity.

**Output Format**:
```
sorts sensors by highest avg humidity :(s2,84), (s1,54), (s3,NAN),
```

**Example**:
```scala
stats.sortsSensorsByHighestAvgHumidity()
// Prints sorted sensors to console
```

---

### 3. SensorStatisticsDTO Class

**Location**: `com.sensor.SensorStatisticsDTO.scala`

#### Description
Concrete implementation of the SensorStatistics trait using Apache Spark for data processing.

#### Constructor
```scala
class SensorStatisticsDTO extends SensorStatistics
```

#### Internal State
- `fileList: List[File]` - List of CSV files to process
- `mean: String` - Temporary storage for calculated averages
- `humidityListBuffer: ListBuffer[List[String]]` - Buffer for humidity values
- `sensoridListBuffer: ListBuffer[List[String]]` - Buffer for sensor IDs
- `map: Map[String, ListBuffer[Int]]` - Sensor ID to humidity values mapping

#### Implementation Details

##### File Processing
The class reads CSV files with the following expected format:
```csv
sensor-id,humidity
s1,10
s2,88
s1,NaN
```

##### Spark Configuration
- **Application Name**: "Sensor statistics"
- **Master**: "local[*]" (uses all available cores)
- **Context**: Creates SparkContext and SQLContext for data processing

##### Data Processing Pipeline
1. **File Discovery**: Scans directory for CSV files
2. **Header Removal**: Filters out CSV headers
3. **Data Parsing**: Converts CSV rows to SensorMeasurement objects
4. **Data Aggregation**: Groups measurements by sensor ID
5. **Statistics Calculation**: Computes min/avg/max values
6. **Sorting**: Orders sensors by average humidity

---

### 4. SensorStatisticsRun Object

**Location**: `com.sensor.SensorStatisticsRun.scala`

#### Description
Main application entry point providing command-line interface.

#### Main Method
```scala
def main(args: Array[String]): Unit
```

#### Parameters
- `args(0): String` - Directory path containing CSV files

#### Usage Examples

##### Command Line Execution
```bash
# Using SBT
sbt "run resources"

# Using compiled JAR
scala -cp target/scala-2.11/statistics-sensor_2.11-0.1.jar com.sensor.SensorStatisticsRun resources
```

##### Programmatic Usage
```scala
import com.sensor.SensorStatisticsRun

// Set directory path in args
val args = Array("resources")
SensorStatisticsRun.main(args)
```

#### Expected Output
```
Num processed files: 3
Num processed measurements: 7
Num failed measurements: 3
sensor-id,min,avg,max :
s1,10,54,98
s2,78,84,88
s3,NaN,NaN,NaN
sorts sensors by highest avg humidity :(s2,84), (s1,54), (s3,NAN),
```

---

## Data Format Specifications

### Input CSV Format
Each CSV file must follow this structure:

```csv
sensor-id,humidity
s1,10
s2,88
s1,NaN
s3,45
```

#### Requirements
- **Header Row**: Must contain "sensor-id,humidity"
- **Sensor ID**: String identifier (e.g., "s1", "s2", "sensor_001")
- **Humidity Values**: 
  - Numeric values for valid measurements
  - "NaN" for failed/missing measurements
- **File Extension**: .csv
- **Encoding**: UTF-8 (default)

### Directory Structure
```
project-root/
├── resources/           # Default data directory
│   ├── leader-1.csv
│   ├── leader-2.csv
│   └── leader-3.csv
└── src/main/scala/
    └── com/sensor/
```

---

## Complete Usage Examples

### Example 1: Basic Usage
```scala
import com.sensor.{SensorStatisticsDTO, SensorStatisticsRun}

// Create instance
val sensorStats = new SensorStatisticsDTO()

// Process files in directory
val fileCount = sensorStats.numOfProcessedFiles("resources")
println(s"Processing $fileCount files")

// Get measurement statistics
val totalMeasurements = sensorStats.numOfProcessedMeasurements()
val failedMeasurements = sensorStats.numOfFailedMeasurements()
val successRate = ((totalMeasurements - failedMeasurements).toDouble / totalMeasurements * 100)

println(s"Total measurements: $totalMeasurements")
println(s"Failed measurements: $failedMeasurements")
println(s"Success rate: ${successRate}%")

// Display detailed statistics
sensorStats.minAvgMaxHumidity()
sensorStats.sortsSensorsByHighestAvgHumidity()
```

### Example 2: Custom Data Directory
```scala
// Process files from custom directory
val customStats = new SensorStatisticsDTO()
val customDir = "/path/to/sensor/data"

try {
  val files = customStats.numOfProcessedFiles(customDir)
  if (files > 0) {
    customStats.numOfProcessedMeasurements()
    customStats.minAvgMaxHumidity()
  }
} catch {
  case e: Exception => println(s"Error processing directory: ${e.getMessage}")
}
```

### Example 3: Integration with Spark Application
```scala
import org.apache.spark.{SparkConf, SparkContext}
import com.sensor.SensorStatisticsDTO

// Custom Spark configuration
val conf = new SparkConf()
  .setAppName("Custom Sensor Analysis")
  .setMaster("local[4]")

val sc = new SparkContext(conf)

try {
  val stats = new SensorStatisticsDTO()
  // Process data...
  stats.numOfProcessedFiles("data")
  stats.minAvgMaxHumidity()
} finally {
  sc.stop()
}
```

---

## Error Handling

### Common Error Scenarios

#### 1. Directory Not Found
```scala
// Input: Non-existent directory
val stats = new SensorStatisticsDTO()
stats.numOfProcessedFiles("/invalid/path")
// Output: "No files in the folder provided.Please give the proper directory."
// System exits with code 1
```

#### 2. Empty Directory
```scala
// Input: Empty directory
val stats = new SensorStatisticsDTO()
val count = stats.numOfProcessedFiles("/empty/directory")
// Returns: 0
```

#### 3. Invalid CSV Format
The application expects CSV files with proper headers. Files without "sensor-id,humidity" header may cause parsing errors.

#### 4. Memory Considerations
For large datasets, ensure adequate heap space:
```bash
export JAVA_OPTS="-Xmx4g -Xms2g"
sbt "run data-directory"
```

---

## Performance Considerations

### Spark Configuration Tuning
```scala
val conf = new SparkConf()
  .setAppName("Sensor Statistics")
  .setMaster("local[*]")
  .set("spark.sql.adaptive.enabled", "true")
  .set("spark.sql.adaptive.coalescePartitions.enabled", "true")
```

### Memory Management
- **Small datasets** (< 1GB): Default settings sufficient
- **Medium datasets** (1-10GB): Increase driver memory
- **Large datasets** (> 10GB): Consider cluster deployment

### Optimization Tips
1. **File Partitioning**: Keep CSV files reasonably sized (< 100MB each)
2. **Data Locality**: Store CSV files on same machine as Spark driver
3. **Caching**: For repeated analysis, consider caching intermediate RDDs
4. **Parallelism**: Adjust Spark parallelism based on available cores

---

## Build and Deployment

### Building the Project
```bash
# Compile
sbt compile

# Run tests
sbt test

# Create JAR
sbt package

# Run application
sbt "run resources"
```

### Dependencies Installation
```bash
# Ensure Java 8+ is installed
java -version

# Install SBT if not present
# On Ubuntu/Debian:
sudo apt-get install sbt

# On macOS:
brew install sbt
```

### Running in Production
```bash
# Create fat JAR with dependencies
sbt assembly

# Run with specific memory settings
java -Xmx4g -jar target/scala-2.11/statistics-sensor-assembly-0.1.jar /path/to/data
```

---

## API Reference Summary

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `numOfProcessedFiles` | `dir: String` | `Int` | Count CSV files in directory |
| `numOfProcessedMeasurements` | None | `Int` | Count total measurements |
| `numOfFailedMeasurements` | None | `Int` | Count failed measurements |
| `minAvgMaxHumidity` | None | `Unit` | Display sensor statistics |
| `sortsSensorsByHighestAvgHumidity` | None | `Unit` | Display sorted sensors |

---

## Troubleshooting

### Common Issues

1. **"No files in the folder provided"**
   - Verify directory path exists
   - Check directory contains .csv files
   - Ensure proper file permissions

2. **OutOfMemoryError**
   - Increase JVM heap size
   - Reduce dataset size
   - Optimize Spark configuration

3. **Spark Context Already Exists**
   - Ensure proper cleanup of SparkContext
   - Use singleton pattern for SparkContext

4. **CSV Parsing Errors**
   - Verify CSV format matches expected structure
   - Check for special characters in data
   - Ensure UTF-8 encoding

### Debug Mode
Enable Spark logging for detailed debugging:
```scala
import org.apache.log4j.{Level, Logger}
Logger.getLogger("org.apache.spark").setLevel(Level.DEBUG)
```

---

## Version History

- **v0.1**: Initial release with basic sensor statistics functionality
  - File processing capabilities
  - Measurement counting and analysis
  - Min/avg/max calculations
  - Sensor ranking by average humidity

---

## Contributing

When extending this API:

1. **Follow Scala conventions**: Use camelCase for methods, PascalCase for classes
2. **Maintain trait contract**: Implement all SensorStatistics methods
3. **Add error handling**: Provide meaningful error messages
4. **Update documentation**: Keep this documentation current
5. **Add tests**: Include unit tests for new functionality

---

## License

This project uses the dependencies under their respective licenses:
- Apache Spark: Apache License 2.0
- ScalaTest: Apache License 2.0