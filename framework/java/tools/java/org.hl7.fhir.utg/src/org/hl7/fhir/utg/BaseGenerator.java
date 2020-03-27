package org.hl7.fhir.utg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r4.model.CodeSystem.ConceptPropertyComponent;
import org.hl7.fhir.r4.model.CodeSystem.PropertyComponent;
import org.hl7.fhir.r4.model.CodeSystem.PropertyType;
import org.hl7.fhir.utilities.Utilities;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class BaseGenerator {

	protected String dest;
	protected Map<String, CodeSystem> csmap;
	protected Set<String> knownCS;
	protected Map<String, HashSet<String>> undefinedConceptProperties = new HashMap<String, HashSet<String>>();

	protected static final String INTERNAL_CS_OID_PREFIX = "2.16.840.1.113883.18.";
	protected static final String V3_INTERNAL_CS_OID_PREFIX = "2.16.840.1.113883.5.";
	protected static final String FHIR_CS_OID_PREFIX = "2.16.840.1.113883.4.642.1.";
	
	public BaseGenerator(String dest, Map<String, CodeSystem> csmap, Set<String> knownCS) {
		super();
		this.dest = dest;
		this.csmap = csmap;
		this.knownCS = knownCS;
		knownCS.add("http://snomed.info/sct");
		knownCS.add("http://loinc.org");
		knownCS.add("http://hl7.org/fhir/sid/cvx");
		knownCS.add("http://phdsc.org/standards/payer-typology.asp");
		knownCS.add("http://www.nlm.nih.gov/research/umls/rxnorm");
		knownCS.add("http://ncimeta.nci.nih.gov");
	}

	protected List<String> sorted(Set<String> keys) {
		List<String> res = new ArrayList<String>();
		res.addAll(keys);
		Collections.sort(res);
		return res;
	}

	protected static String csext(String name) {
		return "http://hl7.org/fhir/StructureDefinition/codesystem-" + name;
	}

	protected static String vsext(String name) {
		return "http://hl7.org/fhir/StructureDefinition/valueset-" + name;
	}

	protected static String resext(String name) {
		return "http://hl7.org/fhir/StructureDefinition/resource-" + name;
	}

	protected String makeSafeId(String s) {
		if (s.contains("(")) {
			s = s.replace('(', '-').replace(')', ' ').trim();
		}

		return Utilities.makeId(s);
	}

	protected String identifyOID(String oid) {
		if (Utilities.noString(oid))
			return null;
		
		if (OIDLookup.hasUrlOverride(oid)) 
			return OIDLookup.getUrl(oid);
		
		if (OIDLookup.noUrl(oid))
			return null;
		
		if ("SNOMEDCT".equals(oid))
			return "http://snomed.info/sct";
		if ("2.16.840.1.113883.6.96".equals(oid))
			return "http://snomed.info/sct";
		if ("2.16.840.1.113883.6.1".equals(oid))
			return "http://loinc.org";
		if ("2.16.840.1.113883.12.292".equals(oid))
			return "http://hl7.org/fhir/sid/cvx";
		if ("2.16.840.1.113883.3.221.5".equals(oid))
			return "http://phdsc.org/standards/payer-typology.asp";
		if ("2.16.840.1.113883.6.88".equals(oid))
			return "http://www.nlm.nih.gov/research/umls/rxnorm";

		if (csmap.containsKey(oid))
			return csmap.get(oid).getUrl();

		return "urn:oid:" + oid;
	}

	public String getDest() {
		return dest;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public Map<String, CodeSystem> getCsmap() {
		return csmap;
	}

	public void setCsmap(Map<String, CodeSystem> csmap) {
		this.csmap = csmap;
	}

	public static boolean isInternalOid(String oid) {
		return oid == null || oid.isEmpty() || oid.startsWith(INTERNAL_CS_OID_PREFIX);
	}

	public static boolean isV3Oid(String oid) {
		return oid != null && !oid.isEmpty() && oid.startsWith(V3_INTERNAL_CS_OID_PREFIX);
	}

	public static boolean isFhirOid(String oid) {
		return oid != null && !oid.isEmpty() && oid.startsWith(FHIR_CS_OID_PREFIX);
	}

	
	protected static Document removeXMLNSAttribute(Document doc)
			throws ParserConfigurationException, SAXException, IOException {
		NodeList nodeList = doc.getElementsByTagName("*");
		for (int i = 0; i < nodeList.getLength(); i++) {
			if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element ele = (Element) nodeList.item(i);
				NamedNodeMap nnm = ele.getAttributes();
				for (int a = nnm.getLength() - 1; a >= 0; a--) { // back to front because of remove in loop!
					Attr attr = (Attr) nnm.item(a);
					if (attr.getNodeName().startsWith("xmlns")) {
						ele.removeAttributeNode(attr);
					}
				}
			}
		}
		return doc;
	}

	protected static Document addListXMLNSAttribute(Document doc) {
		NodeList nodeList = doc.getElementsByTagName("List");
		for (int i = 0; i < nodeList.getLength(); i++) {
			if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element ele = (Element) nodeList.item(i);
				ele.setAttribute("this", "http://hl7.org/fhir");
			}
		}
		return doc;
	}
	
	protected static Document merge(String title, File... files) throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setIgnoringElementContentWhitespace(true);
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		final String ns = "http://hl7.org/fhir";
		
		//Document base = docBuilder.parse(files[0]);
		//Element rootNode = base.getDocumentElement();
		
		Document base = docBuilder.newDocument();
		Element listElement = base.createElementNS(ns, "List");
		base.appendChild(listElement);
		
		Element idElement = base.createElementNS(ns, "id");
		idElement.setAttribute("value", "id-here");
		listElement.appendChild(idElement);
		
		Element statusElement = base.createElement("status");
		statusElement.setAttribute("value", "current");
		listElement.appendChild(statusElement);
		
		Element modeElement = base.createElement("mode");
		modeElement.setAttribute("value", "working");
		listElement.appendChild(modeElement);
		
		Element titleElement = base.createElement("title");
		titleElement.setAttribute("value", title);
		listElement.appendChild(titleElement);
		
		
		
		for (int i = 1; i < files.length; i++) {
			Document merge = docBuilder.parse(files[i]);
			NodeList nextResults = merge.getDocumentElement().getElementsByTagName("entry");

			for (int j = 0; j < nextResults.getLength(); j++) {
				Element entryElement = (Element) nextResults.item(j);
				//kid.
				//kid = base.importNode(kid, true);
				//rootNode.appendChild(kid);
				entryElement.setAttribute("xmlns", ns);
				
				Node newEntryNode = base.importNode(entryElement, true);
				listElement.appendChild(newEntryNode);
				
			}
		}
		return base;
	}

	protected void findUndefinedConceptProperties(CodeSystem cs) {

		// Check for concept properties not defined in code system
		List<ConceptDefinitionComponent> concepts = cs.getConcept();
		for (ConceptDefinitionComponent concept : concepts) {
			List<ConceptPropertyComponent> conceptProperties = concept.getProperty();
			for (ConceptPropertyComponent conceptProperty : conceptProperties) {
				String propertyCode = conceptProperty.getCode();
				if (propertyCode != null && !propertyCode.isEmpty()) {
					if (!cs.hasPropertyCode(propertyCode)) {
						if (!undefinedConceptProperties.containsKey(cs.getName())) {
							undefinedConceptProperties.put(cs.getName(), new HashSet<String>());
						}
						undefinedConceptProperties.get(cs.getName()).add(propertyCode);
					}
				}
			}
		}
	}
	
	protected void reportUndefinedConceptProperties(String name) {
		if (undefinedConceptProperties.isEmpty()) {
			System.out.println("No undefined concept properties found in " + name);
		} else {
			for (String csName : undefinedConceptProperties.keySet()) {
				for (String propertyCode : undefinedConceptProperties.get(csName)) {
				    System.out.println("Concept property code not defined in " + name + ": " + csName + ": " + propertyCode);
				}
			}
		}
	}

	protected PropertyComponent addUTGConceptProperty(CodeSystem cs, String propertyCode) {
		return addUTGConceptProperty(cs, propertyCode, PropertyType.STRING);
	}
	
	protected PropertyComponent addUTGConceptProperty(CodeSystem cs, String propertyCode, PropertyType type) {
		return cs.addProperty()
				.setCode(propertyCode)
				.setUri(PropertyLookup.getPropertyUri(propertyCode))
				.setType(type)
				.setDescription(PropertyLookup.getPropertyDisplay(propertyCode));
	}
}
