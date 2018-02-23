package org.activity.io;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ReadXML
{

	public static Document loadXMLFromString(String xml) throws Exception
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

			Document doc = loadXMLFromString(s);
			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

			NodeList nList = doc.getElementsByTagName("staff");

			System.out.println("----------------------------");

			for (int temp = 0; temp < nList.getLength(); temp++)
			{

				Node nNode = nList.item(temp);

				System.out.println("\nCurrent Element :" + nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE)
				{

					Element eElement = (Element) nNode;

					System.out.println("Staff id : " + eElement.getAttribute("id"));
					System.out.println("lat : " + eElement.getElementsByTagName("lat").item(0).getTextContent());
					System.out.println(
							"Last Name : " + eElement.getElementsByTagName("lastname").item(0).getTextContent());
					System.out.println(
							"Nick Name : " + eElement.getElementsByTagName("nickname").item(0).getTextContent());
					System.out.println("Salary : " + eElement.getElementsByTagName("salary").item(0).getTextContent());

				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}