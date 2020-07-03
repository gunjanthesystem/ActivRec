package org.activity.generator;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.activity.constants.Constant;
import org.activity.io.Serializer;
import org.activity.objects.FlatActivityLogEntry;
import org.activity.objects.LocationObject;
import org.activity.stats.StatsUtils;
import org.activity.util.ConnectDatabase;

/**
 * The was used in intial phase of the project to generate synthetic timelines
 * 
 * @author gunjan
 *
 */
public class GenerateSyntheticData
{
	public static void main(String[] args)
	{
		int timeId = -1;
		int dateId = -1;

		ArrayList<Integer> uniqueActivityIds = new ArrayList<Integer>();
		ArrayList<Integer> uniqueDateIds = new ArrayList<Integer>();
		ArrayList<Integer> uniqueLocationIds = new ArrayList<Integer>();
		ArrayList<Integer> uniqueTimeIds = new ArrayList<Integer>();

		ArrayList<FlatActivityLogEntry> listOfActivityEntries = new ArrayList<FlatActivityLogEntry>();
		for (int userId = 0; userId <= 6; userId++) // 7 Users
		{
			String userName = getUserName(userId);
			int userAge = StatsUtils.randomInRange(20, 35);
			String ageCategory = getAgeCategory(userAge);
			String profession = getProfession(userId);
			String personality = getPersonality(profession);

			ConnectDatabase.insertIntoUserDimension(userId, userName, userAge, personality, profession, ageCategory);
			int year = 2014; // 14
			for (int month = 3; month <= 4; month++)
			{
				int numberOfDaysInMonth = 0;
				if (month % 2 == 0)
					numberOfDaysInMonth = 30;
				else
					numberOfDaysInMonth = 31;
				for (int day = 1; day <= numberOfDaysInMonth; day++)// 30 days
				{
					dateId = Integer.parseInt(Integer.toString(day) + Integer.toString(month) + Integer.toString(year));

					// int numberOfActivities=randomInRange(4,7);// a user performs 3 to 5 activities per day. Min +
					// (int)(Math.random() * ((Max - Min) + 1))

					int startInDayHour = StatsUtils.randomInRange(5, 8);
					int startInDayMinutes = StatsUtils.randomInRange(0, 59);
					int previousEndHour = startInDayHour;
					int previousEndMinute = startInDayMinutes;
					int previousActivityId = -1; // to make sure that consecutive activities are not the same. (this
													// will create an effect of merging similar consecutive activities)
					boolean breakDayLoop = false;

					for (int activityIterator = -1;/* activityIterator<=numberOfActivities && (previousEndHour+4 <23) */; activityIterator++)
					{
						int activityId;
						int startHour;
						int startMinute;
						int endHour;
						int endMinute;

						if (activityIterator == -1) // This is the activity before the listed activities start in the
													// day, this will be an 'other' activity
						{
							activityId = 99; // 'Other' activity
							previousActivityId = -1;

							startHour = 0;
							startMinute = 0;

							endHour = startInDayHour;
							endMinute = startInDayMinutes;
						}
						// this is the last activity of the day and it will be an other activity
						// no activity starts after 7pm
						else if (!(previousEndHour + 4 < 23) || activityIterator == 7)
						// other activity after the listed activities end in the day
						{
							System.out.println("@@@@@@@@@@@@@@@@@@@@creating ending day other for day= " + day);
							activityId = 99; // 'Other Activity'
							previousActivityId = -1;

							startHour = previousEndHour;// randomInRange(0,16);
							startMinute = previousEndMinute;// randomInRange(0,59);

							endHour = 23;
							endMinute = 59;

							breakDayLoop = true;
						}

						else
						// listed activity
						{
							activityId = StatsUtils.randomInRange(0, 14); // 15 Activities
							if (activityId == previousActivityId) // if the new activity is same as the last one
							{
								if (activityId < 14)
									activityId += 1;
								else
									activityId += -1;
							}

							previousActivityId = activityId;

							startHour = previousEndHour;// randomInRange(0,16);
							startMinute = previousEndMinute;// randomInRange(0,59);

							// int randomDurationInMinutes =
							// randomInRange(15,getActivityMaxDurationInMinutes(activityId));

							// endHour = startHour + randomDurationInMinutes/60;
							// endHour=startHour+randomInRange(1,getActivityMaxDurationInMinutes(activityId)/60);

							endHour = startHour + StatsUtils.randomInRange(1, 4);
							endMinute = StatsUtils.randomInRange(0, 59);
							// endMinute = randomDurationInMinutes%60;
							// endMinute=randomInRange(0,getActivityMaxDurationInMinutes(activityId)%60);
						}
						previousEndHour = endHour;
						previousEndMinute = endMinute;

						String activityName = getActivityName(activityId);
						String activityCategory = getActivityCategory(activityId);

						if (!(uniqueActivityIds.contains(activityId)))
						{
							ConnectDatabase.insertIntoActivityDimension(activityId, activityName, activityCategory);
							uniqueActivityIds.add(activityId);
						}

						LocationObject locationObject = LocationObject.getSyntheticLocationObjectInstance(userId,
								activityId);

						timeId = Integer.parseInt(Integer.toString(startHour) + Integer.toString(startMinute)
								+ Integer.toString(endHour) + Integer.toString(endMinute));

						String timeCategory = getTimeCategory(startHour);
						Timestamp startTimestamp = new Timestamp(year - 1900, month - 1, day, startHour, startMinute, 0,
								0);// Timestamp(int year, int month, int date, int hour, int minute, int
									// second, int nano)
						String weekDay = "default", weekOfYear = "default";
						try
						{
							weekDay = ConnectDatabase
									.getSQLStringResultSingleColumn("SELECT DAYNAME('" + startTimestamp + "');").get(0);
							weekOfYear = ConnectDatabase
									.getSQLStringResultSingleColumn("SELECT WEEKOFYEAR('" + startTimestamp + "');")
									.get(0);
						}

						catch (Exception e)
						{
							e.printStackTrace();
						}

						FlatActivityLogEntry activityLogEntry = new FlatActivityLogEntry(); // represents a row of all
																							// combined table

						activityLogEntry.setUser_ID(userId);
						activityLogEntry.setActitivity_ID(activityId);
						activityLogEntry.setTime_ID(timeId);
						activityLogEntry.setDate_ID(dateId);
						activityLogEntry.setLocation_ID(locationObject.getLocationId());
						activityLogEntry.setDuration((endHour - startHour) * 60 + (endMinute - startMinute));
						activityLogEntry.setFrequency(1);
						activityLogEntry.setUser_Name(userName);
						activityLogEntry.setPersonality_Tags(personality);
						activityLogEntry.setProfession(profession);
						activityLogEntry.setAge_Category(ageCategory);
						activityLogEntry.setUser_Age(userAge);
						activityLogEntry.setActivity_Name(activityName);
						activityLogEntry.setActivity_Category(activityCategory);
						activityLogEntry.setStart_Time(startHour, startMinute, 0);
						activityLogEntry.setEnd_Time(endHour, endMinute, 0);
						activityLogEntry.setTime_Category(timeCategory);
						activityLogEntry.setStart_Date(year, month, day);
						activityLogEntry.setWeek_Day(weekDay);
						activityLogEntry.setMonth(month);
						activityLogEntry.setQuarter(month);
						activityLogEntry.setWeek(Integer.parseInt(weekOfYear));
						activityLogEntry.setYear(year);
						// activityLogEntry.setLatitude(locationObject.latitude);
						// activityLogEntry.setLongitude(locationObject.longitude);
						activityLogEntry.setLocation_Name(locationObject.locationName);
						activityLogEntry.setLocation_Category(locationObject.locationCategory);
						activityLogEntry.setCity(locationObject.city);
						activityLogEntry.setCounty(locationObject.county);
						activityLogEntry.setCountry(locationObject.country);
						activityLogEntry.setContinent(locationObject.continent);

						ConnectDatabase.insertIntoActivityFact(activityLogEntry);

						if (!(uniqueTimeIds.contains(timeId)))
						{
							ConnectDatabase.insertIntoTimeDimension(activityLogEntry);
							uniqueTimeIds.add(timeId);
						}

						if (!(uniqueDateIds.contains(dateId)))
						{
							ConnectDatabase.insertIntoDateDimension(activityLogEntry);
							uniqueDateIds.add(dateId);
						}

						if (!(uniqueLocationIds.contains(locationObject.getLocationId())))
						{
							ConnectDatabase.insertIntoLocationDimension(activityLogEntry);
							uniqueLocationIds.add(locationObject.getLocationId());
						}

						if (breakDayLoop) // Reached end of that day, 'other' activity after the listed activities done.
						{
							break; // no more activities in the day
						}
						listOfActivityEntries.add(activityLogEntry);

					}
				}
			}

		} // end of user loop

		Serializer.serializeAllLogEntries(listOfActivityEntries, Constant.getCommonPath() + "SyntheticDataLogEntries");
	}

