package org.activity.util;

import java.util.ArrayList;

import org.activity.objects.AttributeControlObject;
import org.activity.objects.DimensionControlObject;

public class MetaBase
{
	static ArrayList<DimensionControlObject> allDimensionControlObjects;

	public static void setAllDimensionControlObjects(ArrayList<DimensionControlObject> allDimensionControlObjectssss)
	{
		allDimensionControlObjects = allDimensionControlObjectssss;
		System.out.println("inside setting dimension control objects");
	}

	public static AttributeControlObject identifyAttribute(String attributeName)
	{
		AttributeControlObject targetAttributeControlObject = new AttributeControlObject();
		System.out.println("inside identify AttributeControl:  Name to search for= " + attributeName);
		for (DimensionControlObject dimensionControlObject : allDimensionControlObjects)
		{
			ArrayList<AttributeControlObject> attributeControlObjects = dimensionControlObject
					.getAttributeControlObjects();

			// System.out.println("inside identify AttributeControl: current dimension
			// search"+dimensionControlObject.getDimensionName());

			for (AttributeControlObject attributeControlObject : attributeControlObjects)
			{
				// System.out.println("inside identify AttributeControl: current attribute to
				// see"+attributeControlObject.getName());
				if (attributeControlObject.getName().equalsIgnoreCase(attributeName))
				{
					targetAttributeControlObject = attributeControlObject;
					System.out.println("AttributeControl identified=" + attributeControlObject.getName());
					break;
				}
			}
		}

		return targetAttributeControlObject;
	}
}
