package answers.dataprocessing

import answers.dataprocessing.TimeUtil._
import kantan.csv._
import kantan.csv.ops._
import Ordering.Double.TotalOrdering

object TemperatureNotebookAnswers extends App {

  val rawData: java.net.URL = getClass.getResource("/city_temperature.csv")

  val reader: CsvReader[Either[ReadError, Sample]] = rawData.asCsvReader[Sample](rfc.withHeader)

  val (failures, samples) = timeOne("load data")(reader.toList.partitionMap(identity))

  println(s"Parsed ${samples.size} rows successfully and ${failures.size} rows failed")

  val partitions    = 10
  val partitionSize = samples.length / partitions + 1
  val computeEC     = ThreadPoolUtil.fixedSizeExecutionContext(8, "compute")

  val parSamples      = ParList.byNumberOfPartition(computeEC, 10, samples)
  val samplesArray    = samples.toArray
  val parSamplesArray = ParArray(computeEC, samplesArray, partitionSize)

  val minSampleByTemperature: Option[Sample] =
    TemperatureAnswers.minSampleByTemperature(parSamples)

  println(s"Min sample by temperature is $minSampleByTemperature")
  println(s"Max sample by temperature is ${parSamples.maxBy(_.temperatureFahrenheit)}")
  println(s"Min sample by date is ${parSamples.minBy(_.localDate)}")
  println(s"Max sample by date is ${parSamples.maxBy(_.localDate)}")

  val averageTemperature: Option[Double] =
    TemperatureAnswers.averageTemperature(parSamples)

  println(s"Average temperature is $averageTemperature")

  println(s"Temperature summary is ${parSamples.parFoldMap(SummaryV1.one)(SummaryV1.monoid)}")

  val summariesPerCity = aggregatePerLabel(parSamples)(s => List(s.city))

  summariesPerCity.get("Bordeaux").foreach(println)
  summariesPerCity.get("London").foreach(println)

  bench("sum")(
    Labelled("List foldLeft", () => samples.foldLeft(0.0)((state, sample) => state + sample.temperatureFahrenheit)),
    Labelled("List map + sum", () => samples.map(_.temperatureFahrenheit).sum),
    Labelled("ParList foldMap", () => parSamples.foldMap(_.temperatureFahrenheit)(Monoid.sumNumeric)),
    Labelled("ParList parFoldMap", () => parSamples.parFoldMap(_.temperatureFahrenheit)(Monoid.sumNumeric)),
    Labelled("ParArray parFoldMap", () => parSamplesArray.parFoldMap(_.temperatureFahrenheit)(Monoid.sumNumeric)),
    Labelled("Array foldLeft",
             () => samplesArray.foldLeft(0.0)((state, sample) => state + sample.temperatureFahrenheit)),
  )

  bench("min")(
    Labelled("ParList minBy", () => parSamples.minBy(_.temperatureFahrenheit)),
    Labelled("List minByOption", () => samples.minByOption(_.temperatureFahrenheit)),
  )

  bench("summary")(
    Labelled("List 4 passes", () => TemperatureAnswers.summaryList(samples)),
    Labelled("List one-pass", () => TemperatureAnswers.summaryListOnePass(samples)),
    Labelled("ParList one-pass foldMap hard-coded Monoid",
             () => parSamples.parFoldMap(SummaryV1.one)(SummaryV1.monoid)),
    Labelled("ParList one-pass foldMap derived Monoid",
             () => parSamples.parFoldMap(SummaryV1.one)(SummaryV1.monoidDerived)),
    Labelled("ParList one-pass reduceMap hard-coded Semigroup",
             () => SummaryV1.fromSummary(parSamples.parReduceMap(Summary.one)(Summary.semigroup))),
    Labelled("ParList one-pass reduceMap derived Semigroup",
             () => SummaryV1.fromSummary(parSamples.parReduceMap(Summary.one)(Summary.semigroupDerived))),
  )

  bench("aggregatePerLabel")(
    Labelled("ParList city", () => aggregatePerLabel(parSamples)(s => List(s.city))),
    Labelled("ParList country", () => aggregatePerLabel(parSamples)(s => List(s.country))),
    Labelled("ParList Bordeaux", () => aggregatePerLabel(parSamples)(s => List(s.city).filter(_ == "Bordeaux"))),
    Labelled("ParList city, country, region",
             () => aggregatePerLabel(parSamples)(s => List(s.city, s.country, s.region))),
  )

  def aggregatePerLabel[Label](parList: ParList[Sample])(labels: Sample => List[Label]): Map[Label, Summary] = {
    def sampleToLabelledSummary(sample: Sample): Map[Label, Summary] = {
      val summary = Summary.one(sample)
      labels(sample).map(_ -> summary).toMap
    }

    parList.parReduceMap(sampleToLabelledSummary)(Monoid.map(Summary.semigroup)).getOrElse(Map.empty)
  }

}
