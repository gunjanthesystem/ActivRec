/*
 * 
 * CURRENTLY THIS CLASS IS NOT BEING USED AND SHOULD BE REMOVED IN THE PRODUCTION RELEASE
 */

package org.activity.objects;

import java.sql.ResultSet;

import org.activity.util.ConnectDatabase;

public class UserDimension
{
	String userID, userName, personalityTags, profession, ageCategory;
	int userAge;
	
	/**
	 * Populate the values for all the attributes of User Dimension for a given userID
	 * 
	 * @param userID
	 */
	UserDimension(String userID)
	{
		
		try
		{
			ResultSet resultSet = ConnectDatabase.getSQLResultSet("*", "user_dimension", "User_ID = " + userID);
			
			// Check if resultSet is null: TO DO later, problem resultSet returned from executeQuery is never null
			// /
			
			this.userID = userID;
			this.userName = resultSet.getString("User_Name");
			this.personalityTags = resultSet.getString("Personality_Tags");
			this.profession = resultSet.getString("Profesion");
			this.ageCategory = resultSet.getString("Age_Category");
			
			this.userAge = resultSet.getInt("User_Age");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void setUserID(String userID)
	{
		this.userID = userID;
	}
	
	public void setUserName(String userName)
	{
		this.userName = userName;
	}
	
	public void setPersonalityTags(String personalityTags)
	{
		this.personalityTags = personalityTags;
	}
	
	public void setProfession(String profession)
	{
		this.profession = profession;
	}
	
	public void setAgeCategory(String ageCategory)
	{
		this.ageCategory = ageCategory;
	}
	
	public void setUserAge(int age)
	{
		this.userAge = age;
	}
	
}
