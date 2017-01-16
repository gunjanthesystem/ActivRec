package org.activity.util;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class HjorthParameters
{
	double activity, mobility, complexity;

	/**
	 * Computes the Hjort Parameters for the given time series
	 * 
	 * for hjorth forumal see: http://goo.gl/5CaKDH and http://goo.gl/ryaYpo papers in my research folder
	 * "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Research/LifeloggingResearch/Sequence mining/time series/hjorth
	 * parameters/" not using the matlab implementation because it introduces a '0' when calculating derivatives to keep
	 * the length of derivative and vals same. However, in my opinion this should not be done for time series which do
	 * not start at 0. for reference of implementation: see
	 * http://www.mathworks.com/matlabcentral/fileexchange/27561-measures-of-analysis-of-time-series-toolkit--mats-/content//MATS/HjorthParameters.m
	 * 
	 * @param yt
	 */
	//
	public HjorthParameters(double[] yt)
	{
		double varianceYt = new DescriptiveStatistics(yt).getVariance();
		double sdYt = new DescriptiveStatistics(yt).getStandardDeviation();

		double[] firstDerivative = getDerivative(yt);
		double varianceFirstDerivative = new DescriptiveStatistics(firstDerivative).getVariance();
		double sdFirstDerivative = new DescriptiveStatistics(firstDerivative).getStandardDeviation();

		double[] secondDerivative = getDerivative(firstDerivative);
		double varianceSecondDerivative = new DescriptiveStatistics(secondDerivative).getVariance();
		double sdSecondDerivative = new DescriptiveStatistics(secondDerivative).getStandardDeviation();

		activity = varianceYt; // m0, spectral moment at order 0
		// mobility = Math.sqrt(varianceFirstDerivative) / Math.sqrt(varianceYt); // m2
		// complexity = (Math.sqrt(varianceSecondDerivative) / Math.sqrt(varianceFirstDerivative)) /
		// (Math.sqrt(varianceFirstDerivative) / Math.sqrt(varianceYt));
		mobility = sdFirstDerivative / sdYt; // m2, spectral moment at order 2
		complexity = (sdSecondDerivative / sdFirstDerivative) / (sdFirstDerivative / sdYt); // m4, spectral moment at
																							// order 4
	}

	/**
	 * 
	 * @param xt
	 *            the given time series
	 * @return the first derivatives (𝛿=1) of the given time series for the entire interval
	 */
	public double[] getDerivative(double[] xt)
	{
		double der[] = new double[xt.length - 1];

		for (int i = 1; i < xt.length; i++)
		{
			der[i - 1] = xt[i] - xt[i - 1];
		}

		return der;
	}

	/**
	 * 
	 * @return
	 */
	public double getActivity()
	{
		return activity;
	}

	/**
	 * 
	 * @return
	 */
	public double getMobility()
	{
		return mobility;
	}

	/**
	 * 
	 * @return
	 */
	public double getComplexity()
	{
		return complexity;
	}
}
