package org.hl7.fhir.utg.v3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemContentMode;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemHierarchyMeaning;
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
import org.hl7.fhir.r4.model.ListResource.ListEntryComponent;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.r4.model.ValueSet.ConceptSetFilterComponent;
import org.hl7.fhir.r4.model.ValueSet.FilterOperator;
import org.hl7.fhir.utg.BaseGenerator;
import org.hl7.fhir.utg.OIDLookup;
import org.hl7.fhir.utg.PropertyLookup;
import org.hl7.fhir.utg.UTGGenerator;
import org.hl7.fhir.utg.external.ExternalProvider;
import org.hl7.fhir.utg.fhir.ListResourceExt;
import org.hl7.fhir.utilities.FolderNameConstants;
import org.hl7.fhir.utilities.MarkDownProcessor;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.xml.XMLUtil;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class V3SourceGenerator extends BaseGenerator {

	public V3SourceGenerator(String dest, Map<String, CodeSystem> csmap, Set<String> knownCS, Map<String, ExternalProvider> externalProviders) {
		super(dest, csmap, knownCS);
		this.externalProviders = externalProviders;
	}

	private Element mif;
	private List<ConceptDomain> v3ConceptDomains = new LinkedList<ConceptDomain>();
	private Set<String> notations = new HashSet<String>();
	private Set<String> systems = new HashSet<String>();
	private Map<String, ExternalProvider> externalProviders;
	private Map<String, CodeSystem> csByName = new HashMap<String, CodeSystem>();
	
	private static final String V3_INITIAL_VERSION = "2.0.0";
	
	private static final Map<String, String> DEFINITION_TEXT_PROPERTY_TAGS = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("open issue", "openIssue");
			put("openissue", "openIssue");
			put("deprecation comment", "deprecationInfo");
			put("deprecationcomment", "deprecationInfo");
		}
	};
	
	private static final Map<String, String> V3_STEWARD_MAP = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("attachments wg", "att");
			put("cqi wg", "cqi");
			put("oo wg", "oo");
			put("security wg", "sec");
		}
	};

	public class ConceptDomain {
		private String name;
		// private XhtmlNode definition;
		private String text;
		private List<ConceptDomain> children = new LinkedList<ConceptDomain>();
		public String parent;
		public String conceptualClass;
		public List<ContextBinding> contextBindings = new LinkedList<ContextBinding>();
	}
	
	public class ContextBinding {
		public String valueSetOID;
		public String bindingRealmName;
		public String codingStrength;
		public String effectiveDate;
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
		List<Element> contextBindingElements = new LinkedList<>();
		Map<String, List<ContextBinding>> contextBindings = new HashMap<>();
		XMLUtil.getNamedChildren(mif, "contextBinding", contextBindingElements);
		for (Element cbElement : contextBindingElements) {
			String cdName = cbElement.getAttribute("conceptDomain");
			ContextBinding cb = new ContextBinding();
			cb.valueSetOID = cbElement.getAttribute("valueSet");
			cb.bindingRealmName = cbElement.getAttribute("bindingRealmName");
			cb.codingStrength = cbElement.getAttribute("codingStrength");
			cb.effectiveDate = cbElement.getAttribute("effectiveDate");
			if (!contextBindings.containsKey(cdName)) {
				contextBindings.put(cdName, new LinkedList<ContextBinding>());
			}
			contextBindings.get(cdName).add(cb);
		}
		
		List<Element> conceptDomains = new LinkedList<>();
		XMLUtil.getNamedChildren(mif, "conceptDomain", conceptDomains);
		for (Element e : conceptDomains) {
			ConceptDomain cd = new ConceptDomain();
			cd.name = e.getAttribute("name");
			Element xhtml = XMLUtil.getNamedChild(XMLUtil.getNamedChild(
					XMLUtil.getNamedChild(XMLUtil.getNamedChild(e, "annotations"), "documentation"), "definition"),
					"text");
			// cd.definition = new XhtmlParser().parseHtmlNode(xhtml);
			
			//cd.text = XMLUtil.htmlToXmlEscapedPlainText(xhtml);
			cd.text = MarkDownProcessor.htmlToMarkdown(xhtml);
			
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
			cd.contextBindings = contextBindings.get(cd.name);
			v3ConceptDomains.add(cd);
		}
		
		List<ConceptDomain> removed = new LinkedList<ConceptDomain>();
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

	public int addConceptDomains(List<ConceptDefinitionComponent> concepts, Map<String, String> codes) throws Exception {
		return addConceptDomains(v3ConceptDomains, concepts, codes);
	}

	private int addConceptDomains(List<ConceptDomain> domains, List<ConceptDefinitionComponent> concepts,
			Map<String, String> codes) throws Exception {
		int res = 0;
		for (ConceptDomain cd : domains) {
			ConceptDefinitionComponent c = new ConceptDefinitionComponent();
			concepts.add(c);
			res++;
			
			c.setCode(cd.name);
			c.setDisplay(cd.name);
			c.setDefinition(cd.text);

			Map<String, String> properties = extractAdditionalPropertiesFromText(c.getDefinition());
			for (String propertyName : properties.keySet()) {
				c.addProperty().setCode(propertyName).setValue(new StringType(properties.get(propertyName)));
			}
			
			if (codes.containsKey(c.getCode()))
				System.out.println("Name clash for Domain \"" + c.getCode() + ": used on " + codes.get(c.getCode())
						+ " and in v3");
			else
				codes.put(c.getCode(), "v3");
			c.addProperty().setCode("source").setValue(new CodeType("v3"));
			
			if (cd.conceptualClass != null) {
				String conClass = cd.conceptualClass;
				String[] conClassParts = conClass.split("\\.");
				if (conClassParts.length >= 2) {
					String csName = conClassParts[0].toLowerCase();
					String code = String.join(".", Arrays.copyOfRange(conClassParts, 1, conClassParts.length));
					
					if (csByName.containsKey(csName)) {
						String csSystem = csByName.get(csName).getUrl();
						Coding value = new Coding();
						value.setSystem(csSystem); // system url
						value.setCode(code); // code
						c.addProperty().setCode("ConceptualSpaceForClassCode").setValue(value);
					} else {
						System.out.println("***   No code system match for '" + csName + "'");
					}
					
				} else {
					System.out.println("***   Invalid Conceptual Class Code found for concept domain '" + cd.name + "': '" + conClass + "'");
				}
			}
			
			if (cd.contextBindings != null) {
				for (ContextBinding cb : cd.contextBindings) {
					String realm = cb.bindingRealmName;
					String propertyCodePrefix = UTGGenerator.CONTEXT_BINDING_PREFIX + realm; 

					if (!UTGGenerator.BINDING_REALMS.contains(realm)) {
						throw new Exception("Unrecognized context binding realm: '" + realm + "'");
					}
					
					c.addProperty()
						.setCode(propertyCodePrefix + "-valueSet")
						.setValue(new StringType(cb.valueSetOID))
						.addExtension(resext("concept-binding-strength"), new CodeType(cb.codingStrength));
					
					//c.addProperty().setCode(propertyCodePrefix + "-codingStrength").setValue(new CodeType(cb.codingStrength));
					c.addProperty().setCode(propertyCodePrefix + "-effectiveDate").setValue(new DateTimeType(cb.effectiveDate));
				}
			}
			
			res = res + addConceptDomains(cd.children, c.getConcept(), codes);
		}
		return res;
	}

	public void generateCodeSystems(ListResource v3PublishingManifest, ListResource internalManifest, ListResource externalManifest, ListResource deprecatedManifest) throws Exception {
		List<Element> list = new LinkedList<Element>();
		List<NamingSystem> depNamingSystems = new LinkedList<NamingSystem>();
		List<NamingSystem> intNamingSystems = new LinkedList<NamingSystem>();
		List<NamingSystem> extNamingSystems = new LinkedList<NamingSystem>();
		List<CodeSystem> intCodeSystems = new LinkedList<CodeSystem>();
		List<CodeSystem> extCodeSystems = new LinkedList<CodeSystem>();
		List<CodeSystem> depCodeSystems = new LinkedList<CodeSystem>();
		
		XMLUtil.getNamedChildren(mif, "codeSystem", list);
		for (Element l : list) {
			CodeSystem cs = generateV3CodeSystem(l);
			if (cs != null) {
				String oid = cs.getUserString("oid"); 
				OIDLookup.put_v3_to_v2_url_bridge(oid, cs.getUrl());
				csmap.put(oid, cs);

				ListEntryComponent manifestEntry = ListResourceExt.createCodeSystemListEntry(cs);
				
				if (OIDLookup.hasContent(oid)) {
					if (OIDLookup.isDeprecated(oid)) {
						depCodeSystems.add(cs);
						deprecatedManifest.addEntry(manifestEntry);
					} else {
						v3PublishingManifest.addEntry(manifestEntry);
						if (cs.getInternal()) {
							intCodeSystems.add(cs);
							internalManifest.addEntry(manifestEntry);
						} else {
							extCodeSystems.add(cs);
							externalManifest.addEntry(manifestEntry);
						}
					}
				
				} else {
					NamingSystem ns = new NamingSystem(cs);
					manifestEntry = ListResourceExt.createNamingSystemListEntry(ns);
					if (OIDLookup.isDeprecated(oid)) {
						depNamingSystems.add(ns);
						deprecatedManifest.addEntry(manifestEntry);
					} else {
						v3PublishingManifest.addEntry(manifestEntry);
						// namingSystems do not go in rendering manifest
						if (cs.getInternal()) {
							//internalManifest.addEntry(manifestEntry);
							intNamingSystems.add(ns);
						} else {
							//externalManifest.addEntry(manifestEntry);
							extNamingSystems.add(ns);
						}
					}
				}
			}
		}

		postProcess();

		for (CodeSystem cs : intCodeSystems) {
			String resourcePath = Utilities.path(dest, FolderNameConstants.V3, FolderNameConstants.CODESYSTEMS, cs.getId()) + ".xml";
			new XmlParser().setOutputStyle(OutputStyle.PRETTY).compose(new FileOutputStream(resourcePath), cs);
		}
		
		for (CodeSystem cs : extCodeSystems) {
			String resourcePath = Utilities.path(dest, FolderNameConstants.EXTERNAL, FolderNameConstants.V3, FolderNameConstants.CODESYSTEMS, cs.getId()) + ".xml";
			new XmlParser().setOutputStyle(OutputStyle.PRETTY).compose(new FileOutputStream(resourcePath), cs);
		}
		
		for (CodeSystem cs : depCodeSystems) {
			String resourcePath = Utilities.path(dest, FolderNameConstants.RETIRED, FolderNameConstants.CODESYSTEMS, cs.getId()) + ".xml";
			new XmlParser().setOutputStyle(OutputStyle.PRETTY).compose(new FileOutputStream(resourcePath), cs);
		}
		
		for (NamingSystem ns : intNamingSystems) {
			String resourcePath = Utilities.path(dest, FolderNameConstants.V3, FolderNameConstants.NAMINGSYSTEMS, ns.getId()) + ".xml";
			new XmlParser().setOutputStyle(OutputStyle.PRETTY).compose(new FileOutputStream(resourcePath), ns);
		}
		
		for (NamingSystem ns : extNamingSystems) {
			String resourcePath = Utilities.path(dest, FolderNameConstants.EXTERNAL, FolderNameConstants.V3, FolderNameConstants.NAMINGSYSTEMS, ns.getId()) + ".xml";
			new XmlParser().setOutputStyle(OutputStyle.PRETTY).compose(new FileOutputStream(resourcePath), ns);
		}
		
		for (NamingSystem ns : depNamingSystems) {
			String resourcePath = Utilities.path(dest, FolderNameConstants.RETIRED, FolderNameConstants.NAMINGSYSTEMS, ns.getId()) + ".xml";
			new XmlParser().setOutputStyle(OutputStyle.PRETTY).compose(new FileOutputStream(resourcePath), ns);
		}
		
		
		System.out.println("Save v3 code systems (" + Integer.toString(csmap.size()) + " found)");

		reportUndefinedConceptProperties("V3");
	}

	private CodeSystem generateV3CodeSystem(Element item) throws Exception {
		String oid = item.getAttribute("codeSystemId");
		
		if (OIDLookup.doNotGenerate(oid))
			return null;
		
		CodeSystem cs = new CodeSystem();
		
		String originalName = item.getAttribute("name").trim();
		String className = Utilities.makeClassName(originalName);
		String shortClassName = StringUtils.left(className, 61);
		String shortId = StringUtils.left(makeSafeId(originalName), 61);

		cs.setId("v3-" + shortId);
		cs.setName(shortClassName);
		cs.setTitle(item.getAttribute("title"));

		cs.setHierarchyMeaning(CodeSystemHierarchyMeaning.ISA);
		cs.setVersion(V3_INITIAL_VERSION);
		
		if (OIDLookup.hasUrlOverride(oid)) {
			cs.setUrl(OIDLookup.getUrl(oid));
		} else if (OIDLookup.noUrl(oid)) {
			cs.setUrl(null);
		} else {
			cs.setUrl("http://terminology.hl7.org/CodeSystem/" + cs.getId());
		}
		if (cs.getUrl() != null) {
			knownCS.add(cs.getUrl());
		}

		cs.getIdentifier().setSystem("urn:ietf:rfc:3986").setValue("urn:oid:" + oid);
		cs.setUserData("oid", item.getAttribute("codeSystemId"));
		cs.setStatus(PublicationStatus.ACTIVE);

		// DT 6/4/19
		// Make two passes through elements, to insure that header and released version are processed 
		// before history items and annotations
		
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("header")) {
				processHeader(child, cs);
			} else if (child.getNodeName().equals("releasedVersion")) {
				processReleasedVersion(child, cs);
			} else if (child.getNodeName().equals("historyItem")) {
				// NO OP on first pass
			} else if (child.getNodeName().equals("annotations")) {
				// NO OP on first pass
			} else {
				throw new Exception("Unprocessed element " + child.getNodeName());
			}
			child = XMLUtil.getNextSibling(child);
		}
		
		child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("header")) {
				// NO OP second pass
			} else if (child.getNodeName().equals("releasedVersion")) {
				// NO OP second pass
			} else if (child.getNodeName().equals("historyItem")) {
				processHistoryItem(child, cs);
			} else if (child.getNodeName().equals("annotations")) {
				processCSAnnotations(child, cs);
			} else {
				throw new Exception("Unprocessed element " + child.getNodeName());
			}
			child = XMLUtil.getNextSibling(child);
		}
		
		
		// Check for concept properties not defined in code system
		findUndefinedConceptProperties(cs);
		
		csByName.put(originalName.toLowerCase(), cs);

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
			
			List<ConceptDefinitionComponent> moved = new LinkedList<ConceptDefinitionComponent>();
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
							addCodeSystemProperty(cs, "subsumedBy", PropertyType.CODE, "The concept code of a parent concept");
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
		String licenseTerms = "";
		String versioningPolicy = "";
		while (child != null) {
			if (child.getNodeName().equals("notation")) {
				notations.add(child.getTextContent());
				cs.addExtension(csext("legalese"), new StringType(child.getTextContent()));
			} else if (child.getNodeName().equals("licenseTerms")) {
				licenseTerms = child.getTextContent();
				//cs.setCopyright(child.getTextContent());
			} else if (child.getNodeName().equals("versioningPolicy")) {
				versioningPolicy = child.getTextContent();
				//cs.addExtension(resext("versioningPolicy"), new StringType(child.getTextContent()));
			} else {
				throw new Exception("Unprocessed element " + child.getNodeName());
			}
			child = XMLUtil.getNextSibling(child);
		}
		String copyright = String.join(" ", licenseTerms, versioningPolicy).trim();
		if (!copyright.isEmpty()) {
			cs.setCopyright(copyright);
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

		
/*		Extension ext = new Extension().setUrl(csext("contributor"));
		cs.getExtension().add(ext);
		ext.addExtension("name", new StringType(name));
		ext.addExtension("role", new StringType(role));
		if (notes != null) {
			ext.addExtension("notes", new StringType(notes));
		}
*/
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
		cs.setInternal(Boolean.parseBoolean(item.getAttribute("hl7MaintainedIndicator")));
		if (!cs.getInternal()) {
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
		//if (item.hasAttribute("isSubstantiveChange"))
		//	ext.addExtension("substantive", new BooleanType(item.getAttribute("isSubstantiveChange")));
		//if (item.hasAttribute("isBackwardCompatibleChange"))
		//	ext.addExtension("backwardCompatible", new BooleanType(item.getAttribute("isBackwardCompatibleChange")));

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
				String desc = XMLUtil.htmlToXmlEscapedPlainText(child).trim();
				if (desc.isEmpty()) {
					System.out.println("Empty description for code system '" + cs.getName() + "'");
				} else {
					//cs.setDescription(desc);
					cs.setDescription(MarkDownProcessor.htmlToMarkdown(child));
					Map<String, String> additionalProperties = extractAdditionalPropertiesFromText(desc);
					for (String propertyName : additionalProperties.keySet()) {
						cs.addExtension(resext(propertyName), new StringType(additionalProperties.get(propertyName)));
					}
					// Do not add text block - will be auto-generated
					/*
					XhtmlNode html = new XhtmlParser().parseHtmlNode(child);
					html.setName("div");
					if (cs.hasLanguage()) {
						html.getAttributes().put("xml:lang", cs.getLanguage());
					}
					cs.getText().setDiv(html);
					cs.getText().setStatus(NarrativeStatus.GENERATED);
					*/
				}
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
		// cs.addExtension(resext("versionDeprecated"), Factory.newString_(v));
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
			if (PropertyLookup.V3_PROPERTY_URIS.containsKey(item.getAttribute("name"))) {
				pd.setUri(PropertyLookup.V3_PROPERTY_URIS.get(item.getAttribute("name")));
			}
			pd.setType(PropertyType.CODING);
			// pd.addExtension(csext("relationshipKind"), new CodeType(item.getAttribute("relationshipKind")));
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
		if (PropertyLookup.V3_PROPERTY_URIS.containsKey(item.getAttribute("propertyName"))) {
			pd.setUri(PropertyLookup.V3_PROPERTY_URIS.get(item.getAttribute("propertyName")));
		}
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
		if (PropertyLookup.V3_PROPERTY_URIS.containsKey(propertyName)) {
			pd.setUri(PropertyLookup.V3_PROPERTY_URIS.get(propertyName));
		}
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
					addCodeSystemProperty(cs, "synonymCode", PropertyType.CODE, "An additional concept code that was also attributed to a concept");
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
			if (child.getNodeName().equals("text")) {
				String def = XMLUtil.htmlToXmlEscapedPlainText(child).trim();
				//cd.setDefinition(def);
				cd.setDefinition(MarkDownProcessor.htmlToMarkdown(child));
				
				Map<String, String> additionalProperties = extractAdditionalPropertiesFromText(def);
				for (String propertyName : additionalProperties.keySet()) {
					cd.addExtension(resext(propertyName), new StringType(additionalProperties.get(propertyName)));
				}
			
			} else {
				throw new Exception("Unprocessed element " + child.getNodeName());
			}
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
			if (child.getNodeName().equals("text")) {
				//cd.addExtension(resext("openIssue"), new StringType(MarkDownProcessor.htmlToMarkdown(child)));
			} else {
				throw new Exception("Unprocessed element " + child.getNodeName());
			}
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processConceptDeprecationInfo(Element item, ConceptDefinitionComponent cd) throws Exception {
		String v = item.getAttribute("deprecationEffectiveVersion");
		if (Utilities.noString(v))
			throw new Exception("Element not understood: " + item.getNodeName());
		// cd.addExtension(resext("versionDeprecated"), Factory.newString_(v));
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

		/*
		if (!"true".equals(item.getAttribute("preferredForLanguage")))
			cd.addDesignation().setUse(new Coding().setSystem("http://terminology.hl7.org/hl7TermMaintInfra").setCode("deprecated alias"))
					.setValue(item.getAttribute("text"));
		else if (Utilities.noString(item.getAttribute("language"))
				|| item.getAttribute("language").equals(cs.getLanguage()))
			cd.setDisplay(item.getAttribute("text"));
		else
			cd.addDesignation().setLanguage(item.getAttribute("language")).setValue(item.getAttribute("text"));
		*/
		
		String language = item.getAttribute("language").trim();
		// Default language to code system language if not specified
		if (language.isEmpty()) {
			language = cs.getLanguage();
		}
		String printName = item.getAttribute("text").trim();
		boolean isPreferred = ("true".equalsIgnoreCase(item.getAttribute("preferredForLanguage")));
		boolean isCodeSystemLanguage = (language.equalsIgnoreCase(cs.getLanguage()));
		
		if (isCodeSystemLanguage && isPreferred && (cd.getDisplay() == null || cd.getDisplay().isEmpty())) {
			cd.setDisplay(printName);
		} else {
			Coding use = (isPreferred)? 
							new Coding().setSystem("http://terminology.hl7.org/hl7TermMaintInfra").setCode("preferredForLanguage") :
							new Coding().setSystem("http://snomed.info/sct").setCode("synonym");
							
			cd.addDesignation().setLanguage(language)
					.setUse(use)
					.setValue(printName);
		}
		
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
		addCodeSystemProperty(cs, "status", PropertyType.CODE, "Designation of a concept's state. Normally is not populated unless the state is retired.");
		
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
						parents = new LinkedList<String>();
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

	public void generateValueSets(ListResource v3PublishingManifest, ListResource v3RenderingManifest, ListResource deprecatedManifest) throws Exception {
		List<Element> list = new LinkedList<Element>();
		XMLUtil.getNamedChildren(mif, "valueSet", list);
		HashMap<String, ValueSet> vsmap = new HashMap<String, ValueSet>();
		List<ValueSet> deprecatedValueSets = new LinkedList<ValueSet>();
		
		for (Element l : list) {
			ValueSet vs = generateV3ValueSet(l);
			ListEntryComponent manifestEntry = ListResourceExt.createValueSetListEntry(vs); 
			String oid = vs.getUserString("oid");
			
			if (OIDLookup.isDeprecated(oid)) {
				deprecatedValueSets.add(vs);
				deprecatedManifest.addEntry(manifestEntry);
			} else {
				vsmap.put(oid, vs);
				v3PublishingManifest.addEntry(manifestEntry);
				v3RenderingManifest.addEntry(manifestEntry);
			}

		}
		postProcess(vsmap);

		for (ValueSet vs : vsmap.values()) {
			new XmlParser().setOutputStyle(OutputStyle.PRETTY)
					.compose(new FileOutputStream(Utilities.path(dest, FolderNameConstants.V3, FolderNameConstants.VALUESETS, vs.getId()) + ".xml"), vs);
		}

		for (ValueSet vs : deprecatedValueSets) {
			new XmlParser().setOutputStyle(OutputStyle.PRETTY)
					.compose(new FileOutputStream(Utilities.path(dest, FolderNameConstants.RETIRED, FolderNameConstants.VALUESETS, vs.getId()) + ".xml"), vs);
		}
		
		
		System.out.println("Save v3 value sets (" + Integer.toString(vsmap.size()) + " found)");
		System.out.println("Save deprecated v3 value sets (" + Integer.toString(deprecatedValueSets.size()) + " found)");
		System.out.println("Unknown systems");
		for (String s : sorted(systems))
			if (!knownCS.contains(s))
				System.out.println(" ..unknown.. " + s);
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
		
		String shortSafeName = StringUtils.left(makeSafeId(item.getAttribute("name")), 61);
		
		vs.setId("v3-" + shortSafeName);
		vs.setUrl("http://terminology.hl7.org/ValueSet/" + vs.getId());
		vs.setName(shortSafeName);
		vs.setTitle(item.getAttribute("name"));
		vs.addIdentifier().setSystem("urn:ietf:rfc:3986").setValue("urn:oid:" + item.getAttribute("id"));
		vs.setUserData("oid", item.getAttribute("id"));
		vs.setStatus(PublicationStatus.ACTIVE);
		if ("true".equals(item.getAttribute("isImmutable")))
			vs.setImmutable(true);
		
		vs.setVersion(V3_INITIAL_VERSION);
		
		Element child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("header")) {
				processHeader(child, vs);
			} else if (child.getNodeName().equals("version")) {
				processVersion(child, vs);
			} else if (child.getNodeName().equals("annotations")) {
				// No op first pass
			} else if (child.getNodeName().equals("historyItem")) {
				// No op first pass
			} else {
				throw new Exception("Unprocessed element " + child.getNodeName());
			}
			child = XMLUtil.getNextSibling(child);
		}

		child = XMLUtil.getFirstChild(item);
		while (child != null) {
			if (child.getNodeName().equals("header")) {
				// No op second pass
			} else if (child.getNodeName().equals("version")) {
				// No op second pass
			} else if (child.getNodeName().equals("annotations")) {
				processVSAnnotations(child, vs);
			} else if (child.getNodeName().equals("historyItem")) {
				processHistoryItem(child, vs);
			} else {
				throw new Exception("Unprocessed element " + child.getNodeName());
			}
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
		String licenseTerms = "";
		String versioningPolicy = "";
		while (child != null) {
			if (child.getNodeName().equals("notation"))
				notations.add(child.getTextContent());
			else if (child.getNodeName().equals("licenseTerms"))
				licenseTerms = child.getTextContent();
			else if (child.getNodeName().equals("versioningPolicy"))
				versioningPolicy = child.getTextContent();
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
		String copyright = String.join(" ", licenseTerms, versioningPolicy).trim();
		if (!copyright.isEmpty()) {
			vs.setCopyright(copyright);
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
				String desc = XMLUtil.htmlToXmlEscapedPlainText(child).trim();
				if (desc == null || desc.isEmpty()) {
					System.out.println("Empty description for value set '" + vs.getName() + "'");
				} else {
					//vs.setDescription(desc);
					vs.setDescription(MarkDownProcessor.htmlToMarkdown(child));
					
					// Do not generate text block - will be auto-generated per Ted
					/*
					XhtmlNode html = new XhtmlParser().parseHtmlNode(child);
					html.setName("div");
					if (vs.hasLanguage()) {
						html.getAttributes().put("xml:lang", vs.getLanguage());
					}
					vs.getText().setDiv(html);
					vs.getText().setStatus(NarrativeStatus.GENERATED);
					*/

					Element grandChild = XMLUtil.getFirstChild(child);
					while (grandChild != null) {
						if (grandChild.getNodeName().equals("p")) {
							Element greatGrandChild = XMLUtil.getFirstChild(grandChild);
							if (greatGrandChild != null && greatGrandChild.getNodeName().equals("i")
									&& greatGrandChild.getTextContent() != null
									&& greatGrandChild.getTextContent().startsWith("Steward:")) {
								String steward = normalizeStewardValue(grandChild.getTextContent().trim());
								vs.addExtension("http://hl7.org/fhir/StructureDefinition/structuredefinition-wg", new CodeType(steward));
							}
						}
						grandChild = XMLUtil.getNextSibling(grandChild);
					}
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
				//vs.setPurpose(XMLUtil.htmlToXmlEscapedPlainText(child));
				vs.setPurpose(MarkDownProcessor.htmlToMarkdown(child));
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
		// vs.addExtension(resext("versionDeprecated"), Factory.newString_(v));
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
		Extension ext = new Extension().setUrl("http://hl7.org/fhir/StructureDefinition/resource-history");
		vs.getExtension().add(ext);
		// ext.addExtension("id", new StringType(item.getAttribute("id")));
		//vs.setVersion(item.getAttribute("id"));
		ext.addExtension("id", new StringType(item.getAttribute("id")));
		ext.addExtension("date", new DateTimeType(item.getAttribute("dateTime")));
		if (item.hasAttribute("responsiblePersonName"))
			ext.addExtension("author", new StringType(item.getAttribute("responsiblePersonName")));
		// if (item.hasAttribute("isSubstantiveChange"))
		//	 ext.addExtension("substantive", new BooleanType(item.getAttribute("isSubstantiveChange")));
		// if (item.hasAttribute("isBackwardCompatibleChange"))
		//	 ext.addExtension("backwardCompatible", new BooleanType(item.getAttribute("isBackwardCompatibleChange")));

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
				processAssociatedConceptProperty(child, vs);
			else if (child.getNodeName().equals("content"))
				processContent(child, vs);
			else
				throw new Exception("Unprocessed element " + child.getNodeName());
			child = XMLUtil.getNextSibling(child);
		}
	}

	private void processAssociatedConceptProperty(Element item, ValueSet vs) throws Exception {
		String propertyName = item.getAttribute("name").trim();
		String propertyValue = item.getAttribute("value").trim();
		
		if (propertyName.isEmpty()) {
			throw new Exception("No name attribute for Associated Concept Property in Value Set " + vs.getName());
		}

		Extension ext = vs.addExtension().setUrl(vsext("hl7-assocConceptProp"));
		ext.addExtension().setUrl("name").setValue(new StringType(propertyName));
		if (!propertyValue.isEmpty()) {
			ext.addExtension().setUrl("value").setValue(new StringType(propertyValue));
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
		boolean noChildren = (child == null);
		
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
	
		if ((noChildren) || (level == 0 && !vs.getCompose().hasExclude() && !vs.getCompose().hasInclude())) {
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

		if (!code.isEmpty() && "false".equalsIgnoreCase(item.getAttribute("includeHeadCode"))) {
			ConceptSetComponent excludeHeadCodeSet = new ConceptSetComponent();
			excludeHeadCodeSet.setSystem(cset.getSystem());
			excludeHeadCodeSet.addConcept().setCode(code);
			addToValuesetCompose(vs, excludeHeadCodeSet, false);
		}
		
		
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
	
	private String normalizeStewardValue(String steward) throws Exception {
		steward = steward.substring(steward.indexOf(":")+1).trim().toLowerCase();
		
		if (!steward.isEmpty() && !V3_STEWARD_MAP.containsKey(steward)) {
			throw new Exception("Unrecognized Steward value: " + steward);
		}
		return V3_STEWARD_MAP.get(steward);
	}

	private void addCodeSystemProperty(CodeSystem cs, String propertyCode, PropertyType type, String description) {
		if (!cs.hasPropertyCode(propertyCode)) {
			PropertyComponent pd = cs.addProperty();
			pd.setCode(propertyCode);
			if (PropertyLookup.V3_PROPERTY_URIS.containsKey(propertyCode)) {
				pd.setUri(PropertyLookup.V3_PROPERTY_URIS.get(propertyCode));
			}
			pd.setType(type);
			pd.setDescription(description);

			Extension ext = new Extension().setUrl(csext("mif-extended-properties"));
			pd.getExtension().add(ext);
			ext.addExtension("isMandatory", new BooleanType(false));

			if (propertyCode.equals("status")) {
				ext.addExtension("defaultValue", new StringType("active"));
			}
			
		}
	}
}
