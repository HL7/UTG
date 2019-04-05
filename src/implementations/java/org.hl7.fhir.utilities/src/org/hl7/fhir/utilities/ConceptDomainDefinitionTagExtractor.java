package org.hl7.fhir.utilities;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConceptDomainDefinitionTagExtractor {

	private static final String COREMIF_FILENAME = "C:/Users/dtriglianos/Documents/UTG/DEFN=UV=VO=1436-20190320.coremif";
	
	public static void main(String args[]) {
		
		//Map<String, ExternalProvider> providers = new HashMap<>();
		List<String> bTags = new LinkedList<String>();
		List<String> iTags = new LinkedList<String>();
		List<String> allTags = new LinkedList<String>();
		
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
		List<Element> conceptDomains = getElements(root, "conceptDomain");
		
		for (Element conceptDomain : conceptDomains) {
			List<Element> annotations = getElements(conceptDomain, "annotations");
			for (Element annotation : annotations) {
				List<Element> documentations = getElements(annotation, "documentation");
				for (Element documentation : documentations) {
					List<Element> definitions = getElements(documentation, "definition");
					for (Element definition : definitions) {
						//List<Element> texts = getElements(definition, "text");
						//for (Element text : texts) {
							//List<Element> pList = getElements(text, "p");
							//for (Element p : pList) {
								List<Element> bList = getElements(definition, "b");
								for (Element b : bList) {
									String tag = (" " + getString(b)).trim();
									if (!tag.isEmpty()) {
										if (!bTags.contains(tag)) {
											bTags.add(tag.trim());
										}
										if (!allTags.contains(tag)) {
											allTags.add(tag.trim());
										}
									}
								}

								List<Element> iList = getElements(definition, "i");
								for (Element i : iList) {
									String tag = (" " + getString(i)).trim();
									if (!tag.isEmpty()) {
										if (!iTags.contains(tag)) {
											iTags.add(tag.trim());
										}
										if (!allTags.contains(tag)) {
											allTags.add(tag.trim());
										}
									}
								}
							//}
						//}
					}
				}
			}
		}
		
		Collections.sort(iTags, String.CASE_INSENSITIVE_ORDER);
		Collections.sort(bTags, String.CASE_INSENSITIVE_ORDER);
		Collections.sort(allTags, String.CASE_INSENSITIVE_ORDER);
		for (String tag : allTags) {
			System.out.println(tag);
		}
			
		System.out.println("\ni tags:");
		for (String tag : iTags) {
			System.out.println(" " + tag);
		}
			
		System.out.println("\nb tags:");
		for (String tag : bTags) {
			System.out.println(" " + tag);
		}
			
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
}
