package org.activity.io;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.activity.objects.Pair;
import org.activity.util.RegexUtils;
import org.apache.commons.io.IOUtils;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;

import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;

public class ProcessUtils
{

	public static void main(String args[])
	{
		// String jarName = "EditDistanceStandAlone-capsule.jar";
		// // String[] commands = { "java", "-jar", jarName, "ape", "aces", "1", "1", "2", "true" };
		//
		// String word1 =
		// "gunjankumarskygreengrasskumarmanaligaurmanaligaurmanaligaurgunjankumarskygreengrasskumarmanaligaurmanaligaurmanaligaur";
		// String word2 =
		// "manaligaurkumarskyeenmanalikumarmanaligaurkumarmanaligaurkumarmanaligaumanaligaurkumarskyeenmanalikumarmanaligaurkumarmanaligaurkumarmanaligau";
		// String[] commands = { "java", "-jar", jarName, word1, word2, "1", "1", "2", "false" };
		//
		// long t1 = System.nanoTime();
		// for (int i = 0; i < 50; i++)
		// executeProcess(commands);
		// long t2 = System.nanoTime();
		// for (int i = 0; i < 50; i++)
		// executeNuProcess(commands);
		// long t3 = System.nanoTime();
		// for (int i = 0; i < 50; i++)
		// executeZTexec(commands);
		// long t4 = System.nanoTime();
		//
		// System.out.println((t2 - t1) + "- executeProcess= ");
		// System.out.println((t3 - t2) + "- executeNuProcess= ");
		// System.out.println((t4 - t3) + "- executeZTexec= ");

		Pair<String, Double> res = executeProcessEditDistance("ape", "aces", "1", "1", "2");
		System.out.print("trace=" + res.getFirst());
		System.out.print("dist=" + res.getSecond());
	}

	public static void executeZTexec(String[] commands)
	{
		System.out.println("executing executeZTexec");
		try
		{
			String output = new ProcessExecutor().command(commands).readOutput(true).execute().outputUTF8();
			System.out.println(output);
		}
		catch (InvalidExitValueException | IOException | InterruptedException | TimeoutException e)
		{
			e.printStackTrace();
		}

	}

	public static String executeZTexec2(String[] commands)
	{
		// System.out.println("executing executeZTexec");
		String output = "";
		try
		{
			output = new ProcessExecutor().command(commands).readOutput(true).execute().outputUTF8();

		}
		catch (InvalidExitValueException | IOException | InterruptedException | TimeoutException e)
		{
			e.printStackTrace();
		}
		return output;

	}

	public static void executeNuProcess(String[] commands)
	{
		System.out.println("executing executeNuProcess");
		try
		{
			NuProcessBuilder pb = new NuProcessBuilder(commands);// (Arrays.asList("java", "-version"));
			ProcessHandler handler = new ProcessHandler();
			pb.setProcessListener(handler);
			NuProcess process = pb.start();

			ByteBuffer buffer = ByteBuffer.wrap("Hello, World!".getBytes());
			process.writeStdin(buffer);

			process.waitFor(0, TimeUnit.SECONDS); // when 0 is used for waitFor() the wait is infinite
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static String executeNuProcess2(String[] commands)
	{
		String output = "";
		try
		{
			NuProcessBuilder pb = new NuProcessBuilder(commands);// (Arrays.asList("java", "-version"));
			ProcessHandler handler = new ProcessHandler();
			pb.setProcessListener(handler);
			NuProcess process = pb.start();

			ByteBuffer buffer = ByteBuffer.wrap("Hello, World!".getBytes());
			process.writeStdin(buffer);

			process.waitFor(0, TimeUnit.SECONDS); // when 0 is used for waitFor() the wait is infinite
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return output;
	}

	/**
	 * Fastest of the approaches here.
	 * 
	 * @param commandss
	 */
	public static void executeProcess(String[] commands)
	{
		System.out.println("executing executeProcess");

		try
		{
			ProcessBuilder pb = new ProcessBuilder(commands);
			Process p = pb.start();
			StringWriter writer = new StringWriter();
			IOUtils.copy(p.getInputStream(), writer, "UTF-8");// , Charset.forName("UTF-8"));
			String theString = writer.toString();
			System.out.println(theString);

			// String pathToLibs = System.getProperty("user.dir") + "/libs/";/// EditDistanceStandAlone-capsule.jar";
			// String jarName = "EditDistanceStandAlone-capsule.jar";
			// // String arguments = "\"ape\" \"aces\" \"1\" \"1\" \"2\" \"false\"";
			// Process p = Runtime.getRuntime().exec("java -version");// jarExecutionCommandPath + " " + arguments);
			// Process p = new ProcessBuilder(jarExecutionCommandPath, "ape", "aces", "1", "1", "2", "true").start();
			// ("java", "-jar", jarName, "ape", "aces", "1", "1", "2", "true"); // pb.command("ls");
			// org.apache.commons.io.IOUtils
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// System.out.println(System.getProperty("user.dir"));
		// stringSplitPerformance();

	}

	/**
	 * 
	 * @param commands
	 * @return
	 */
	public static String executeProcess2(String[] commands)
	{
		String output = "";
		try
		{
			ProcessBuilder pb = new ProcessBuilder(commands);
			Process p = pb.start();
			StringWriter writer = new StringWriter();
			IOUtils.copy(p.getInputStream(), writer);// , Charset.forName("UTF-8"));
			output = writer.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return output;
	}

	/**
	 * 
	 * @param commands
	 * @return {trace,edit dist}
	 */
	public static Pair<String, Double> executeProcessEditDistance(String word1, String word2, String inserWt,
			String deleteWt, String replaceWt)
	{
		String jarName = "EditDistanceStandAlone-capsule.jar";
		String[] commands = { "java", "-jar", jarName, word1, word2, inserWt, deleteWt, replaceWt, "false" };
		String trace = "";
		Double editDist = new Double(-99);

		try
		{
			// ProcessBuilder pb = new ProcessBuilder(commands);
			Process p = new ProcessBuilder(commands).start();
			StringWriter writer = new StringWriter();
			IOUtils.copy(p.getInputStream(), writer, "UTF-8");// StandardCharsets.UTF_8);// , Charset.forName("UTF-8"));
			String str = writer.toString();
			// System.out.println("str= " + str);
			// System.out.println("RegexUtils.patternCaret=" + RegexUtils.patternCaret);
			String[] splitted = RegexUtils.patternCaret.split(str, 2);// writer.toString(), 2); // split only on first
			// occurrence
			// String[] splitted = str.split("^", 2); // split only on first occurrence
			// System.out.println("splitted[0]=" + splitted[0]);
			// System.out.println("splitted[1]=" + splitted[1]);
			// System.out.println(Arrays.asList(splitted).toString()); // of ^
			// System.out.println(" for word1=" + word1 + "word2=" + word2 + "splitted[0]" + splitted[0] + "splitted[1]"
			// + splitted[1]);

			editDist = Double.valueOf(splitted[0]);
			trace = splitted[1].trim();// to remove trailing newline
		}
		catch (Exception e)
		{
			System.out.println("Exception for word1=" + word1 + "word2=" + word2);
			e.printStackTrace();
		}

		return new Pair<String, Double>(trace, editDist);
	}

}