	public static String getUserName(int userId)
	{
		String userName = "";

		switch (userId)
		{
		case 0:
			userName = "Tessa";
			break;
		case 1:
			userName = "Jacob";
			break;
		case 2:
			userName = "Rahul";
			break;
		case 3:
			userName = "Christopher";
			break;
		case 4:
			userName = "Maggi";
			break;
		case 5:
			userName = "Liu";
			break;
		case 6:
			userName = "Andrew";
			break;
		default:
			userName = "Nefertiti";
			break;
		}
		return userName;
	}

	public static String getProfession(int userId)
	{
		String professionName = "";

		switch (userId)
		{
		case 0:
			professionName = "Administrator";
			break;
		case 1:
			professionName = "Soccer Player";
			break;
		case 2:
			professionName = "Accountant";
			break;
		case 3:
			professionName = "Teacher";
			break;
		case 4:
			professionName = "Researcher";
			break;
		case 5:
			professionName = "Student";
			break;
		case 6:
			professionName = "Researcher";
			break;
		default:
			professionName = "Default Profession";
			break;
		}
		return professionName;
	}

	public static String getPersonality(String profession)
	{
		String personalityName = "";

		switch (profession)
		{
		case "Administrator":
			personalityName = "Enterprising";
			break;
		case "Soccer Player":
			personalityName = "Realistic Enterprising";
			break;
		case "Accountant":
			personalityName = "Realistic";
			break;
		case "Teacher":
			personalityName = "Investigative";
			break;
		case "Researcher":
			personalityName = "Investigative";
			break;
		case "Student":
			personalityName = "Student";
			break;
		default:
			personalityName = "Default personality";
			break;
		}
		return personalityName;
	}

