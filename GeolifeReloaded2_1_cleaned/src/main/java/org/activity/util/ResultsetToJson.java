package org.activity.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class ResultsetToJson
{
	public static JSONArray convertToJsonArray(ResultSet resultSet) throws SQLException, JSONException
	{
		JSONArray jsonArray = new JSONArray();
		ResultSetMetaData metaData = resultSet.getMetaData();

		int numColumns = metaData.getColumnCount();

		while (resultSet.next())
		{

			JSONObject jsonObject = new JSONObject();

			for (int i = 1; i < numColumns + 1; i++)
			{
				String column_name = metaData.getColumnName(i);

				if (metaData.getColumnType(i) == java.sql.Types.ARRAY)
				{
					jsonObject.put(column_name, resultSet.getArray(column_name));
				}
				else if (metaData.getColumnType(i) == java.sql.Types.BIGINT)
				{
					jsonObject.put(column_name, resultSet.getString(i));// resultSet.getInt(i));
				}
				else if (metaData.getColumnType(i) == java.sql.Types.BOOLEAN)
				{
					jsonObject.put(column_name, resultSet.getBoolean(column_name));
				}
				else if (metaData.getColumnType(i) == java.sql.Types.BLOB)
				{
					jsonObject.put(column_name, resultSet.getBlob(column_name));
				}
				else if (metaData.getColumnType(i) == java.sql.Types.DOUBLE)
				{
					jsonObject.put(column_name, resultSet.getDouble(column_name));
				}
				else if (metaData.getColumnType(i) == java.sql.Types.FLOAT)
				{
					jsonObject.put(column_name, resultSet.getFloat(column_name));
				}
				else if (metaData.getColumnType(i) == java.sql.Types.INTEGER)
				{
					jsonObject.put(column_name, resultSet.getInt(i));
				}
				else if (metaData.getColumnType(i) == java.sql.Types.NVARCHAR)
				{
					jsonObject.put(column_name, resultSet.getNString(column_name));
				}
				else if (metaData.getColumnType(i) == java.sql.Types.VARCHAR)
				{
					jsonObject.put(column_name, resultSet.getString(column_name));
				}
				else if (metaData.getColumnType(i) == java.sql.Types.TINYINT)
				{
					jsonObject.put(column_name, resultSet.getInt(i));
				}
				else if (metaData.getColumnType(i) == java.sql.Types.SMALLINT)
				{
					jsonObject.put(column_name, resultSet.getInt(i));
				}
				else if (metaData.getColumnType(i) == java.sql.Types.DATE)
				{
					jsonObject.put(column_name, resultSet.getDate(column_name));
				}
				else if (metaData.getColumnType(i) == java.sql.Types.TIMESTAMP)
				{
					jsonObject.put(column_name, resultSet.getTimestamp(column_name));
				}
				else
				{
					jsonObject.put(column_name, resultSet.getObject(column_name));
				}
			}

			jsonArray.put(jsonObject);
		}

		return jsonArray;
	}
}
