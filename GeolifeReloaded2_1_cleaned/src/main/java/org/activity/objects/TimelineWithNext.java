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
	private static final long serialVersionUID = 1L;
	/**
	 * currently the nextActivityObject is set to be always a valid ActivityObject
	 */
	ActivityObject nextActivityObject; //
	/**
	 * -1 means 'not determined yet', '0' means 'was not invalid', '1' means 'was invalid'
	 */
	int immediateNextActivityIsInvalid = -1; //

	/**
	 * Create Timeline from given Activity Objects and the given next Activity Object to be considered
	 * 
	 * 
	 * @param activityObjects
	 * @param nextActivityObject
	 * @param shouldBelongToSingleDay
	 * @param shouldBelongToSingleUser
	 */
	public TimelineWithNext(ArrayList<ActivityObject> activityObjects, ActivityObject nextActivityObject,
			boolean shouldBelongToSingleDay, boolean shouldBelongToSingleUser)
	{
		super(activityObjects, shouldBelongToSingleDay, shouldBelongToSingleUser);
		this.nextActivityObject = nextActivityObject;
	}

	public ActivityObject getNextActivityObject()
	{
		return this.nextActivityObject;
	}

	// public List<ActivityObject> getActivityObjectsInTimelineWithNext()
	// {
	// ArrayList<ActivityObject> a = new ArrayList(this.getActivityObjectsInTimeline());
	// a.add(nextActivityObject);
	// return null;
	// }

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