	/**
	 * Returns the maximum duration in minutes for the given activity id.
	 * 
	 * @param activityId
	 * @return
	 */
	public static int getActivityMaxDurationInMinutes(int activityId)
	{
		int maxDurationInMinutes = 4 * 60;
		switch (activityId)
		{
		case 0:
			maxDurationInMinutes = 2 * 60;// activityName= "Commuting";
			break;
		case 1:
			maxDurationInMinutes = 4 * 60;// activityName= "Working";
			break;
		case 2:
			maxDurationInMinutes = (3 * 60) + 10;// activityName= "Socialising";
			break;
		case 3:
			maxDurationInMinutes = 4 * 60;// activityName= "Computer";
			break;
		case 4:
			maxDurationInMinutes = 2 * 55;// activityName= "WatchingTV";
			break;
		case 5:
			maxDurationInMinutes = 4 * 45;// activityName= "Shopping";
			break;
		case 6:
			maxDurationInMinutes = 30;// activityName= "On Phone";
			break;
		case 7:
			maxDurationInMinutes = 4 * 55;// activityName= "Relaxing";
			break;
		case 8:
			maxDurationInMinutes = 30;// activityName= "Meditating";
			break;
		case 9:
			maxDurationInMinutes = 3 * 35;// activityName= "Taking care of children";
			break;
		case 10:
			maxDurationInMinutes = 85;// activityName= "Preparing Food";
			break;
		case 11:
			maxDurationInMinutes = 2 * 60;// activityName= "Housework";
			break;
		case 12:
			maxDurationInMinutes = 30;// activityName= "Eating";
			break;
		case 13:
			maxDurationInMinutes = 75;// activityName= "Nap/Resting";
			break;
		case 14:
			maxDurationInMinutes = 60;// activityName= "Exercising";
			break;
		default:
			maxDurationInMinutes = 4 * 60;// activityName= "Others";
			break;
		}
		return maxDurationInMinutes;
	}

