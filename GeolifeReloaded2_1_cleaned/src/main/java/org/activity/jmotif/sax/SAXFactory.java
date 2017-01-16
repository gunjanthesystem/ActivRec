package org.activity.jmotif.sax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.activity.jmotif.distance.EuclideanDistance;
import org.activity.jmotif.sax.alphabet.Alphabet;
import org.activity.jmotif.sax.alphabet.NormalAlphabet;
import org.activity.jmotif.sax.datastructures.DiscordRecord;
import org.activity.jmotif.sax.datastructures.DiscordRecords;
import org.activity.jmotif.sax.datastructures.DiscordsAndMotifs;
import org.activity.jmotif.sax.datastructures.MotifRecord;
import org.activity.jmotif.sax.datastructures.MotifRecords;
import org.activity.jmotif.sax.datastructures.SAXFrequencyData;
import org.activity.jmotif.sax.trie.SAXTrie;
import org.activity.jmotif.sax.trie.SAXTrieHitEntry;
import org.activity.jmotif.sax.trie.TrieException;
import org.activity.jmotif.sax.trie.VisitRegistry;
import org.activity.jmotif.timeseries.TSException;
import org.activity.jmotif.timeseries.TSUtils;
import org.activity.jmotif.timeseries.Timeseries;
//import org.activity.jmotif.util.BriefFormatter;

import net.seninp.util.StackTrace;
//import net.seninp.util.StackTrace;
//import net.seninp.util.StackTrace;
//import org.hackystat.utilities.logger.HackystatLogger;disabled by Gunjan
//import org.hackystat.utilities.stacktrace.StackTrace; disabled by Gunjan
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
//import edu.hawaii.jmotif.util.BriefFormatter;

/**
 * Implements SAX algorithms.
 * 
 * @author Pavel Senin
 * 
 */
public final class SAXFactory
{

	public static final int DEFAULT_COLLECTION_SIZE = 50;

	private static Logger consoleLogger;
	// disabled by gunjan private static final String LOGGING_LEVEL = "SEVERE";

	static
	{
		// disabled by Gunjan consoleLogger = HackystatLogger.getLogger("jmotif.debug.console", "jmotif");
		// disabled by Gunjan consoleLogger.setUseParentHandlers(false);
		// disabled by Gunjanfor (Handler handler : consoleLogger.getHandlers())
		// disabled by Gunjan {
		// disabled by Gunjan consoleLogger.removeHandler(handler);
		// disabled by Gunjan }
		// disabled by GunjanConsoleHandler handler = new ConsoleHandler();
		// disabled by Gunjan Formatter formatter = new BriefFormatter();
		// disabled by Gunjan handler.setFormatter(formatter);
		// disabled by GunjanconsoleLogger.addHandler(handler);
		// disabled by Gunjan HackystatLogger.setLoggingLevel(consoleLogger, LOGGING_LEVEL);
	}

	/**
	 * Constructor.
	 */
	private SAXFactory()
	{
		super();
	}

	/**
	 * Convert the timeseries into SAX string representation, normalizes each of the pieces before SAX conversion.
	 * NOSKIP means that ALL SAX words reported.
	 * 
	 * @param ts
	 *            The timeseries given.
	 * @param windowSize
	 *            The sliding window size used.
	 * @param paaSize
	 *            The number of the points used in the PAA reduction of the time series.
	 * @param cuts
	 *            The alphabet cuts to use.
	 * @return The SAX representation of the timeseries.
	 * @throws TSException
	 *             If error occurs.
	 * @throws CloneNotSupportedException
	 */

	public static SAXFrequencyData ts2saxZnormByCutsNoSkip(Timeseries ts, int windowSize, int paaSize, double[] cuts)
			throws TSException, CloneNotSupportedException
	{

		// Initialize symbolic result data
		SAXFrequencyData res = new SAXFrequencyData();

		// scan across the time series extract sub sequences, and converting
		// them to strings
		for (int i = 0; i < ts.size() - (windowSize - 1); i++)
		{

			// fix the current subsection
			Timeseries subSection = ts.subsection(i, i + windowSize - 1);

			// Z normalize it
			subSection = TSUtils.zNormalize(subSection);

			// perform PAA conversion if needed
			Timeseries paa;
			try
			{
				paa = TSUtils.paa(subSection, paaSize);
			}
			catch (CloneNotSupportedException e)
			{
				throw new TSException("Unable to clone: " + StackTrace.toString(e));
			}

			// Convert the PAA to a string.
			char[] currentString = TSUtils.ts2StringWithNaNByCuts(paa, cuts);

			res.put(new String(currentString), i);
		}
		return res;
	}

	/**
	 * Convert the timeseries into SAX string representation, normalizes each of the pieces before SAX conversion. Not
	 * all SAX words reported, if the new SAX word is the same as current it will not be reported.
	 * 
	 * @param ts
	 *            The timeseries given.
	 * @param windowSize
	 *            The sliding window size used.
	 * @param paaSize
	 *            The number of the points used in the PAA reduction of the time series.
	 * @param cuts
	 *            The alphabet cuts to use.
	 * @return The SAX representation of the timeseries.
	 * @throws TSException
	 *             If error occurs.
	 * @throws CloneNotSupportedException
	 */

	public static SAXFrequencyData ts2saxZnormByCuts(Timeseries ts, int windowSize, int paaSize, double[] cuts)
			throws TSException, CloneNotSupportedException
	{

		// Initialize symbolic result data
		SAXFrequencyData res = new SAXFrequencyData();
		String previousString = "";

		// scan across the time series extract sub sequences, and converting
		// them to strings
		for (int i = 0; i < ts.size() - (windowSize - 1); i++)
		{

			// fix the current subsection
			Timeseries subSection = ts.subsection(i, i + windowSize - 1);

			// Z normalize it
			subSection = TSUtils.zNormalize(subSection);

			// perform PAA conversion if needed
			Timeseries paa;
			try
			{
				paa = TSUtils.paa(subSection, paaSize);
			}
			catch (CloneNotSupportedException e)
			{
				throw new TSException("Unable to clone: " + StackTrace.toString(e));
			}

			// Convert the PAA to a string.
			char[] currentString = TSUtils.ts2StringWithNaNByCuts(paa, cuts);

			// check if previous one was the same, if so, ignore that (don't
			// know why though, but guess
			// cause we didn't advance much on the timeseries itself)
			if (!previousString.isEmpty() && previousString.equalsIgnoreCase(new String(currentString)))
			{
				continue;
			}
			previousString = new String(currentString);
			res.put(new String(currentString), i);
		}
		return res;
	}

