package org.activity.objects;

import java.io.Serializable;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.activity.util.ConnectDatabase;

public class Dimension implements Serializable
{
	String dimensionName;
	HashMap<String, Object> attributeNameValuePairs; // eg., ("User_Age","2"), ("User_Name","Tessa")
	HashMap<String, String> attributeNameDatatypePairs; // eg., "User_Name","String"
	HashMap<String, String> attributeNameHierarchyPairs; // not used currently

	public HashMap<String, Object> getAttributeNameValuePairs()
	{
		return this.attributeNameValuePairs;
	}

	/**
	 * populate the values for all the attributes of Dimension for a given dimensionID name and value
	 * 
	 * @param dimensionName
	 *            // can get this from meta table, like User_Dimension
	 * @param dimensionID
	 *            // value of User_ID
	 */
	Dimension(String dimensionName, String dimensionPrimaryKeyValue)
	{
		// System.out.println("\n\nInside Dimension constructor for "+dimensionName);//@toremoveatruntime
		this.dimensionName = dimensionName;

		attributeNameValuePairs = new HashMap<String, Object>();
		attributeNameDatatypePairs = new HashMap<String, String>();
		attributeNameHierarchyPairs = new HashMap<String, String>();

		String dimensionPrimaryKey = ConnectDatabase.getPrimaryKeyName(dimensionName);

		try
		{
			// Check if resultSet is null: TO DO later, problem resultSet returned from executeQuery is never null
			// /
			// Populate the attribute names
			// ArrayList of Attributes of this dimension. Each ArrayList element is an ArrayList of attribute's
			// parameters. The attribute parameters are 'attribute name','datatype'
			// and 'hierarchy' in that order.
			/*
			 * example of dimensionAttributes, (User_Age, Int, h1), (User_Name, String, h2) , .....
			 */
			ArrayList<ArrayList<String>> dimensionAttributeInfos = ConnectDatabase
					.getDimensionAttributes(dimensionName);
			// System.out.println("dimensionAttributeInfos size "+dimensionAttributeInfos.size());//@toremoveatruntime
			// LOGGSystem.out.println("Got attributes for dimension="+dimensionName+" no. of
			// attributes="+dimensionAttributeInfos.size());

			for (int i = 0; i < dimensionAttributeInfos.size(); i++)
			{
				// attributeNameValuePairs.put(dimensionAttributes.get(i).get(0),new String()); // (User_Id, 2)
				// LOGG< System.out.println("Writing "+i+"th Attribute");

				ArrayList<String> dimensionAttributeInfo = dimensionAttributeInfos.get(i); // example (User_Name,
																							// String, h2)

				// LOGG
				// System.out.println("this attribute information is of size="+dimensionAttributeInfo.size());
				// System.out.println("this attribute information is ="+dimensionAttributeInfo.toString());

				attributeNameDatatypePairs.put(dimensionAttributeInfo.get(0), dimensionAttributeInfo.get(1)); // (User_ID,String)

				attributeNameHierarchyPairs.put(dimensionAttributeInfo.get(0), dimensionAttributeInfo.get(2));
			}

			// SELECT STRING, FROM STRING, WHERESTRING
			// Example: Select * from user_dimension_table where User_ID =2,
			// Select * from location_dimension_table where Location_ID=110
			ResultSet resultSet = ConnectDatabase.getSQLResultSet(" * ", dimensionName.toLowerCase() + "_table ",
					dimensionPrimaryKey + " = " + dimensionPrimaryKeyValue);// "User_ID = "+userID);

			// while(resultSet.next()) // the while loops only once, we are using loops to check that there are not more
			// than one rows
			// {
			// System.out.println("resultSet.next()");
			// }
			// /////
			// ResultSetMetaData rsmd = resultSet.getMetaData();
			// int columnCount = rsmd.getColumnCount();
			//
			// // The column count starts from 1
			// for (int i = 1; i < columnCount + 1; i++ )
			// {
			// String name = rsmd.getColumnName(i);
			// System.out.println("- "+name);
			// // Do stuff with name
			// }
			// /////
			// System.out.println(resultSet.toString());
			// Populate the values for the attribute names
			int countCheck = 0; // If all is correct then, the result set should contain only one row as we are
								// selecting by a primary key value

			while (resultSet.next()) // the while loops only once, we are using loops to check that there are not more
										// than one rows
			{
				// System.out.println("!!Check point!!");
				for (Map.Entry<String, String> entry : attributeNameDatatypePairs.entrySet())
				{
					String datatype = entry.getValue().trim();
					// System.out.print(entry.getKey()+" "+entry.getValue()+" ");//@toremoveatexecution
					// System.out.println(datatype);//@toremoveatexecution

					if (datatype.equalsIgnoreCase("Varchar"))
					{
						String attributeValue = resultSet.getString(entry.getKey()); // example:
																						// resultSet.getString("Use_Name")
						attributeNameValuePairs.put(entry.getKey(), attributeValue); // ("User_Name", "Tessa")
					}

					else if (datatype.equalsIgnoreCase("int"))
					{
						Integer attributeValue = new Integer(resultSet.getInt(entry.getKey()));
						attributeNameValuePairs.put(entry.getKey(), attributeValue);
					}

					else if (datatype.equalsIgnoreCase("Date"))
					{
						Date attributeValue = resultSet.getDate(entry.getKey());
						// System.out.println("Date encountered: value
						// ="+attributeValue.toString());////@toremoveatruntime
						attributeNameValuePairs.put(entry.getKey(), attributeValue);
					}

					else if (datatype.equalsIgnoreCase("Time"))
					{
						Time attributeValue = resultSet.getTime(entry.getKey());
						// System.out.println("Time encountered: value
						// ="+attributeValue.toString());////@toremoveatruntime
						attributeNameValuePairs.put(entry.getKey(), attributeValue);
					}
				}

				countCheck++;
			}

			if (countCheck > 1)
			{
				System.err.println(
						"Error in Dimension constructor: more than one rows returned from database for dimension="
								+ dimensionName + " for a give primary key value");
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			// System.exit(10);
		}
	}

	public Object getValueOfDimensionAttribute(String attributeName)
	{
		// System.out.println("Value of dimension attribute "+attributeName+" is
		// "+this.attributeNameValuePairs.get(attributeName));
		return this.attributeNameValuePairs.get(attributeName);
	}

	public String getDimensionName()
	{
		return this.dimensionName;
	}

	public void traverseDimensionAttributeNameValuepairs()
	{
		System.out.println("---Dimension Name: " + this.dimensionName + "---------");
		for (Map.Entry<String, Object> entry : this.attributeNameValuePairs.entrySet())
		{
			System.out.print(" >>" + entry.getKey() + " : " + entry.getValue() + " ");
		}
		System.out.println("-----");
	}

}
