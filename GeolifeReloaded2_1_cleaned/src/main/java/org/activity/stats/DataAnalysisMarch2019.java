package org.activity.stats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.activity.constants.PathConstants;
import org.activity.io.ReadingFromFile;
import org.activity.objects.Pair;

import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.io.csv.CsvReadOptions.Builder;

public class DataAnalysisMarch2019
{

	public DataAnalysisMarch2019()
	{

	}

	public static void main(String args[])
	{
		String databaseName = "geolife1";
		analysisTableSaw(databaseName);
		analysisNative(databaseName);
	}

	private static void analysisTableSaw(String databaseName)
	{
		Builder builder = CsvReadOptions
				.builder(PathConstants.getPathToCleanedTimelinesFeb2019(databaseName) + "AllActObjs.csv").separator(',')
				.header(true) // no header
				.dateFormat("yyyy-MM-dd"); // the date format to use.//2008-06-24
		CsvReadOptions options = builder.build();

		try
		{
			Table t1 = Table.read().csv(options);
			System.out.println(t1.structure());
			System.out.println(t1.first(5));
			System.out.println(t1.shape());

			String colName = "durationInSeconds";
			System.out.println("Type = " + t1.column(colName).type());
			List<?> colAsList = t1.column(colName).asList();

			// List<?> listOfPairs = IntStream.range(0, t1.rowCount()).mapToObj(i -> new Pair<>(i, colAsList.get(i)))
			// .collect(Collectors.toList());
			// System.out.println(listOfPairs.toString());

			// .mapToObject(i -> new Pair<>(i, res11.get(i)));
			// Plot.show(TimeSeriesPlot.create("Fox approval ratings", t1, "STdateOnly", "durInSecPrev"));

			// Plot.show(LinePlot.create("Monthly Boston Robberies: Jan 1966-Oct 1975", t1, "distanceTravelledInKm",
			// "durInSecPrev"));

			// Plot.show(Histogram.create("Distribution of prices", t1, "durationInSeconds"));
			// Plot.show(HorizontalBarPlot.create("fatalities by scale", // plot title
			// t1, // table
			// "activityName", // grouping column name
			// "sum[durationInSeconds]")); // numeric column name
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	private static void analysisNative(String databaseName)
	{
		List<List<String>> allAOData = ReadingFromFile.readLinesIntoListOfLists(
				PathConstants.getPathToCleanedTimelinesFeb2019(databaseName) + "AllActObjs.csv", ",");

		System.out.println("allAOData.size() = " + allAOData.size());
		List<String> header = allAOData.remove(0);
		System.out.println("Headers are: " + header);
		System.out.println("First 5 lines:\n" + header.stream().collect(Collectors.joining("\t")));
		System.out.println(allAOData.stream().limit(5).map(e -> e.stream().collect(Collectors.joining("\t")))
				.collect(Collectors.joining("\n")));

		int indexOfactivityName = header.indexOf("activityName");
		int indexOfdurInSecPrev = header.indexOf("durInSecPrev");

		List<Pair<List<String>, List<List<String>>>> currAndDragEntries = new ArrayList<>(allAOData.size());

		for (int rowIndex = 3; rowIndex < 10/* allAOData.size() */; rowIndex++)// List<String> rowE : allAOData)
		{
			String currentActName = allAOData.get(rowIndex).get(indexOfactivityName);

			List<List<String>> drag3Entries = new ArrayList<>(3);

			for (int shiftBackIndex = rowIndex - 1; shiftBackIndex >= rowIndex - 3; shiftBackIndex--)
			{
				System.out.println("rowIndex = " + rowIndex + " \n\tshiftBackIndex = " + shiftBackIndex);
				drag3Entries.add(allAOData.get(shiftBackIndex));
			}
			System.out.println();
		}
	}
}
