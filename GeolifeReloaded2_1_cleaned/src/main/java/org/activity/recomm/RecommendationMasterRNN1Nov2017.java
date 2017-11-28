package org.activity.recomm;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.activity.nn.BasicRNNForSeqRecSys;
import org.activity.objects.ActivityObject;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.ui.PopUps;
import org.activity.util.DateTimeUtils;
import org.activity.util.RegexUtils;
import org.activity.util.StringCode;
import org.apache.commons.lang3.ArrayUtils;

public class RecommendationMasterRNN1Nov2017 implements RecommendationMasterI
{
	static private BasicRNNForSeqRecSys rnn1;
	static boolean rnnTrained = false;
	private Date dateAtRecomm;
	private Time timeAtRecomm;
	private String userIDAtRecomm;
	private String userAtRecomm;

	public RecommendationMasterRNN1Nov2017(int numOfNeuronsInHiddenLayer, int numOfHiddenLayers, String dateAtRecomm,
			String timeAtRecomm, int userAtRecomm,
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsers,
			LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuous)
	{
		boolean verbose;

		// dd/mm/yyyy // okay java.sql.Date with no hidden time
		this.dateAtRecomm = DateTimeUtils.getDateFromDDMMYYYY(dateAtRecomm, RegexUtils.patternForwardSlash);
		this.timeAtRecomm = Time.valueOf(timeAtRecomm);
		this.userAtRecomm = Integer.toString(userAtRecomm);
		this.userIDAtRecomm = Integer.toString(userAtRecomm);
		System.out.println("	User at Recomm = " + this.userAtRecomm + "\tDate at Recomm = " + this.dateAtRecomm
				+ "\tTime at Recomm = " + this.timeAtRecomm + "\n");

		verbose = true;
		if (rnnTrained == false)
		{
			rnn1 = new BasicRNNForSeqRecSys(numOfNeuronsInHiddenLayer, numOfHiddenLayers);
			rnn1.createTrainingString(createTrainingString(trainTimelinesAllUsersContinuous), verbose);
			// rnnA.createTrainingString("hellohelo".toCharArray(), true);
			// // rnnA.createTrainingString(s.toCharArray(), true);
			rnn1.configureAndTrainRNN(1, 0.001, 5, false);
			rnnTrained = true;
		}
		else
		{
			System.out.println("rnn already trained rnnTrained=" + rnnTrained);
		}

		// System.exit(0);
		// rnnA.createTestString("he".toCharArray(), true);
		// rnnA.predictNextNValues(9, false);
	}

	private char[] createTrainingString(LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuous)
	{

		List<char[]> allUsersAOsAsCodeList = new ArrayList<>();

		long totalLengthOfConcatenatedTrainingTimelinesOfAllUser = 0;

		for (Entry<String, Timeline> entry : trainTimelinesAllUsersContinuous.entrySet())
		{
			ArrayList<ActivityObject> aos = entry.getValue().getActivityObjectsInTimeline();
			totalLengthOfConcatenatedTrainingTimelinesOfAllUser += aos.size();

			if (aos.size() > (Integer.MAX_VALUE - 100))
			{
				PopUps.showError("Error:aos.size() " + aos.size() + " >Integer.MAX_VALUE-100");
				System.exit(-1);
			}
			allUsersAOsAsCodeList.add(StringCode.getCharCodeArrayForAOs(aos));
		}

		System.out.println("totalLengthOfConcatenatedTrainingTimelinesOfAllUser ="
				+ totalLengthOfConcatenatedTrainingTimelinesOfAllUser);

		if (totalLengthOfConcatenatedTrainingTimelinesOfAllUser > (Integer.MAX_VALUE - 100))
		{
			PopUps.showError("Error:totalLengthOfConcatenatedTrainingTimelinesOfAllUser "
					+ totalLengthOfConcatenatedTrainingTimelinesOfAllUser + " >Integer.MAX_VALUE-100");
			System.exit(-1);
		}
		char[] allUsersAOsAsCode = new char[0];

		for (char[] l : allUsersAOsAsCodeList)
		{
			allUsersAOsAsCode = ArrayUtils.addAll(allUsersAOsAsCode, l);
		}

		System.out.println(totalLengthOfConcatenatedTrainingTimelinesOfAllUser == allUsersAOsAsCode.length);
		System.out.println("allUsersAOsAsCode.size()= " + allUsersAOsAsCode.length);
		System.out.println("totalLengthOfConcatenatedTrainingTimelinesOfAllUser= "
				+ totalLengthOfConcatenatedTrainingTimelinesOfAllUser);
		// System.exit(0);

		return allUsersAOsAsCode;

	}

	@Override
	public int getNumOfCandidateTimelines()
	{
		return -1;
	}

	@Override
	public int getNumOfActsInActsGuidingRecomm()
	{
		return -1;
	}

	@Override
	public int getNumOfValidActsInActsGuidingRecomm()
	{
		return -1;
	}

	@Override
	public Timeline getCandidateTimeline(String timelineID)
	{
		return null;
	}

	@Override
	public ArrayList<Timeline> getOnlyCandidateTimeslines()
	{
		return null;
	}

	@Override
	public Set<String> getCandidateTimelineIDs()
	{
		return null;
	}

	@Override
	public boolean hasCandidateTimeslines()
	{
		return true;
	}

	@Override
	public boolean hasCandidateTimelinesBelowThreshold()
	{
		return true;
	}

	@Override
	public boolean hasThresholdPruningNoEffect()
	{
		return false;
	}

	@Override
	public boolean isNextActivityJustAfterRecommPointIsInvalid()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ActivityObject getActivityObjectAtRecomm()
	{
		return new ActivityObject("dummy", new Timestamp(0), new Timestamp(0));
	}

	@Override
	public ArrayList<ActivityObject> getActsGuidingRecomm()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumOfCandTimelinesBelowThresh()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getActivityNamesGuidingRecommwithTimestamps()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getActivityNamesGuidingRecomm()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRankedRecommendedActNamesWithoutRankScores()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRankedRecommendedActNamesWithRankScores()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumOfDistinctRecommendations()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getThresholdAsDistance()
	{
		return -1;
	}

	@Override
	public String getNextActNamesWithDistString()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNextActNamesWithoutDistString()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedHashMap<String, Pair<String, Double>> getDistancesSortedMap()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedHashMap<String, Integer> getEndPointIndicesConsideredInCands()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getDateAtRecomm()
	{
		return dateAtRecomm;
	}

	@Override
	public LinkedHashMap<String, String> getCandUserIDs()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
