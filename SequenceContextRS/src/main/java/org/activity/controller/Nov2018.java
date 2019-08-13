package org.activity.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.activity.io.ReadingFromFile;

public class Nov2018
{

	String[] alphas = { "0.0", "0.15", "0.25", "0.5", "0.75", "1.0" };

	public static void main(String args[])
	{
		String chosenMuFile = "/home/gunjan/RWorkspace/GowallaRWorks/ChosenMU.csv";

		// List<String> res =
		// ReadingFromFile.twoColumnReaderString("/home/gunjan/RWorkspace/GowallaRWorks/ChosenMU.csv",
		// ",", 0, 1, true);
		List<List<Double>> res = ReadingFromFile.nColumnReaderDouble(chosenMuFile, ",", true, false);
		Map<Double, Double> userChosenMU = new LinkedHashMap<>();

		for (List<Double> e : res)
		{
			userChosenMU.put(e.get(0), e.get(1));
		}

		System.out.println("userChosenMU.size() = " + userChosenMU.size());
		System.out.println("userChosenMU = " + userChosenMU);

		// ReadingFromFile.twoColumnReaderString("/home/gunjan/RWorkspace/GowallaRWorks/ChosenMU.csv", ",", 0, 1, true);
		for (int user = 1; user <= 18; user++)
		{

		}
	}
}