	public static int getActivityMinDurationInMinutes(int activityId)
	{
		int minDurationInMinutes = 15;
		switch (activityId)
		{
		case 0:
			minDurationInMinutes = 30;// activityName= "Commuting";
			break;
		case 1:
			minDurationInMinutes = 1 * 60;// activityName= "Working";
			break;
		case 2:
			minDurationInMinutes = 20;// activityName= "Socialising";
			break;
		case 3:
			minDurationInMinutes = 1 * 60;// activityName= "Computer";
			break;
		case 4:
			minDurationInMinutes = 30;// activityName= "WatchingTV";
			break;
		case 5:
			minDurationInMinutes = 80;// activityName= "Shopping";
			break;
		case 6:
			minDurationInMinutes = 15;// activityName= "On Phone";
			break;
		case 7:
			minDurationInMinutes = 60;// activityName= "Relaxing";
			break;
		case 8:
			minDurationInMinutes = 20;// activityName= "Meditating";
			break;
		case 9:
			minDurationInMinutes = 60;// activityName= "Taking care of children";
			break;
		case 10:
			minDurationInMinutes = 30;// activityName= "Preparing Food";
			break;
		case 11:
			minDurationInMinutes = 50;// activityName= "Housework";
			break;
		case 12:
			minDurationInMinutes = 30;// activityName= "Eating";
			break;
		case 13:
			minDurationInMinutes = 30;// activityName= "Nap/Resting";
			break;
		case 14:
			minDurationInMinutes = 20;// activityName= "Exercising";
			break;
		default:
			minDurationInMinutes = 0;// activityName= "Others";
			break;
		}
		return minDurationInMinutes;
	}

	public static String getActivityName(int activityId)
	{
		String activityName = " ";
		switch (activityId)
		{
		case 0:
			activityName = "Commuting";
			break;
		case 1:
			activityName = "Working";
			break;
		case 2:
			activityName = "Socialising";
			break;
		case 3:
			activityName = "Computer";
			break;
		case 4:
			activityName = "WatchingTV";
			break;
		case 5:
			activityName = "Shopping";
			break;
		case 6:
			activityName = "On Phone";
			break;
		case 7:
			activityName = "Relaxing";
			break;
		case 8:
			activityName = "Meditating";
			break;
		case 9:
			activityName = "Taking care of children";
			break;
		case 10:
			activityName = "Preparing Food";
			break;
		case 11:
			activityName = "Housework";
			break;
		case 12:
			activityName = "Eating";
			break;
		case 13:
			activityName = "Nap/Resting";
			break;
		case 14:
			activityName = "Exercising";
			break;
		default:
			activityName = "Others";
			break;
		}
		return activityName;
	}

