package org.activity.generator;

import java.util.Random;

import org.activity.io.CSV2Arff;
import org.activity.io.WToFile;
import org.activity.stats.StatsUtils;

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
			for (int timeID = 1; timeID <= 50/* 1000 */; timeID++)
			{
				Random random = new Random();
				// double val = Math.random.nextGaussian();

				int activityID = 0; // = StatsUtils.randomInRangeWithBias(0, 5, 4, 0.55);

				// if (Math.random() < 0.80 && (prevNumber == 4 || prevNumber == 2))
				// {
				// activityID = 1;
				// }
				// else
				{
					activityID = StatsUtils.randomInRangeWithBias(0, 4, 4, 0.70);
				}
				prevNumber = activityID;
				sb.append(seqID + "," + timeID + "," + getSimpleCharCodeFromActivityID(activityID) + "\n");
			}
		}

		String nameLabel = "SynthDataBias70E";
		WToFile.writeToNewFile(sb.toString(),
				"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Mar29HMM/" + nameLabel + ".csv");// Jan26HMM

		CSV2Arff csv2arff = new CSV2Arff(
				"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Mar29HMM/" + nameLabel + ".csv",
				"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Mar29HMM/" + nameLabel + ".arff");
	}

	public static void main(String[] args)
	{
		// new GenerateSyntheticDataForHMMExperiments();
		// generateSyntheticDataForHMMExperiments2();
		generateSyntheticDataForHMMExperimentsR();
	}

	public static char getSimpleCharCodeFromActivityID(int activityID)
	{
		// uncode char from 127 to 159 are non printable, hence do not use them
		char code = '\u0000';// null character new String();
		try
		{
			code = (char) (activityID + 65); // 65 is A
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return code;
	}

	public static void generateSyntheticDataForHMMExperiments2()
	{
		StringBuilder sb = new StringBuilder();

		String header = "SEQUENCE_ID,TIME_ID,ActivityName";
		sb.append(header + "\n");

		for (int seqID = 0; seqID < 500; seqID++)
		{
			for (int timeID = 0; timeID < 500/* 1000 */; timeID++)
			{
				int activityID = timeID % 4; // = StatsUtils.randomInRangeWithBias(0, 5, 4, 0.55);
				sb.append(seqID + "," + timeID + "," + getSimpleCharCodeFromActivityID(activityID) + "\n");
			}
		}

		String nameLabel = "SynthDataTrivial2";
		WToFile.writeToNewFile(sb.toString(),
				"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Mar29HMM/" + nameLabel + ".csv");// Jan26HMM

		CSV2Arff csv2arff = new CSV2Arff(
				"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Mar29HMM/" + nameLabel + ".csv",
				"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Mar29HMM/" + nameLabel + ".arff");
	}

	public static void generateSyntheticDataForHMMExperimentsR()
	{

		int tLen = 50;
		int numUsers = 10;
		StringBuilder header = new StringBuilder("UserID");

		for (int i = 1; i <= tLen; i++)
		{
			header.append(',').append('t').append(i);
		}
		header.append('\n');

		for (int u = 1; u <= numUsers; u++)
		{
			StringBuilder tdata = new StringBuilder();
			tdata.append(u);
			for (int i = 1; i <= tLen; i++)
			{
				int activityID = StatsUtils.randomInRangeWithBias(0, 4, 4, 0.70);// (i - 1) % 4)

				tdata.append(',').append(getSimpleCharCodeFromActivityID(activityID));
			}
			tdata.append('\n');

			header.append(tdata);
		}

		String nameLabel = "SynthDataRBias70";
		WToFile.writeToNewFile(header.toString(),
				"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Mar29HMM/" + nameLabel + ".csv");// Jan26HMM

		// CSV2Arff csv2arff = new CSV2Arff(
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Mar29HMM/" + nameLabel + ".csv",
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Mar29HMM/" + nameLabel + ".arff");
	}

}
