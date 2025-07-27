# Quick Start Guide - Sensor Statistics

## Prerequisites

- Java 8 or higher installed
- SBT (Scala Build Tool) installed
- Basic understanding of Scala and Apache Spark

## Installation

### 1. Clone or Download the Project
```bash
# If using git
git clone <repository-url>
cd statistics-sensor

# Or extract from archive
unzip statistics-sensor.zip
cd statistics-sensor
```

### 2. Verify Dependencies
```bash
# Check Java version
java -version

# Check SBT installation
sbt version
```

### 3. Build the Project
```bash
# Compile the project
sbt compile

# Run tests (if available)
sbt test

# Create executable JAR
sbt package
```

## Running the Application

### Method 1: Using SBT (Recommended for Development)
```bash
# Run with sample data
sbt "run resources"

# Run with custom directory
sbt "run /path/to/your/csv/files"
```

### Method 2: Using Compiled JAR
```bash
# Create the JAR first
sbt package

# Run the JAR
scala -cp target/scala-2.11/statistics-sensor_2.11-0.1.jar com.sensor.SensorStatisticsRun resources
```

### Method 3: Using Fat JAR (For Production)
```bash
# Install assembly plugin first (add to project/plugins.sbt):
# addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")

# Create fat JAR with all dependencies
sbt assembly

# Run fat JAR
java -jar target/scala-2.11/statistics-sensor-assembly-0.1.jar resources
```

## Sample Data Format

### Create Test CSV Files
Create a directory called `test-data` with the following files:

**test-data/sensor1.csv:**
```csv
sensor-id,humidity
s1,45
s2,67
s3,NaN
s1,55
```

**test-data/sensor2.csv:**
```csv
sensor-id,humidity
s2,78
s3,23
s1,NaN
s4,89
```

### Run with Test Data
```bash
sbt "run test-data"
```

## Expected Output

```
Num processed files: 2
Num processed measurements: 8
Num failed measurements: 2
sensor-id,min,avg,max :
s1,45,50,55
s2,67,72,78
s3,23,23,23
s4,89,89,89
sorts sensors by highest avg humidity :(s4,89), (s2,72), (s1,50), (s3,23),
```

## Common Use Cases

### 1. Basic Analysis
```scala
import com.sensor.SensorStatisticsDTO

val stats = new SensorStatisticsDTO()
val fileCount = stats.numOfProcessedFiles("data")
val measurements = stats.numOfProcessedMeasurements()
stats.minAvgMaxHumidity()
```

### 2. Quality Assessment
```scala
val stats = new SensorStatisticsDTO()
stats.numOfProcessedFiles("data")
val total = stats.numOfProcessedMeasurements()
val failed = stats.numOfFailedMeasurements()
val successRate = (total - failed).toDouble / total * 100
println(f"Data quality: $successRate%.1f%% success rate")
```

### 3. Sensor Ranking
```scala
val stats = new SensorStatisticsDTO()
stats.numOfProcessedFiles("data")
stats.numOfProcessedMeasurements()
println("Sensor Performance Ranking:")
stats.sortsSensorsByHighestAvgHumidity()
```

## Troubleshooting

### Issue: "No files in the folder provided"
**Solution:** 
- Check if the directory exists
- Ensure it contains .csv files
- Verify file permissions

### Issue: OutOfMemoryError
**Solution:**
```bash
# Increase heap size
export JAVA_OPTS="-Xmx2g"
sbt "run data"

# Or for JAR execution
java -Xmx2g -jar target/scala-2.11/statistics-sensor-assembly-0.1.jar data
```

### Issue: Spark Context Already Exists
**Solution:** This typically happens when running multiple times in the same JVM session. Restart your SBT session or IDE.

## Next Steps

1. **Explore the API Documentation:** See `API_DOCUMENTATION.md` for detailed API reference
2. **Customize for Your Data:** Modify CSV format handling if needed
3. **Scale Up:** Consider cluster deployment for large datasets
4. **Add Features:** Extend the SensorStatistics trait for additional functionality

## Performance Tips

- **Small datasets (< 100MB):** Use default settings
- **Medium datasets (100MB - 1GB):** Increase heap size to 2-4GB
- **Large datasets (> 1GB):** Consider Spark cluster deployment

## Getting Help

- Check the full API documentation for detailed method descriptions
- Review error messages carefully - they often contain helpful information
- Ensure your CSV files match the expected format exactly
- For performance issues, try with a smaller subset of data first