package org.activity.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.activity.objects.Pair;
import org.json.JSONException;
import org.json.JSONObject;

public class Test3
{

	public static void main2(String[] args)
	{
		String testJson = "{'url': '/categories/15', 'name': 'Mexican'}";
		// "{'url': '/categories/449', 'name': ""Dunkin' Donuts""}"

		try
		{
			JSONObject jObj = new JSONObject(testJson);

			String[] urlSplitted = jObj.get("url").toString().split("/");
			String catID = urlSplitted[urlSplitted.length - 1];
			System.out.println(catID);
			System.out.println(jObj.get("name"));
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main1(String[] args)
	{
		// TODO Auto-generated method stub
		System.out.println("Hello world");
		String subset1Filename = "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/Gowalla/link to Gowalla dataset/another source/gowalla/gowalla_spots_subset1.csv";

		HashMap<Long, ArrayList<String>> subset1Map = org.activity.generator.DatabaseCreatorGowallaQuickerPreprocessor
				.readSpotSubset(subset1Filename, 3, 2);

		System.out.println("traversing subset 1");
		for (Entry<Long, ArrayList<String>> e : subset1Map.entrySet())
		{
			ArrayList<String> vals = e.getValue();
			System.out.println("Key:" + e.getKey() + " Value:" + vals.toString());

			String catIDString = vals.get(vals.size() - 2);
			String catNameString = vals.get(vals.size() - 1);

			System.out.println("catIDString = " + catIDString);
			System.out.println("catNameString = " + catNameString);

			// Pattern forCatIDString = Pattern.compile(regex);

			// Extract Cat ID
			String catIDStringSplitted[] = catIDString.split("'");
			String catIDString2 = catIDStringSplitted[catIDStringSplitted.length - 1];
			String catIDStringSplitted3[] = catIDString2.split("/");
			String catID = catIDStringSplitted3[catIDStringSplitted3.length - 1];
			System.out.println("catID: " + catID);
			System.out.println("catID: " + getSpotCatID(vals));

			// Extract Cat Name
			String catNameStringSplitted[] = catNameString.split("'");
			String catName = catNameStringSplitted[catNameStringSplitted.length - 2];
			System.out.println("catNameString2: " + catName);
			System.out.println("catNameString2: " + getSpotCatName(vals));

		}

	}

	// public static void main(String[] args)
	// {
	// Strins jsonString = "'name': ""Doctor's Office""}]"]";
	// jsonString = jsonString.replaceAll("[a-z]+'[a-z]+", "[a-z]+[a-z]+");
	// }

	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		System.out.println("Hello world");
		String subset1Filename = "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/Gowalla/link to Gowalla dataset/another source/gowalla/gowalla_spots_subset1.csv";

		HashMap<Long, ArrayList<String>> subset1Map = org.activity.generator.DatabaseCreatorGowallaQuickerPreprocessor
				.readSpotSubset(subset1Filename, 3, 2);

		System.out.println("traversing subset 1");
		for (Entry<Long, ArrayList<String>> e : subset1Map.entrySet())
		{
			ArrayList<String> vals = e.getValue();
			System.out.println("Key:" + e.getKey() + " Value:" + vals.toString());

			Pair<String, String> res = getSpotCatIDCatName(vals);

			System.out.println("catID: " + getSpotCatID(vals));
			System.out.println("catNameString2: " + getSpotCatName(vals));

			System.out.println("--catID: " + res.getFirst());
			System.out.println("--catNameString2: " + res.getSecond());

		}

	}

	/**
	 * Replace all double and double double quotes in json values with single quote
	 * 
	 * @param vals
	 * @return
	 */
	public static Pair<String, String> getSpotCatIDCatName(ArrayList<String> vals)
	{
		Pair<String, String> res = null;// new Pair<String, String>();

		String jsonString = vals.get(vals.size() - 2) + "," + vals.get(vals.size() - 1);
		jsonString = jsonString.substring(2, jsonString.length() - 2);

		// jsonString = StringEscapeUtils.escapeJson(jsonString);
		jsonString = jsonString.replaceAll("\"\"", "'");
		jsonString = jsonString.replaceAll("\'s", "^s");
		jsonString = jsonString.replaceAll("n\' D", "n^ D");

		// jsonString = jsonString.replaceAll("([a-z]+)(')([s])", "\2\4");
		// jsonString = jsonString.replaceAll("(\\D+)(')(\\D+)", "\2\4");
		System.out.println("--> jsonString =" + jsonString);
		// jsonString = jsonString.replaceAll("\"", "'");

		try
		{
			JSONObject jObj = new JSONObject(jsonString);

			String[] urlSplitted = jObj.get("url").toString().split("/");
			String catID = urlSplitted[urlSplitted.length - 1];

			res = new Pair<String, String>(catID, jObj.get("name").toString());

			// System.out.println(catID);
			// System.out.println(jObj.get("name"));
		}
		catch (Exception e)
		{
			System.out.println("json String used was " + jsonString);
			e.printStackTrace();
		}
		return res;
	}

	public static String getSpotCatID(ArrayList<String> vals)
	{
		String catIDString = vals.get(vals.size() - 2);
		String catIDStringSplitted[] = catIDString.split("'");
		String catIDString2 = catIDStringSplitted[catIDStringSplitted.length - 1];
		String catIDStringSplitted3[] = catIDString2.split("/");
		String catID = catIDStringSplitted3[catIDStringSplitted3.length - 1];
		return catID;
	}

	public static String getSpotCatName(ArrayList<String> vals)
	{
		String catNameString = vals.get(vals.size() - 1);
		String catNameStringSplitted[] = catNameString.split("'");
		String catName = catNameStringSplitted[catNameStringSplitted.length - 2];
		return catName;
	}
}