	/**
	 * Convert the timeseries into SAX string representation, normalizes each of the pieces before SAX conversion.
	 * NOSKIP means that ALL SAX words reported.
	 * 
	 * @param s
	 *            The timeseries given.
	 * @param windowSize
	 *            The sliding window size used.
	 * @param paaSize
	 *            The number of the points used in the PAA reduction of the time series.
	 * @param cuts
	 *            The alphabet cuts to use.
	 * @return The SAX representation of the timeseries.
	 * @throws TSException
	 *             If error occurs.
	 * @throws CloneNotSupportedException
	 */
	public static SAXFrequencyData ts2saxZnormByCutsNoSkip(double[] s, int windowSize, int paaSize, double[] cuts)
			throws TSException, CloneNotSupportedException
	{
		long[] ticks = new long[s.length];
		for (int i = 0; i < s.length; i++)
		{
			ticks[i] = i;
		}
		Timeseries ts = new Timeseries(s, ticks);
		return ts2saxZnormByCutsNoSkip(ts, windowSize, paaSize, cuts);
	}

	/**
	 * Convert the timeseries into SAX string representation, normalizes each of the pieces before SAX conversion. Not
	 * all SAX words reported, if the new SAX word is the same as current it will not be reported.
	 * 
	 * @param s
	 *            The timeseries given.
	 * @param windowSize
	 *            The sliding window size used.
	 * @param paaSize
	 *            The number of the points used in the PAA reduction of the time series.
	 * @param cuts
	 *            The alphabet cuts to use.
	 * @return The SAX representation of the timeseries.
	 * @throws TSException
	 *             If error occurs.
	 * @throws CloneNotSupportedException
	 */
	public static SAXFrequencyData ts2saxZnormByCuts(double[] s, int windowSize, int paaSize, double[] cuts)
			throws TSException, CloneNotSupportedException
	{
		long[] ticks = new long[s.length];
		for (int i = 0; i < s.length; i++)
		{
			ticks[i] = i;
		}
		Timeseries ts = new Timeseries(s, ticks);
		return ts2saxZnormByCuts(ts, windowSize, paaSize, cuts);
	}

	/**
	 * Convert the timeseries into SAX string representation. It doesn't normalize anything.
	 * 
	 * @param ts
	 *            The timeseries given.
	 * @param windowSize
	 *            The sliding window size used.
	 * @param paaSize
	 *            The number of the points used in the PAA reduction of the time series.
	 * @param cuts
	 *            The alphabet cuts to use.
	 * @return The SAX representation of the timeseries.
	 * @throws TSException
	 *             If error occurs.
	 */

	public static SAXFrequencyData ts2saxNoZnormByCuts(Timeseries ts, int windowSize, int paaSize, double[] cuts)
			throws TSException
	{

		// Initialize symbolic result data
		SAXFrequencyData res = new SAXFrequencyData();
		String previousString = "";

		// scan across the time series extract sub sequences, and converting
		// them to strings
		for (int i = 0; i < ts.size() - (windowSize - 1); i++)
		{

			// fix the current subsection
			Timeseries subSection = ts.subsection(i, i + windowSize - 1);

			// Z normalize it
			// subSection = TSUtils.normalize(subSection);

			// perform PAA conversion if needed
			Timeseries paa;
			try
			{
				paa = TSUtils.paa(subSection, paaSize);
			}
			catch (CloneNotSupportedException e)
			{
				throw new TSException("Unable to clone: " + StackTrace.toString(e));
			}

			// Convert the PAA to a string.
			char[] currentString = TSUtils.ts2StringWithNaNByCuts(paa, cuts);

			// check if previous one was the same, if so, ignore that (don't
			// know why though, but guess
			// cause we didn't advance much on the timeseries itself)
			if (!previousString.isEmpty() && previousString.equalsIgnoreCase(new String(currentString)))
			{
				previousString = new String(currentString);
				continue;
			}
			previousString = new String(currentString);
			res.put(new String(currentString), i);
		}
		return res;
	}

	/**
	 * Convert the timeseries into SAX string representation.
	 * 
	 * @param ts
	 *            The timeseries given.
	 * @param windowSize
	 *            The sliding window size used.
	 * @param paaSize
	 *            The number of the points used in the PAA reduction of the time series.
	 * @param alphabet
	 *            The alphabet to use.
	 * @param alphabetSize
	 *            The alphabet size used.
	 * @return The SAX representation of the timeseries.
	 * @throws TSException
	 *             If error occurs.
	 * @throws CloneNotSupportedException
	 */

	public static SAXFrequencyData ts2saxZNorm(Timeseries ts, int windowSize, int paaSize, Alphabet alphabet,
			int alphabetSize) throws TSException, CloneNotSupportedException
	{

		if (alphabetSize > alphabet.getMaxSize())
		{
			throw new TSException("Unable to set the alphabet size greater than " + alphabet.getMaxSize());
		}

		return ts2saxZnormByCuts(ts, windowSize, paaSize, alphabet.getCuts(alphabetSize));

	}

	/**
	 * Convert the timeseries into SAX string representation.
	 * 
	 * @param ts
	 *            The timeseries given.
	 * @param windowSize
	 *            The sliding window size used.
	 * @param paaSize
	 *            The number of the points used in the PAA reduction of the time series.
	 * @param alphabet
	 *            The alphabet to use.
	 * @param alphabetSize
	 *            The alphabet size used.
	 * @return The SAX representation of the timeseries.
	 * @throws TSException
	 *             If error occurs.
	 */

	public static SAXFrequencyData ts2saxNoZnorm(Timeseries ts, int windowSize, int paaSize, Alphabet alphabet,
			int alphabetSize) throws TSException
	{

		if (alphabetSize > alphabet.getMaxSize())
		{
			throw new TSException("Unable to set the alphabet size greater than " + alphabet.getMaxSize());
		}

		return ts2saxNoZnormByCuts(ts, windowSize, paaSize, alphabet.getCuts(alphabetSize));

	}

