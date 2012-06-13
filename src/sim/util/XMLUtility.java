package sim.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLUtility {

	/**
	 *
	 * @param fileName
	 * @return
	 */
//	public static Document createDomDocument(String fileName) {
	public static Document createDomDocument(URI fileName) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		Document document = null;
		try {
//			File file = new File(fileName);
			java.io.InputStream is = fileName.toURL().openStream();

			DocumentBuilder builder = factory.newDocumentBuilder();
//			document = builder.parse(file);
			document = builder.parse(is);
		} catch (SAXParseException spe) {
			spe.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return document;
	}

	public static Document createDomDocument(String fileName) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		Document document = null;
		try {
			File file = new File(fileName);

			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(file);
		} catch (SAXParseException spe) {
			spe.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return document;
	}

	/**
	 *
	 * @param document
	 * @param tagName
	 * @return
	 */
	public static String getTagValueAsString(Document document, String tagName) {
		if (document == null || tagName == null || tagName.length() == 0) return null;

		String tagValue = null;
		NodeList nlist = null;
		Element element = null;

		Element root = document.getDocumentElement();

		nlist = root.getElementsByTagName(tagName);
		element = (Element)nlist.item(0);
		if (element.hasChildNodes()) {
			tagValue = element.getFirstChild().getNodeValue();
		}
		return tagValue;
	}

	/**
	 *
	 * @param document
	 * @param tagName
	 * @return
	 */
	public static int getTagValueAsInt(Document document, String tagName) {
		int tagValue = -1;

//		if (document == null || tagName == null || tagName.length() == 0) return tagValue;
//
//		NodeList nlist = null;
//		Element element = null;
//
//		Element root = document.getDocumentElement();
//
//		nlist = root.getElementsByTagName(tagName);
//		element = (Element)nlist.item(0);
//		if (element.hasChildNodes()) {
//			String tempValue = element.getFirstChild().getNodeValue();
//			tagValue = Integer.parseInt(tempValue);
//		}
		String tempVal = getTagValueAsString(document, tagName);
		tagValue = Integer.parseInt(tempVal);

		return tagValue;
	}

	/**
	 *
	 * @param document
	 * @param tagName
	 * @return
	 */
	public static long getTagValueAsLong(Document document, String tagName) {
		long tagValue = -1;

//		if (document == null || tagName == null || tagName.length() == 0) return tagValue;
//
//		NodeList nlist = null;
//		Element element = null;
//
//		Element root = document.getDocumentElement();
//
//		nlist = root.getElementsByTagName(tagName);
//		element = (Element)nlist.item(0);
//		if (element.hasChildNodes()) {
//			String tempValue = element.getFirstChild().getNodeValue();
//			tagValue = Long.parseLong(tempValue);
//		}

		String tempVal = getTagValueAsString(document, tagName);
		tagValue = Long.parseLong(tempVal);

		return tagValue;
	}

	public static double getTagValueAsDouble(Document document, String tagName) {
		double tagValue = -1.0;

		String tempVal = getTagValueAsString(document, tagName);
		tagValue = Double.parseDouble(tempVal);

		return tagValue;
	}

	public static float getTagValueAsFloat(Document document, String tagName) {
		float tagValue = -1.0f;

		String tempVal = getTagValueAsString(document, tagName);
		tagValue = Float.parseFloat(tempVal);

		return tagValue;
	}

	public static boolean getTagValueAsBoolean(Document document, String tagName) {
		boolean tagValue = false;

		String tempVal = getTagValueAsString(document, tagName);
		tagValue = Boolean.parseBoolean(tempVal);

		return tagValue;
	}

}
