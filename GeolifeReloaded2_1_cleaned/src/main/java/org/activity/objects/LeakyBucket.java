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

	public void addToLeakyBucketWithNewline(String s)
	{
		addToLeakyBucket(s + "\n");
	}

}