	/**
	 * Convert the timeseries into SAX string representation.
	 * 
	 * @param ts
	 *            The timeseries given.
	 * @param paaSize
	 *            The number of the points used in the PAA reduction of the time series.
	 * @param alphabet
	 *            The alphabet to use.
	 * @param alphabetSize
	 *            The alphabet size used.
	 * @return The SAX representation of the timeseries.
	 * @throws TSException
	 *             If error occurs.
	 * @throws CloneNotSupportedException
	 */
	public static String ts2string(Timeseries ts, int paaSize, Alphabet alphabet, int alphabetSize)
			throws TSException, CloneNotSupportedException
	{

		if (alphabetSize > alphabet.getMaxSize())
		{
			throw new TSException("Unable to set the alphabet size greater than " + alphabet.getMaxSize());
		}

		int tsLength = ts.size();
		if (tsLength == paaSize)
		{
			return new String(TSUtils.ts2String(TSUtils.zNormalize(ts), alphabet, alphabetSize));
		}
		else
		{
			// perform PAA conversion
			Timeseries PAA;
			try
			{
				PAA = TSUtils.paa(TSUtils.zNormalize(ts), paaSize);
			}
			catch (CloneNotSupportedException e)
			{
				throw new TSException("Unable to clone: " + StackTrace.toString(e));
			}
			return new String(TSUtils.ts2String(PAA, alphabet, alphabetSize));
		}
	}

	/**
	 * Build the SAX trie out of the series.
	 * 
	 * @param tsData
	 *            The timeseries.
	 * @param windowSize
	 *            PAA window size to use.
	 * @param alphabetSize
	 *            The SAX alphabet size.
	 * @return Discords found within the series.
	 * @throws TrieException
	 *             if error occurs.
	 * @throws TSException
	 *             if error occurs.
	 */
	public static DiscordRecords ts2Discords(double[] tsData, int windowSize, int alphabetSize)
			throws TrieException, TSException
	{

		// make alphabet available
		NormalAlphabet normalA = new NormalAlphabet();

		// get a trie instance
		SAXTrie trie = new SAXTrie(tsData.length - windowSize, alphabetSize);

		// build the trie sliding over the series
		//
		int currPosition = 0;
		while ((currPosition + windowSize) < tsData.length)
		{
			// get the window SAX representation
			double[] subSeries = getSubSeries(tsData, currPosition, currPosition + windowSize);
			char[] saxVals = getSaxVals(subSeries, windowSize, normalA.getCuts(alphabetSize));
			// add result to the structure
			trie.put(String.valueOf(saxVals), currPosition);
			// increment the position
			currPosition++;
		}

		// delegate the job to discords extraction engine
		DiscordRecords discords = getDiscordsAlgorithm(tsData, windowSize, trie, DEFAULT_COLLECTION_SIZE,
				new LargeWindowAlgorithm());

		return discords;
	}

	/**
	 * Compute the distance between the two strings, this function use the numbers associated with ASCII codes, i.e.
	 * distance between a and b would be 1.
	 * 
	 * @param a
	 *            The first string.
	 * @param b
	 *            The second string.
	 * @return The pairwise distance.
	 * @throws TSException
	 *             if length are differ.
	 */
	public static int strDistance(char[] a, char[] b) throws TSException
	{
		if (a.length == b.length)
		{
			int distance = 0;
			for (int i = 0; i < a.length; i++)
			{
				int tDist = Math.abs(Character.getNumericValue(a[i]) - Character.getNumericValue(b[i]));
				if (tDist > 1)
				{
					distance += tDist;
				}
			}
			return distance;
		}
		else
		{
			throw new TSException("Unable to compute SAX distance, string lengths are not equal");
		}
	}

	/**
	 * Compute the distance between the two chars based on the ASCII symbol codes.
	 * 
	 * @param a
	 *            The first char.
	 * @param b
	 *            The second char.
	 * @return The distance.
	 */
	public static int strDistance(char a, char b)
	{
		return Math.abs(Character.getNumericValue(a) - Character.getNumericValue(b));
	}

	/**
	 * This function implements SAX MINDIST function which uses alphabet based distance matrix.
	 * 
	 * @param a
	 *            The SAX string.
	 * @param b
	 *            The SAX string.
	 * @param distanceMatrix
	 *            The distance matrix to use.
	 * @return distance between strings.
	 * @throws TSException
	 *             If error occurs.
	 */
	public static double saxMinDist(char[] a, char[] b, double[][] distanceMatrix) throws TSException
	{
		if (a.length == b.length)
		{
			double dist = 0.0D;
			for (int i = 0; i < a.length; i++)
			{
				if (Character.isLetter(a[i]) && Character.isLetter(b[i]))
				{
					int numA = Character.getNumericValue(a[i]) - 10;
					int numB = Character.getNumericValue(b[i]) - 10;
					if (numA > 19 || numA < 0 || numB > 19 || numB < 0)
					{
						throw new TSException("The character index greater than 19 or less than 0!");
					}
					double localDist = distanceMatrix[numA][numB];
					dist += localDist;
				}
				else
				{
					throw new TSException("Non-literal character found!");
				}
			}
			return dist;
		}
		else
		{
			throw new TSException("Data arrays lengths are not equal!");
		}
	}

	public MotifRecords series2Motifs(double[] series, int windowSize, int alphabetSize, int motifsNumToReport,
			SlidingWindowMarkerAlgorithm markerAlgorithm) throws TrieException, TSException
	{
		// init the SAX structures
		//
		SAXTrie trie = new SAXTrie(series.length - windowSize, alphabetSize);

		StringBuilder sb = new StringBuilder();
		sb.append("data size: ").append(series.length);

		double max = TSUtils.max(series);
		sb.append("; max: ").append(max);

		double min = TSUtils.min(series);
		sb.append("; min: ").append(min);

		double mean = TSUtils.mean(series);
		sb.append("; mean: ").append(mean);

		int nans = TSUtils.countNaN(series);
		sb.append("; NaNs: ").append(nans);

		consoleLogger.fine(sb.toString());
		consoleLogger.fine("window size: " + windowSize + ", alphabet size: " + alphabetSize + ", SAX Trie size: "
				+ (series.length - windowSize));

		Alphabet normalA = new NormalAlphabet();

		Date start = new Date();
		// build the trie
		//
		int currPosition = 0;
		while ((currPosition + windowSize) < series.length)
		{
			// get the window SAX representation
			double[] subSeries = getSubSeries(series, currPosition, currPosition + windowSize);
			char[] saxVals = getSaxVals(subSeries, windowSize, normalA.getCuts(alphabetSize));
			// add result to the structure
			trie.put(String.valueOf(saxVals), currPosition);
			// increment the position
			currPosition++;
		}
		Date end = new Date();
		consoleLogger.fine("trie built in: " + timeToString(start.getTime(), end.getTime()));

		start = new Date();
		MotifRecords motifs = getMotifs(trie, motifsNumToReport);
		end = new Date();

		consoleLogger.fine("motifs retrieved in: " + timeToString(start.getTime(), end.getTime()));

		return motifs;
	}

