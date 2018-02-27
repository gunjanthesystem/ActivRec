package org.activity.evaluation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.activity.io.ReadingFromFile;
import org.activity.io.SFTPFile;
import org.activity.objects.Pair;
import org.activity.plotting.ChartUtils;
import org.activity.plotting0.FXUtils;
import org.activity.ui.ChartBoard4;
import org.activity.util.PerformanceAnalytics;

import com.jcraft.jsch.Session;

import javafx.scene.Node;

/**
 * 
 * To see how edit distance varies at level 1 (activity name) and level 2 (features)
 * 
 * @since 4 Feb 2018
 * @author gunjan
 *
 */
public class EditDistanceEvaluation
{

	public static void main(String args[])
	{
		// main0();
		// plotActFeatLevelED();
		EDAnalysis26Feb2018();
	}

	public static void EDAnalysis26Feb2018()
	{
		// String fileToRead =
		// "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Feb4_NCount_5DayFilter_ThreshPer55EditDists/1/MatchingUnit3.0/EditSimilarityCalculations.csv";
		String fileToRead2 = "/home/gunjankumar/SyncedWorkspace/Aug2Workspace/GeolifeReloaded2_1_cleaned/dataWritten/Feb26NCount_5Day_NN500MedRepCinsNormEDAlpha0.5DistDurOnlyFeature_EDAnalysis/1/MatchingUnit3.0/EditSimilarityCalculations.csv";
		String host = Utils.clarityHost;
		int port = 22;
		Pair<InputStream, Session> inputAndSession = SFTPFile.getInputStreamForSFTPFile(host, port, fileToRead2,
				Utils.getUserForHost(host), Utils.getPassWordForHost(host));

		long t1 = System.currentTimeMillis();
		List<List<String>> EDTraces = ReadingFromFile.nColumnReaderStringLargeFileSelectedColumnsWithWrite(
				inputAndSession.getFirst(), ",", true, true, true, "dataWritten/Feb26_EDAnalysis/OnlyEDTraces.csv",
				new int[] { 7 });// 2000000, 8000000,

		inputAndSession.getSecond().disconnect();
		System.out.println("Num of lines read = " + EDTraces.size());
		// = ReadingFromFile.nColumnReaderStringLargeFileSelectedColumnsInChunks(inputAndSession.getFirst(), ",",
		// true, true, 10000, new int[] { 0, 4, 5, 6, 7 });

		long t2 = System.currentTimeMillis();

		// System.out.println("Res=\n" + res.toString());

		System.out.println("time taken to read " + fileToRead2 + " = \n\t" + (t2 - t1) + "ms");
		// System.out.println("lines= " + res.size());
		// inputAndSession.getSecond().disconnect();
		//
		// WritingToFile.writeListOfList(res, , "", ",");
	}

