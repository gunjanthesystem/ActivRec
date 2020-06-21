/*
 * 
 * CURRENTLY THIS CLASS IS NOT BEING USED AND SHOULD BE REMOVED IN THE PRODUCTION RELEASE
 */

package org.activity.objects;

import java.sql.ResultSet;

import org.activity.util.ConnectDatabase;

public class UserObject
{
	String userID, userName, personalityTags, profession, ageCategory;
	int userAge;

	/**
	 * 
	 * @param userID
	 * @param userName
	 * @param personalityTags
	 * @param profession
	 * @param ageCategory
	 * @param userAge
	 */
	public UserObject(String userID, String userName, String personalityTags, String profession, String ageCategory,
			int userAge)
	{
		this.userID = userID;
		this.userName = userName;
		this.personalityTags = personalityTags;
		this.profession = profession;
		this.ageCategory = ageCategory;
		this.userAge = userAge;
	}

	/**
	 * Populate the values for all the attributes of User Dimension for a given userID
	 * 
	 * @param userID
	 */
	UserObject(String userID)
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

	public UserObject()
	{

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

	public String getUserID()
	{
		return userID;
	}

	public String getUserName()
	{
		return userName;
	}

	public String getPersonalityTags()
	{
		return personalityTags;
	}

	public String getProfession()
	{
		return profession;
	}

	public String getAgeCategory()
	{
		return ageCategory;
	}

	public int getUserAge()
	{
		return userAge;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ageCategory == null) ? 0 : ageCategory.hashCode());
		result = prime * result + ((personalityTags == null) ? 0 : personalityTags.hashCode());
		result = prime * result + ((profession == null) ? 0 : profession.hashCode());
		result = prime * result + userAge;
		result = prime * result + ((userID == null) ? 0 : userID.hashCode());
		result = prime * result + ((userName == null) ? 0 : userName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		UserObject other = (UserObject) obj;
		if (ageCategory == null)
		{
			if (other.ageCategory != null) return false;
		}
		else if (!ageCategory.equals(other.ageCategory)) return false;
		if (personalityTags == null)
		{
			if (other.personalityTags != null) return false;
		}
		else if (!personalityTags.equals(other.personalityTags)) return false;
		if (profession == null)
		{
			if (other.profession != null) return false;
		}
		else if (!profession.equals(other.profession)) return false;
		if (userAge != other.userAge) return false;
		if (userID == null)
		{
			if (other.userID != null) return false;
		}
		else if (!userID.equals(other.userID)) return false;
		if (userName == null)
		{
			if (other.userName != null) return false;
		}
		else if (!userName.equals(other.userName)) return false;
		return true;
	}

}
