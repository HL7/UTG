package org.hl7.fhir.utg.v3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemContentMode;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionDesignationComponent;
import org.hl7.fhir.r4.model.CodeSystem.ConceptPropertyComponent;
import org.hl7.fhir.r4.model.CodeSystem.PropertyComponent;
import org.hl7.fhir.r4.model.CodeSystem.PropertyType;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Factory;
import org.hl7.fhir.r4.model.ListResource;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.Narrative.NarrativeStatus;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.r4.model.ValueSet.ConceptSetFilterComponent;
import org.hl7.fhir.r4.model.ValueSet.FilterOperator;
import org.hl7.fhir.utg.BaseGenerator;
import org.hl7.fhir.utg.external.ExternalProvider;
import org.hl7.fhir.utg.fhir.ListResourceExt;
import org.hl7.fhir.utilities.FolderNameConstants;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.hl7.fhir.utilities.xhtml.XhtmlParser;
import org.hl7.fhir.utilities.xml.XMLUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class V3SourceGenerator extends BaseGenerator {

	public V3SourceGenerator(String dest, Map<String, CodeSystem> csmap, Set<String> knownCS, Map<String, ExternalProvider> externalProviders) {
		super(dest, csmap, knownCS);
		this.externalProviders = externalProviders;
	}

	private Element mif;
	private List<ConceptDomain> v3ConceptDomains = new ArrayList<ConceptDomain>();
	private Set<String> notations = new HashSet<String>();
	private Set<String> systems = new HashSet<String>();
	private Map<String, ExternalProvider> externalProviders;
	
	private static final Map<String, String> DEFINITION_TEXT_PROPERTY_TAGS = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("open issue", "openIssue");
			put("openissue", "openIssue");
			put("deprecation comment", "deprecationInfo");
		}
	};
	
	public class ConceptDomain {
		private String name;
		// private XhtmlNode definition;
		private String text;
		private List<ConceptDomain> children = new ArrayList<ConceptDomain>();
		public String parent;
		public String conceptualClass;
	}

	public void load(String v3source)
			throws ParserConfigurationException, FileNotFoundException, SAXException, IOException {
		System.out.println("loading v3 Source");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		this.mif = builder.parse(new FileInputStream(new File(v3source))).getDocumentElement();
	}

	public void loadMif() throws FHIRFormatError {
		List<Element> conceptDomains = new ArrayList<>();
		XMLUtil.getNamedChildren(mif, "conceptDomain", conceptDomains);
		for (Element e : conceptDomains) {
			ConceptDomain cd = new ConceptDomain();
			cd.name = e.getAttribute("name");
			Element xhtml = XMLUtil.getNamedChild(XMLUtil.getNamedChild(
					XMLUtil.getNamedChild(XMLUtil.getNamedChild(e, "annotations"), "documentation"), "definition"),
					"text");
			// cd.definition = new XhtmlParser().parseHtmlNode(xhtml);
			cd.text = XMLUtil.htmlToXmlEscapedPlainText(xhtml);
			Element spec = XMLUtil.getNamedChild(e, "specializesDomain");
			if (spec != null)
				cd.parent = spec.getAttribute("name");
			Element prop = XMLUtil.getNamedChild(e, "property");
			if (prop != null) {
				if (prop.getAttribute("name").equals("ConceptualSpaceForClassCode"))
					cd.conceptualClass = prop.getAttribute("value");
				else
					throw new Error("Unknown Property");
			}
			v3ConceptDomains.add(cd);
		}
		List<ConceptDomain> removed = new ArrayList<ConceptDomain>();
		for (ConceptDomain cd : v3ConceptDomains) {
			if (cd.parent != null) {
				ConceptDomain parent = getConceptDomain(cd.parent);
				if (parent == null)
					throw new Error("not found");
				parent.children.add(cd);
				removed.add(cd);
			}
		}
		v3ConceptDomains.removeAll(removed);
	}

	private ConceptDomain getConceptDomain(String code) {
		for (ConceptDomain cd : v3ConceptDomains) {
			if (cd.name.equals(code))
				return cd;
		}
		return null;
	}

	public int addConceptDomains(List<ConceptDefinitionComponent> concepts, Map<String, String> codes) {
		return addConceptDomains(v3ConceptDomains, concepts, codes);
	}

	private int addConceptDomains(List<ConceptDomain> domains, List<ConceptDefinitionComponent> concepts,
			Map<String, String> codes) {
		int res = 0;
		for (ConceptDomain cd : domains) {
			ConceptDefinitionComponent c = new ConceptDefinitionComponent();
			concepts.add(c);
			res++;
			
			c.setCode(cd.name);
			c.setDisplay(cd.name);
			c.setDefinition(cd.text);
			extractAndAddPropertiesFromDefinitionText(c);
			
			if (codes.containsKey(c.getCode()))
				System.out.println("Name clash for Domain \"" + c.getCode() + ": used on " + codes.get(c.getCode())
						+ " and in v3");
			else
				codes.put(c.getCode(), "v3");
			c.addProperty().setCode("source").setValue(new CodeType("v3"));
			if (cd.conceptualClass != null)
				c.addProperty().setCode("ConceptualSpaceForClassCode").setValue(new CodeType(cd.conceptualClass));
			res = res + addConceptDomains(cd.children, c.getConcept(), codes);
		}
		return res;
	}

	private void extractAndAddPropertiesFromDefinitionText(ConceptDefinitionComponent c) {
		try {
			if (c.getDefinition() != null) {
				BufferedReader reader = new BufferedReader(new StringReader(c.getDefinition()));
				String line = reader.readLine();
				while (line != null) {
					line = line.trim();
					int colonIdx = line.indexOf(":");
					if (colonIdx >= 0) {
						String tag = line.substring(0, colonIdx).toLowerCase();
						if (DEFINITION_TEXT_PROPERTY_TAGS.containsKey(tag)) {
							String propertyName = DEFINITION_TEXT_PROPERTY_TAGS.get(tag);
							String propertyValue = line.substring(colonIdx+1).trim();
							c.addProperty().setCode(propertyName).setValue(new StringType(propertyValue));
						}
					}
					line = reader.readLine();
				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void generateCodeSystems() throws Exception {
		List<Element> list = new ArrayList<Element>();
		ListResource manifest = ListResourceExt.createManifestList("V3 Code System Release Manifest");
		XMLUtil.getNamedChildren(mif, "codeSystem", list);
		for (Element l : list) {
			CodeSystem cs = generateV3CodeSystem(l);
			csmap.put(cs.getUserString("oid"), cs);
			
			manifest.addEntry(ListResourceExt.createCodeSystemListEntry(cs));
			//manifest.addEntry(ListResourceExt.createCodeSystemListEntry(cs, true));
		}

		postProcess();
		saveCodeSystemManifest(manifest);

		// Extension ext = null;
		for (CodeSystem cs : csmap.values()) {
			new XmlParser().setOutputStyle(OutputStyle.PRETTY)
					.compose(new FileOutputStream(Utilities.path(dest, FolderNameConstants.V3, FolderNameConstants.CODESYSTEMS, cs.getId()) + ".xml"), cs);
		}
		System.out.println("Save v3 code systems (" + Integer.toString(csmap.size()) + " found)");
	}

	private void saveCodeSystemManifest(ListResource manifest) throws Exception {
		new XmlParser().setOutputStyle(OutputStyle.PRETTY)
				.compose(new FileOutputStream(Utilities.path(dest, FolderNameConstants.RELEASE, "v3-CodeSystem-Manifest.xml")), manifest);
		System.out.println("V3 Code System Manifest saved");
	}

	private CodeSystem generateV3CodeSystem(Element item) throws Exception {
		CodeSystem cs = new CodeSystem();
		cs.setId("v3-" + makeSafeId(item.getAttribute("name")));
		cs.setUrl("http://terminology.hl7.org/CodeSystem/" + cs.getId());
		knownCS.add(cs.getUrl());
		cs.setName(item.getAttribute("name"));
		cs.setTitle(item.getAttribute("title"));
		cs.getIdentifier().setSystem("urn:ietf:rfc:3986").setValue("urn:oid:" + item.getAttribute("codeSystemId"));
		cs.setUserData("oid", item.getAttribute("codeSystemId"));
		cs.setStatus(PublicationStatus.ACTIVE);
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("header"))
				processHeader(child, cs);
			else if (child.getNodeName().equals("annotations"))
				processCSAnnotations(child, cs);
			else if (child.getNodeName().equals("releasedVersion"))
				processReleasedVersion(child, cs);
			else if (child.getNodeName().equals("historyItem"))
				processHistoryItem(child, cs);
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
		return cs;
	}

	private void postProcess() throws FHIRException {
		// first, resolve all the code system references
		for (CodeSystem cs : csmap.values()) {
			for (ConceptDefinitionComponent cd : cs.getConcept()) {
				for (ConceptPropertyComponent p : cd.getProperty()) {
					if (p.hasValueCoding() && !p.getValueCoding().hasSystem()) {
						CodeSystem ref = csmap.get(p.getValue().getUserString("oid"));
						if (ref == null)
							throw new FHIRException(
									"reference to unknown code system " + p.getValue().getUserString("oid")
											+ " in property of code " + cd.getCode() + " on " + cs.getId());
						p.getValueCoding().setSystem(ref.getUrl());
					}
				}
			}
		}
		// now, fix the heirarchies
		for (CodeSystem cs : csmap.values()) {
			boolean isOntylog = codeSystemIsOntylog(cs);
			
			List<ConceptDefinitionComponent> moved = new ArrayList<ConceptDefinitionComponent>();
			for (ConceptDefinitionComponent cd : cs.getConcept()) {
				@SuppressWarnings("unchecked")
				List<String> parents = (List<String>) cd.getUserData("parents");
				if (parents != null && parents.size() > 0) {
					if (isOntylog) {
						for (int i = 0; i < parents.size(); i++) {
							ConceptDefinitionComponent parentConcept = find(cs, parents.get(i), cd.getCode(), cs.getUrl());
							ConceptPropertyComponent parentProperty = cd.addProperty();
							parentProperty.setCode("subsumedBy");
							parentProperty.setValue(new CodeType(parentConcept.getCode()));
						}
					} else {
						moved.add(cd);
						ConceptDefinitionComponent parentConcept = find(cs, parents.get(0), cd.getCode(), cs.getUrl());
						parentConcept.getConcept().add(cd);
					}
				}
			}
			cs.getConcept().removeAll(moved);
		}
	}

	private ConceptDefinitionComponent find(CodeSystem cs, String code, String src, String srcU) {
		for (ConceptDefinitionComponent cd : cs.getConcept()) {
			if (cd.getCode().equals(code))
				return cd;
			for (ConceptDefinitionDesignationComponent d : cd.getDesignation()) {
				if (d.getUse().getCode().equals("synonym") && d.getValue().equals(code))
					return cd;
			}
		}
		throw new Error("Unable to resolve reference to " + cs.getUrl() + "#" + code + " from " + srcU + "#" + src);
	}

	private void processHeader(Element item, CodeSystem cs) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("legalese"))
				processLegalese(child, cs);
			else if (child.getNodeName().equals("responsibleGroup"))
				cs.setPublisher(child.getAttribute("organizationName"));
			else if (child.getNodeName().equals("contributor"))
				processContributor(child, cs);
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processLegalese(Element item, CodeSystem cs) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("notation")) {
				notations.add(child.getTextContent());
				cs.addExtension(csext("legalese"), new StringType(child.getTextContent()));
			} else if (child.getNodeName().equals("licenseTerms"))
				cs.setCopyright(child.getTextContent());
			else if (child.getNodeName().equals("versioningPolicy"))
				cs.addExtension(resext("versioningPolicy"), new StringType(child.getTextContent()));
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processContributor(Element item, CodeSystem cs) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		String role = null;
		String name = null;
		String notes = null;
		while (child != null) {
			if (child.getNodeName().equals("role"))
				role = child.getTextContent();
			else if (child.getNodeName().equals("name"))
				name = child.getAttribute("name");
			else if (child.getNodeName().equals("notes"))
				notes = child.getTextContent();
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}

		
		Extension ext = new Extension().setUrl(csext("contributor"));
		cs.getExtension().add(ext);
		ext.addExtension("name", new StringType(name));
		ext.addExtension("role", new StringType(role));
		if (notes != null) {
			ext.addExtension("notes", new StringType(notes));
		}

		if (!Utilities.existsInList(role, "Publisher", "Sponsor"))
			throw new Exception("Unprocessed role " + role);
		if (name.equalsIgnoreCase("(see notes)"))
			name = notes;
		if (!cs.hasPublisher())
			cs.setPublisher(name);
		else if (!name.equals(cs.getPublisher()))
			cs.addContact().setName(name);
	}

	private void processReleasedVersion(Element item, CodeSystem cs) throws Exception {
		// ignore: hl7MaintainedIndicator, hl7ApprovedIndicator
		if (!Boolean.parseBoolean(item.getAttribute("hl7MaintainedIndicator"))) {
			// Is External
			String codeSystemId = cs.getUserData("oid").toString();
			ExternalProvider forcedInclusionProvider = ExternalProvider.getForcedInclusionProvider(codeSystemId);
			if (forcedInclusionProvider != null) {
				forcedInclusionProvider.addCodeSystem(cs);
			} else {
				if (externalProviders.containsKey(cs.getPublisher())) {
					externalProviders.get(cs.getPublisher()).addCodeSystem(cs);
				} else {
					ExternalProvider.handleUnclassifiedCodeSystem(cs);
				}
			}
		}
		
		cs.setDateElement(new DateTimeType(item.getAttribute("releaseDate")));

		//cs.addExtension("http://hl7.org/fhir/StructureDefinition/hl7-approved-indicator",
		//		new BooleanType(item.getAttribute("hl7ApprovedIndicator")));

		Element child = XMLUtil.getFirstChild(item);
		boolean hasConcepts = false;
		while (child != null) {
			if (child.getNodeName().equals("supportedLanguage"))
				processSupportedLanguage(child, cs);
			else if (child.getNodeName().equals("supportedConceptRelationship"))
				processSupportedConceptRelationship(child, cs);
			else if (child.getNodeName().equals("supportedConceptProperty"))
				processSupportedConceptProperty(child, cs);
			else if (child.getNodeName().equals("concept")) {
				processConcept(child, cs);
				hasConcepts = true;
			} else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}

		if ("false".equals(item.getAttribute("completeCodesIndicator")))
			cs.setContent((hasConcepts) ? CodeSystemContentMode.FRAGMENT : CodeSystemContentMode.NOTPRESENT);
		else
			cs.setContent(CodeSystemContentMode.COMPLETE);
	}

	private void processHistoryItem(Element item, CodeSystem cs) throws Exception {
		Extension ext = new Extension().setUrl("http://hl7.org/fhir/StructureDefinition/resource-history");
		cs.getExtension().add(ext);
		ext.addExtension("date", new DateTimeType(item.getAttribute("dateTime")));
		ext.addExtension("id", new StringType(item.getAttribute("id")));
		if (item.hasAttribute("responsiblePersonName"))
			ext.addExtension("author", new StringType(item.getAttribute("responsiblePersonName")));
		if (item.hasAttribute("isSubstantiveChange"))
			ext.addExtension("substantiative", new BooleanType(item.getAttribute("isSubstantiveChange")));
		if (item.hasAttribute("isBackwardCompatibleChange"))
			ext.addExtension("backwardCompatible", new BooleanType(item.getAttribute("isBackwardCompatibleChange")));

		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("description"))
				ext.addExtension("notes", new StringType(child.getTextContent()));
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processSupportedLanguage(Element item, MetadataResource mr) throws Exception {
		mr.setLanguage(item.getTextContent());
		Element child = XMLUtil.getFirstChild(item);
		if (child != null) {
			throw new Exception("Unprocessed element " + child.getNodeName());
		}
	}

	private void processCSAnnotations(Element item, CodeSystem cs) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("documentation"))
				processCSDocumentation(child, cs);
			else if (child.getNodeName().equals("appInfo"))
				processCSAppInfo(child, cs);
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processCSDocumentation(Element item, CodeSystem cs) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("description"))
				processCSDescription(child, cs);
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processCSDescription(Element item, CodeSystem cs) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("text")) {
				cs.setDescription(XMLUtil.htmlToXmlEscapedPlainText(child).trim());
				XhtmlNode html = new XhtmlParser().parseHtmlNode(child);
				html.setName("div");
				cs.getText().setDiv(html);
				cs.getText().setStatus(NarrativeStatus.GENERATED);
			} else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processCSAppInfo(Element item, CodeSystem cs) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("deprecationInfo"))
				processCSDeprecationInfo(child, cs);
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processCSDeprecationInfo(Element item, CodeSystem cs) throws Exception {
		String v = item.getAttribute("deprecationEffectiveVersion");
		if (Utilities.noString(v))
			throw new Exception("Element not understood: " + item.getNodeName());
		cs.addExtension(resext("versionDeprecated"), Factory.newString_(v));
		cs.setStatus(PublicationStatus.RETIRED);
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("text")) {
				if (!"This Code System was deprecated (retired) as of the Vocabulary Model version shown in \"deprecationEffectiveVersion\"."
						.equals(child.getTextContent()))
					throw new Exception("Unprocessed element text: " + child.getTextContent());
			} else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processSupportedConceptRelationship(Element item, CodeSystem cs) throws Exception {
		if ("Specializes".equals(item.getAttribute("name")))
			return;
		if ("Generalizes".equals(item.getAttribute("name")))
			return;
		if (Utilities.existsInList(item.getAttribute("relationshipKind"), "NonDefinitionallyQualifiedBy", "Specializes",
				"ComponentOf", "Other", "LessThan")) {
			PropertyComponent pd = cs.addProperty();
			pd.setCode(item.getAttribute("name"));
			pd.setType(PropertyType.CODING);
			pd.addExtension(csext("relationshipKind"), new CodeType(item.getAttribute("relationshipKind")));
			Element child = XMLUtil.getFirstChild(item);
			while (child != null) {
				if (child.getNodeName().equals("description"))
					pd.setDescription(child.getTextContent());
				else
					throw new Exception("Unprocessed element " + child.getNodeName());
				child = XMLUtil.getNextSibling(child);
			}
		} else
			throw new Exception("Unprocessed relationship " + item.getAttribute("name") + " : "
					+ item.getAttribute("relationshipKind"));
	}

	private void processSupportedConceptProperty(Element item, CodeSystem cs) throws Exception {
		// item.getAttribute("propertyName").equals("internalId"));
		PropertyComponent pd = cs.addProperty();
		pd.setCode(item.getAttribute("propertyName"));
		String type = item.getAttribute("type");
		if ("Token".equals(type))
			pd.setType(PropertyType.CODE);
		else if ("String".equals(type))
			pd.setType(PropertyType.STRING);
		else if ("Boolean".equals(type))
			pd.setType(PropertyType.BOOLEAN);
		else
			throw new Exception("unknown type " + type);

		String isMandatory = item.getAttribute("isMandatoryIndicator");
		String defaultHandlingCode = item.getAttribute("defaultHandlingCode");
		String defaultValue = item.getAttribute("defaultValue");

		if (!Utilities.noString(defaultValue)) {
			if (!("active".equals(defaultValue) && pd.getCode().equals("status"))
					&& !(Utilities.existsInList(defaultValue, "&", "as&") && pd.getCode().startsWith("Name:")))
				throw new Exception("Unsupported default value " + defaultValue);
		}
		
		if (!(isMandatory + defaultHandlingCode + defaultValue).isEmpty()) {
			Extension ext = new Extension().setUrl(csext("mif-extended-properties"));
			pd.getExtension().add(ext);
			if (!isMandatory.isEmpty())
				ext.addExtension("isMandatory", new BooleanType(isMandatory));
			if (!defaultHandlingCode.isEmpty())
				ext.addExtension("defaultHandlingCode", new CodeType(defaultHandlingCode));
			if (!defaultValue.isEmpty())
				ext.addExtension("defaultValue", new StringType(defaultValue));
		}
		
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("description"))
				pd.setDescription(child.getTextContent());
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void addConceptProperty(CodeSystem cs, String propertyName, PropertyType type, String description)
			throws Exception {
		PropertyComponent pd = cs.addProperty();
		pd.setCode(propertyName);
		pd.setUri("http://hl7.org/fhir/concept-properties#" + propertyName);
		pd.setType(type);
		pd.setDescription(description);
	}

	private void processConcept(Element item, CodeSystem cs) throws Exception {
		final String NotSelectableProperty = "notSelectable";

		List<Element> codeElements = extractCodeElements(item);
		if (codeElements.isEmpty()) {
			throw new Exception("Found concept without any codes: " + item.getNodeName());
		}
		for (Element codeElement : codeElements) {
			ConceptDefinitionComponent cd = cs.addConcept();

			String isSelectable = item.getAttribute("isSelectable");
			if (!Utilities.noString(isSelectable)) {
				// cd.addExtension(csext("isSelectable"), Factory.newString_(isSelectable));
				Boolean bIsSelectable = !Boolean.parseBoolean(isSelectable);

				PropertyComponent pc = cs.getProperty(NotSelectableProperty);
				if (pc == null) {
					addConceptProperty(cs, NotSelectableProperty, PropertyType.BOOLEAN,
							"Indicates that the code is abstract - only intended to be used as a selector for other concepts");
				}

				ConceptPropertyComponent cpc = cd.addProperty();
				cpc.setCode(NotSelectableProperty);
				cpc.setValue(new BooleanType(bIsSelectable));
			}
			
			processCode(codeElement, cd, cs);
			
			for (Element otherCodeElement : codeElements) {
				if (otherCodeElement != codeElement) {
					ConceptPropertyComponent codeSynonymProperty = cd.addProperty();
					codeSynonymProperty.setCode("synonymCode");
					codeSynonymProperty.setValue(new CodeType(otherCodeElement.getAttribute("code")));
				}
			}

			Element child = XMLUtil.getFirstChild(item);
			while (child != null) {
				if (child.getNodeName().equals("annotations"))
					processConceptAnnotations(child, cd);
				else if (child.getNodeName().equals("code")) {
					// no op
				}
				else if (child.getNodeName().equals("conceptProperty"))
					processConceptProperty(child, cd, cs);
				else if (child.getNodeName().equals("printName"))
					processPrintName(child, cd, cs);
				else if (child.getNodeName().equals("conceptRelationship"))
					processConceptRelationship(child, cd, cs);
				else
					throw new Exception("Unprocessed element " + child.getNodeName());

				child = XMLUtil.getNextSibling(child);
				
			}
		}
	}

	private void processConceptAnnotations(Element item, ConceptDefinitionComponent cd) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("documentation"))
				processConceptDocumentation(child, cd);
			else if (child.getNodeName().equals("appInfo"))
				processConceptAppInfo(child, cd);
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processConceptDocumentation(Element item, ConceptDefinitionComponent cd) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("definition"))
				processConceptDefinition(child, cd);

			// There does not seem to be a 'description' element at this level. 
			// Commented out this option, and we'll see if an exception eventually gets thrown.
			//else if (child.getNodeName().equals("description"))
			//	processConceptDescription(child, cd);
			
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	/*
	private void processConceptDescription(Element item, ConceptDefinitionComponent cd) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("text"))
				cd.addExtension(csext("description"), new StringType(XMLUtil.htmlToXmlEscapedPlainText(child)));
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}
	*/

	private void processConceptDefinition(Element item, ConceptDefinitionComponent cd) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("text"))
				cd.setDefinition(XMLUtil.htmlToXmlEscapedPlainText(child));
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processConceptAppInfo(Element item, ConceptDefinitionComponent cd) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("openIssue"))
				processConceptOpenIssue(child, cd);
			else if (child.getNodeName().equals("deprecationInfo"))
				processConceptDeprecationInfo(child, cd);
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processConceptOpenIssue(Element item, ConceptDefinitionComponent cd) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("text"))
				cd.addExtension(csext("openIssue"), new StringType(XMLUtil.htmlToXmlEscapedPlainText(child)));
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processConceptDeprecationInfo(Element item, ConceptDefinitionComponent cd) throws Exception {
		String v = item.getAttribute("deprecationEffectiveVersion");
		if (Utilities.noString(v))
			throw new Exception("Element not understood: " + item.getNodeName());
		cd.addExtension(resext("versionDeprecated"), Factory.newString_(v));
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("text")) {
				if (!"This element was deprecated as of the release indicated.".equals(child.getTextContent()))
					throw new Exception("Unprocessed element " + child.getNodeName());
			} else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processConceptProperty(Element item, ConceptDefinitionComponent cd, CodeSystem cs) throws Exception {
		String id = item.getAttribute("name");

		if (id.equalsIgnoreCase("status")) {
			return;
		}
		
		PropertyComponent pd = cs.getProperty(id);
		if (pd == null) {
			// throw new Exception("Unknown Property "+id+" on "+item.getTagName()+" for
			// code system "+cs.getId());
			System.out
					.println("Unknown Property " + id + " on " + item.getTagName() + " for code system " + cs.getId());
			return;
		}
		ConceptPropertyComponent p = cd.addProperty();
		p.setCode(id);
		if (pd.getType() == PropertyType.CODE)
			p.setValue(new CodeType(item.getAttribute("value")));
		else if (pd.getType() == PropertyType.STRING)
			p.setValue(new StringType(item.getAttribute("value")));
		else if (pd.getType() == PropertyType.BOOLEAN)
			p.setValue(new BooleanType(item.getAttribute("value")));
		else
			throw new Exception("Unsupported Property type " + pd.getType().toCode());
		Element child = XMLUtil.getFirstChild(item);
		if (child != null) {
			throw new Exception("Unprocessed element " + child.getNodeName());
		}
	}

	private void processPrintName(Element item, ConceptDefinitionComponent cd, CodeSystem cs) throws Exception {
		if (!"true".equals(item.getAttribute("preferredForLanguage")))
			cd.addDesignation().setUse(new Coding().setSystem("http://terminology.hl7.org/hl7TermMaintInfra").setCode("deprecated alias"))
					.setValue(item.getAttribute("text"));
		else if (Utilities.noString(item.getAttribute("language"))
				|| item.getAttribute("language").equals(cs.getLanguage()))
			cd.setDisplay(item.getAttribute("text"));
		else
			cd.addDesignation().setLanguage(item.getAttribute("language")).setValue(item.getAttribute("text"));
		Element child = XMLUtil.getFirstChild(item);
		if (child != null) {
			throw new Exception("Unprocessed element " + child.getNodeName());
		}
	}

	private void processCode(Element item, ConceptDefinitionComponent cd, CodeSystem cs) throws Exception {
		if (!"active".equals(item.getAttribute("status")) && !"retired".equals(item.getAttribute("status")))
			throw new Exception("Unexpected value for attribute status " + item.getAttribute("status"));
		if (cd.hasCode())
			// change this to an extension once the build defines the extension to use
			cd.addDesignation().setUse(new Coding().setSystem("http://terminology.hl7.org/hl7TermMaintInfra").setCode("synonym"))
					.setValue(item.getAttribute("code"));
		else
			cd.setCode(item.getAttribute("code"));
		cd.addProperty().setCode("status").setValue(new CodeType(item.getAttribute("status")));
		Element child = XMLUtil.getFirstChild(item);
		if (child != null) {
			throw new Exception("Unprocessed element " + child.getNodeName());
		}
	}

	private void processConceptRelationship(Element item, ConceptDefinitionComponent cd, CodeSystem cs)
			throws Exception {
		if ("Specializes".equals(item.getAttribute("relationshipName"))) {
			Element child = XMLUtil.getFirstChild(item);
			while (child != null) {
				if (child.getNodeName().equals("targetConcept")) {
					@SuppressWarnings("unchecked")
					List<String> parents = (List<String>) cd.getUserData("parents");
					if (parents == null) {
						parents = new ArrayList<String>();
						cd.setUserData("parents", parents);
					}
					parents.add(child.getAttribute("code"));
				} else
					throw new Exception("Unprocessed element " + child.getNodeName());
				child = XMLUtil.getNextSibling(child);
			}
		} else if (cs.getProperty(item.getAttribute("relationshipName")) != null) {
			PropertyComponent pd = cs.getProperty(item.getAttribute("relationshipName"));
			Element child = XMLUtil.getFirstChild(item);
			while (child != null) {
				if (child.getNodeName().equals("targetConcept")) {
					ConceptPropertyComponent t = cd.addProperty();
					t.setCode(pd.getCode());
					Coding c = new Coding();
					c.setCode(child.getAttribute("code"));
					if (Utilities.noString(child.getAttribute("codeSystem")))
						c.setSystem(cs.getUrl());
					else
						c.setUserData("oid", child.getAttribute("codeSystem"));
					t.setValue(c);
				} else
					throw new Exception("Unprocessed element " + child.getNodeName());
				child = XMLUtil.getNextSibling(child);
			}
		} else
			throw new Exception(
					"Unexpected value for attribute relationshipName " + item.getAttribute("relationshipName"));
	}

	public void generateValueSets() throws Exception {
		List<Element> list = new ArrayList<Element>();
		ListResource manifest = ListResourceExt.createManifestList("V3 Value Set Release Manifest");
		XMLUtil.getNamedChildren(mif, "valueSet", list);
		HashMap<String, ValueSet> vsmap = new HashMap<String, ValueSet>();
		for (Element l : list) {
			ValueSet vs = generateV3ValueSet(l);
			vsmap.put(vs.getUserString("oid"), vs);

			manifest.addEntry(ListResourceExt.createValueSetListEntry(vs));
			//manifest.addEntry(ListResourceExt.createValueSetListEntry(vs, true));
		}
		postProcess(vsmap);
		saveValueSetManifest(manifest);
		for (ValueSet vs : vsmap.values())
			new XmlParser().setOutputStyle(OutputStyle.PRETTY)
					.compose(new FileOutputStream(Utilities.path(dest, FolderNameConstants.V3, FolderNameConstants.VALUESETS, vs.getId()) + ".xml"), vs);
		System.out.println("Save v3 value sets (" + Integer.toString(vsmap.size()) + " found)");
		System.out.println("Unknown systems");
		for (String s : sorted(systems))
			if (!knownCS.contains(s))
				System.out.println(" ..unknown.. " + s);
	}

	private void saveValueSetManifest(ListResource manifest) throws Exception {
		new XmlParser().setOutputStyle(OutputStyle.PRETTY)
				.compose(new FileOutputStream(Utilities.path(dest, FolderNameConstants.RELEASE, "v3-ValueSet-Manifest.xml")), manifest);
		System.out.println("V3 Value Set Manifest saved");
	}

	private void saveV3Manifest(Document document) throws Exception {
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		Result result = new StreamResult(new File(Utilities.path(dest, FolderNameConstants.RELEASE, "v3-Manifest.xml")));
		Source source = new DOMSource(document);
		transformer.transform(source, result);
		System.out.println("V3 Manifest saved");
	}

	public void mergeV3Manifests() throws Exception {
		File codeSystemManifestFile = new File(Utilities.path(dest, FolderNameConstants.RELEASE, "v3-CodeSystem-Manifest.xml"));
		File valueSetSystemManifestFile = new File(Utilities.path(dest, FolderNameConstants.RELEASE, "v3-ValueSet-Manifest.xml"));
		Document doc = merge(codeSystemManifestFile, valueSetSystemManifestFile);
		removeXMLNSAttribute(doc);
		saveV3Manifest(doc);
	}

	private static Document merge(File... files) throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setIgnoringElementContentWhitespace(true);
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document base = docBuilder.parse(files[0]);
		Element rootNode = base.getDocumentElement();

		for (int i = 1; i < files.length; i++) {
			Document merge = docBuilder.parse(files[i]);
			NodeList nextResults = merge.getDocumentElement().getElementsByTagName("entry");

			for (int j = 0; j < nextResults.getLength(); j++) {
				Node kid = nextResults.item(j);
				kid = base.importNode(kid, true);
				rootNode.appendChild(kid);
			}
		}
		return base;
	}

	private static Document removeXMLNSAttribute(Document doc)
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

	private void postProcess(HashMap<String, ValueSet> vsmap) throws Exception {
		// resolve vs references
		for (ValueSet vs : vsmap.values()) {
			for (ConceptSetComponent cs : vs.getCompose().getInclude())
				resolveVsReferences(vs, cs, vsmap);
			for (ConceptSetComponent cs : vs.getCompose().getExclude())
				resolveVsReferences(vs, cs, vsmap);
		}
	}

	private void resolveVsReferences(ValueSet vs, ConceptSetComponent cs, HashMap<String, ValueSet> vsmap)
			throws Exception {
		for (UriType vsref : cs.getValueSet()) {
			ValueSet vtgt = vsmap.get(vsref.getUserString("vsref"));
			if (vtgt == null)
				throw new Exception("Unable to resolve reference to value set " + vsref.getUserString("vsref")
						+ " on value set " + vs.getId());
			vsref.setValue(vtgt.getUrl());
		}
	}

	private ValueSet generateV3ValueSet(Element item) throws Exception {
		ValueSet vs = new ValueSet();
		vs.setId("v3-" + makeSafeId(item.getAttribute("name")));
		vs.setUrl("http://terminology.hl7.org/ValueSet/" + vs.getId());
		vs.setName(item.getAttribute("name"));
		vs.setTitle(item.getAttribute("name"));
		vs.addIdentifier().setSystem("urn:ietf:rfc:3986").setValue("urn:oid:" + item.getAttribute("id"));
		vs.setUserData("oid", item.getAttribute("id"));
		vs.setStatus(PublicationStatus.ACTIVE);
		if ("true".equals(item.getAttribute("isImmutable")))
			vs.setImmutable(true);
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("header"))
				processHeader(child, vs);
			else if (child.getNodeName().equals("annotations"))
				processVSAnnotations(child, vs);
			else if (child.getNodeName().equals("version"))
				processVersion(child, vs);
			else if (child.getNodeName().equals("historyItem"))
				processHistoryItem(child, vs);
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}

		for (ConceptSetComponent cmp : vs.getCompose().getInclude())
			checkCompose(vs.getId(), cmp);
		for (ConceptSetComponent cmp : vs.getCompose().getExclude())
			checkCompose(vs.getId(), cmp);
		return vs;
	}

	private void checkCompose(String string, ConceptSetComponent cmp) {
		if (cmp.hasSystem())
			systems.add(cmp.getSystem());
	}

	private void processHeader(Element item, ValueSet vs) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("legalese"))
				processLegalese(child, vs);
			else if (child.getNodeName().equals("responsibleGroup"))
				vs.setPublisher(child.getAttribute("organizationName"));
			else if (child.getNodeName().equals("contributor"))
				processContributor(child, vs);
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processLegalese(Element item, ValueSet vs) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("notation"))
				notations.add(child.getTextContent());
			else if (child.getNodeName().equals("licenseTerms"))
				vs.setCopyright(child.getTextContent());
			else if (child.getNodeName().equals("versioningPolicy"))
				vs.addExtension(resext("versioningPolicy"), new StringType(child.getTextContent()));
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processContributor(Element item, ValueSet vs) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		String role = null;
		String name = null;
		String notes = null;
		while (child != null) {
			if (child.getNodeName().equals("role"))
				role = child.getTextContent();
			else if (child.getNodeName().equals("name"))
				name = child.getAttribute("name");
			else if (child.getNodeName().equals("notes"))
				notes = child.getTextContent();
			else
				throw new Exception("Unprocessed element " + child.getNodeName());

			child = XMLUtil.getNextSibling(child);
		}
		if (!Utilities.existsInList(role, "Publisher", "Sponsor"))
			throw new Exception("Unprocessed role " + role);
		if (name.equalsIgnoreCase("(see notes)"))
			name = notes;
		if (!vs.hasPublisher())
			vs.setPublisher(name);
		else if (!name.equals(vs.getPublisher()))
			vs.addContact().setName(name);
	}

	private void processVSAnnotations(Element item, ValueSet vs) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("documentation"))
				processVSDocumentation(child, vs);
			else if (child.getNodeName().equals("appInfo"))
				processVSAppInfo(child, vs);
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processVSDocumentation(Element item, ValueSet vs) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("description"))
				processVSDescription(child, vs);
			else if (child.getNodeName().equals("otherAnnotation"))
				processVSOtherAnnotation(child, vs);
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processVSDescription(Element item, ValueSet vs) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("text")) {
				vs.setDescription(XMLUtil.htmlToXmlEscapedPlainText(child));
				XhtmlNode html = new XhtmlParser().parseHtmlNode(child);
				html.setName("div");
				vs.getText().setDiv(html);
				vs.getText().setStatus(NarrativeStatus.GENERATED);

				Element grandChild = XMLUtil.getFirstChild(child);
				while (grandChild != null) {
					if (grandChild.getNodeName().equals("p")) {
						Element greatGrandChild = XMLUtil.getFirstChild(grandChild);
						if (greatGrandChild != null && greatGrandChild.getNodeName().equals("i")
								&& greatGrandChild.getTextContent() != null
								&& greatGrandChild.getTextContent().startsWith("Steward:")) {
							vs.addExtension(resext("steward"), new StringType(grandChild.getTextContent().trim()));
						}
					}
					grandChild = XMLUtil.getNextSibling(grandChild);
				}

			} else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processVSOtherAnnotation(Element item, ValueSet vs) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("text")) {
				vs.setPurpose(XMLUtil.htmlToXmlEscapedPlainText(child));
			} else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processVSAppInfo(Element item, ValueSet vs) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("deprecationInfo"))
				processVSDeprecationInfo(child, vs);
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processVSDeprecationInfo(Element item, ValueSet vs) throws Exception {
		String v = item.getAttribute("deprecationEffectiveVersion");
		if (Utilities.noString(v))
			throw new Exception("Element not understood: " + item.getNodeName());
		vs.addExtension(resext("versionDeprecated"), Factory.newString_(v));
		vs.setStatus(PublicationStatus.RETIRED);
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("text")) {
				if (!"This element was deprecated as of the release indicated.".equals(child.getTextContent()))
					throw new Exception("Unprocessed element text: " + child.getTextContent());
			} else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processHistoryItem(Element item, ValueSet vs) throws Exception {
		// TODO should be valueset-history?
		Extension ext = new Extension().setUrl("http://hl7.org/fhir/StructureDefinition/resource-history");
		vs.getExtension().add(ext);
		// ext.addExtension("id", new StringType(item.getAttribute("id")));
		vs.setVersion(item.getAttribute("id"));
		ext.addExtension("id", new StringType(item.getAttribute("id")));
		ext.addExtension("date", new DateTimeType(item.getAttribute("dateTime")));
		if (item.hasAttribute("responsiblePersonName"))
			ext.addExtension("author", new StringType(item.getAttribute("responsiblePersonName")));
		if (item.hasAttribute("isSubstantiveChange"))
			ext.addExtension("substantiative", new BooleanType(item.getAttribute("isSubstantiveChange")));
		if (item.hasAttribute("isBackwardCompatibleChange"))
			ext.addExtension("backwardCompatible", new BooleanType(item.getAttribute("isBackwardCompatibleChange")));

		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("description"))
				// Changed url from 'version annotation' to 'notes'
				ext.addExtension("notes", new StringType(child.getTextContent()));
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processVersion(Element item, ValueSet vs) throws Exception {
		// ignore: hl7MaintainedIndicator, hl7ApprovedIndicator
		
		vs.setDateElement(new DateTimeType(item.getAttribute("versionDate")));

		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("supportedLanguage"))
				processSupportedLanguage(child, vs);
			else if (child.getNodeName().equals("supportedCodeSystem"))
				; // ignore this
			else if (child.getNodeName().equals("associatedConceptProperty"))
				; // ignore this - for now? todo!
			else if (child.getNodeName().equals("content"))
				processContent(child, vs);
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processContent(Element item, ValueSet vs) throws Exception {
		String oid = item.getAttribute("codeSystem");
		String url = identifyOID(oid);
		processGeneralContent(item, vs, url, true, 0);
	}

	private void processGeneralContent(Element item, ValueSet vs, String url, boolean include, int level)
			throws Exception {
		Element child = XMLUtil.getFirstChild(item);

		ConceptSetComponent baseSet = new ConceptSetComponent();
		ConceptSetComponent enumeratedSet = new ConceptSetComponent();
		ConceptSetComponent filteredSet = new ConceptSetComponent();
		ConceptSetComponent valuesetSet = new ConceptSetComponent();

		if (item.hasAttribute("codeSystem")) {
			url = identifyOID(item.getAttribute("codeSystem"));
		}
		
		if (url != null && !url.isEmpty()) {
			baseSet.setSystem(url);
			enumeratedSet.setSystem(url);
			filteredSet.setSystem(url);
		}

		while (child != null) {
			if (child.getNodeName().equals("codeBasedContent") && !child.hasChildNodes()) {
				processCodeBasedContent(child, vs, enumeratedSet);
			
			} else if (child.getNodeName().equals("codeBasedContent")) {
				processCodeBasedContent(child, vs, filteredSet);
					
			} else if (child.getNodeName().equals("combinedContent")) {
				if (!include && level > 0)
					throw new Exception("recursion not supported on exclusion in " + vs.getUrl());
				processCombinedContent(child, vs, url);
				
			} else if (child.getNodeName().equals("valueSetRef")) {
				// ConceptSetComponent vset = include ? vs.getCompose().addInclude() :
				// vs.getCompose().addExclude() ;

				CanonicalType vsref = new CanonicalType();
				valuesetSet.getValueSet().add(vsref);
				vsref.setUserData("vsref", child.getAttribute("id"));
				vsref.setUserData("vsname", child.getAttribute("name"));
				
			} else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
		
		addToValuesetCompose(vs, enumeratedSet, include);
		addToValuesetCompose(vs, filteredSet, include);
		addToValuesetCompose(vs, valuesetSet, include);
		
		if (level == 0 && !vs.getCompose().hasExclude() && !vs.getCompose().hasInclude()) {
			addToValuesetCompose(vs, baseSet, include, true);
		}

	}

	private void addToValuesetCompose(ValueSet vs, ConceptSetComponent csc, boolean include) {
		addToValuesetCompose(vs, csc, include, false);
	}

	private void addToValuesetCompose(ValueSet vs, ConceptSetComponent csc, boolean include, boolean forceAdd) {
		if (forceAdd || csc.getConcept().size() > 0 || csc.getValueSet().size() > 0 || csc.getFilter().size() > 0) {
			if (include) {
				vs.getCompose().addInclude(csc);
			} else {
				vs.getCompose().addExclude(csc);
			}
		}
	}
	
	private void processCodeBasedContent(Element item, ValueSet vs, ConceptSetComponent cset) throws Exception {
		String code = item.getAttribute("code");
		boolean filtered = false;
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("includeRelatedCodes")) {
				// common case: include a child and all or some of it's descendants
				ConceptSetFilterComponent f = new ValueSet.ConceptSetFilterComponent();
				f.setOp("false".equals(child.getAttribute("includeHeadCode")) ? FilterOperator.DESCENDENTOF
						: FilterOperator.ISA);
				f.setProperty("concept");
				f.setValue(code);
				cset.getFilter().add(f);
				filtered = true;
			} else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
		if (!filtered)
			cset.addConcept().setCode(code);
	}

	private void processCombinedContent(Element item, ValueSet vs, String url) throws Exception {
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("unionWithContent")) {
				processGeneralContent(child, vs, url, true, 1);
			} else if (child.getNodeName().equals("excludeContent")) {
				processGeneralContent(child, vs, url, false, 1);
			} else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private boolean codeSystemIsOntylog(CodeSystem cs) {
		boolean isOntylog = false;
		List<ConceptDefinitionComponent> conceptList = cs.getConcept();
		for (ConceptDefinitionComponent concept : conceptList) {
			@SuppressWarnings("unchecked")
			List<String> parents = (List<String>) concept.getUserData("parents");
			if (parents != null && parents.size() > 1) {
				isOntylog = true;
				break;
			}
		}
		return isOntylog;
	}
	
	private List<Element> extractCodeElements(Element item) {
		List<Element> codeElements = new LinkedList<Element>();
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("code"))
				codeElements.add(child);
			child = XMLUtil.getNextSibling(child);
		}
		return codeElements;
	}
}
