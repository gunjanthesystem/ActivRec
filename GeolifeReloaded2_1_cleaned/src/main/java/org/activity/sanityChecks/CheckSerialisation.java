package org.activity.sanityChecks;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.activity.io.Serializer;

import java.util.TreeMap;

public class CheckSerialisation
{
	static String testString;
	
	static String commonPath = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/TrajectorySpace/SerializerSanityChecks/";
	
	public static void main(String[] args)
	{
		try
		{
			// Object objectToSerialise = new<String> Object();
			testString = generateTestString(100);
			
			// $LinkedHashMap<String, LinkedHashMap<String, Integer>> mapLL = generateLLTestMap(8, 10);
			// $LinkedHashMap<String, TreeMap<String, Integer>> mapLT = generateLTTestMap(8, 10);
			// objectToSerialise = testString;
			// System.out.println("test String generate = " + testString);
			// $checkSerialisationLL(mapLL);
			// $checkSerialisationLT(mapLT);
			
			byte[] bytes = Serializer.getJSONBytesfst(testString);
			Path path = Paths.get(commonPath + "String.json");
			java.nio.file.Files.write(path, bytes);
			
			byte[] des = java.nio.file.Files.readAllBytes(path);
			String result = (String) Serializer.getObjectFromJSONBytesfst(des);
			assertEquals(testString, result);
			System.out.println("---------------------------");
			
			// // Serializer.serializeThis(testString, commonPath + "testString.strObj");
			// String deserialisedString = (String) Serializer.deSerializeThis(commonPath + "testString.strObj");// (testString, commonPath + "testString.strObj");
			//
			Serializer.fstSerializeThis(testString, commonPath + "testString.strObj_fst");
			String fstDeserialisedString = (String) Serializer.fstDeSerializeThis(commonPath + "testString.strObj_fst");// , new String());// (testString, commonPath +
			// //
			// // Serializer.kryoSerializeThis(testString, commonPath + "testString.strObj_kryo");
			// String kryoDeserialisedString = (String) Serializer.kryoDeSerializeThis(commonPath + "testString.strObj_kryo");// , new String());// (testString, commonPath +
			//
			// // System.out.println("deserialisedString = " + deserialisedString);
			// System.out.println("---------------------------");
			// // System.out.println("kryoDeserialisedString = " + kryoDeserialisedString);
			// System.out.println("---------------------------");
			//
			// assertEquals(deserialisedString, kryoDeserialisedString);
			// assertEquals(deserialisedString, fstDeserialisedString);
			//
			// System.out.println("Both deserialised strings are equal ? " + deserialisedString.equals(kryoDeserialisedString));
			// System.out.println("original string == kryoDeserialisedString ? " + testString.equals(kryoDeserialisedString));
			//
			// System.out.println("Both deserialised strings are equal ? " + deserialisedString.equals(fstDeserialisedString));
			// System.out.println("original string == fstDeserialisedString ? " + testString.equals(fstDeserialisedString));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void checkSerialisationLL(LinkedHashMap<String, LinkedHashMap<String, Integer>> mapLL)
	{
		System.out.println("----------checkSerialisationLL start----------------");
		Serializer.kryoSerializeThis(mapLL, commonPath + "mapLL.ser");
		LinkedHashMap<String, LinkedHashMap<String, Integer>> kryoDeserialisedMap =
				(LinkedHashMap<String, LinkedHashMap<String, Integer>>) Serializer.kryoDeSerializeThis(commonPath + "mapLL.ser");// , new String());// (testString, commonPath +
		// "testString.strObj");
		
		System.out.println("Size of mapLL to be serialised = " + mapLL.size());
		System.out.println("Size of deserialised mapLL = " + kryoDeserialisedMap.size());
		
		System.out.println("travserse mapll to be serialised:");
		traverseMapLL(mapLL);
		
		System.out.println("travserse dserialised mapll:");
		traverseMapLL(kryoDeserialisedMap);
		
		// System.out.println("deserialisedString = " + deserialisedString);
		
		// System.out.println("kryoDeserialisedString = " + kryoDeserialisedString);
		System.out.println("-----------checkSerialisationLL end----------------");
		
		assertEquals(mapLL, kryoDeserialisedMap);
		
	}
	
	public static void checkSerialisationLT(LinkedHashMap<String, TreeMap<String, Integer>> mapLT)
	{
		System.out.println("-----------checkSerialisationLT start----------------");
		Serializer.kryoSerializeThis(mapLT, commonPath + "mapLT.ser");
		LinkedHashMap<String, TreeMap<String, Integer>> kryoDeserialisedMap =
				(LinkedHashMap<String, TreeMap<String, Integer>>) Serializer.kryoDeSerializeThis(commonPath + "mapLT.ser");// , new String());// (testString, commonPath +
		// "testString.strObj");
		
		System.out.println("Size of mapLT to be serialised = " + mapLT.size());
		System.out.println("Size of deserialised mapLT = " + kryoDeserialisedMap.size());
		
		System.out.println("travserse maplt to be serialised:");
		traverseMapLT(mapLT);
		
		System.out.println("travserse dserialised maplt:");
		traverseMapLT(kryoDeserialisedMap);
		
		// System.out.println("deserialisedString = " + deserialisedString);
		
		// System.out.println("kryoDeserialisedString = " + kryoDeserialisedString);
		System.out.println("-----------checkSerialisationLT end----------------");
		
		assertEquals(mapLT, kryoDeserialisedMap);
		
	}
	
	public static void traverseMapLL(LinkedHashMap<String, LinkedHashMap<String, Integer>> mapLL)
	{
		for (Entry<String, LinkedHashMap<String, Integer>> out : mapLL.entrySet()) // Iterate over Users
		{
			System.out.println("Outer key = " + out.getKey());
			
			System.out.println("Number of inners in this outer =" + out.getValue().size());
			
			for (Map.Entry<?, ?> in : out.getValue().entrySet())
			{
				System.out.println("Inner key : " + in.getKey() + " Inner val : " + in.getValue());
				// dayTimelineEntry.getValue().printActivityEventNamesInSequence();
				// 21Oct dayTimelineEntry.getValue().printActivityEventNamesWithTimestampsInSequence();
				System.out.println();
			}
		}
		
	}
	
	public static void traverseMapLT(LinkedHashMap<String, TreeMap<String, Integer>> mapLT)
	{
		for (Entry<String, TreeMap<String, Integer>> out : mapLT.entrySet()) // Iterate over Users
		{
			System.out.println("Outer key = " + out.getKey());
			
			System.out.println("Number of inners in this outer =" + out.getValue().size());
			
			for (Map.Entry<?, ?> in : out.getValue().entrySet())
			{
				System.out.println("Inner key : " + in.getKey() + " Inner val : " + in.getValue());
				// dayTimelineEntry.getValue().printActivityEventNamesInSequence();
				// 21Oct dayTimelineEntry.getValue().printActivityEventNamesWithTimestampsInSequence();
				System.out.println();
			}
		}
		
	}
	
	public static String generateTestString(int rounds)
	{
		long dt = System.currentTimeMillis();
		StringBuffer t = new StringBuffer();
		
		for (int i = 1; i < rounds; i++)
		{
			// for (char c = 'a'; c <= 'z'; c++)
			// {
			// t.append(c);
			// }
			
			for (int j = 1; j <= rounds; j++)
			{
				t.append("ajooba" + i + "_" + j + ",");
			}
		}
		long lt = System.currentTimeMillis();
		System.out.println("generateTestString took " + (lt - dt) / 1000 + " secs");
		return t.toString();
	}
	
	public static LinkedHashMap<String, LinkedHashMap<String, Integer>> generateLLTestMap(int roundA, int roundB)
	{
		LinkedHashMap<String, LinkedHashMap<String, Integer>> mapLL = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();
		
		long dt = System.currentTimeMillis();
		StringBuffer t = new StringBuffer();
		
		for (int i = 1; i < roundA; i++)
		{
			LinkedHashMap<String, Integer> mapInside = new LinkedHashMap<String, Integer>();
			
			for (int j = 1; j <= roundB; j++)
			{
				mapInside.put("In" + j, i + j);
			}
			mapLL.put("Out" + i, mapInside);
		}
		
		long lt = System.currentTimeMillis();
		System.out.println("generateTestString took " + (lt - dt) / 1000 + " secs");
		return mapLL;
	}
	
	public static LinkedHashMap<String, TreeMap<String, Integer>> generateLTTestMap(int roundA, int roundB)
	{
		LinkedHashMap<String, TreeMap<String, Integer>> mapLL = new LinkedHashMap<String, TreeMap<String, Integer>>();
		
		long dt = System.currentTimeMillis();
		StringBuffer t = new StringBuffer();
		
		for (int i = 1; i < roundA; i++)
		{
			TreeMap<String, Integer> mapInside = new TreeMap<String, Integer>();
			
			for (int j = 1; j <= roundB; j++)
			{
				mapInside.put("In" + j, i + j);
			}
			mapLL.put("Out" + i, mapInside);
		}
		
		long lt = System.currentTimeMillis();
		System.out.println("generateTestString took " + (lt - dt) / 1000 + " secs");
		return mapLL;
	}
	
}
