package org.activity.circular;

import java.util.Arrays;

/**
 * For Circular/Directional Statistic
 * 
 * @author gunjan
 * @since 11 Mar 2019
 */
public class CircularUtils
{
	public static final long NumOfSecsInDay = 24 * 60 * 60;

	public CircularUtils()
	{
	}

	public static void main(String[] args)
	{
		printMeanAngle(350.0, 1.0);
		printMeanAngle(90.0, 180.0, 270.0, 360.0);
		printMeanAngle(10.0, 20.0, 30.0);
		printMeanAngle(370.0);
		printMeanAngle(180.0);

		System.out.println(getCircularMeanTimeOfDayInSecs(23 * 60 * 60, 200));
	}

	private static void printMeanAngle(double... sample)
	{
		double meanAngle = getMeanAngle(sample);
		System.out.printf("The mean angle of %s is %s%n", Arrays.toString(sample), meanAngle);
	}

	public static double convertTimeOfDayInSecsToAnglesDeg(long secs)
	{
		return (360.0 * secs) / NumOfSecsInDay;
	}

	public static double convertAnglesDegToTimeOfDayInSecs(double anglesDeg)
	{
		return (anglesDeg * NumOfSecsInDay) / 360.0;
	}

	/**
	 * 
	 * @param timesOfDayInSecs
	 * @return
	 * @since 11 March 2019
	 */
	public static Double getCircularMeanTimeOfDayInSecs(long... timesOfDayInSecs)
	{
		double[] timesOfDayInSecsInAnglesDeg = new double[timesOfDayInSecs.length];
		int i = 0;
		for (long t : timesOfDayInSecs)
		{
			timesOfDayInSecsInAnglesDeg[i++] = convertTimeOfDayInSecsToAnglesDeg(t);
		}
		double meanAngle = getMeanAngle(timesOfDayInSecsInAnglesDeg);
		return convertAnglesDegToTimeOfDayInSecs(meanAngle);
	}

	/**
	 * src: https://rosettacode.org/wiki/Averages/Mean_angle#Java
	 * 
	 * @param anglesDeg
	 * @return
	 */
	public static double getMeanAngle(double... anglesDeg)
	{
		double x = 0.0;
		double y = 0.0;

		for (double angleD : anglesDeg)
		{
			double angleR = Math.toRadians(angleD);
			x += Math.cos(angleR);
			y += Math.sin(angleR);
		}
		double avgR = Math.atan2(y / anglesDeg.length, x / anglesDeg.length);
		return Math.toDegrees(avgR);
	}
}
