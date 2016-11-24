package org.activity.sanityChecks;
//package org.activity.util;
//
//import java.sql.Timestamp;
//import java.util.ArrayList;
//
//import org.activity.distances.HJEditDistance;
//
//public class TestActivityEvents
//{
//	public static void main(String args[])
//	{
//		ActivityObject AE11 = new ActivityObject("A", "loc", 60 * 60, new Timestamp(2014 - 1900, 4 - 1, 12, 7, 0, 0, 0));// ActivityObject(String activityName, String location, long durationInSeconds,
//																															// Timestamp startTimeStamp)
//		ActivityObject AE12 = new ActivityObject("X", "loc", 45 * 60, new Timestamp(2014 - 1900, 4 - 1, 12, 8, 0, 0, 0));
//		ActivityObject AE13 = new ActivityObject("B", "loc", 50 * 60, new Timestamp(2014 - 1900, 4 - 1, 12, 8, 45, 0, 0));
//		
//		ArrayList<ActivityObject> TL1 = new ArrayList<ActivityObject>();
//		TL1.add(AE11);
//		TL1.add(AE12);
//		TL1.add(AE13);
//		
//		ActivityObject AE21 = new ActivityObject("A", "loc", 130 * 60, new Timestamp(2014 - 1900, 4 - 1, 12, 7, 0, 0, 0));// ActivityObject(String activityName, String location, long
//																															// durationInSeconds, Timestamp startTimeStamp)
//		ActivityObject AE22 = new ActivityObject("B", "loc", 40 * 60, new Timestamp(2014 - 1900, 4 - 1, 12, 9, 10, 0, 0));
//		
//		ActivityObject AE23 = new ActivityObject("D", "loc", 40 * 60, new Timestamp(2014 - 1900, 4 - 1, 12, 9, 10, 0, 0));
//		
//		ArrayList<ActivityObject> TL2 = new ArrayList<ActivityObject>();
//		TL2.add(AE21);
//		TL2.add(AE22);
//		// TL2.add(AE12);
//		// TL2.add(AE23);
//		// TL2.add(AE22);//TL2.add(AE13);
//		
//		// ActivityObject a1= new ActivityObject("Unknown","k",58549,new Timestamp(0,0,0,0,0,0,0));
//		// ActivityObject a2= new ActivityObject("Others","k",15,new Timestamp(0,0,0,16,15,0,0));
//		// ActivityObject a3= new ActivityObject("Computer","k",13,new Timestamp(0,0,0,16,16,4,0));
//		//
//		// ActivityObject b1= new ActivityObject("Unknown","k",57379,new Timestamp(0,0,0,0,0,0,0));
//		// ActivityObject b2= new ActivityObject("Others","k",22,new Timestamp(0,0,0,15,56,19,0));
//		// ActivityObject b3= new ActivityObject("Computer","k",7241,new Timestamp(0,0,15,56,41,0,0));
//		//
//		// TL1.add(a1);TL1.add(a2);TL1.add(a3);
//		// TL2.add(b1);TL2.add(b2);TL2.add(b3);
//		HJEditDistance s = new HJEditDistance();
//		s.getHJEditDistanceWithTrace(TL1, TL2);
//		
//		//
//		// Activity Events 1:
//		// <Unknown 2014-04-14 00:00:00.0 58549>,<Others 2014-04-14 16:15:49.0 15>,<Computer 2014-04-14 16:16:04.0 13>,
//		// Activity Events 2:
//		// <Unknown 2014-03-04 00:00:00.0 57379>,<Others 2014-03-04 15:56:19.0 22>,<Computer 2014-03-04 15:56:41.0 7241>,
//		
//	}
// }
