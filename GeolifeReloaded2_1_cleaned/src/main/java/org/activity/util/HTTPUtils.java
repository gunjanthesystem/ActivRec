package org.activity.util;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.activity.io.ReadXML;
import org.activity.io.ReadingFromFile;
import org.activity.io.WritingToFile;
import org.activity.objects.OpenStreetAddress;
import org.activity.objects.Pair;
import org.activity.objects.Triple;
import org.w3c.dom.Document;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HTTPUtils
{
	OkHttpClient client = new OkHttpClient();

	String getResponse(String url) throws IOException
	{
		Request request = new Request.Builder().url(url).build();

		try (Response response = client.newCall(request).execute())
		{
			return response.body().string();
		}
	}

	/**
	 * 
	 * @param lat
	 * @param lon
	 * @param httpUtil
	 * @param readXML
	 * @return
	 */
	public static Pair<Map<String, String>, String> getAddress(String lat, String lon, HTTPUtils httpUtil,
			ReadXML readXML)
	{
		Map<String, String> allVals = new LinkedHashMap<>();
		String response = "";
		try
		{
			// String response = httpUtil.getResponse("https://nominatim.openstreetmap.org/reverse?format=xml&lat=" +
			// lat
			// + "&lon=" + lon + "&zoom=18&addressdetails=1");
			//
			response = httpUtil.getResponse("https://nominatim.openstreetmap.org/reverse?format=xml&lat=" + lat
					+ "&lon=" + lon + "&zoom=18&addressdetails=1&accept-language=en");

			// $$ System.out.println(response);// enable to see actual response with all details

			Document doc = readXML.loadXMLFromString(response);
			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			// $System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			String[] listOfTags = { /* "house_number", */ "road", "pedestrian", "city", "town", "village", "hamlet",
					"county", "state", "region", "postcode", "country", "country_code" };
			for (String tagName : listOfTags)
			{
				String val = "";
				// System.out.println("tagName=" + tagName);

				if ((doc.getElementsByTagName(tagName) == null) || (doc.getElementsByTagName(tagName).item(0) == null)
						|| (doc.getElementsByTagName(tagName).item(0).getTextContent() == null))
				{
					// tags allowed empty individually
					if ((tagName.equals("city") == false) && (tagName.equals("town") == false)
							&& (tagName.equals("village") == false) && (tagName.equals("hamlet") == false)
							&& (tagName.equals("pedestrian") == false) && (tagName.equals("road") == false)
							&& (tagName.equals("county") == false) && (tagName.equals("state") == false)
							&& (tagName.equals("region") == false) && (tagName.equals("postcode") == false))
					{
						System.out.println("null for=" + tagName + " response was:\n" + response);
					}
				}
				else
				// ((doc.getElementsByTagName(tagName) != null)
				// && (doc.getElementsByTagName(tagName).item(0).getTextContent() != null))
				{
					val = doc.getElementsByTagName(tagName).item(0).getTextContent();
				}

				allVals.put(tagName, val);
			}
			// // NodeList nList = doc.getElementsByTagName("house_number");
			// StringBuilder sb = new StringBuilder();
			// allVals.entrySet().stream().forEachOrdered(e -> sb.append(e.getKey() + "-" + e.getValue() + "\n"));
			// System.out.println(sb.toString() + "\n----------------------------");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return new Pair<>(allVals, response);
	}

	/**
	 * 
	 * @param lat
	 * @param lon
	 * @param httpUtil
	 * @param readXML
	 * @return
	 */
	public static Triple<OpenStreetAddress, Boolean, String> getAddressOSA(String lat, String lon, HTTPUtils httpUtil,
			ReadXML readXML)
	{
		Pair<Map<String, String>, String> allValsRes = getAddress(lat, lon, httpUtil, readXML);

		Map<String, String> allVals = allValsRes.getFirst();
		String response = allValsRes.getSecond();

		String cityOrTownOrVillageOrHamlet = "", roadOrPedestrian = "", stateOrRegion = "";

		boolean neitherCityTownOrVillageOrHamlet = false, neitherRoadOrPedestrian = false,
				neitherStateNorRegion = false;

		// StringBuilder sb = new StringBuilder();
		// allVals.entrySet().stream().forEachOrdered(e -> sb.append(e.getKey() + "-" + e.getValue() + "\n"));
		// System.out.println("What we got is:--" + sb.toString() + "\n");

		if (allVals.get("city").trim().length() != 0)// != null)
		{
			cityOrTownOrVillageOrHamlet = allVals.get("city");
		}
		else if (allVals.get("town").trim().length() != 0)// != null)
		{
			cityOrTownOrVillageOrHamlet = allVals.get("town");
		}
		else if (allVals.get("village").trim().length() != 0)// != null)
		{
			cityOrTownOrVillageOrHamlet = allVals.get("village");
		}
		else if (allVals.get("hamlet").trim().length() != 0)// != null)
		{
			cityOrTownOrVillageOrHamlet = allVals.get("hamlet");
			// System.out.println("This is a hamlet");
		}
		else
		{
			cityOrTownOrVillageOrHamlet = "";
			neitherCityTownOrVillageOrHamlet = true;
			// System.out.println("neitherCityTownOrVillageOrHamlet= " + neitherCityTownOrVillageOrHamlet);
		}

		if (allVals.get("road").trim().length() != 0)// != null)
		{
			roadOrPedestrian = allVals.get("road");
		}
		else if (allVals.get("pedestrian").trim().length() != 0)// != null)
		{
			roadOrPedestrian = allVals.get("pedestrian");
		}
		else
		{
			roadOrPedestrian = "";
			neitherRoadOrPedestrian = true;
		}

		if (allVals.get("state").trim().length() != 0)// != null)
		{
			stateOrRegion = allVals.get("state");
		}
		else if (allVals.get("region").trim().length() != 0)// != null)
		{
			stateOrRegion = allVals.get("region");
		}
		else
		{
			stateOrRegion = "";
			neitherStateNorRegion = true;
		}

		if (neitherCityTownOrVillageOrHamlet)
		{
			System.out.println("neitherCityTownOrVillageOrHamlet= " + neitherCityTownOrVillageOrHamlet);
		}

		// System.out.println("cityOrTownOrVillageOrHamlet=" + cityOrTownOrVillageOrHamlet);
		return new Triple<>(new OpenStreetAddress(/* allVals.get("house_number"), */ roadOrPedestrian,
				cityOrTownOrVillageOrHamlet, allVals.get("county"), stateOrRegion, allVals.get("postcode"),
				allVals.get("country"), allVals.get("country_code")), neitherCityTownOrVillageOrHamlet, response);

	}

	public static void main0(String args[])
	{

		HTTPUtils httpUtils = new HTTPUtils();
		try
		{
			System.out.println(httpUtils
					.getResponse("https://maps.google.com/maps/api/geocode/xml?address=Malvern+City+of+Stonnington"));
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// https://maps.google.com/maps/api/geocode/xml?address=Malvern+City+of+Stonnington
	public static void main(String[] args)
	{
		Map<Integer, OpenStreetAddress> locIDAllAddressMap = new LinkedHashMap<>();
		String commonPathToRead = "/home/gunjan/JupyterWorkspace/data/d10/";
		// String commonPathToWrite = commonPathToRead;
		long totalNumOfEmptyAddresses = 0, totalNumOfNeitherCityTownOrVillage = 0;
		for (int iteratorID = 1; iteratorID <= 10; iteratorID++)
		{
			String fileNameToReadPhrase = "gowalla_spots_subset1_fromRaw28Feb2018smallerFileWithSampleWithTZ"
					+ iteratorID + ".csv";
			String fileNameToWritePhrase = "gowalla_spots_subset1_fromRaw28Feb2018smallerFileWithSampleWithTZAddress"
					+ iteratorID + ".csv";

			Pair<Map<Integer, OpenStreetAddress>, Long> locIDAddressMapRes = getAddressesForLatLonForRawFile(
					commonPathToRead, fileNameToReadPhrase, fileNameToWritePhrase);

			Map<Integer, OpenStreetAddress> locIDAddressMap = locIDAddressMapRes.getFirst();
			long numOfNeitherCityTownOrVillage = locIDAddressMapRes.getSecond();

			if (numOfNeitherCityTownOrVillage > 0)
			{
				StringBuilder sb = new StringBuilder();
				locIDAddressMap.entrySet().stream()
						.forEachOrdered(e -> sb.append(e.getKey() + ": " + e.getValue().toString('|') + "\n"));
				System.out.println(sb.toString() + "\n locIDAddressMap.size= " + locIDAddressMap.size());
			}
			long numOfNonEmptyAdress = locIDAddressMap.entrySet().stream()
					.mapToLong(e -> e.getValue().isEmptyAddress() == false ? 1 : 0).sum();

			long numOfEmptyAddresses = (locIDAddressMap.size() - numOfNonEmptyAdress);
			totalNumOfEmptyAddresses += numOfEmptyAddresses;
			totalNumOfNeitherCityTownOrVillage += numOfNeitherCityTownOrVillage;
			// System.out.println("Num of empty adresses = " + numOfEmptyAddresses);
			// System.out.println("numOfNeitherCityTownOrVillage = " + numOfNeitherCityTownOrVillage);
		}

		System.out.println("totalNumOfEmptyAddresses = " + totalNumOfEmptyAddresses);
		System.out.println("totalNumOfNeitherCityTownOrVillage = " + totalNumOfNeitherCityTownOrVillage);
	}

	/**
	 * 
	 * @param commonPathToRead
	 * @param fileNameToReadPhrase
	 * @param fileNameToWritePhrase
	 * @return
	 */
	public static Pair<Map<Integer, OpenStreetAddress>, Long> getAddressesForLatLonForRawFile(String commonPathToRead,
			String fileNameToReadPhrase, String fileNameToWritePhrase)
	{
		Map<Integer, OpenStreetAddress> locIDAddressMap = new LinkedHashMap<>();

		long numOfNeitherCityTownOrVillage = 0;
		// String commonPathToRead = "/home/gunjan/JupyterWorkspace/data/d10/";
		// // String commonPathToWrite = commonPathToRead;
		// String fileNameToReadPhrase = "gowalla_spots_subset1_fromRaw28Feb2018smallerFileWithSampleWithTZ1.csv";
		// String fileNameToWritePhrase =
		// "gowalla_spots_subset1_fromRaw28Feb2018smallerFileWithSampleWithTZAddress1.csv";
		try
		{
			BufferedWriter bwToWrite = WritingToFile.getBWForNewFile(commonPathToRead + fileNameToWritePhrase);
			bwToWrite.write("id|lng|lat|TZ|road|cityOrTownOrVillage|county|state|postcode|country|country_code\n");

			List<List<String>> allLines = ReadingFromFile.nColumnReaderStringLargeFile(
					new FileInputStream(commonPathToRead + fileNameToReadPhrase), ",", true, false);

			int count = 0;
			List<String> header = new ArrayList<>();

			for (List<String> line : allLines)
			{
				count += 1;
				if (count == 1)
				{
					header = line;
					continue;
				}
				if (count > 20)
				{
					break;
				}
				// String lat = "40.774269";
				// String lon = "-89.603806";
				// int id = 1;
				String lat = line.get(3);
				String lon = line.get(2);
				int id = Integer.valueOf(line.get(1));

				HTTPUtils httpUtils = new HTTPUtils();
				ReadXML readXML = new ReadXML();

				// Map<String, String> allVals = getAddressOSA(lat, lon, httpUtils, readXML);
				// StringBuilder sb = new StringBuilder();
				// allVals.entrySet().stream().forEachOrdered(e -> sb.append(e.getKey() + ": " + e.getValue() + "\n"));
				// System.out.println(sb.toString() + "\n----------------------------");
				Triple<OpenStreetAddress, Boolean, String> addressRes = getAddressOSA(lat, lon, httpUtils, readXML);

				if (addressRes.getSecond() == true)
				{
					numOfNeitherCityTownOrVillage += 1;
					System.out.println("Response for neitherCityTownOrVillage true is:\n "
							+ ReadXML.prettyPrint(addressRes.getThird()));
				}

				OpenStreetAddress address = addressRes.getFirst();
				locIDAddressMap.put(id, address);

				StringBuilder sb2 = new StringBuilder();

				char delimiter = '|';
				sb2.append(line.stream().skip(1).collect(Collectors.joining(String.valueOf(delimiter))).toString()
						+ delimiter);
				sb2.append(address.toString(delimiter) + "\n");
				bwToWrite.write(sb2.toString());
			}

			bwToWrite.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return new Pair<>(locIDAddressMap, numOfNeitherCityTownOrVillage);
	}
}