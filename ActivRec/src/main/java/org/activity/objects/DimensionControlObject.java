package org.activity.objects;

import java.sql.SQLException;
import java.util.ArrayList;

import org.activity.util.ConnectDatabase;

public class DimensionControlObject
{
	String name;
	boolean hasClicked;
	ArrayList<AttributeControlObject> attributeControlObjects;

	public static ArrayList<DimensionControlObject> createDimensionControlObjects()
	{
		ArrayList<DimensionControlObject> dimensionControlObjects = new ArrayList<DimensionControlObject>();

		try
		{
			ArrayList<String> dimensionNames = ConnectDatabase.getDimensionNamesFromDatabase();

			for (String dimensionName : dimensionNames)
			{
				DimensionControlObject dimensionControlObject = new DimensionControlObject();
				dimensionControlObject.name = dimensionName;

				dimensionControlObject.attributeControlObjects = createAttributeControlObjects(dimensionName.trim());
				dimensionControlObjects.add(dimensionControlObject);
			}
		}

		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return dimensionControlObjects;
	}

	public ArrayList<String> getAttributeObjectNames()
	{
		ArrayList<String> attributeObjectNames = new ArrayList<String>();
		for (AttributeControlObject attributeControlObject : this.attributeControlObjects)
		{
			attributeObjectNames.add(attributeControlObject.getName());
		}
		return attributeObjectNames;
	}

	public ArrayList<AttributeControlObject> getAttributeControlObjects()
	{
		return this.attributeControlObjects;
	}

	public static ArrayList<AttributeControlObject> createAttributeControlObjects(String dimensionName)
	{
		ArrayList<AttributeControlObject> attributeControlObjects = new ArrayList<AttributeControlObject>();
		System.out.println("inside create Att control objs");
		try
		{
			ArrayList<ArrayList<String>> listDimensionAttributes = ConnectDatabase
					.getDimensionAttributes(dimensionName);

			for (ArrayList<String> dimensionAttribute : listDimensionAttributes)
			{
				AttributeControlObject attributeControlObject = new AttributeControlObject();

				attributeControlObject.setName(dimensionAttribute.get(0));
				System.out.println("inside createAttri " + dimensionAttribute.get(0));
				attributeControlObject.setDatatype(dimensionAttribute.get(1));

				attributeControlObject.setHasOperatorSelection(dimensionAttribute.get(1));

				attributeControlObject.setHierarchy(dimensionAttribute.get(2));
				attributeControlObject.setBelongsToDimension(dimensionName);
				attributeControlObjects.add(attributeControlObject);
			}
		}

		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return attributeControlObjects;
	}

	public String getDimensionName()
	{
		return this.name;
	}
}
