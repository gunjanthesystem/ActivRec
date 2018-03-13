package org.activity.plotting0;

public class ChartUtil
{

	/**
	 * 
	 * @param number
	 * @return
	 */
	public static Integer calculateFloorPowerOfTen(Integer number)
	{
		Integer powerOfTen = 0;
		Double number2 = number / 10.0;
		while (number2 > 10)
		{
			powerOfTen++;
			number2 = number2 / 10;
		}
		Double d = new Double(Math.pow(10, powerOfTen));
		Integer result = new Double(Math.floor(number2) * d).intValue();
		if (result == 0)
		{
			result = 1;
		}
		return result;
	}

	/**
	 * 
	 * @param upperbound
	 * @param tickUnit
	 * @return
	 */
	public static Float calculateUpperbound(Float upperbound, Integer tickUnit)
	{
		float temp = upperbound / tickUnit;
		return new Float(Math.ceil(new Float(temp).doubleValue()) * tickUnit);
	}

	/**
	 * 
	 * @param upperbound
	 * @param tickUnit
	 * @return
	 */
	public static Float calculateLowerbound(Float upperbound, Integer tickUnit)
	{
		float temp = upperbound / tickUnit;
		return new Float(Math.floor(new Float(temp).doubleValue()) * tickUnit);
	}

}