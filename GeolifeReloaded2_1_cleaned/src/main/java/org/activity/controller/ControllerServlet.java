
// a full curtain over the thing just to prevent annoying compilation error

// package org.activity.controller;
//
// import org.activity.util.*;
// import org.activity.evaluation.RecommendationTestsMU;
// import org.activity.objects.ActivityObject;
// import org.activity.objects.AttributeControlObject;
// import org.activity.objects.UserDayTimeline;
// import org.activity.recomm.RecommendationMasterMU;
//
// import java.io.BufferedWriter;
// import java.io.FileWriter;
// import java.io.IOException;
// import java.io.File;
// import java.io.PrintWriter;
// import java.sql.Date;
// import java.sql.SQLException;
// import java.sql.Timestamp;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.LinkedHashMap;
//
// import javax.servlet.ServletContext;
// import javax.servlet.ServletException;
// import javax.servlet.ServletOutputStream;
// import javax.servlet.annotation.WebServlet;
// import javax.servlet.http.HttpServlet;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
//
// import org.json.JSONArray;
//
/// **
// * Servlet implementation class ControllerServlet
// */
// @WebServlet("/ControllerServlet")
// public class ControllerServlet extends HttpServlet
// {
// private static final long serialVersionUID = 1L;
//
// /**
// * @see HttpServlet#HttpServlet()
// */
// public ControllerServlet()
// {
// super();
// // TODO Auto-generated constructor stub
// }
//
// /**
// * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
// */
// protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
// {
// // System.out.println("inside do get");
// ;
// }
//
// /**
// * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
// */
// protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
// {
// System.out.println("inside do post");
//
// if (request.getParameter("aggregateBy") != null)
// {
// System.out.println("inside aggregate by ");
// String whereQueryString = request.getParameter("whereQueryStringPost");
// // %%System.out.println("where query string in controller servlet="+whereQueryString);
//
// String selectAttributeString = request.getParameter("selectStringPost");
// // %%System.out.println("select attribute string in controller servlet="+selectAttributeString);
//
// String aggregateByString = request.getParameter("aggregateBy");
//
// String aggregateMethodString = request.getParameter("aggregateMethod").trim();
// System.out.println("Aggregation method is" + aggregateMethodString);
// if (aggregateMethodString.equalsIgnoreCase("Max Frequency"))
// aggregateMethodString = "MaxFrequency";
// if (aggregateMethodString.equalsIgnoreCase("Above Threshold"))
// aggregateMethodString = "ThresholdFrequency";
//
// try
// {
// // System.out.println(ConnectDatabase.getInnerJoinString());
// JSONArray jsonArray = ConnectDatabase.getJSONArrayOfDataTable(selectAttributeString, whereQueryString, "");
//
// // jsonArray is actually an array of JSON objects
// HashMap<String, ArrayList<ActivityObject>> timeLines = TimelineUtils.createTimelinesFromJsonArray(jsonArray);
// System.out.println("QQQQQQQQ1 timeLines.size() =" + timeLines.size());
//
// TimelineUtils.traverseTimelines(timeLines);
// System.out.println("QQQQQQQQ2 timeLines.size() =" + timeLines.size());
//
// // Timestamp earliestTimestamp = UtilityBelt.getEarliestTimestamp(timeLines.get(0));
// // System.out.println("earlest time stamp="+earliestTimestamp);
// // ArrayList<String> aggregatedSplittedTimeline =
//
// String aggregatedTimelineJSON = UtilityBelt.aggregateTimelines(timeLines, aggregateByString, aggregateMethodString);// //"MaxFrequency");//"ThresholdFrequency");//
// System.out.println("QQQQQQQQ3 timeLines.size() =" + timeLines.size());
//
// // %%System.out.println(jsonArrayForDataTable.toString());
//
// // JSONArray jsonArrayForDataTable = jsonArray;
//
// // $$request.setAttribute("jsonStringAggregatedTimeline", aggregatedTimelineJSON);
//
// response.setContentType("text/plain");
// response.setContentLength(aggregatedTimelineJSON.getBytes().length);
//
// ServletOutputStream out = response.getOutputStream();
// // PrintWriter out = response.getWriter();
// out.print(aggregatedTimelineJSON);// .toString().replace('_',' '));
//
// out.close();
//
// }
// catch (Exception e)
// {
// e.printStackTrace();
// }
// }
//
// if (request.getParameter("selectStringPost") != null && request.getParameter("aggregateBy") == null && request.getParameter("createActivityEventObjects") == null)
// {
// String whereQueryString = request.getParameter("whereQueryStringPost");
// // %%System.out.println("where query string in controller servlet="+whereQueryString);
//
// String selectAttributeString = request.getParameter("selectStringPost");
// // %%System.out.println("select attribute string in controller servlet="+selectAttributeString);
//
// String orderByString = request.getParameter("orderByStringPost");
// try
// {
// // System.out.println(ConnectDatabase.getInnerJoinString());
// // JSONArray jsonArrayForDataTable = ConnectDatabase.getJSONArrayOfDataTable(selectAttributeString,whereQueryString,orderByString);
// // System.out.println(jsonArrayForDataTable.toString());
// // $$request.setAttribute("jsonStringForDataTable", jsonArrayForDataTable);
//
// // //////////////
// JSONArray jsonArrayForDataTable = ConnectDatabase.getJSONArrayOfDataTable(selectAttributeString, whereQueryString, orderByString);
// // System.out.println(jsonArrayForDataTable.toString());
//
// // ArrayList<ActivityObject> allActivityEvents= UtilityBelt.createActivityEventsFromJsonArray(jsonArrayForDataTable);
//
// // LinkedHashMap<String,LinkedHashMap<Date,UserDayTimeline>> userTimelines= UtilityBelt.createUserTimelinesFromActivityEvents(allActivityEvents);
//
// // ////////////
//
// response.setContentType("text/plain");
// response.setContentLength(jsonArrayForDataTable.toString().getBytes().length);
//
// ServletOutputStream out = response.getOutputStream();
// // PrintWriter out = response.getWriter();
// out.print(jsonArrayForDataTable.toString().replace('_', ' '));
// out.close();
//
// }
// catch (Exception e)
// {
// e.printStackTrace();
// }
// }
//
// /*
// * if(request.getParameter("selectStringPost")!=null && request.getParameter("aggregateBy")==null && request.getParameter("reduciblePost")!= null) { System.out.println("reducible
// post"); String whereQueryString = request.getParameter("whereQueryStringPost"); //%%System.out.println("where query string in controller servlet="+whereQueryString);
// *
// * String selectAttributeString = request.getParameter("selectStringPost"); //%%System.out.println("select attribute string in controller servlet="+selectAttributeString);
// *
// * try { //System.out.println(ConnectDatabase.getInnerJoinString()); JSONArray jsonArrayForDataTable =
// ConnectDatabase.getJSONArrayOfDataTable(selectAttributeString,whereQueryString); //%%System.out.println(jsonArrayForDataTable.toString());
// //$$request.setAttribute("jsonStringForDataTable", jsonArrayForDataTable); String jsonArrayForDataTableString=jsonArrayForDataTable.toString();
// *
// * jsonArrayForDataTableString =UtilityBelt.reduceJSONString(selectAttributeString,jsonArrayForDataTableString); //jsonArrayForDataTableString =
// jsonArrayForDataTableString.replace('_',' ');
// *
// *
// * response.setContentType("text/plain"); response.setContentLength(jsonArrayForDataTableString.getBytes().length);
// *
// * ServletOutputStream out=response.getOutputStream(); //PrintWriter out = response.getWriter(); out.print(jsonArrayForDataTableString); out.close();
// *
// * } catch(Exception e) { e.printStackTrace(); } }
// */
//
// if (request.getParameter("attributeControlFilterPost") != null)
// {
// String attributeControlNamePost = "";
// attributeControlNamePost = request.getParameter("attributeControlFilterPost");
//
// if (!(attributeControlNamePost.isEmpty()))
// {
// String htmlString = AttributeControlObject.getHTMLStringControlObject(attributeControlNamePost.trim());
//
// // $$request.setAttribute("htmlResponse", htmlString);
//
// response.setContentType("text/html");
// PrintWriter out = response.getWriter();
// out.println(htmlString);
// out.close();
// }
// }
//
// if (request.getParameter("getUserNames") != null)
// {
// // String attributeControlNamePost="";
// // attributeControlNamePost=request.getParameter("attributeControlFilterPost");
//
// // if(!(attributeControlNamePost.isEmpty()))
// // {
// String htmlString = AttributeControlObject.getHTMLStringControlObject("User_Name");
//
// // $$request.setAttribute("htmlResponse", htmlString);
//
// response.setContentType("text/html");
// PrintWriter out = response.getWriter();
// out.println(htmlString);
// out.close();
// // }
// }
//
// if (request.getParameter("createActivityEventObjectsAndRecommend") != null)
// {
// String whereQueryString = request.getParameter("whereQueryStringPost");
// // %%System.out.println("where query string in controller servlet="+whereQueryString);
//
// String selectAttributeString = request.getParameter("selectStringPost");
// // %%System.out.println("select attribute string in controller servlet="+selectAttributeString);
//
// String orderByString = request.getParameter("orderByStringPost");
//
// String dateAtRecomm = request.getParameter("dateAtRecommPost");
// String timeAtRecomm = request.getParameter("timeAtRecommPost");
// String weekDayAtRecomm = request.getParameter("weekDayAtRecommPost");
// String userAtRecomm = request.getParameter("userAtRecommPost");
//
// try
// {
// System.out.println("inside create>>>>");
// JSONArray jsonArray = ConnectDatabase.getJSONArrayOfDataTable(selectAttributeString, whereQueryString, orderByString);
// System.out.println(jsonArray.toString());
//
// ArrayList<ActivityObject> allActivityEvents = UtilityBelt.createActivityObjectsFromJsonArray(jsonArray);
//
// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> userTimelines = TimelineUtils.createUserTimelinesFromActivityObjects(allActivityEvents);
//
// System.out.println("userTimelines.size()=" + userTimelines.size());
//
// // UtilityBelt.traverseUserTimelines(userTimelines); //Debugging Check: OK Cheked again with timestamp: OK
// // UtilityBelt.traverseActivityEvents(allActivityEvents); //Debugging Check: OK
//
// // for actual recommendation
// // RecommendationMaster recommendationMaster=new RecommendationMaster(userTimelines,dateAtRecomm,timeAtRecomm,weekDayAtRecomm,userAtRecomm);
//
// // for testing
// RecommendationTestsMU recommendationsTest = new RecommendationTestsMU(userTimelines);// ,userAtRecomm);
//
// response.setContentType("text/plain");
// response.setContentLength(jsonArray.toString().getBytes().length);
//
// ServletOutputStream out = response.getOutputStream();
// out.print(jsonArray.toString().replace('_', ' '));
// out.close();
//
// }
// catch (Exception e)
// {
// e.printStackTrace();
// }
// }
//
// // else if attributeControlAggregationPost
// /*
// * if(request.getParameter("attributeControlAggregationPost")!=null) { String attributeControlNamePost="";
// attributeControlNamePost=request.getParameter("attributeControlAggregationPost");
// *
// * if(!(attributeControlNamePost.isEmpty())) { String htmlString=AttributeControlObject.getHTMLStringAggregationControlObject(attributeControlNamePost.trim());
// *
// * //$$request.setAttribute("htmlResponse", htmlString);
// *
// * response .setContentType ("text/html") ; PrintWriter out = response. getWriter () ; out.println (htmlString) ; out .close () ; } }
// */
//
// }
//
// }