	public static String getActivityCategory(String activityName)
	{
		int id = 0;

		switch (activityName)
		{
		case "Commuting":
			id = 0;
			break;
		case "Working":
			id = 1;
			break;
		case "Socialising":
			id = 2;
			break;
		case "Computer":
			id = 3;
			break;
		case "WatchingTV":
			id = 4;
			break;
		case "Shopping":
			id = 5;
			break;
		case "On Phone":
			id = 6;
			break;
		case "Relaxing":
			id = 7;
			break;
		case "Meditating":
			id = 8;
			break;
		case "Taking care of children":
			id = 9;
			break;
		case "Preparing Food":
			id = 10;
			break;
		case "Housework":
			id = 11;
			break;
		case "Eating":
			id = 12;
			break;
		case "Nap/Resting":
			id = 13;
			break;
		case "Exercising":
			id = 14;
			break;
		default:
			id = 99;
			break;
		}

		return getActivityCategory(id);
	}

	public static int getActivityid(String activityName)
	{
		int id = 0;

		switch (activityName)
		{
		case "Commuting":
			id = 0;
			break;
		case "Working":
			id = 1;
			break;
		case "Socialising":
			id = 2;
			break;
		case "Computer":
			id = 3;
			break;
		case "WatchingTV":
			id = 4;
			break;
		case "Shopping":
			id = 5;
			break;
		case "On Phone":
			id = 6;
			break;
		case "Relaxing":
			id = 7;
			break;
		case "Meditating":
			id = 8;
			break;
		case "Taking care of children":
			id = 9;
			break;
		case "Preparing Food":
			id = 10;
			break;
		case "Housework":
			id = 11;
			break;
		case "Eating":
			id = 12;
			break;
		case "Nap/Resting":
			id = 13;
			break;
		case "Exercising":
			id = 14;
			break;
		default:
			id = 15;
			break;
		}

		return id;
	}

	public static String getActivityCategory(int activityId)
	{
		String activityCategory = "default";

		// Category PRODUCTIVE: (activity ids: 1,3,8,14,12)
		/*
		 * 1. Working 2. Computer 3. meditating 4. exercising 5. eating
		 */

		// Category NECESSITIES: (activity ids: 6, 5, 11,10,9, 0)
		/*
		 * 1. on phone 2. shopping 3. housework 4. preparing food 5. taking care of children 6. commuting
		 */

		// Category LEISURE: (activity ids: 4, 13, 7, 2)
		/*
		 * 1. Watchingtv 2. nap/resting 3. relaxing 4. socialising
		 */

		switch (activityId)
		{
		case 1:
		case 3:
		case 8:
		case 14:
		case 12:
			activityCategory = "Productive";
			break;

		case 6:
		case 5:
		case 11:
		case 10:
		case 9:
		case 0:
			activityCategory = "Necessities";
			break;

		case 4:
		case 13:
		case 7:
		case 2:
			activityCategory = "Leisure";
			break;

		default:
			activityCategory = "others";
			break;

		}

		/*
		 * switch(activityId%3) { case 0: activityCategory="category 1"; break; //commuting, computer, on phone, taking
		 * care, eating, case 1: activityCategory="category 2"; break; //working, watchingtv. relaxing, preparing food,
		 * nap/resting case 2: activityCategory="category 3"; break; //socialising, shopping, meditating, housework,
		 * exercising default: activityCategory="others"; break; }
		 */
		return activityCategory;
	}

	public static String getAgeCategory(int age)
	{
		String category = "default";

		if (age <= 35 && age >= 28)
			category = "Adult";
		else
			category = "Young";
		return category;
	}

	public static String getTimeCategory(int startHour)
	{
		String category = "default";

		if (startHour >= 4 && startHour <= 11)
			category = "Morning";
		else if (startHour >= 12 && startHour <= 13)
			category = "Noon";
		else if (startHour >= 14 && startHour <= 19)
			category = "Evening";
		else if (startHour >= 19 && startHour <= 23) category = "Night";
		return category;
	}

}