	/**
	 * Build the SAX trie out of Instances reporting discords.
	 * 
	 * @param tsData
	 *            The timeseries.
	 * @param windowSize
	 *            PAA window size to use.
	 * @param alphabetSize
	 *            The SAX alphabet size.
	 * @param dataAttributeName
	 *            The WEKA attribute - essentially points on the instance attribute which bears the data value in this
	 *            case.
	 * @param discordsNumToReport
	 *            how many discords to report.
	 * @return Discords found within the series.
	 * @throws TrieException
	 *             if error occurs.
	 * @throws TSException
	 *             if error occurs.
	 */
	public static DiscordRecords instances2Discords(Instances tsData, String dataAttributeName, int windowSize,
			int alphabetSize, int discordsNumToReport) throws TrieException, TSException
	{

		// get the timestamps and data attributes
		//
		Attribute dataAttribute = tsData.attribute(dataAttributeName);
		double[] series = toRealSeries(tsData, dataAttribute);
		NormalAlphabet normalA = new NormalAlphabet();
		SAXTrie trie = new SAXTrie(series.length - windowSize, alphabetSize);

		StringBuilder sb = new StringBuilder();
		sb.append("data size: ").append(series.length);

		double max = TSUtils.max(series);
		sb.append("; max: ").append(max);

		double min = TSUtils.min(series);
		sb.append("; min: ").append(min);

		double mean = TSUtils.mean(series);
		sb.append("; mean: ").append(mean);

		int nans = TSUtils.countNaN(series);
		sb.append("; NaNs: ").append(nans);

		consoleLogger.fine(sb.toString());
		consoleLogger.fine("window size: " + windowSize + ", alphabet size: " + alphabetSize + ", SAX Trie size: "
				+ (series.length - windowSize));

		// build the trie
		//
		int currPosition = 0;
		while ((currPosition + windowSize) < series.length)
		{
			// get the window SAX representation
			double[] subSeries = getSubSeries(series, currPosition, currPosition + windowSize);
			char[] saxVals = getSaxVals(subSeries, windowSize, normalA.getCuts(alphabetSize));
			// add result to the structure
			trie.put(String.valueOf(saxVals), currPosition);
			// increment the position
			currPosition++;
		}

		Date start = new Date();
		int reportNum = DEFAULT_COLLECTION_SIZE;
		if (discordsNumToReport > 0 && discordsNumToReport < 50)
		{
			reportNum = discordsNumToReport;
		}
		DiscordRecords discords = getDiscordsAlgorithm(toRealSeries(tsData, dataAttribute), windowSize, trie, reportNum,
				new LargeWindowAlgorithm());
		Date end = new Date();
		consoleLogger.fine("discords search finished in : " + timeToString(start.getTime(), end.getTime()));

		return discords;
	}

	/**
	 * Build the SAX trie out of the series and reports discords.
	 * 
	 * @param series
	 *            The timeseries.
	 * @param windowSize
	 *            PAA window size to use.
	 * @param alphabetSize
	 *            The SAX alphabet size.
	 * @param discordsNumToReport
	 *            how many discords to report.
	 * @return Discords found within the series.
	 * @throws TrieException
	 *             if error occurs.
	 * @throws TSException
	 *             if error occurs.
	 */
	public static DiscordRecords series2Discords(double[] series, int windowSize, int alphabetSize,
			int discordsNumToReport, SlidingWindowMarkerAlgorithm markerAlgorithm) throws TrieException, TSException
	{

		// get the timestamps and data attributes
		//
		NormalAlphabet normalA = new NormalAlphabet();
		SAXTrie trie = new SAXTrie(series.length - windowSize, alphabetSize);

		StringBuilder sb = new StringBuilder();
		sb.append("data size: ").append(series.length);

		double max = TSUtils.max(series);
		sb.append("; max: ").append(max);

		double min = TSUtils.min(series);
		sb.append("; min: ").append(min);

		double mean = TSUtils.mean(series);
		sb.append("; mean: ").append(mean);

		int nans = TSUtils.countNaN(series);
		sb.append("; NaNs: ").append(nans);

		consoleLogger.fine(sb.toString());
		consoleLogger.fine("window size: " + windowSize + ", alphabet size: " + alphabetSize + ", SAX Trie size: "
				+ (series.length - windowSize));

		// build the trie
		//
		int currPosition = 0;
		while ((currPosition + windowSize) < series.length)
		{
			// get the window SAX representation
			double[] subSeries = getSubSeries(series, currPosition, currPosition + windowSize);
			char[] saxVals = getSaxVals(subSeries, windowSize, normalA.getCuts(alphabetSize));
			// add result to the structure
			trie.put(String.valueOf(saxVals), currPosition);
			// increment the position
			currPosition++;
		}

		Date start = new Date();
		int reportNum = DEFAULT_COLLECTION_SIZE;
		if (discordsNumToReport > 0 && discordsNumToReport < 50)
		{
			reportNum = discordsNumToReport;
		}
		DiscordRecords discords = getDiscordsAlgorithm(series, windowSize, trie, reportNum, markerAlgorithm);
		Date end = new Date();
		consoleLogger.fine("discords search finished in : " + timeToString(start.getTime(), end.getTime()));

		return discords;
	}

