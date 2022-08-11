package com.sensor

case class SensorMeasurment(sensorid:String,humidity : String)

object SensorStatisticsRun {

  def main(args: Array[String]): Unit = {
    var dir= args(0)

    //The test files are present under this folder
    // one can uncomment below line and can execute

    //var dir = "resources"

    val sensorObj=new SensorStatisticsDTO()
    val noOfProcessedFile=sensorObj.numOfProcessedFiles(dir)
    println("Num processed files:" + noOfProcessedFile)
    val numOfProcessedMeasure=sensorObj.numOfProcessedMeasurements()
    println("Num processed measurements: "+numOfProcessedMeasure)
    val numOfFailedMeasure=sensorObj.numOfFailedMeasurements()
    println("Num failed measurements: "+numOfFailedMeasure)
    sensorObj.minAvgMaxHumidity()
    sensorObj.sortsSensorsByHighestAvgHumidity()
  }

}
