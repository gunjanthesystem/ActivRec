package org.activity.objects;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

//import java.math.String;

public class FlatActivityLogEntry implements Serializable
{// TODO remove public visibility of variables
	public int User_ID, Activity_ID; // here the naming convention is in tandem with SQL database for readability
	public long Time_ID, Location_ID, Date_ID;
	public String Trajectory_ID; // Added on 18th April 2016
	
	public String User_Name, Personality_Tags, Profession, Age_Category;
	public int User_Age;
	
	public String Activity_Name, Activity_Category, Time_Category;
	public Time Start_Time, End_Time;
	
	public Date Start_Date, End_Date;
	
	public int Duration, Frequency;
	
	public String Week_Day;
	public String Month;
	public String Quarter;
	public int Week;
	public int Year;
	
	public String Start_Latitude, Start_Longitude, Start_Altitude, End_Latitude, End_Longitude, End_Altitude, Avg_Altitude;
	
	public String Latitudes, Longitudes, Altitudes; // stores list of Latitudes,..
	
	public String Location_Name, Location_Category, City, County, Country, Continent;
	
	/**
	 * note: corresponding header:
	 * "User_ID,Activity_ID,Date_ID,Time_ID,Location_ID,User_Name,Activity_Name,Start_Time,End_Time,Start_Date,End_Date,Duration,Start_Latitude,Start_Longitude,Start_Altitude,End_Latitude,End_Longitude,End_Altitude,Avg_Altitude"
	 * 
	 * @return
	 */
	public String toStringWithoutHeaders()
	{
		return User_ID + ", " + Activity_ID + ", " + Date_ID + ", " + Time_ID + ", " + Location_ID + ", " + Trajectory_ID + ", " + User_Name
				+ ", " + Activity_Name + ", " + Start_Time + ", " + End_Time + ", " + Start_Date + ", " + End_Date + ", " + Duration + ", "
				+ Start_Latitude + ", " + Start_Longitude + ", " + Start_Altitude + ", " + End_Latitude + ", " + End_Longitude + ", "
				+ End_Altitude + ", " + Avg_Altitude;
	}
	
	public String toString()
	{
		return "User_ID=" + User_ID + ", Activity_ID=" + Activity_ID + ", Date_ID=" + Date_ID + ", Time_ID=" + Time_ID + ", Location_ID="
				+ Location_ID + ", Trajectory_ID=" + Trajectory_ID + ", User_Name=" + User_Name + ", Activity_Name=" + Activity_Name
				+ ", Start_Time=" + Start_Time + ", End_Time=" + End_Time + ", Start_Date=" + Start_Date + ", End_Date=" + End_Date
				+ ", Duration=" + Duration + ", Start_Latitude=" + Start_Latitude + ", Start_Longitude=" + Start_Longitude
				+ ", Start_Altitude=" + Start_Altitude + ", End_Latitude=" + End_Latitude + ", End_Longitude=" + End_Longitude
				+ ", End_Altitude=" + End_Altitude + ", Avg_Altitude=" + Avg_Altitude;
	}
	
	public FlatActivityLogEntry()
	{
		
	}
	
	//
	// public String toString()
	// {
	// String s= User_ID"
	// }
	public void setUser_ID(int userID)
	{
		User_ID = userID;
	}
	
	public void setActitivity_ID(int activityID)
	{
		Activity_ID = activityID;
	}
	
	public void setTime_ID(long timeID)
	{
		Time_ID = timeID;
	}
	
	public void setDate_ID(long dateID)
	{
		Date_ID = dateID;
	}
	
	public void setLocation_ID(long locationID)
	{
		Location_ID = locationID;
	}
	
	/**
	 * Set the trajectory IDs for this activity object (data point). Note: there can be multiple trajectoryIDs if the mergers during data creating was not trajectory sensitive.
	 * 
	 * @param trajectoryID
	 */
	public void setTrajectory_IDs(String trajectoryID)
	{
		Trajectory_ID = trajectoryID;
	}
	
	public void setDuration(int duration)
	{
		Duration = duration;
	}
	
	public void setFrequency(int frequency)
	{
		Frequency = frequency;
	}
	
	public void setUser_Name(String userName)
	{
		User_Name = userName;
	}
	
	public void setPersonality_Tags(String personalityTags)
	{
		Personality_Tags = personalityTags;
	}
	
	public void setProfession(String profession)
	{
		Profession = profession;
	}
	
	public void setAge_Category(String ageCategory)
	{
		Age_Category = ageCategory;
	}
	
	public void setUser_Age(int userAge)
	{
		User_Age = userAge;
	}
	
	public void setActivity_Name(String activityName)
	{
		Activity_Name = activityName;
	}
	
	public void setActivity_Category(String activityCategory)
	{
		Activity_Category = activityCategory;
	}
	