	/**
	 * Builds two collections - collection of "discords" - the surprise or unique patterns and the collection of the
	 * motifs - most frequent patterns. This method leveraging the Trie structure - so the sliding window size will be
	 * translated into the alphabet size by using PAA.
	 * 
	 * @param series
	 *            The data series.
	 * @param windowSize
	 *            The sliding window size.
	 * @param alphabetSize
	 *            The alphabet size.
	 * @param discordCollectionSize
	 *            The size of the discord collection - how many top discords we want to keep.
	 * @param motifsCollectionSize
	 *            The size of the motif collection - how many top motifs we want to keep.
	 * @return All what was promised if finishes.
	 * 
	 * @throws TrieException
	 *             if error occurs.
	 * @throws TSException
	 *             if error occurs.
	 */
	public static DiscordsAndMotifs series2DiscordsAndMotifs(double[] series, int windowSize, int alphabetSize,
			int discordCollectionSize, int motifsCollectionSize, SlidingWindowMarkerAlgorithm markerAlgorithm)
			throws TrieException, TSException
	{

		// init the SAX structures
		//
		DiscordsAndMotifs res = new DiscordsAndMotifs(discordCollectionSize, motifsCollectionSize);
		SAXTrie trie = new SAXTrie(series.length - windowSize, alphabetSize);

		StringBuilder sb = new StringBuilder();
		sb.append("data size: ").append(series.length);

		double max = TSUtils.max(series);
		sb.append("; max: ").append(max);

		double min = TSUtils.min(series);
		sb.append("; min: ").append(min);

		double mean = TSUtils.mean(series);
		sb.append("; mean: ").append(mean);

		int nans = TSUtils.countNaN(series);
		sb.append("; NaNs: ").append(nans);

		consoleLogger.fine(sb.toString());
		consoleLogger.fine("window size: " + windowSize + ", alphabet size: " + alphabetSize + ", SAX Trie size: "
				+ (series.length - windowSize));

		Alphabet normalA = new NormalAlphabet();

		Date start = new Date();
		// build the trie
		//
		int currPosition = 0;
		while ((currPosition + windowSize) < series.length)
		{
			// get the window SAX representation
			double[] subSeries = getSubSeries(series, currPosition, currPosition + windowSize);
			char[] saxVals = getSaxVals(subSeries, windowSize, normalA.getCuts(alphabetSize));
			// add result to the structure
			trie.put(String.valueOf(saxVals), currPosition);
			// increment the position
			currPosition++;
		}
		Date end = new Date();
		consoleLogger.fine("trie built in: " + timeToString(start.getTime(), end.getTime()));

		start = new Date();
		MotifRecords motifs = getMotifs(trie, motifsCollectionSize);
		end = new Date();

		consoleLogger.fine("motifs retrieved in: " + timeToString(start.getTime(), end.getTime()));

		start = new Date();
		DiscordRecords discords = getDiscordsAlgorithm(series, windowSize, trie, discordCollectionSize,
				markerAlgorithm);
		end = new Date();
		consoleLogger.fine("discords collected in: " + timeToString(start.getTime(), end.getTime()));

		res.addDiscords(discords);
		res.addMotifs(motifs);

		return res;
	}

	/**
	 * The discords extraction method.
	 * 
	 * Here I need to keep a continuous stack of knowledge with information not only about distance, but about
	 * abandoning or conducting a full search for words. Thus, I will not be doing the same expensive search on the
	 * rarest word all over again.
	 * 
	 * @param series
	 *            The series we work with.
	 * @param windowSize
	 *            The series window size.
	 * @param marker
	 *            The algorithm for marking visited locations.
	 * @param trie
	 * @param discordCollectionSize
	 * @return
	 * @throws TSException
	 * @throws TrieException
	 */
	private static DiscordRecords getDiscordsAlgorithm(double[] series, int windowSize, SAXTrie trie,
			int discordCollectionSize, SlidingWindowMarkerAlgorithm marker) throws TSException, TrieException
	{

		consoleLogger.fine("starting discords finding routines");

		// resulting discords collection
		DiscordRecords discords = new DiscordRecords(discordCollectionSize);

		// visit registry. the idea is to mark as visited all the discord
		// locations for all searches
		VisitRegistry discordsVisitRegistry = new VisitRegistry(series.length - windowSize);

		// the collection of seen words and their best so far distances
		// in the collection, in addition to pairs <word, distance> I store a
		// semaphore
		// which indicates whether the full search was conducted with this word,
		// or it was
		// abandoned at some point, so we do not know the final distance
		//
		TreeMap<String, DistanceEntry> knownWordsAndTheirCurrentDistances = new TreeMap<String, DistanceEntry>();

		// the words already in the discords collection, so we do not have to
		// re-consider them
		//
		TreeSet<String> completeWords = new TreeSet<String>();

		// we conduct the search until the number of discords is less than
		// desired
		//
		while (discords.getSize() < discordCollectionSize)
		{

			consoleLogger.fine("currently known discords: " + discords.getSize() + " out of " + discordCollectionSize);

			Date start = new Date();
			DiscordRecord bestDiscord = findBestDiscord(series, windowSize, trie, completeWords,
					knownWordsAndTheirCurrentDistances, discordsVisitRegistry, marker);
			Date end = new Date();

			// if the discord is null we getting out of the search
			if (bestDiscord.getDistance() == 0.0D || bestDiscord.getPosition() == -1)
			{
				consoleLogger.fine("breaking the outer search loop, discords found: " + discords.getSize()
						+ " last seen discord: " + bestDiscord.toString());
				break;
			}

			consoleLogger.fine("new discord: " + bestDiscord.getPayload() + ", position " + bestDiscord.getPosition()
					+ ", distance " + bestDiscord.getDistance() + ", elapsed time: "
					+ timeToString(start.getTime(), end.getTime()));

			// collect the result
			//
			discords.add(bestDiscord);

			// and maintain data structures
			//
			marker.markVisited(discordsVisitRegistry, bestDiscord.getPosition(), windowSize);
			completeWords.add(String.valueOf(bestDiscord.getPayload()));
		}

		// done deal
		//
		return discords;
	}

