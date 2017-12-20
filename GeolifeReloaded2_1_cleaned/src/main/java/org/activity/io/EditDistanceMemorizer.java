package org.activity.io;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.activity.constants.Constant;
import org.activity.objects.Pair;

/**
 * 
 * @author gunjan
 * @since 9 Aug 2017
 */
public class EditDistanceMemorizer
{
	int bufferSizeBeforeWrite;
	static int countOfSerialisedMaps = 0;
	// int currentCountInBuffer;
	// StringBuilder editDistancesToWrite;
	ConcurrentMap<String, Pair<String, Double>> editDistancesMemory;

	public EditDistanceMemorizer(int bufferSizeBeforeWrite)
	{
		super();
		this.bufferSizeBeforeWrite = bufferSizeBeforeWrite;

		// currentCountInBuffer = 0;
		editDistancesMemory = new ConcurrentHashMap<>(bufferSizeBeforeWrite);
		// editDistancesToWrite = new StringBuilder();
	}

	public EditDistanceMemorizer()
	{
		this.bufferSizeBeforeWrite = Integer.MAX_VALUE;
		editDistancesMemory = new ConcurrentHashMap<>(bufferSizeBeforeWrite);
	}

	public void clearBuffer()
	{
		// currentCountInBuffer = 0;
		editDistancesMemory = new ConcurrentHashMap<>();
		// editDistancesToWrite = new StringBuilder();
	}

	public void addToMemory(String candTimelineID, String currentTimelineID,
			Pair<String, Double> editDistanceForThisCandidate)
	{

		editDistancesMemory.put(candTimelineID + "|" + currentTimelineID, editDistanceForThisCandidate);
		// System.out
		// .println("Debug8Aug2: addToMemory() called: editDistancesMemory.size()=" + editDistancesMemory.size());
		// PopUps.showMessage(
		// "Debug8Aug2: addToMemory() called: editDistancesMemory.size()=" + editDistancesMemory.size());

		// Curtain start because not working well with parallel processing
		if (editDistancesMemory.size() > bufferSizeBeforeWrite)
		{
			System.out.println("editDistancesMemory.size()= " + editDistancesMemory.size());

			// StringBuilder sb = new StringBuilder();
			// editDistancesMemory.entrySet().stream()
			// .forEachOrdered(e -> sb.append(e.getKey() + " : " + e.getValue() + "\n"));
			// System.out.println(sb.toString());

			countOfSerialisedMaps += 1;

			// long t0 = System.nanoTime();
			// $Serializer.kryoSerializeThis(editDistancesMemory,
			// $Constant.outputCoreResultsPath + "EditDistanceMemorised" + countOfSerialisedMaps + ".kryo");
			//
			// long t1 = System.nanoTime();
			StringBuilder sb = new StringBuilder();
			editDistancesMemory.entrySet().stream().forEach(e -> sb.append(e.getKey() + "," + e.getValue() + "\n"));
			WritingToFile.writeToNewFile(sb.toString(),
					Constant.getOutputCoreResultsPath() + "EditDistanceMemorised" + countOfSerialisedMaps + ".csv");
			// long t2 = System.nanoTime();

			// System.out.println("Writing took:" + (t2 - t1) + " nsecs");
			// System.out.println("Serial took:" + (t1 - t0) + " nsecs");
			editDistancesMemory.clear();
		}
		// Curtain end
	}

	public void serialise()
	{
		System.out.println("editDistancesMemory.size()= " + editDistancesMemory.size());

		// StringBuilder sb = new StringBuilder();
		// editDistancesMemory.entrySet().stream()
		// .forEachOrdered(e -> sb.append(e.getKey() + " : " + e.getValue() + "\n"));
		// System.out.println(sb.toString());

		countOfSerialisedMaps += 1;
		Serializer.kryoSerializeThis(editDistancesMemory,
				Constant.getOutputCoreResultsPath() + "EditDistanceMemorised" + countOfSerialisedMaps + ".kryo");

		editDistancesMemory.clear();

	}

	public void serialise(String userID)
	{
		System.out.println("editDistancesMemory.size()= " + editDistancesMemory.size());

		// StringBuilder sb = new StringBuilder();
		// editDistancesMemory.entrySet().stream()
		// .forEachOrdered(e -> sb.append(e.getKey() + " : " + e.getValue() + "\n"));
		// System.out.println(sb.toString());
		countOfSerialisedMaps += 1;
		Serializer.kryoSerializeThis(editDistancesMemory, Constant.getOutputCoreResultsPath()
				+ "EditDistanceMemorised_User" + userID + "_" + countOfSerialisedMaps + ".kryo");

		editDistancesMemory.clear();

	}

}
