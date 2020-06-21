package org.activity.util;

import java.util.ArrayList;

/**
 * 
 * @author gunjan
 *
 */
public class HTMLStrings
{
	/**
	 * 
	 * @param attributeName
	 * @param dimensionName
	 * @return
	 */
	public static String getHTMLOptionValueVarchar(String attributeName /* varcharColumnName */,
			String dimensionName /* String dataTableName */) // like varcharColumnName = user_name
	{
		String htmlString = "";
		try
		{
			String orderString = "";
			if (attributeName.equalsIgnoreCase("Week_Day"))
			{
				orderString = "ORDER BY FIELD(Week_Day, 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')";
			}
			ArrayList<String> column = ConnectDatabase.getSQLStringResultSingleColumn(
					"SELECT DISTINCT(" + attributeName + ") from " + ConnectDatabase.getDatabaseName() + "."
							+ dimensionName.toLowerCase() + "_table " + orderString);// +" ORDER BY
																						// "+varcharColumnName+";");//getColumnFromActivityLogDatabase(varcharColumnName);
			for (int i = 0; i < column.size(); i++)
			{
				htmlString = htmlString + "<option value=\"" + column.get(i) + "\">" + column.get(i) + "</option>";
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
			System.out.print("exception in getHTMLOptionValueVarchar");
		}

		return htmlString;
	}

	public static String getStringForTimeline(String jsonText)
	{
		String stringForTimeline = "";
		// System.out.println(">>>"+jsonText);
		return stringForTimeline;
	}
}