	/**
	 * This method reports the best found discord. Note, that this discord is approximately the best. Due to the
	 * fuzzy-logic search with randomization and aggressive labeling of the magic array locations.
	 * 
	 * @param series
	 *            The series we are looking for discord in.
	 * @param windowSize
	 *            The sliding window size.
	 * @param trie
	 *            The trie (index of the series).
	 * @param foundDiscordsWords
	 *            Already found discords.
	 * @param knownWordsAndTheirDistances
	 *            The best known distances for certain word. I use the early search abandoning optimization in oder to
	 *            reduce complexity.
	 * @param visitedLocations
	 *            The magic array.
	 * @return The best discord instance.
	 * @throws TSException
	 *             If error occurs.
	 * @throws TrieException
	 *             If error occurs.
	 */
	private static DiscordRecord findBestDiscord(double[] series, int windowSize, SAXTrie trie,
			TreeSet<String> foundDiscordsWords, TreeMap<String, DistanceEntry> knownWordsAndTheirDistances,
			VisitRegistry visitedLocations, SlidingWindowMarkerAlgorithm marker) throws TSException, TrieException
	{

		// we extract all seen words from the trie
		// and sort them by the frequency decrease
		//
		ArrayList<SAXTrieHitEntry> frequencies = trie.getFrequencies();
		Collections.sort(frequencies);
		// StringBuilder sb = new StringBuilder();
		// for (int i = 0; i < 10; i++) {
		// sb.append("top frequencies: ").append(frequencies.get(i).getStr()).append(",")
		// .append(frequencies.get(i).getPosition()).append(" ; ");
		// }
		// consoleLogger.finer(sb.toString());

		// init variables
		int bestSoFarPosition = -1;
		double bestSoFarDistance = 0.0D;
		String bestSoFarString = "";

		// we will iterate over words from rarest to frequent ones
		//
		int idx = 0;
		int limit = frequencies.size();
		while (idx < limit)
		{

			SAXTrieHitEntry currentEntry = frequencies.get(idx);
			String currentWord = String.valueOf(currentEntry.getStr());
			int currentPosition = currentEntry.getPosition();

			// take care about this entry
			cleanUpFrequencies(frequencies, currentWord, idx);
			// and update the length
			limit = frequencies.size();

			if (foundDiscordsWords.contains(currentWord) || visitedLocations.isVisited(currentPosition))
			{
				consoleLogger.finer("skipping the search for " + currentWord);
				idx++;
				continue;
			}
			else
			{
				consoleLogger.finer(
						"conducting search for " + currentWord + " iteration " + idx + " from " + frequencies.size());
			}

			// so, lets search begin
			//
			double nearestNeighborDist = Double.MAX_VALUE;
			boolean doRandomSearch = true;

			// get a copy of visited locations
			VisitRegistry registry = new VisitRegistry(series.length - windowSize);
			registry.transferVisited(visitedLocations);
			// & mark visited current substring
			double[] currentLocations = getSubSeries(series, currentPosition, currentPosition + windowSize);
			marker.markVisited(registry, currentPosition, windowSize);

			// WE QRE GOING TO ITERATE OVER THE CURRENT WORD OCCURENCES HERE
			//
			DistanceEntry bestKnownDistance = knownWordsAndTheirDistances.get(String.valueOf(currentEntry.getStr()));

			if (null != bestKnownDistance && !(bestKnownDistance.isAbandoned()))
			{
				consoleLogger
						.finer("skipping iterations over " + currentWord + " retrieved result from known distances ");
				nearestNeighborDist = bestKnownDistance.getDistance();
				if (bestKnownDistance.getDistance() < bestSoFarDistance)
				{
					consoleLogger
							.finer("breaking the inner loop flow, bestKnownDistance is less than bestSoFarDistance ");
					doRandomSearch = false;
					idx++;
					continue;
				}
			}
			else
			{

				List<Integer> currentOccurences = trie.getOccurences(currentEntry.getStr());
				consoleLogger.finer(currentWord + " has " + currentOccurences.size() + " occurrences, iterating...");

				for (Integer nextOccurrence : currentOccurences)
				{

					// skip the location we standing at
					if (Math.abs(nextOccurrence.intValue() - currentPosition) < windowSize)
					{
						continue;
					}

					// mark current next visited
					marker.markVisited(registry, nextOccurrence, windowSize);

					// get the piece of the timeseries
					double[] occurrenceValues = getSubSeries(series, nextOccurrence, nextOccurrence + windowSize);
					double dist = EuclideanDistance.distance(currentLocations, occurrenceValues);

					// keep track of best so far distance
					if (dist < nearestNeighborDist)
					{
						nearestNeighborDist = dist;
						if (dist < bestSoFarDistance)
						{
							consoleLogger.finer(" ** abandoning the occurrences iterations");
							doRandomSearch = false;
							break;
						}
					}
				}

			}

			if (!(Double.MAX_VALUE == nearestNeighborDist))
			{
				consoleLogger.finer("for " + currentWord + " occurrences, smallest nearest neighbor distance: "
						+ nearestNeighborDist);
			}
			else
			{
				consoleLogger.finer("nothing changed after iterations...");
			}

			boolean completeSearch = true;
			// check if we must continue with random neighbors
			if (doRandomSearch)
			{
				// it is heuristics here
				//
				int nextRandomVisitTarget = -1;

				int visitCounter = 0;
				while ((nextRandomVisitTarget = registry.getNextRandomUnvisitedPosition()) != -1)
				{
					consoleLogger.finer(" random position pick step " + visitCounter + " visited: "
							+ registry.getVisited().size() + ", unvisited: " + registry.getUnvisited().size()
							+ "; nearest neighbor: " + nearestNeighborDist);

					// registry.markVisited(nextRandomVisitTarget);
					marker.markVisited(registry, nextRandomVisitTarget, windowSize);

					double[] randomTargetValues = getSubSeries(series, nextRandomVisitTarget,
							nextRandomVisitTarget + windowSize);
					double randomTargetDistance = EuclideanDistance.distance(currentLocations, randomTargetValues);

					// early abandoning of the search, the current word is not
					// discord, we seen better
					if (randomTargetDistance < bestSoFarDistance)
					{
						nearestNeighborDist = randomTargetDistance;
						consoleLogger.finer(" ** abandoning random visits loop, seen distance " + nearestNeighborDist
								+ " at iteration " + visitCounter);
						completeSearch = false;
						break;
					}

					// keep track
					if (randomTargetDistance < nearestNeighborDist)
					{
						nearestNeighborDist = randomTargetDistance;
					}

					visitCounter = visitCounter + 1;
				} // while inner loop
				consoleLogger.finer("random visits loop finished, total positions considered: " + visitCounter);

			} // if break loop

			if (nearestNeighborDist > bestSoFarDistance)
			{
				bestSoFarDistance = nearestNeighborDist;
				bestSoFarPosition = currentPosition;
				bestSoFarString = String.valueOf(currentEntry.getStr());
			}
			if (knownWordsAndTheirDistances.containsKey(currentWord)
					&& knownWordsAndTheirDistances.get(currentWord).isAbandoned())
			{
				knownWordsAndTheirDistances.put(String.valueOf(currentWord),
						new DistanceEntry(nearestNeighborDist, completeSearch));
			}
			else
			{
				knownWordsAndTheirDistances.put(String.valueOf(currentWord),
						new DistanceEntry(nearestNeighborDist, completeSearch));
			}
			consoleLogger.finer(" . . iterated " + idx + " times, best distance:  " + bestSoFarDistance
					+ " for a string " + bestSoFarString);

			idx++;
		} // outer loop

		return new DiscordRecord(bestSoFarPosition, bestSoFarDistance, bestSoFarString);
	}

	private static void cleanUpFrequencies(ArrayList<SAXTrieHitEntry> frequencies, String currentWord,
			int startPosition)
	{
		int i = startPosition + 1;
		while (i < frequencies.size())
		{
			if (currentWord.equalsIgnoreCase(String.valueOf(frequencies.get(i).getStr())))
			{
				frequencies.remove(i);
			}
			else
			{
				i++;
			}
		}

	}