	public void setTime_Category(String timeCategory)
	{
		Time_Category = timeCategory;
	}
	
	@SuppressWarnings("deprecation")
	public void setStart_Time(int hour, int min, int sec)
	{
		Start_Time = new Time(hour, min, sec);
	}
	
	public void setStart_Time(Timestamp startTimestamp)
	{
		Start_Time = new Time(startTimestamp.getTime());
		System.out.println("\tSetting start time =" + Start_Time + "  for given end timestamp:" + startTimestamp);
	}
	
	@SuppressWarnings("deprecation")
	public void setEnd_Time(int hour, int min, int sec)
	{
		End_Time = new Time(hour, min, sec);
	}
	
	public void setEnd_Time(Timestamp endTimeStamp)
	{
		End_Time = new Time(endTimeStamp.getTime() - 1000); // decreasing 1 second
		System.out.println("\tSetting end time =" + End_Time + "  for given end timestamp:" + endTimeStamp);
	}
	
	// year the year minus 1900; must be 0 to 8099. (Note that 8099 is 9999 minus 1900.)
	@SuppressWarnings("deprecation")
	public void setStart_Date(int year, int month, int day)
	{
		Start_Date = new Date(year - 1900, month - 1, day);
	}
	
	// year the year minus 1900; must be 0 to 8099. (Note that 8099 is 9999 minus 1900.)
	@SuppressWarnings("deprecation")
	public void setEnd_Date(int year, int month, int day)
	{
		End_Date = new Date(year - 1900, month - 1, day);
	}
	
	public void setWeek_Day(String weekDay)
	{
		Week_Day = weekDay;
	}
	
	public void setMonth(int month)
	{
		
		switch (month)
		{
			case 1:
				Month = "January";
				break;
			case 2:
				Month = "February";
				break;
			case 3:
				Month = "March";
				break;
			case 4:
				Month = "April";
				break;
			case 5:
				Month = "May";
				break;
			case 6:
				Month = "June";
				break;
			case 7:
				Month = "July";
				break;
			case 8:
				Month = "August";
				break;
			case 9:
				Month = "September";
				break;
			case 10:
				Month = "October";
				break;
			case 11:
				Month = "November";
				break;
			case 12:
				Month = "December";
				break;
			default:
				Month = "Invalid month";
				break;
		}
	}
	
	public void setQuarter(int month)
	{
		if (month >= 1 && month <= 3)
			Quarter = "Q1";
		else if (month >= 4 && month <= 6)
			Quarter = "Q2";
		else if (month >= 7 && month <= 9)
			Quarter = "Q3";
		else if (month >= 10 && month <= 12)
			Quarter = "Q4";
		else
			Quarter = "Invalid Quarter";
	}
	
	public void setWeek(int week)
	{
		Week = week;
	}
	
	public void setYear(int year)
	{
		Year = year;
	}
	
	public void setLatitudes(String latitudes)
	{
		Latitudes = latitudes;
	}
	
	public void setLongitudes(String longitudes)
	{
		Longitudes = longitudes;
	}
	
	public void setAltitudes(String altitudes)
	{
		Altitudes = altitudes;
	}
	
	/**
	 * @return the start_Latitude
	 */
	public String getStartLatitude()
	{
		return Start_Latitude;
	}
	
	/**
	 * @param start_Latitude
	 *            the start_Latitude to set
	 */
	public void setStartLatitude(String start_Latitude)
	{
		Start_Latitude = start_Latitude;
	}
	
	/**
	 * @return the start_Longitude
	 */
	public String getStartLongitude()
	{
		return Start_Longitude;
	}
	
	/**
	 * @param start_Longitude
	 *            the start_Longitude to set
	 */
	public void setStartLongitude(String start_Longitude)
	{
		Start_Longitude = start_Longitude;
	}
	
	/**
	 * @return the start_Altitude
	 */
	public String getStartAltitude()
	{
		return Start_Altitude;
	}
	
	/**
	 * @param start_Altitude
	 *            the start_Altitude to set
	 */
	public void setStartAltitude(String start_Altitude)
	{
		Start_Altitude = start_Altitude;
	}
	
	/**
	 * @return the end_Latitude
	 */
	public String getEndLatitude()
	{
		return End_Latitude;
	}
	
	/**
	 * @param end_Latitude
	 *            the end_Latitude to set
	 */
	public void setEndLatitude(String end_Latitude)
	{
		End_Latitude = end_Latitude;
	}
	
	/**
	 * @return the end_Longitude
	 */
	public String getEndLongitude()
	{
		return End_Longitude;
	}
	
	/**
	 * @param end_Longitude
	 *            the end_Longitude to set
	 */
	public void setEndLongitude(String end_Longitude)
	{
		End_Longitude = end_Longitude;
	}
	
