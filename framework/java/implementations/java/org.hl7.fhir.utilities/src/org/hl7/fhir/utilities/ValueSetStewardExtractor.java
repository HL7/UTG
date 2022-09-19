package org.hl7.fhir.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hl7.fhir.utilities.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ValueSetStewardExtractor {

	private static final String COREMIF_FILENAME = "C:/Users/dtriglianos/Documents/UTG/DEFN=UV=VO=1436-20190320.coremif";
	
	public static void main(String args[]) {
		
		List<String> stewardValues = new LinkedList<>();
		Document doc = null;
		
		try {
			
			File fXmlFile = new File(COREMIF_FILENAME);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
	
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
			
		Element root = doc.getDocumentElement();
		List<Element> conceptDomains = getElements(root, "valueSet");
		
		for (Element conceptDomain : conceptDomains) {
			List<Element> annotations = getElements(conceptDomain, "annotations");
			for (Element annotation : annotations) {
				List<Element> documentations = getElements(annotation, "documentation");
				for (Element documentation : documentations) {
					List<Element> descriptions = getElements(documentation, "description");
					for (Element description : descriptions) {
						List<Element> texts = getElements(description, "text");
						for (Element text : texts) {
							String desc = XMLUtil.htmlToXmlEscapedPlainText(text).trim();
							
							if (desc.toLowerCase().indexOf("steward:") != desc.toLowerCase().indexOf("steward:")) {
								System.out.println("Ack! multiple stewards!");
							}
							
							Map<String, String> properties = extractAdditionalPropertiesFromText(desc);
							if (!properties.isEmpty()) {
								String steward = properties.get("steward").toLowerCase();
								if (!stewardValues.contains(steward)) {
									stewardValues.add(steward);									
								}
								
							}
						}
					}
				}
			}
		}
		
		Collections.sort(stewardValues, String.CASE_INSENSITIVE_ORDER);
		for (String tag : stewardValues) {
			System.out.println(tag);
		}
			
		System.out.println("\nfinished");
	}
	
	private static List<Element> getElements(Element e, String name) {
		NodeList nodes = e.getElementsByTagName(name);
		return getElements(nodes);
	}
	
	private static List<Element> getElements(NodeList nodes) {
		List<Element> elements = new LinkedList<Element>();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				elements.add((Element) nodes.item(i));
			}
		}
		return elements;
	}
		
	private static List<String> extractTags(Element e) {
		List<String> tags = new LinkedList<String>();
		
		String text = getString(e);
		
		return tags;
	}
	
	private static String getString(Element element) {
        NodeList nodes = element.getChildNodes();

		for (int i = 0; i < nodes.getLength(); i++) {
			String value = nodes.item(i).getNodeValue();
			if (value != null && !value.trim().isEmpty()) {
				return value.trim();
			}
		}
        return null;
    }

	private static final Map<String, String> DEFINITION_TEXT_PROPERTY_TAGS = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			//put("open issue", "openIssue");
			//put("openissue", "openIssue");
			//put("deprecation comment", "deprecationInfo");
			//put("deprecationcomment", "deprecationInfo");
			put("steward", "steward");
		}
	};
	
	private static Map<String, String> extractAdditionalPropertiesFromText(String text) {
		Map<String, String> propertiesFromText = new HashMap<>();
		try {
			if (text != null && !text.isEmpty()) {
				BufferedReader reader = new BufferedReader(new StringReader(text));
				String line = reader.readLine();
				while (line != null) {
					line = line.trim();
					int colonIdx = line.indexOf(":");
					if (colonIdx >= 0) {
						String tag = line.substring(0, colonIdx).toLowerCase();
						if (DEFINITION_TEXT_PROPERTY_TAGS.containsKey(tag)) {
							String propertyName = DEFINITION_TEXT_PROPERTY_TAGS.get(tag);
							String propertyValue = line.substring(colonIdx+1).trim();
							propertiesFromText.put(propertyName, propertyValue);
						}
					}
					line = reader.readLine();
				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return propertiesFromText;
	}
}