	/**
	 * Get N top motifs from trie.
	 * 
	 * @param trie
	 *            The trie.
	 * @param maxMotifsNum
	 *            The number of motifs to report.
	 * @return The motifs collection.
	 * @throws TrieException
	 *             If error occurs.
	 */
	private static MotifRecords getMotifs(SAXTrie trie, int maxMotifsNum) throws TrieException
	{

		MotifRecords res = new MotifRecords(maxMotifsNum);

		ArrayList<SAXTrieHitEntry> frequencies = trie.getFrequencies();

		Collections.sort(frequencies);

		// all sorted - from one end we have unique words - those discords
		// from the other end - we have motifs - the most frequent entries
		//
		// what I'll do here - is to populate non-trivial frequent entries into
		// the resulting container
		//

		// picking those non-trivial patterns this method job
		// non-trivial here means the one which are not the same letters
		//

		Set<SAXTrieHitEntry> seen = new TreeSet<SAXTrieHitEntry>();

		int counter = 0;
		// iterating backward - collection is sorted
		for (int i = frequencies.size() - 1; i >= 0; i--)
		{
			SAXTrieHitEntry entry = frequencies.get(i);
			if (entry.isTrivial(2) || seen.contains(entry) || (2 > entry.getFrequency()))
			{
				if ((2 > entry.getFrequency()))
				{
					break;
				}
				continue;
			}
			else
			{
				counter += 1;
				res.add(new MotifRecord(entry.getStr(), trie.getOccurences(entry.getStr())));
				seen.add(entry);
				if (counter > maxMotifsNum)
				{
					break;
				}
			}
		}
		return res;
	}

	/**
	 * Convert real-valued series into symbolic representation.
	 * 
	 * @param vals
	 *            Real valued timeseries.
	 * @param windowSize
	 *            The PAA window size.
	 * @param cuts
	 *            The cut values array used for SAX transform.
	 * @return The symbolic representation of the given real time-series.
	 * @throws TSException
	 *             If error occurs.
	 */
	public static char[] getSaxVals(double[] vals, int windowSize, double[] cuts) throws TSException
	{
		char[] saxVals;
		if (windowSize == cuts.length + 1)
		{
			saxVals = TSUtils.ts2String(TSUtils.zNormalize(vals), cuts);
		}
		else
		{
			saxVals = TSUtils.ts2String(TSUtils.zNormalize(TSUtils.paa(vals, cuts.length + 1)), cuts);
		}
		return saxVals;
	}

	/**
	 * Extracts sub-series from the WEKA-style series.
	 * 
	 * @param data
	 *            The series.
	 * @param attribute
	 *            The data-bearing attribute.
	 * @param start
	 *            The start timestamp.
	 * @param end
	 *            The end timestamp
	 * @return sub-series from start to end.
	 */
	private static double[] getSubSeries(Instances data, Attribute attribute, int start, int end)
	{
		List<Instance> tmpList = data.subList(start, end);
		double[] vals = new double[end - start];
		for (int i = 0; i < end - start; i++)
		{
			vals[i] = tmpList.get(i).value(attribute.index());
		}
		return vals;
	}

	/**
	 * Converts Instances into double array.
	 * 
	 * @param tsData
	 *            The instances data.
	 * @param dataAttribute
	 *            The attribute to use in conversion.
	 * @return real-valued array.
	 */
	private static double[] toRealSeries(Instances tsData, Attribute dataAttribute)
	{
		double[] vals = new double[tsData.size()];
		for (int i = 0; i < tsData.size(); i++)
		{
			vals[i] = tsData.get(i).value(dataAttribute.index());
		}
		return vals;
	}

	/**
	 * Extracts sub-series from series.
	 * 
	 * @param data
	 *            The series.
	 * @param start
	 *            The start position.
	 * @param end
	 *            The end position
	 * @return sub-series from start to end.
	 */
	public static double[] getSubSeries(double[] data, int start, int end)
	{
		double[] vals = new double[end - start];
		for (int i = 0; i < end - start; i++)
		{
			vals[i] = data[start + i];
		}
		return vals;
	}

	/**
	 * Brute force calculation of the distances.
	 * 
	 * @param tsData
	 *            timeseries.
	 * @param dataAttributeName
	 *            The pointer onto data-bearing attribute.
	 * @param controls
	 *            The control values.
	 * @param window
	 *            Window size.
	 * @throws TSException
	 *             if error occurs.
	 */
	public static void maxDistances(Instances tsData, String dataAttributeName, int[] controls, int window)
			throws TSException
	{
		// get the timestamps and data attributes
		//
		Attribute dataAttribute = tsData.attribute(dataAttributeName);

		double[] distances = new double[controls.length];
		int[] maxPos = new int[controls.length];

		for (int i = 0; i < controls.length; i++)
		{
			distances[i] = Double.MAX_VALUE;
			maxPos[i] = -1;
		}

		// [1.0] PREPROCESSING: in the sliding window loop build SAX string
		// entries
		//
		int currPosition = 0;
		while ((currPosition + window) < tsData.size())
		{

			double[] vals = getSubSeries(tsData, dataAttribute, currPosition, currPosition + window);

			for (int i = 0; i < controls.length; i++)
			{
				if (Math.abs(controls[i] - currPosition) < window)
				{
					continue;
				}
				else
				{
					double[] oVals = getSubSeries(tsData, dataAttribute, controls[i], controls[i] + window);
					double dist = EuclideanDistance.distance(vals, oVals);
					if (distances[i] > dist)
					{
						distances[i] = dist;
						maxPos[i] = currPosition;
					}
				}
			}
			currPosition++;
		}

		// for (int i = 0; i < controls.length; i++) {
		// System.out.println(controls[i] + " - " + distances[i] + ", at " +
		// maxPos[i]);
		// }

		// for (int i = 0; i < controls.length; i++) {
		// double[] is = getSubSeries(tsData, dataAttribute, controls[i],
		// controls[i] + window);
		// double[] os = getSubSeries(tsData, dataAttribute, maxPos[i],
		// maxPos[i] + window);
		// System.out.println(Arrays.toString(is) + "\n" + Arrays.toString(os));
		// }
	}

