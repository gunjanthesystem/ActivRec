package org.activity.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import org.activity.constants.Constant;
import org.activity.objects.FlatActivityLogEntry;
import org.activity.objects.StayPointsAllDataContainer;
import org.activity.objects.TrajectoryEntry;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;

public class Serializer
{

	// static String commonPath="/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data
	// Works/WorkingSet7July/";//Constant.commonPath;
	static final String fileName = Constant.getCommonPath() + "flatFile";
	static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

	/**
	 * Optimised java native serialization </br>
	 * ref: http://java.dzone.com/articles/fast-java-file-serialization
	 * 
	 * @param obj
	 * @param fileName
	 * @deprecated favor kryoSerializeThis
	 */
	public static void serializeThis(Object obj, String fileName)
	{
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		long dt = System.currentTimeMillis();
		try
		{
			RandomAccessFile raf = new RandomAccessFile(fileName, "rw"); // for performance see
																			// http://java.dzone.com/articles/fast-java-file-serialization

			// FileOutputStream fos = new FileOutputStream(raf.getFD());

			fos = new FileOutputStream(raf.getFD());// fileName);
			out = new ObjectOutputStream(fos);
			out.writeObject(obj);
			out.close();
			raf.close();

			long lt = System.currentTimeMillis();
			System.out.println("Successfully serialised " + fileName + " in " + (lt - dt) / 1000 + " secs");
		}

		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Java native deserialization </br>
	 * ref: http://java.dzone.com/articles/fast-java-file-serialization
	 * 
	 * @param fileName
	 * @return
	 */
	public static Object deSerializeThis(String fileName)
	{
		Object obj = null;

		FileInputStream fis = null;
		ObjectInputStream in = null;
		System.out.println("Deserialising ..." + fileName);
		long dt = System.currentTimeMillis();
		try
		{
			RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
			fis = new FileInputStream(raf.getFD());// PfileName);
			in = new ObjectInputStream(fis);
			obj = in.readObject();
			// System.out.println("FF:"+e2.User_Name);
			in.close();
			raf.close();

			long lt = System.currentTimeMillis();
			System.out.println("Successfully deserialised " + fileName + " in " + (lt - dt) / 1000 + " secs");

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return obj;
	}

	/////////////////////////////////////////
	public static void fstSerializeThis(Object obj, String fileName)
	{
		FileOutputStream fos = null;
		FSTObjectOutput out = null;
		long dt = System.currentTimeMillis();
		try
		{
			RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
			fos = new FileOutputStream(raf.getFD());
			out = new FSTObjectOutput(fos);
			out.writeObject(obj);
			out.close();
			raf.close();
			long lt = System.currentTimeMillis();
			System.out.println("Successfully fstserialised " + fileName + " in " + (lt - dt) / 1000 + " secs");
		}

		catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}

	public static void fstSerializeThis2(Object obj, String fileName)
	{
		FileOutputStream fos = null;
		FSTObjectOutput out = null;
		long dt = System.currentTimeMillis();
		try
		{
			// FSTObjectOutput out = conf.getObjectOutput(stream);
			// out.writeObject( toWrite, MyClass.class );
			// // DON'T out.close() when using factory method;
			// out.flush();
			// stream.close();

			RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
			fos = new FileOutputStream(raf.getFD());
			out = new FSTObjectOutput(fos, conf);
			out.writeObject(obj);
			out.close();
			raf.close();
			long lt = System.currentTimeMillis();
			System.out.println("Successfully fstserialised " + fileName + " in " + (lt - dt) / 1000 + " secs");
		}

		catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}

	/**
	 * https://github.com/RuedigerMoeller/fast-serialization/wiki/Serialization#plain-objectoutputstream-replacement
	 * 
	 * @param obj
	 * @param fileName
	 * @param classOfObject
	 */
	public static void fstSerializeThis(Object obj, String fileName, Class classOfObject)
	{
		FileOutputStream fos = null;
		FSTObjectOutput out = null;
		long dt = System.currentTimeMillis();
		try
		{
			RandomAccessFile raf = new RandomAccessFile(fileName, "rw"); // for performance see
																			// http://java.dzone.com/articles/fast-java-file-serialization
			fos = new FileOutputStream(raf.getFD());// fileName);
			out = new FSTObjectOutput(fos);// FSTObjectInput
			out.writeObject(obj, classOfObject.getClass());
			out.flush();
			raf.close();

			long lt = System.currentTimeMillis();
			System.out.println("Successfully fstserialised " + fileName + " in " + (lt - dt) / 1000 + " secs");
		}

		catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}

	public static void fstSerializeThis2(Object obj, String fileName, Class classOfObject)
	{
		FileOutputStream fos = null;
		FSTObjectOutput out = null;
		long dt = System.currentTimeMillis();
		try
		{
			RandomAccessFile raf = new RandomAccessFile(fileName, "rw"); // for performance see
																			// http://java.dzone.com/articles/fast-java-file-serialization
			fos = new FileOutputStream(raf.getFD());// fileName);
			out = new FSTObjectOutput(fos, conf);// FSTObjectInput
			out.writeObject(obj, classOfObject.getClass());
			out.flush();
			raf.close();

			long lt = System.currentTimeMillis();
			System.out.println("Successfully fstserialised " + fileName + " in " + (lt - dt) / 1000 + " secs");
		}

		catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}

	public static void fstSerializeThisNoRandom(Object obj, String fileName, Class classOfObject)
	{
		FileOutputStream fos = null;
		FSTObjectOutput out = null;
		long dt = System.currentTimeMillis();
		try
		{
			fos = new FileOutputStream(fileName);// fileName);
			out = new FSTObjectOutput(fos, conf);// FSTObjectInput
			out.writeObject(obj, classOfObject.getClass());
			out.flush();
			fos.close();

			long lt = System.currentTimeMillis();
			System.out.println("Successfully fstserialised " + fileName + " in " + (lt - dt) / 1000 + " secs");
		}

		catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}

	public static void fstSerializeThisJSON(Object obj, String fileName)
	{
		FileOutputStream fos = null;
		FSTObjectOutput out = null;
		long dt = System.currentTimeMillis();
		try
		{
			FSTConfiguration conf = FSTConfiguration.createJsonNoRefConfiguration();

			byte[] bytes = conf.asByteArray(obj);

			System.out.println(new String(bytes, "UTF-8"));
			Object deser = conf.asObject(bytes);

			// RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
			// fos = new FileOutputStream(raf.getFD());
			// out = new FSTObjectOutput(fos);
			// out.writeObject(obj);
			// out.close();
			// raf.close();
			long lt = System.currentTimeMillis();
			System.out.println("Successfully fstserialised " + fileName + " in " + (lt - dt) / 1000 + " secs");
		}

		catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}

	public static byte[] getJSONBytesfst(Object obj)
	{
		long dt = System.currentTimeMillis();
		byte[] bytes = null;
		try
		{
			FSTConfiguration conf = FSTConfiguration.createJsonNoRefConfiguration();

			bytes = conf.asByteArray(obj);
			long lt = System.currentTimeMillis();
			System.out.println("Successfully jsonised  in " + (lt - dt) / 1000 + " secs");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return bytes;
	}

	public static Object getObjectFromJSONBytesfst(byte[] bytes)
	{
		long dt = System.currentTimeMillis();
		Object obj = null;
		try
		{
			FSTConfiguration conf = FSTConfiguration.createJsonNoRefConfiguration();
			obj = conf.asObject(bytes);
			long lt = System.currentTimeMillis();
			System.out.println("Successfully dejsonised  in " + (lt - dt) / 1000 + " secs");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return obj;
	}

	public static Object fstDeSerializeThis(String fileName)
	{
		Object obj = null;
		FileInputStream fis = null;
		FSTObjectInput in = null;
		System.out.println("Deserialising ..." + fileName);
		long dt = System.currentTimeMillis();
		try
		{
			RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
			fis = new FileInputStream(raf.getFD());
			in = new FSTObjectInput(fis);
			obj = in.readObject();
			in.close();
			raf.close();
			long lt = System.currentTimeMillis();
			System.out.println("Successfully fstdeserialised " + fileName + " in " + (lt - dt) / 1000 + " secs");

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return obj;
	}

	public static Object fstDeSerializeThis2(String fileName)
	{
		Object obj = null;
		FileInputStream fis = null;
		FSTObjectInput in = null;
		System.out.println("Deserialising ..." + fileName);
		long dt = System.currentTimeMillis();
		try
		{
			RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
			fis = new FileInputStream(raf.getFD());
			in = new FSTObjectInput(fis, conf);
			obj = in.readObject();
			in.close();
			raf.close();
			long lt = System.currentTimeMillis();
			System.out.println("Successfully fstdeserialised " + fileName + " in " + (lt - dt) / 1000 + " secs");

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return obj;
	}

	public static StayPointsAllDataContainer fstDeSerializeThis(String fileName,
			StayPointsAllDataContainer classOfObject)
	{
		StayPointsAllDataContainer obj = null;

		FileInputStream fis = null;
		FSTObjectInput in = null;
		System.out.println("Deserialising ..." + fileName);
		long dt = System.currentTimeMillis();
		try
		{
			RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
			fis = new FileInputStream(raf.getFD());// PfileName);
			in = new FSTObjectInput(fis);

			// obj = in.readObject();

			obj = (StayPointsAllDataContainer) in.readObject(StayPointsAllDataContainer.class);
			// DON'T: in.close(); here prevents reuse and will result in an exception
			// in.close();
			raf.close();

			// stream.close();

			long lt = System.currentTimeMillis();
			System.out.println("Successfully fstdeserialised " + fileName + " in " + (lt - dt) / 1000 + " secs");

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return obj;
	}

	public static StayPointsAllDataContainer fstDeSerializeThisNoRandom(String fileName,
			StayPointsAllDataContainer classOfObject)
	{
		StayPointsAllDataContainer obj = null;

		FileInputStream fis = null;
		FSTObjectInput in = null;
		System.out.println("Deserialising ..." + fileName);
		long dt = System.currentTimeMillis();
		try
		{
			fis = new FileInputStream(fileName);// PfileName);
			in = new FSTObjectInput(fis);

			// obj = in.readObject();

			obj = (StayPointsAllDataContainer) in.readObject(StayPointsAllDataContainer.class);
			// DON'T: in.close(); here prevents reuse and will result in an exception
			// in.close();
			fis.close();

			// stream.close();

			long lt = System.currentTimeMillis();
			System.out.println("Successfully fstdeserialised " + fileName + " in " + (lt - dt) / 1000 + " secs");

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return obj;
	}

	public static StayPointsAllDataContainer fstDeSerializeThis2(String fileName,
			StayPointsAllDataContainer classOfObject)
	{
		StayPointsAllDataContainer obj = null;

		FileInputStream fis = null;
		FSTObjectInput in = null;
		System.out.println("Deserialising ..." + fileName);
		long dt = System.currentTimeMillis();
		try
		{
			RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
			fis = new FileInputStream(raf.getFD());// PfileName);
			in = new FSTObjectInput(fis, conf);

			// obj = in.readObject();

			obj = (StayPointsAllDataContainer) in.readObject(StayPointsAllDataContainer.class);
			// DON'T: in.close(); here prevents reuse and will result in an exception
			// in.close();
			raf.close();

			// stream.close();

			long lt = System.currentTimeMillis();
			System.out.println("Successfully fstdeserialised " + fileName + " in " + (lt - dt) / 1000 + " secs");

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return obj;
	}
	/////////////////////////////////////////
	///////////////

	/**
	 * 
	 * @param obj
	 * @param fileName
	 */
	public static void kryoSerializeThis(Object obj, String fileName)
	{
		FileOutputStream fos = null;
		long dt = System.currentTimeMillis();
		try
		{
			RandomAccessFile raf = new RandomAccessFile(fileName, "rw"); // for performance see
																			// http://java.dzone.com/articles/fast-java-file-serialization
			fos = new FileOutputStream(raf.getFD());
			Output kryoOutput = new Output(fos);
			Kryo kryo = new Kryo();
			kryo.addDefaultSerializer(java.sql.Timestamp.class, TimestampSerializer.class);
			kryo.addDefaultSerializer(java.sql.Date.class, SqlDateSerializer.class);

			kryo.writeClassAndObject(kryoOutput, obj);
			kryoOutput.close();
			raf.close();
			// out.close();

			long lt = System.currentTimeMillis();
			System.out.println("Successfully kryo serialised " + fileName + " in " + (lt - dt) / 1000 + " secs");

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}

	/**
	 * 
	 * @param obj
	 * @param fileName
	 */
	public static void kryoSerializeLLThis(Object obj, String fileName)
	{
		FileOutputStream fos = null;
		long dt = System.currentTimeMillis();
		try
		{
			RandomAccessFile raf = new RandomAccessFile(fileName, "rw"); // for performance see
																			// http://java.dzone.com/articles/fast-java-file-serialization
			fos = new FileOutputStream(raf.getFD());
			Output kryoOutput = new Output(fos);
			Kryo kryo = new Kryo();
			kryo.addDefaultSerializer(java.sql.Timestamp.class, TimestampSerializer.class);
			////////////////
			kryo.register(LinkedHashMap.class);

			MapSerializer serializer = new MapSerializer();
			kryo.register(HashMap.class, serializer);
			kryo.register(LinkedHashMap.class, serializer);
			serializer.setKeyClass(String.class, kryo.getSerializer(String.class));
			serializer.setKeysCanBeNull(false);
			serializer.setValueClass(LinkedHashMap.class, serializer);
			serializer.setValuesCanBeNull(false);
			//////////////////

			kryo.writeClassAndObject(kryoOutput, obj);
			kryoOutput.close();
			raf.close();
			// out.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		long lt = System.currentTimeMillis();
		System.out.println("Successfully kryo serialised " + fileName + " in " + (lt - dt) / 1000 + " secs");
	}

	//// -----------
	public void kryoSerializeThis2(Object test, String fileName) throws IOException
	{
		Output output = null;
		try
		{
			RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
			output = new Output(new FileOutputStream(raf.getFD()));// , MAX_BUFFER_SIZE);
			Kryo kryo = new Kryo(); // version 2.x
			kryo.writeObject(output, test);
			raf.close();
		}
		finally
		{
			if (output != null)
			{
				output.close();
			}
		}
	}

	// ---------

	// //// -----------
	// public void kryoDeSerializeThis2(String fileName) throws IOException
	// {
	// Kryo kryo = new Kryo(); // version 2.x
	// Input input = null;
	// try
	// {
	// RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
	// input = new Input(new FileInputStream(raf.getFD()));// , MAX_BUFFER_SIZE);
	// kryo.readObject(input,);// , test);
	// }
	// finally
	// {
	// if (input != null)
	// {
	// input.close();
	// }
	// }
	// }

	// ---------

	///////////////

	public static Object kryoDeSerializeThis(String fileName)
	{
		Object obj = null;
		FileInputStream fis = null;
		long dt = System.currentTimeMillis();
		System.out.println("Kryo Deserialising ..." + fileName + " start-time:" + new Timestamp(dt));
		try
		{
			RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
			fis = new FileInputStream(raf.getFD());
			Input kryoInput = new Input(fis);
			Kryo kryo = new Kryo();
			kryo.addDefaultSerializer(java.sql.Timestamp.class, TimestampSerializer.class);
			kryo.addDefaultSerializer(java.sql.Date.class, SqlDateSerializer.class);

			obj = kryo.readClassAndObject(kryoInput);// , Object.class);
			raf.close();
			kryoInput.close();
			// kryoInput.close();

			long lt = System.currentTimeMillis();
			System.out.println("Successfully kryo deserialised " + fileName + " in " + (lt - dt) / 1000 + " secs");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		return obj;
	}

	public static Object kryoDeSerializeThis(String fileName, Object expectedObject)
	{
		Object obj = null;
		FileInputStream fis = null;
		System.out.println("Kryo Deserialising ..." + fileName);
		long dt = System.currentTimeMillis();
		try
		{
			RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
			fis = new FileInputStream(raf.getFD());
			Input kryoInput = new Input(fis);
			Kryo kryo = new Kryo(); // version 2.x
			obj = kryo.readObject(kryoInput, expectedObject.getClass());
			kryoInput.close();
			raf.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		long lt = System.currentTimeMillis();
		System.out.println("Successfully kryo deserialised " + fileName + " in " + (lt - dt) / 1000 + " secs");
		return obj;
	}

	// public static void toDelete()
	// {
	// kryo.util.DefaultClassResolver;
	// }

	/**
	 * 
	 * @param absPath
	 * @param startIndex
	 * @param endIndex
	 * @param stepSize
	 * @return
	 */
	public static LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> deserialiseAndConsolidateMaps(
			String absPath, int startIndex, int endIndex, int stepSize)
	{
		LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataMergedContinuousWithDuration = new LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>();
		for (int i = startIndex; i <= endIndex; i += stepSize)
		{
			String pathToSerialisedData = absPath + i;
			mapForAllDataMergedContinuousWithDuration
					.putAll((LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>) Serializer
							.deSerializeThis(pathToSerialisedData));
		}

		System.out.println("Consolidated map is of size: " + mapForAllDataMergedContinuousWithDuration.size());
		return mapForAllDataMergedContinuousWithDuration;
	}

	public static void main(String args[])
	{
		Constant.setCommonPath("/run/media/gunjan/HOME/gunjan/Geolife Data Works/");
		String fileName = "flatFile";
		ArrayList<FlatActivityLogEntry> listOfActivityEntries = getAllLogEntries(fileName);

		WToFile.writeArrayListFlatActivityLogEntry(listOfActivityEntries, "FlatActivityLogEntries",
				"User_ID,Activity_ID,Date_ID,Time_ID,Location_ID,User_Name,Activity_Name,Start_Time,End_Time,Start_Date,End_Date,Duration,Start_Latitude,Start_Longitude,Start_Altitude,End_Latitude,End_Longitude,End_Altitude,Avg_Altitude");
		// int count=0;
		// for(FlatActivityLogEntry entry: listOfActivityEntries)
		// {
		// System.out.println(" "+entry.getUser_Name());
		// count++;
		// }
		System.out.println("count of activity entries = " + listOfActivityEntries.size());
	}

	/**
	 * Serialises the given ArrayList of FlatActivityLog entries.
	 * 
	 * @param listOfLogEntries
	 * @param fileName
	 */
	public static void serializeAllLogEntries(ArrayList<FlatActivityLogEntry> listOfLogEntries, String fileName)
	{
		// filename = "flatFile";
		// FlatActivityLogEntry e1 = new FlatActivityLogEntry();

		// e1.setUser_Name("gunjan");
		// save the object to file
		fileName = Constant.getCommonPath() + fileName;
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		long dt = System.currentTimeMillis();
		try
		{
			RandomAccessFile raf = new RandomAccessFile(fileName, "rw"); // for performance see
																			// http://java.dzone.com/articles/fast-java-file-serialization

			// FileOutputStream fos = new FileOutputStream(raf.getFD());
			fos = new FileOutputStream(raf.getFD());// fileName);
			fos = new FileOutputStream(fileName);
			out = new ObjectOutputStream(fos);
			out.writeObject(listOfLogEntries);
			out.close();
		}

		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		long lt = System.currentTimeMillis();
		System.out.println("Serialisation takes " + (lt - dt) / 1000 + " secs");

	}

	public static ArrayList<FlatActivityLogEntry> getAllLogEntries(String fileName)
	{
		fileName = Constant.getCommonPath() + fileName;

		ArrayList<FlatActivityLogEntry> allActivityLogEntries = new ArrayList<FlatActivityLogEntry>();
		FileInputStream fis = null;
		ObjectInputStream in = null;
		long dt = System.currentTimeMillis();
		try
		{
			RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
			fis = new FileInputStream(raf.getFD());// PfileName);
			// fis = new FileInputStream(fileName);
			in = new ObjectInputStream(fis);
			allActivityLogEntries = (ArrayList<FlatActivityLogEntry>) in.readObject();
			// System.out.println("FF:"+e2.User_Name);
			in.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		long lt = System.currentTimeMillis();
		System.out.println("Deserialisation takes " + (lt - dt) / 1000 + " secs");
		return allActivityLogEntries;
	}
}
