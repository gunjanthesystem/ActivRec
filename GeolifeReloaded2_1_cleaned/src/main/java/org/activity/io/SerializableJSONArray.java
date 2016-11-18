package org.activity.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;

public class SerializableJSONArray implements Serializable

{
	private static final long serialVersionUID = 1L;
	private transient JSONArray jsonArray;
	
	public SerializableJSONArray(JSONArray jsonArray)
	{
		this.jsonArray = jsonArray;
	}
	
	public JSONArray getJSONArray()
	{
		return jsonArray;
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException
	{
		oos.defaultWriteObject();
		oos.writeObject(jsonArray.toString());
	}
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException, JSONException
	{
		ois.defaultReadObject();
		jsonArray = new JSONArray((String) ois.readObject());
	}
	
	public String toString()
	{
		StringBuffer s = new StringBuffer();
		
		for (int i = 0; i < jsonArray.length(); i++)
		{
			try
			{
				s.append(jsonArray.get(i));
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		return s.toString();// jsonArray.toString();
	}
}
