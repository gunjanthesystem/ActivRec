package org.activity.objects;

//import java.rmi.server.UID;
import java.util.ArrayList;

/**
 * Timeline is a chronological sequence of Activity Objects with the next Activity Object to be considered after this
 * sequence of Activity Objects
 * 
 * @author gunjan
 *
 */
public class TimelineWithNext extends Timeline
{
	ActivityObject nextActivityObject; // currently the nextActivityObject is set to be always a valid ActivityObject
	int immediateNextActivityIsInvalid = -1; // -1 means 'not determined yet', '0' means 'was not invalid', '1' means
												// 'was invalid'

	/**
	 * Create Timeline from given Activity Objects and the given next Activity Object to be considered
	 * 
	 * @param activityObjects
	 */
	public TimelineWithNext(ArrayList<ActivityObject> activityObjects, ActivityObject nextActivityObject)
	{
		super(activityObjects);
		this.nextActivityObject = nextActivityObject;
	}

	public ActivityObject getNextActivityObject()
	{
		return this.nextActivityObject;
	}

	/**
	 * Sets whether the immediate next Activity Object is valid or not
	 * 
	 * @param code
	 *            -1 means 'not determined yet', '0' means 'was not invalid', '1' means 'was invalid'
	 */
	public void setImmediateNextActivityIsInvalid(int code)
	{
		this.immediateNextActivityIsInvalid = code;
	}

	public int getImmediateNextActivityInvalid()
	{
		return immediateNextActivityIsInvalid;
	}
}
