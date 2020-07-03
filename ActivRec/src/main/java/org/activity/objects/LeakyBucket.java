package org.activity.objects;

import org.activity.io.WToFile;

/**
 * A StringBuilder which flushes itself after a particular threshold
 * <p>
 * Sanity checked ok.
 * 
 * @author gunjan
 * @since 2 July 2018
 */
public class LeakyBucket
{

	String absFileNameToWriteTo;
	StringBuilder bucket;
	int bucketThreshold;
	boolean verboseWriting;

	/**
	 * Don't forget to flush the bucket in the end.
	 * 
	 * @param bucketThreshold
	 * @param absFileNameToWriteTo
	 */
	public LeakyBucket(int bucketThreshold, String absFileNameToWriteTo, boolean verboseWriting)
	{
		this.bucketThreshold = bucketThreshold;
		bucket = new StringBuilder();
		this.absFileNameToWriteTo = absFileNameToWriteTo;
		this.verboseWriting = verboseWriting;
	}

	public void flushLeakyBucket()
	{
		WToFile.appendLineToFileAbs(bucket.toString(), absFileNameToWriteTo);
		bucket.setLength(0);
	}

	/**
	 * Don't forget to flush the bucket in the end.
	 * 
	 * @param s
	 */
	public void addToLeakyBucket(String s)
	{
		bucket.append(s);
		int lengthOfContents = bucket.length();

		if (lengthOfContents > bucketThreshold)
		{
			if (verboseWriting)
			{
				System.out.println(
						"Writing bucket contents, len=" + lengthOfContents + " to file " + absFileNameToWriteTo);
			}
			flushLeakyBucket();
		}
	}

	/**
	 * Don't forget to flush the bucket in the end.
	 * 
	 * @param s
	 */
	public void addToLeakyBucketWithNewline(String s)
	{
		addToLeakyBucket(s + "\n");
	}

}