	public static void plotActFeatLevelED()
	{

		System.out.println(PerformanceAnalytics.getHeapPercentageFree());
		System.out.println(PerformanceAnalytics.getUsedMemoryInMB() + "\n\n");

		String fileToRead = "./dataWritten/Feb4/Res.csv";
		ArrayList<ArrayList<Double>> dataRead = ReadingFromFile.allColumnsReaderDouble(fileToRead, ",",
				new int[] { 0, 1, 2, 3 }, true, 5000, 205000);
		List<String> colHeaders = ReadingFromFile.getColHeaders(fileToRead, ",");

		System.out.println("dataRead.size()=" + dataRead.size());
		System.out.println(PerformanceAnalytics.getHeapPercentageFree());
		System.out.println(PerformanceAnalytics.getUsedMemoryInMB() + "\n\n");

		ArrayList<Double> xData = dataRead.get(0);
		ArrayList<Double> yData = dataRead.get(1);
		String xLabel = colHeaders.get(0);
		String yLabel = colHeaders.get(1);

		System.out.println("here1");
		System.out.println(PerformanceAnalytics.getHeapPercentageFree());
		System.out.println(PerformanceAnalytics.getUsedMemoryInMB() + "\n\n");
		double acts1Threshold = 2;
		double acts2Threshold = 3;

		ArrayList<Pair<Double, Double>> dataForXY = new ArrayList<>();
		ArrayList<String> toolTipArray = new ArrayList<>();
		for (int i = 0; i < xData.size(); i++)
		{
			if (dataRead.get(2).get(i) >= acts1Threshold && dataRead.get(3).get(i) >= acts2Threshold)
			{
				dataForXY.add(new Pair<Double, Double>(xData.get(i), yData.get(i)));
				toolTipArray.add("#Acts1=" + dataRead.get(2).get(i) + "  #Acts2=" + dataRead.get(3).get(i));
			}

		}

		System.out.println("dataForXY.size()= " + dataForXY.size());
		ArrayList<ArrayList<String>> toolTipArrayForAllSeries = new ArrayList<>();
		toolTipArrayForAllSeries.add(toolTipArray);

		// ArrayList<Pair<Double, Double>> dataForXY = (ArrayList<Pair<Double, Double>>) IntStream.range(0,
		// xData.size())
		// .boxed().map(i -> new Pair<Double, Double>(xData.get(i), yData.get(i))).collect(Collectors.toList());

		// ArrayList<Pair<Double, Double>> dataForXY = (ArrayList<Pair<Double, Double>>) dataRead.stream()
		// .map(v -> new Pair<Double, Double>(v.get(0), v.get(1))).collect(Collectors.toList());

		// $StringBuilder sb = new StringBuilder();
		// $dataForXY.stream().forEachOrdered(v -> sb.append(v.getFirst() + "--" + v.getSecond() + "\n"));
		// $System.out.println(sb.toString());

		// Table data1 = Table.createFromCsv("/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/Feb4/Res.csv");
		// NumericColumn x = baseball.nCol("Record");
		// NumericColumn y = baseball.nCol("Robberies");
		// Line.show("Monthly Boston Armed Robberies Jan. 1966 - Oct. 1975", x, y);
		System.out.println("here2");
		System.out.println(PerformanceAnalytics.getHeapPercentageFree());
		System.out.println(PerformanceAnalytics.getUsedMemoryInMB() + "\n\n");
		com.sun.javafx.application.PlatformImpl.startup(() ->
			{
			});
		List<Pair<Node, String>> listOfChatNodesToAdd = new ArrayList<>();

		listOfChatNodesToAdd.add(new Pair<>(
				ChartUtils.createScatterChart(FXUtils.toObservableListOfSeriesOfPairData(dataForXY, "Val"),
						"Edit Distance Distribution", "val=", xLabel, yLabel, toolTipArrayForAllSeries),
				"ScatterChart"));

		// listOfChatNodesToAdd.add(new Pair<>(
		// ChartUtils.createLineChart(FXUtils.toObservableListOfSeriesOfPairData(FXUtils.getSyntheticData(50, 50)),
		// "OLaLineChartTitle", "ajooba"),
		// "LineChart"));
		//
		// listOfChatNodesToAdd.add(new Pair<>(ChartUtils.createScatterChart(
		// FXUtils.toObservableListOfSeriesOfPairData(FXUtils.getSyntheticData(50, 50)), "ScatterChartTitle",
		// "ajooba"), "ScatterChart"));
		// Application.launch();

		ChartBoard4.setCharts(listOfChatNodesToAdd);
		// ChartBoard4 cb = new ChartBoard4();
		ChartBoard4.launch();

	}

	public static void main0()
	{
		String fileToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Feb4_NCount_5DayFilter_ThreshPer55EditDists/1/MatchingUnit3.0/EditSimilarityCalculations.csv";
		String host = Utils.engineHost;
		int port = 22;

		Pair<InputStream, Session> inputAndSession = SFTPFile.getInputStreamForSFTPFile(host, port, fileToRead,
				Utils.howitzerUsr, Utils.getPassWordForHost(host));

		long t1 = System.currentTimeMillis();
		ReadingFromFile.nColumnReaderStringLargeFileSelectedColumnsWithWrite(inputAndSession.getFirst(), ",", true,
				true, true, "dataWritten/Feb4/Res.csv", new int[] { 5, 6, 10, 11 });// 2000000, 8000000,

		// inputAndSession.getFirst().close();
		inputAndSession.getSecond().disconnect();
		// = ReadingFromFile.nColumnReaderStringLargeFileSelectedColumnsInChunks(inputAndSession.getFirst(), ",",
		// true, true, 10000, new int[] { 0, 4, 5, 6, 7 });

		long t2 = System.currentTimeMillis();

		// System.out.println("Res=\n" + res.toString());

		System.out.println("time taken to read " + fileToRead + " = \n\t" + (t2 - t1) + "ms");
		// System.out.println("lines= " + res.size());
		// inputAndSession.getSecond().disconnect();
		//
		// WritingToFile.writeListOfList(res, , "", ",");
	}

}
