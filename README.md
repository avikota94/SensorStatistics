# Sensor Statistics Application

A comprehensive Scala-based Apache Spark application for processing and analyzing sensor humidity measurement data from CSV files.

## 📋 Overview

The Sensor Statistics application provides powerful analytics capabilities for IoT sensor data, including:

- **File Processing**: Automatic discovery and processing of CSV sensor data files
- **Data Quality Analysis**: Detection and reporting of failed measurements
- **Statistical Analysis**: Min/max/average calculations per sensor
- **Sensor Ranking**: Performance-based sensor ordering
- **Scalable Processing**: Apache Spark-powered distributed processing

## 🚀 Quick Start

### Prerequisites
- Java 8 or higher
- SBT (Scala Build Tool)
- Apache Spark 1.6.0 (included as dependency)

### Installation & Running
```bash
# Clone the project
git clone <repository-url>
cd statistics-sensor

# Build and run
sbt compile
sbt "run resources"
```

### Expected Output
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

## 📚 Documentation

### Core Documentation
- **[API Documentation](API_DOCUMENTATION.md)** - Complete API reference with detailed method descriptions, parameters, and examples
- **[Quick Start Guide](QUICK_START_GUIDE.md)** - Step-by-step setup and basic usage instructions
- **[Usage Examples](EXAMPLES.md)** - Comprehensive examples for various use cases and integration scenarios

### Key Features

#### 🔍 Data Processing
- Automatic CSV file discovery in specified directories
- Header-aware CSV parsing with error handling
- Support for "NaN" values in humidity measurements
- Distributed processing using Apache Spark

#### 📊 Analytics Capabilities
- **File Statistics**: Count of processed files
- **Measurement Counting**: Total and failed measurement tracking
- **Sensor Analytics**: Min/max/average humidity per sensor
- **Quality Metrics**: Success rate calculations
- **Performance Ranking**: Sensors ordered by average humidity

#### 🛠️ Technical Features
- **Trait-based Architecture**: Clean separation of interface and implementation
- **Spark Integration**: Leverages RDDs for distributed processing
- **Error Handling**: Graceful handling of missing files and invalid data
- **Memory Efficient**: Optimized for processing large datasets

## 🏗️ Architecture

### Core Components

```
com.sensor/
├── SensorStatistics.scala          # Main trait interface
├── SensorStatisticsDTO.scala       # Spark-based implementation
├── SensorStatisticsRun.scala       # Application entry point
└── SensorMeasurement               # Data model case class
```

### Data Flow

1. **File Discovery** → Scan directory for CSV files
2. **Data Loading** → Read CSV files into Spark RDDs
3. **Data Parsing** → Convert CSV rows to SensorMeasurement objects
4. **Data Processing** → Filter, aggregate, and calculate statistics
5. **Results Output** → Display formatted results to console

## 📁 Data Format

### Input CSV Structure
```csv
sensor-id,humidity
s1,10
s2,88
s1,NaN
s3,45
```

### Requirements
- Header row: `sensor-id,humidity`
- Sensor IDs: String identifiers
- Humidity values: Numeric or "NaN" for failed measurements
- File extension: `.csv`

## 🔧 API Reference

### Main Interface: `SensorStatistics`

| Method | Parameters | Return | Description |
|--------|------------|--------|-------------|
| `numOfProcessedFiles` | `dir: String` | `Int` | Count CSV files in directory |
| `numOfProcessedMeasurements` | None | `Int` | Count total measurements |
| `numOfFailedMeasurements` | None | `Int` | Count failed measurements |
| `minAvgMaxHumidity` | None | `Unit` | Display sensor statistics |
| `sortsSensorsByHighestAvgHumidity` | None | `Unit` | Display sorted sensors |

### Usage Example
```scala
import com.sensor.SensorStatisticsDTO

val stats = new SensorStatisticsDTO()
val fileCount = stats.numOfProcessedFiles("data")
val measurements = stats.numOfProcessedMeasurements()
val failed = stats.numOfFailedMeasurements()

println(s"Processed $measurements measurements from $fileCount files")
println(s"Success rate: ${((measurements - failed).toDouble / measurements * 100).round}%")

stats.minAvgMaxHumidity()
stats.sortsSensorsByHighestAvgHumidity()
```

## 🚀 Advanced Usage

### Batch Processing
```scala
val directories = List("data/2023/q1", "data/2023/q2", "data/2023/q3")
directories.foreach { dir =>
  val stats = new SensorStatisticsDTO()
  stats.numOfProcessedFiles(dir)
  println(s"Processing $dir...")
  stats.minAvgMaxHumidity()
}
```

### Error Handling
```scala
import scala.util.{Try, Success, Failure}

Try {
  val stats = new SensorStatisticsDTO()
  stats.numOfProcessedFiles("data")
  stats.numOfProcessedMeasurements()
} match {
  case Success(_) => println("Processing completed successfully")
  case Failure(e) => println(s"Processing failed: ${e.getMessage}")
}
```

### Performance Optimization
```bash
# Increase heap size for large datasets
export JAVA_OPTS="-Xmx4g -Xms2g"
sbt "run large-dataset"

# Or using JAR
java -Xmx4g -jar target/scala-2.11/statistics-sensor_2.11-0.1.jar large-dataset
```

## 🔧 Build & Deployment

### Development
```bash
# Compile
sbt compile

# Run tests
sbt test

# Run application
sbt "run resources"

# Create JAR
sbt package
```

### Production
```bash
# Create fat JAR with dependencies
sbt assembly

# Run in production
java -Xmx4g -jar statistics-sensor-assembly-0.1.jar /path/to/data
```

## 🐛 Troubleshooting

### Common Issues

**"No files in the folder provided"**
- Verify directory exists and contains .csv files
- Check file permissions

**OutOfMemoryError**
- Increase JVM heap size: `-Xmx4g`
- Reduce dataset size for testing
- Optimize Spark configuration

**Spark Context Already Exists**
- Restart SBT session or IDE
- Ensure proper SparkContext cleanup

## 📈 Performance Guidelines

| Dataset Size | Recommended Settings | Notes |
|--------------|---------------------|-------|
| < 100MB | Default settings | Standard configuration |
| 100MB - 1GB | `-Xmx2g` heap size | Increase memory |
| 1GB - 10GB | `-Xmx4g` + optimized Spark config | Consider cluster deployment |
| > 10GB | Spark cluster | Distributed processing required |

## 🤝 Contributing

### Development Guidelines
1. Follow Scala naming conventions
2. Maintain trait-based architecture
3. Add comprehensive error handling
4. Include usage examples in documentation
5. Write unit tests for new features

### Code Style
- Use camelCase for methods and variables
- Use PascalCase for classes and traits
- Add ScalaDoc comments for public methods
- Follow functional programming principles

## 📄 License

This project uses the following dependencies under their respective licenses:
- **Apache Spark**: Apache License 2.0
- **ScalaTest**: Apache License 2.0

## 🔗 Additional Resources

- [Apache Spark Documentation](https://spark.apache.org/docs/1.6.0/)
- [Scala Documentation](https://docs.scala-lang.org/scala3/)
- [SBT Documentation](https://www.scala-sbt.org/documentation.html)

## 📞 Support

For questions, issues, or contributions:
1. Check the documentation files in this repository
2. Review common troubleshooting steps
3. Examine the provided examples for similar use cases
4. Ensure your data format matches the expected CSV structure

---

**Version**: 0.1  
**Scala Version**: 2.11.8  
**Spark Version**: 1.6.0  
**Last Updated**: 2024