	/**
	 * @return the end_Altitude
	 */
	public String getEndAltitude()
	{
		return End_Altitude;
	}
	
	/**
	 * @param end_Altitude
	 *            the end_Altitude to set
	 */
	public void setEndAltitude(String end_Altitude)
	{
		End_Altitude = end_Altitude;
	}
	
	/**
	 * @return the avg_Altitude
	 */
	public String getAvgAltitude()
	{
		return Avg_Altitude;
	}
	
	/**
	 * @param avg_Altitude
	 *            the avg_Altitude to set
	 */
	public void setAvgAltitude(String avg_Altitude)
	{
		Avg_Altitude = avg_Altitude;
	}
	
	public void setLocation_Name(String loc)
	{
		Location_Name = loc;
	}
	
	public void setLocation_Category(String locCat)
	{
		Location_Category = locCat;
	}
	
	public void setCity(String city)
	{
		City = city;
	}
	
	public void setCounty(String county)
	{
		County = county;
	}
	
	public void setCountry(String country)
	{
		Country = country;
	}
	
	public void setContinent(String continent)
	{
		Continent = continent;
	}
	
	// ////////////
	public int getUser_ID()
	{
		return User_ID;
	}
	
	public int getActitivity_ID()
	{
		return Activity_ID;
	}
	
	public long getTime_ID()
	{
		return Time_ID;
	}
	
	public Long getDate_ID()
	{
		return Date_ID;
	}
	
	public String trajectory_ID()
	{
		return Trajectory_ID;
	}
	
	/*
	 * public void getLocation_ID(int locationID) { Location_ID=locationID; }
	 * 
	 * public void getDuration(int duration) { Duration=duration; }
	 * 
	 * public void getFrequency(int frequency) { Frequency=frequency; }
	 */
	public String getUser_Name()
	{
		return User_Name;
	}
	
	/*
	 * public void getPersonality_Tags(String personalityTags) { Personality_Tags=personalityTags; }
	 * 
	 * public void getProfession(String profession ) { Profession=profession; }
	 * 
	 * public void getAge_Category(String ageCategory) { Age_Category=ageCategory; }
	 * 
	 * public void getUser_Age(int userAge) { User_Age=userAge; }
	 * 
	 * public void getActivity_Name(String activityName) { Activity_Name=activityName; }
	 * 
	 * public void getActivity_Category(String activityCategory) { Activity_Category=activityCategory; }
	 * 
	 * public void getTime_Category(String timeCategory) { Time_Category=timeCategory; }
	 * 
	 * 
	 * @SuppressWarnings("deprecation") public void getStart_Time(int hour, int min, int sec) { Start_Time=new Time(hour,min,sec); }
	 * 
	 * @SuppressWarnings("deprecation") public void getEnd_Time(int hour, int min, int sec) { End_Time=new Time(hour, min, sec); }
	 * 
	 * //year the year minus 1900; must be 0 to 8099. (Note that 8099 is 9999 minus 1900.)
	 * 
	 * @SuppressWarnings("deprecation") public void getStart_Date(int year, int month, int day) { Start_Date=new Date(year-1900, month-1, day); }
	 * 
	 * public void getWeek_Day(String weekDay) { Week_Day=weekDay; }
	 * 
	 * public void getMonth( int month) {
	 * 
	 * switch (month) { case 1: Month = "January"; break; case 2: Month = "February"; break; case 3: Month = "March"; break; case 4: Month = "April"; break; case 5: Month = "May";
	 * break; case 6: Month = "June"; break; case 7: Month = "July"; break; case 8: Month = "August"; break; case 9: Month = "September"; break; case 10: Month = "October"; break;
	 * case 11: Month = "November"; break; case 12: Month = "December"; break; default: Month = "Invalid month"; break; } }
	 * 
	 * public void getQuarter(int month) { if(month>=1 && month<=3) Quarter = "Q1"; else if(month>=4 && month<=6) Quarter = "Q2"; else if(month>=7 && month<=9) Quarter = "Q3"; else
	 * if(month>=10 && month<=12) Quarter = "Q4"; else Quarter = "Invalid Quarter"; }
	 * 
	 * 
	 * public void getWeek(int week) { Week=week; }
	 * 
	 * public void getYear(int year) { Year=year; }
	 * 
	 * public void getLatitude(String lat) { Latitude=lat; }
	 * 
	 * public void getLongitude(String lon) { Longitude=lon; }
	 * 
	 * public void getLocation_Name(String loc) { Location_Name=loc; }
	 * 
	 * public void getLocation_Category(String locCat) { Location_Category=locCat; }
	 * 
	 * public void getCity(String city) { City=city; }
	 * 
	 * public void getCounty(String county) { County=county; }
	 * 
	 * public void getCountry(String country) { Country=country; }
	 * 
	 * public void getContinent(String continent) { Continent=continent; }
	 */
}
