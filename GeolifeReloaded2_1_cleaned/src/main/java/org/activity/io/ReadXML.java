package org.activity.io;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ReadXML
{

	public Document loadXMLFromString(String xml) throws Exception
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xml));
		return builder.parse(is);
	}

	public static void main(String argv[])
	{
		try
		{

			String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
					+ "<reversegeocode timestamp='Thu, 22 Feb 18 20:54:05 +0000' attribution='Data © OpenStreetMap contributors, ODbL 1.0. http://www.openstreetmap.org/copyright' querystring='format=xml&amp;lat=40.774269&amp;lon=-89.603806&amp;zoom=18&amp;addressdetails=1'>\n"
					+ "<result place_id=\"192195412\" osm_type=\"way\" osm_id=\"21977522\" lat=\"40.773624\" lon=\"-89.603672\" boundingbox=\"40.773524,40.773724,-89.603772,-89.603572\">7099, North Kerwood Drive, Peoria, Peoria County, Illinois, 61614, United States of America</result><addressparts><house_number>7099</house_number><road>North Kerwood Drive</road><city>Peoria</city><county>Peoria County</county><state>Illinois</state><postcode>61614</postcode><country>United States of America</country><country_code>us</country_code></addressparts></reversegeocode>";
			// DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			// DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			// Document doc = dBuilder.parse(s);
			System.out.println(prettyPrint(s));

			ReadXML readXML = new ReadXML();
			Document doc = readXML.loadXMLFromString(s);
			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();
			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			String[] listOfTags = { "house_number", "road", "city", "county", "state", "postcode", "country",
					"country_code" };
			Map<String, String> allVals = new LinkedHashMap<>();
			for (String tagName : listOfTags)
			{
				allVals.put(tagName, doc.getElementsByTagName(tagName).item(0).getTextContent());
			}
			// NodeList nList = doc.getElementsByTagName("house_number");
			StringBuilder sb = new StringBuilder();
			allVals.entrySet().stream().forEachOrdered(e -> sb.append(e.getKey() + "-" + e.getValue() + "\n"));
			System.out.println(sb.toString() + "\n----------------------------");
			//
			// for (int temp = 0; temp < nList.getLength(); temp++)
			// {
			//
			// Node nNode = nList.item(temp);
			//
			// System.out.println("\nCurrent Element :" + nNode.getNodeName());
			//
			// // if (nNode.getNodeType() == Node.ELEMENT_NODE)
			// // {
			// //
			// // Element eElement = (Element) nNode;
			// //
			// // System.out.println("Staff id : " + eElement.getAttribute("id"));
			// // System.out.println("lat : " + eElement.getElementsByTagName("lat").item(0).getTextContent());
			// // System.out.println(
			// // "Last Name : " + eElement.getElementsByTagName("lastname").item(0).getTextContent());
			// // System.out.println(
			// // "Nick Name : " + eElement.getElementsByTagName("nickname").item(0).getTextContent());
			// // System.out.println("Salary : " + eElement.getElementsByTagName("salary").item(0).getTextContent());
			// //
			// // }
			// }
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * ref: https://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java
	 * 
	 * @param unformattedXml
	 * @return
	 */
	public static String prettyPrint(String unformattedXml)
	{
		try
		{
			Document document = parseXmlFile(unformattedXml);

			OutputFormat format = new OutputFormat(document);
			format.setLineWidth(65);
			format.setIndenting(true);
			format.setIndent(2);
			Writer out = new StringWriter();
			XMLSerializer serializer = new XMLSerializer(out, format);
			serializer.serialize(document);
			return out.toString();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * ref: https://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java
	 * 
	 * @param in
	 * @return
	 */
	private static Document parseXmlFile(String in)
	{
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(in));
			return db.parse(is);
		}
		catch (ParserConfigurationException e)
		{
			throw new RuntimeException(e);
		}
		catch (SAXException e)
		{
			throw new RuntimeException(e);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

}