package org.hl7.fhir.utg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r4.model.CodeSystem.ConceptPropertyComponent;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.xml.XMLUtil;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class UTGUpdater {

	private static Path sotPath;
	private static Element mif;

	public static void main(String[] args) throws Exception {
		String problems = "";
		if (args.length > 2) {
			System.out.println("Warning: Only six arguments are required: Output Folder, V2 Access DB filename, V3 Coremif filename, FHIR Source URL, CDA Source Folder, External Provider Manifest filename. Additional arguments will be ignored.");
		}
		
		if (args.length < 2) {
			problems += "\t- Two arguments are required: Output Folder, V3 Coremif filename.\n";
		} else {
			if (!Files.isDirectory(Paths.get(args[0]))) {
				problems += "\t- Specified output folder does not exist or is not a directory.\n";
			}
			if (Files.notExists(Paths.get(args[1]))) {
				problems += "\t- Specified V3 Coremif file does not exist.\n";
			}
		}

		if (problems.isEmpty()) {
			String dest = args[0]; // the vocabulary repository to populate
			String v3source = args[1]; // MIF file name
			sotPath = Paths.get(dest);
			
			loadMif(v3source);
			execute();
			
		} else {
			System.out.println("One or more problems exist with the specified parameters:\n" + problems);
		}

	}
	
	
	private static void execute() throws Exception {

		List<Element> codeSystemElements = new LinkedList<Element>();
		XMLUtil.getNamedChildren(mif, "codeSystem", codeSystemElements);
		
		for (Element codeSystemElement : codeSystemElements) {
			
			List<ConceptPropertyComponent> defaultProperties = new LinkedList<>();
			List<ConceptPropertyComponent> hierarchicalProperties = new LinkedList<>();
			
			Element releasedVersionElement = XMLUtil.getNamedChildren(codeSystemElement, "releasedVersion").get(0);
			List<Element> propertyElements = XMLUtil.getNamedChildren(releasedVersionElement, "supportedConceptProperty");

			for (Element propertyElement : propertyElements) {
				String propertyName = propertyElement.getAttribute("propertyName");
				String propertyType = propertyElement.getAttribute("type");
				String defaultValue = propertyElement.getAttribute("defaultValue");
				String handlingCode = propertyElement.getAttribute("defaultHandlingCode");

				ConceptPropertyComponent cpc = new ConceptPropertyComponent();
				cpc.setCode(propertyName);

				if (!defaultValue.isEmpty()) {
					switch (propertyType.toUpperCase()) {
					case "TOKEN":
						cpc.setValue(new CodeType(defaultValue));
						break;
					case "STRING":
						cpc.setValue(new StringType(defaultValue));
						break;
					case "BOOLEAN":
						cpc.setValue(new BooleanType(defaultValue));
						break;
					default:
						throw new RuntimeException("Unexpected property type '" + propertyType + "'");
					}
					defaultProperties.add(cpc);
				}
				
				if (handlingCode.equalsIgnoreCase("Hierarchical")) {
					hierarchicalProperties.add(cpc);
				}
			}
			
			if (!defaultProperties.isEmpty() || !hierarchicalProperties.isEmpty()) {
				Path csPath = getCodeSystemFilePath(codeSystemElement);
				CodeSystem cs = getCodeSystemFromPath(csPath);
				cs.setUserData("modified", false);

				// process hierarchical properties
				if (!hierarchicalProperties.isEmpty()) {
					loadConceptHierarchy(cs);
					for (ConceptPropertyComponent hierarchicalProperty : hierarchicalProperties) {
						for (ConceptDefinitionComponent concept : cs.getConcept()) {
							processHierarchicalProperty(cs, concept, hierarchicalProperty.getCode());
						}
					}
				}

				// process default properties
				if (!defaultProperties.isEmpty()) {
					for (ConceptDefinitionComponent concept : cs.getConcept()) {
						List<ConceptPropertyComponent> conceptProperties = concept.getProperty();
						
						for (ConceptPropertyComponent defaultProperty : defaultProperties) {
							String propertyName = defaultProperty.getCode();
							if (conceptProperties.stream().noneMatch(p -> p.getCode().equalsIgnoreCase(propertyName))) {
								concept.addProperty(defaultProperty.copy());
								cs.setUserData("modified", true);
							}
						}
					}
				}
				
			
				
				if (cs.getUserData("modified").equals(true)) {
					System.out.println("Writing changes to CodeSystem " + cs.getId());
					new XmlParser().setOutputStyle(OutputStyle.PRETTY).compose(new FileOutputStream(csPath.toFile()), cs);
				}
			}
		}

		System.out.println("Done.");

	}
	
	@SuppressWarnings("unchecked")
	private static void processHierarchicalProperty(CodeSystem cs, ConceptDefinitionComponent concept, String hierarchicalPropertyName) {
		List<ConceptDefinitionComponent> subsumedChildren = (List<ConceptDefinitionComponent>) concept.getUserData("children");
		List<ConceptDefinitionComponent> definedChildren = concept.getConcept();

		for (ConceptPropertyComponent conceptProperty : concept.getProperty()) {
			if (conceptProperty.getCode().equalsIgnoreCase(hierarchicalPropertyName)) {
				if (subsumedChildren != null) {
					for (ConceptDefinitionComponent childConcept : subsumedChildren) {
						if (childConcept.getProperty().stream().noneMatch(childConProp -> childConProp.getCode().equalsIgnoreCase(hierarchicalPropertyName))) {
							childConcept.addProperty(conceptProperty.copy());
							cs.setUserData("modified", true);
						}
					}
				}
				
				if (definedChildren != null) {
					for (ConceptDefinitionComponent childConcept : definedChildren) {
						if (childConcept.getProperty().stream().noneMatch(childConProp -> childConProp.getCode().equalsIgnoreCase(hierarchicalPropertyName))) {
							childConcept.addProperty(conceptProperty.copy());
							cs.setUserData("modified", true);
						}
					}
				}
			}
		}
		if (subsumedChildren != null) subsumedChildren.stream().forEach(childConcept -> processHierarchicalProperty(cs, childConcept, hierarchicalPropertyName));
		if (definedChildren != null)  definedChildren.stream().forEach(childConcept -> processHierarchicalProperty(cs, childConcept, hierarchicalPropertyName));
	}
	
	@SuppressWarnings("unchecked")
	private static void loadConceptHierarchy(CodeSystem cs) throws Exception {
		Map<String, ConceptDefinitionComponent> conceptMap = new HashMap<>();
		
		for (ConceptDefinitionComponent concept : cs.getConcept()) {
			concept.setUserData("children", new LinkedList<ConceptDefinitionComponent>());
			conceptMap.put(concept.getCode(), concept);
		}
		
		for (ConceptDefinitionComponent concept : cs.getConcept()) {
			concept.getProperty().stream()
				.filter(p -> p.getCode().equalsIgnoreCase("subsumedBy"))
				.forEach(p -> {
					String parentConceptCode;
					try {
						parentConceptCode = p.getValueCodeType().asStringValue();
						List<ConceptDefinitionComponent> children = (List<ConceptDefinitionComponent>) conceptMap.get(parentConceptCode).getUserData("children");
						children.add(concept);
					} catch (FHIRException e) {
						throw new RuntimeException("Error loading concept hierarchy", e);
					}
				});
		}
		
	}
	
	public static void loadMif(String v3source)
			throws ParserConfigurationException, FileNotFoundException, SAXException, IOException {
		System.out.println("loading v3 Source");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		mif = builder.parse(new FileInputStream(new File(v3source))).getDocumentElement();

	}

	private static CodeSystem getCodeSystemFromPath(Path csPath) throws Exception {
		File csFile = csPath.toFile();
		InputStream inputStream = new FileInputStream(csFile);
		Resource resource = null;
		CodeSystem cs = null;
		try {
			resource = new XmlParser().parse(inputStream);
		} catch (FHIRFormatError e) {
			throw new RuntimeException("FHIR Format Error parsing file '" + csPath.getFileName().toString() + "'");
		} finally {
			inputStream.close();
		}
		
		if (resource instanceof CodeSystem) {
			cs = (CodeSystem) resource;
		} else {
			throw new RuntimeException("Resource from '" + csPath.getFileName().toString() + "' could not be cast to a CodeSystem");
		}
		
		return cs;
	}

	private static Path getCodeSystemFilePath(Element csElement) throws Exception {
		String originalName = csElement.getAttribute("name").trim();
		String shortId =  StringUtils.left(makeSafeId(originalName), 61);
		String csFileName = "v3-" + shortId + ".xml";
		
		Optional<Path> csPath = Files.find(sotPath, 100, (path, basicFileAttributes) -> {
			File file = path.toFile();
            return !file.isDirectory() &&
                    file.getName().contains(csFileName);
		}).findFirst();
		
		if (!csPath.isPresent()) {
			throw new RuntimeException("No file found for '" + csFileName + "'");
		}
		
		return csPath.get();
	}

	
	private static String makeSafeId(String s) {
		if (s.contains("(")) {
			s = s.replace('(', '-').replace(')', ' ').trim();
		}

		return Utilities.makeId(s);
	}


}
