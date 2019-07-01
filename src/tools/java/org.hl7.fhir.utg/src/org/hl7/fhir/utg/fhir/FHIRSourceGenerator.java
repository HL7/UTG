package org.hl7.fhir.utg.fhir;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.ListResource;
import org.hl7.fhir.r4.model.ListResource.ListEntryComponent;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.utg.BaseGenerator;
import org.hl7.fhir.utilities.FolderNameConstants;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

public class FHIRSourceGenerator extends BaseGenerator {

	private static final String DEFAULT_FHIR_SOURCE_URL = "http://build.fhir.org/valuesets.xml";
	// private static final String INTERNAL_FHIR_URL_PREFIX =
	// "http://hl7.org/fhir/";
	private static final String EXTERNAL_FHIR_URL_PREFIX = "http://terminology.hl7.org/codesystem/";

	private Element sourceRoot;

	public FHIRSourceGenerator(String dest, Map<String, CodeSystem> csmap, Set<String> knownCS) {
		super(dest, csmap, knownCS);
	}

	public void load() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		this.load(DEFAULT_FHIR_SOURCE_URL);
	}

	public void load(String fhirSourceUrl)
			throws ParserConfigurationException, FileNotFoundException, SAXException, IOException {
		System.out.println("loading FHIR Source");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		// String sourceXML = readUrl(FHIR_SOURCE_URL);
		this.sourceRoot = builder.parse(fhirSourceUrl).getDocumentElement();
	}

	public void generateCodeSystems(ListResource fhirManifest, ListResource fhirNormativeManifest)
			throws FHIRFormatError, IOException {

		System.out.println("Generating FHIR CodeSystems");
		
		List<Element> resourceList = new LinkedList<Element>();

		int codeSystemCount = 0;
		int normativeCount = 0;
		
		XMLUtil.getNamedChildren(sourceRoot, "entry", resourceList);
		for (Element entryElement : resourceList) {
			Element resourceElement = XMLUtil.getNamedChild(entryElement, "resource");
			String codeSystemXml = innerXml(resourceElement);

			InputStream inputStream = new ByteArrayInputStream(codeSystemXml.getBytes(Charset.forName("UTF-8")));

			Resource resource = new XmlParser().parse(inputStream);

			
			if (resource instanceof CodeSystem) {
				CodeSystem cs = (CodeSystem) resource;
				Extension standardsStatusExt = cs.getExtensionByUrl(
						"http://hl7.org/fhir/StructureDefinition/structuredefinition-standards-status");

				boolean isExternal = (cs.getUrl().toLowerCase().startsWith(EXTERNAL_FHIR_URL_PREFIX));
				boolean isNormative = (standardsStatusExt != null
						&& standardsStatusExt.getValue().toString().equalsIgnoreCase("normative"));

				if (isExternal) {
					codeSystemCount++;
					// Do any massaging here
					
					// Write resource to file
					String resourcePath = Utilities.path(dest, FolderNameConstants.FHIR, cs.getId()) + ".xml";
					new XmlParser().setOutputStyle(OutputStyle.PRETTY).compose(new FileOutputStream(resourcePath), cs);

					// Create manifest entries
					ListEntryComponent manifestEntry = ListResourceExt.createCodeSystemListEntry(cs);
					fhirManifest.addEntry(manifestEntry);
					if (isNormative) {
						normativeCount++;
						fhirNormativeManifest.addEntry(manifestEntry);
					}
				}
			}
		}

		System.out.println("Generated " + codeSystemCount + " FHIR CodeSystems, " + normativeCount + " Normative");
	}

	public String innerXml(Node node) {
		DOMImplementationLS lsImpl = (DOMImplementationLS) node.getOwnerDocument().getImplementation().getFeature("LS",
				"3.0");
		LSSerializer lsSerializer = lsImpl.createLSSerializer();
		lsSerializer.getDomConfig().setParameter("xml-declaration", false);
		NodeList childNodes = node.getChildNodes();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < childNodes.getLength(); i++) {
			sb.append(lsSerializer.writeToString(childNodes.item(i)));
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		try {
			FHIRSourceGenerator gen = new FHIRSourceGenerator("", new HashMap<String, CodeSystem>(),
					new HashSet<String>());
			gen.load();
			System.out.println("Done Reading Source");

			gen.generateCodeSystems(null, null);
			System.out.println("Done generating code systems");

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