	// /**
	// *
	// * "We are given n, the length of the discords in advance, and we must
	// choose two parameters,
	// the
	// * cardinality of the SAX alphabet size a, and the SAX word size w. We
	// defer a discussion of how
	// * to set these parameters until"
	// *
	// *
	// * @param tsData timeseries.
	// * @param windowLength window length.
	// * @param paaSize The PAA window size.
	// * @param alphabetSize The SAX alphabet size.
	// * @param timeAttributeName Time-stamp attribute.
	// * @param dataAttributeName Value attribute.
	// * @return top discords for the time-series given
	// * @throws TSException if error occurs.
	// */
	// public static DiscordRecords getBruteForceDiscords(Instances tsData, int
	// windowLength,
	// int paaSize, int alphabetSize, String timeAttributeName, String
	// dataAttributeName)
	// throws TSException {
	//
	// double[] cuts = normalAlphabet.getCuts(alphabetSize);
	//
	// // get the timestamps and data attributes
	// //
	// Attribute dataAttribute = tsData.attribute(dataAttributeName);
	// double[] theRawData =
	// TSUtils.zNormalize(tsData.attributeToDoubleArray(dataAttribute.index()));
	//
	// // Init variables
	// //
	// DiscordRecords discords = new DiscordRecords(10);
	// DiscordRecord discord = new DiscordRecord();
	//
	// XMLGregorianCalendar cTstamp =
	// Tstamp.makeTimestamp(System.currentTimeMillis());
	//
	// // run the search loop
	// //
	// for (int i = 0; i < tsData.size() - windowLength; i++) {
	//
	// if (i % 100 == 0) {
	// XMLGregorianCalendar nTstamp =
	// Tstamp.makeTimestamp(System.currentTimeMillis());
	// System.out.println("i: " + i + ", at: " + nTstamp + ", diff: "
	// + Tstamp.diff(cTstamp, nTstamp) + ", discord at: " +
	// discord.getPosition()
	// + ", distance: " + discord.getDistance());
	// cTstamp = nTstamp;
	// }
	//
	// // fix the i-s string
	// //
	// // char[] ssA = TSUtils.ts2String(TSUtils.paa(TSUtils.normalize(Arrays
	// // .copyOfRange(theRawData, i, i + windowLength)), paaSize), cuts);
	// //
	// char[] ssA = TSUtils.ts2String(
	// TSUtils.paa(Arrays.copyOfRange(theRawData, i, i + windowLength),
	// paaSize), cuts);
	//
	// Integer nearestNeighborDist = Integer.MAX_VALUE;
	//
	// // the inner loop
	// //
	// for (int j = 0; j < tsData.size() - windowLength; j++) {
	//
	// // check for the trivial match
	// //
	// if (Math.abs(i - j) >= windowLength) {
	//
	// // get the SAX approximations of both series here
	// //
	// // char[] ssB = TSUtils.ts2String(TSUtils.paa(TSUtils.normalize(Arrays
	// // .copyOfRange(theRawData, j, j + windowLength)), paaSize), cuts);
	//
	// char[] ssB = TSUtils.ts2String(
	// TSUtils.paa(Arrays.copyOfRange(theRawData, j, j + windowLength),
	// paaSize), cuts);
	//
	// // get the distance here and early terminate if it's less than the
	// // largest
	// //
	// Integer tmpDist = strDistance(ssA, ssB);
	// // System.out.println(String.valueOf(ssA) + " VS " +
	// // String.valueOf(ssB)
	// // + " : " + tmpDist);
	// if (tmpDist == 0 || tmpDist < discords.getMinDistance()) {
	// break;
	// }
	// if (tmpDist < nearestNeighborDist) {
	// nearestNeighborDist = tmpDist;
	// }
	// }
	//
	// }
	// if ((nearestNeighborDist != Integer.MAX_VALUE)
	// && (nearestNeighborDist > discords.getMinDistance())) {
	// discord.setDistance(nearestNeighborDist);
	// discord.setIndex(i);
	// discords.add(discord);
	// discord = new DiscordRecord();
	// }
	// } // i loop - outer
	// return discords;
	// }

	/**
	 * 
	 * "We are given n, the length of the discords in advance, and we must choose two parameters, the cardinality of the
	 * SAX alphabet size a, and the SAX word size w. We defer a discussion of how to set these parameters until"
	 * 
	 * 
	 * @param tsData
	 *            timeseries.
	 * @param windowLength
	 *            window length.
	 * @return top discords for the time-series given
	 * @throws TSException
	 *             if error occurs.
	 */
	public static DiscordRecords getBruteForceDiscords(double[] tsData, int windowLength) throws TSException
	{

		DiscordRecords discords = new DiscordRecords(100);

		// run the search loop
		//
		for (int i = 0; i < tsData.length - windowLength; i++)
		{

			double[] seriesA = getSubSeries(tsData, i, i + windowLength);
			Double nearestNeighborDist = Double.MAX_VALUE;

			// the inner loop
			//
			for (int j = 0; j < tsData.length - windowLength; j++)
			{

				if (Math.abs(i - j) < windowLength)
				{
					continue;
				}

				double[] seriesB = getSubSeries(tsData, j, j + windowLength);
				double dist = EuclideanDistance.distance(seriesA, seriesB);

				if (dist < nearestNeighborDist)
				{
					nearestNeighborDist = dist;
				}

			} // inner loop

			// if (nearestNeighborDist > bestSoFarDist) {
			// bestSoFarDist = nearestNeighborDist;
			// bestSoFarLoc = i;
			discords.add(new DiscordRecord(i, nearestNeighborDist));
			// }
		}

		return discords;
	}

	/**
	 * Generic method to convert the milliseconds into the elapsed time string.
	 * 
	 * @param start
	 *            Start timestamp.
	 * @param finish
	 *            End timestamp.
	 * @return String representation of the elapsed time.
	 */
	private static String timeToString(long start, long finish)
	{
		long diff = finish - start;

		long secondInMillis = 1000;
		long minuteInMillis = secondInMillis * 60;
		long hourInMillis = minuteInMillis * 60;
		long dayInMillis = hourInMillis * 24;
		long yearInMillis = dayInMillis * 365;

		@SuppressWarnings("unused")
		long elapsedYears = diff / yearInMillis;
		diff = diff % yearInMillis;

		@SuppressWarnings("unused")
		long elapsedDays = diff / dayInMillis;
		diff = diff % dayInMillis;

		@SuppressWarnings("unused")
		long elapsedHours = diff / hourInMillis;
		diff = diff % hourInMillis;

		long elapsedMinutes = diff / minuteInMillis;
		diff = diff % minuteInMillis;

		long elapsedSeconds = diff / secondInMillis;
		diff = diff % secondInMillis;

		long elapsedMilliseconds = diff % secondInMillis;

		return elapsedMinutes + "m " + elapsedSeconds + "s " + elapsedMilliseconds + "ms";
	}

}
