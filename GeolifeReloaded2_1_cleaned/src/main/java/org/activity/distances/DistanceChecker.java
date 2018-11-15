package org.activity.distances;

import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.PathConstants;
import org.activity.generator.ToyTimelineUtils;
import org.activity.io.Serializer;
import org.activity.io.WToFile;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.Timeline;
import org.activity.stats.StatsUtils;
import org.activity.util.DateTimeUtils;
import org.apache.commons.text.similarity.HammingDistance;
import org.apache.commons.text.similarity.JaccardDistance;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.commons.text.similarity.SimilarityScore;

/**
 * 
 * @author gunjan
 * @since 31 Aug 2018
 */
public class DistanceChecker
{

	public static void useApacheCommonsText()
	{
		// org.apache.commons.text.

		String word1 = "fogr", word2 = "frog";

		SimilarityScore jd = new JaccardDistance();
		SimilarityScore ld = new LevenshteinDistance();

		SimilarityScore hd = new HammingDistance();
		SimilarityScore jwd = new JaroWinklerDistance();

		// List<SimilarityScore<Double>> distanceMetrics = new ArrayList<>();

		System.out.println("word1 = " + word1 + "  word2 = " + word2);
		System.out.println("jd= " + jd.apply(word1, word2));
		System.out.println("ld = " + ld.apply(word1, word2));
		System.out.println("hd = " + hd.apply(word1, word2));
		System.out.println("jwd= " + jwd.apply(word1, word2));

	}

	public static void main(String args[])
	{
		useApacheCommonsText();
	}

	public static void main0(String args[])
	{
		PrimaryDimension givenDimension = PrimaryDimension.ActivityID;
		String commonPath = "/run/media/gunjan/BackupVault/GOWALLA/GowallaResults/"
				+ DateTimeUtils.getMonthDateHourMinLabel() + "DistanceChecker/";
		WToFile.createDirectoryIfNotExists(commonPath);

		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersToyDayTimelines = (LinkedHashMap<String, LinkedHashMap<Date, Timeline>>) Serializer
				.kryoDeSerializeThis(PathConstants.pathToToyTimelines12AUG);

		WToFile.writeUsersDayTimelinesSameFile(usersToyDayTimelines, "usersToyDayTimelines", false, false, false,
				"GowallaUserDayToyTimelines.csv", commonPath);

		ToyTimelineUtils.writeOnlyActIDs(usersToyDayTimelines, commonPath + "ToyTimelinesOnlyActIDs.csv");
		ToyTimelineUtils.writeOnlyActIDs2(usersToyDayTimelines, commonPath + "ToyTimelinesOnlyActIDs2.csv");
		ToyTimelineUtils.writeActIDTS(usersToyDayTimelines, commonPath + "ToyTimelinesActIDTS.csv");

		List<ArrayList<ActivityObject2018>> allDayTimelines = usersToyDayTimelines.entrySet().stream()
				.map(e -> e.getValue()).flatMap(v -> v.entrySet().stream())
				.map(e -> e.getValue().getActivityObjectsInTimeline()).collect(Collectors.toList());

		StringBuilder sb = new StringBuilder();
		for (ArrayList<ActivityObject2018> timeline : allDayTimelines)
		{
			for (ActivityObject2018 ao : timeline)
			{
				sb.append(">>" + ao.getGivenDimensionVal("_", givenDimension));
			}
			sb.append("\n");
		}
		WToFile.writeToNewFile(sb.toString(), commonPath + "allDayTimelinesFromList.csv");

		DistMetricI jaccardDistance = new JaccardDistance1();

		computeDistances(givenDimension, commonPath, allDayTimelines, jaccardDistance,
				commonPath + "JaccardDistance.csv");
	}

	/**
	 * For sanity checking jaccard implementation.
	 * 
	 * @param givenDimension
	 * @param commonPath
	 * @param allDayTimelines
	 * @param distMetric
	 * @param absFileNameToWrite
	 */
	private static void computeDistances(PrimaryDimension givenDimension, String commonPath,
			List<ArrayList<ActivityObject2018>> allDayTimelines, DistMetricI distMetric, String absFileNameToWrite)
	{
		StringBuilder sb2 = new StringBuilder("timeline1Length, timeline2Length, distance,timeline1,timeline2\n");
		for (ArrayList<ActivityObject2018> timeline1 : allDayTimelines)
		{
			String timeline1String = timeline1.stream().map(ao -> ao.getGivenDimensionVal("_", givenDimension))
					.collect(Collectors.joining(">>"));

			for (ArrayList<ActivityObject2018> timeline2 : allDayTimelines)
			{
				String timeline2String = timeline2.stream().map(ao -> ao.getGivenDimensionVal("_", givenDimension))
						.collect(Collectors.joining(">>"));

				double jd = StatsUtils.round(distMetric.getDistance(timeline1, timeline2, givenDimension), 4);
				sb2.append(timeline1.size() + "," + timeline2.size() + "," + jd + "," + timeline1String + ","
						+ timeline2String + "\n");
			}
		}
		WToFile.writeToNewFile(sb2.toString(), absFileNameToWrite);
	}

}
