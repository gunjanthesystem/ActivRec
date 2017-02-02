package org.activity.generator;

import java.util.Random;

import org.activity.io.WritingToFile;
import org.activity.util.StatsUtils;
import org.activity.util.StringCode;
import org.activity.util.weka.CSV2Arff;

public class GenerateSyntheticDataForHMMExperiments
{

	public GenerateSyntheticDataForHMMExperiments()
	{
		StringBuilder sb = new StringBuilder();

		String header = "SEQUENCE_ID,TIME_ID,ActivityName";
		sb.append(header + "\n");

		for (int seqID = 1; seqID <= 10/* 50 */; seqID++)
		{
			// Timestamp t = new Timestamp(2005, 1, 1, 0, 0, 0, 0);

			int prevNumber = -1;
			for (int timeID = 1; timeID <= 200/* 1000 */; timeID++)
			{
				Random random = new Random();
				// double val = Math.random.nextGaussian();

				int activityID = 0; // = StatsUtils.randomInRangeWithBias(0, 5, 4, 0.55);
				if (Math.random() < 0.80 && (prevNumber == 4 || prevNumber == 2))
				{
					activityID = 1;
				}
				else
				{
					activityID = StatsUtils.randomInRangeWithBias(0, 5, 4, 0.55);
				}
				prevNumber = activityID;
				sb.append(seqID + "," + timeID + "," + StringCode.getCharCodeFromActivityID(activityID) + "\n");
			}
		}

		WritingToFile.writeToNewFile(sb.toString(),
				"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Jan26HMM/SynthData2_test.csv");

		CSV2Arff csv2arff = new CSV2Arff(
				"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Jan26HMM/SynthData2_test.csv",
				"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Jan26HMM/SynthData2_test.arff");
	}

	public static void main(String[] args)
	{
		new GenerateSyntheticDataForHMMExperiments();
	}

}
