package org.hl7.fhir.utg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemContentMode;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemHierarchyMeaning;
import org.hl7.fhir.r4.model.CodeSystem.PropertyType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.ListResource;
import org.hl7.fhir.r4.model.TemporalPrecisionEnum;
import org.hl7.fhir.utg.cda.CDASourceGenerator;
import org.hl7.fhir.utg.external.ExternalProvider;
import org.hl7.fhir.utg.fhir.FHIRSourceGenerator;
import org.hl7.fhir.utg.fhir.ListResourceExt;
import org.hl7.fhir.utg.v2.V2SourceGenerator;
import org.hl7.fhir.utg.v3.V3SourceGenerator;
import org.hl7.fhir.utilities.FolderNameConstants;
import org.hl7.fhir.utilities.PlaceHolderFile;
import org.hl7.fhir.utilities.Utilities;
import org.xml.sax.SAXException;

public class UTGGenerator extends BaseGenerator {

	public static final String CONTEXT_BINDING_PREFIX = "contextBinding";
	
	public static final List<String> BINDING_REALMS = new ArrayList<String>(
			Arrays.asList("UV","R1","C1","X1","US"));
	
	public static void main(String[] args) throws Exception {

		String problems = "";
		if (args.length > 6) {
			System.out.println("Warning: Only six arguments are required: Output Folder, V2 Access DB filename, V3 Coremif filename, FHIR Source URL, CDA Source Folder, External Provider Manifest filename. Additional arguments will be ignored.");
		}
		
		if (args.length < 6) {
			problems += "\t- Six arguments are required: Output Folder, V2 Access DB filename, V3 Coremif filename, FHIR Source URL, CDA Source Folder, External Provider Manifest filename.\n";
		} else {
			if (!Files.isDirectory(Paths.get(args[0]))) {
				problems += "\t- Specified output folder does not exist or is not a directory.\n";
			}
			if (Files.notExists(Paths.get(args[1]))) {
				problems += "\t- Specified V2 Access DB file does not exist.\n";
			}
			if (Files.notExists(Paths.get(args[2]))) {
				problems += "\t- Specified V3 Coremif file does not exist.\n";
			}
			if (Files.notExists(Paths.get(args[4]))) {
				problems += "\t- Specified CDA source folder does not exist.\n";
			}
			if (Files.notExists(Paths.get(args[5]))) {
				problems += "\t- Specified External Provider Manifest file does not exist.\n";
			}
		}

		if (problems.isEmpty()) {
			String dest = args[0]; // the vocabulary repository to populate
			String v2source = args[1]; // access database name
			String v3source = args[2]; // MIF file name
			String fhirSourceUrl = args[3]; // FHIR Source URL
			String cdaSourceFolder = args[4]; // CDA Source Folder
			String externalProviderManifest = args[5]; // External Provider Manifest
			
			new UTGGenerator(dest, v2source, v3source, fhirSourceUrl, cdaSourceFolder, externalProviderManifest).execute();
		} else {
			System.out.println("One or more problems exist with the specified parameters:\n" + problems);
		}

	}

	private Date currentVersionDate;
	private V3SourceGenerator v3;
	private V2SourceGenerator v2;
	private FHIRSourceGenerator fhirGenerator;
	private CDASourceGenerator cdaGenerator;
	private Map<String, ExternalProvider> externalProviders;
	
	public UTGGenerator(String dest, String v2source, String v3source, String fhirSourceUrl, String cdaSourceFolder, String externalProviderManifest) throws IOException, ClassNotFoundException,
			SQLException, FHIRException, SAXException, ParserConfigurationException {
		super(dest, new HashMap<String, CodeSystem>(), new HashSet<String>());
		createMissingOutputFolders();

		externalProviders = ExternalProvider.getExternalProviders(externalProviderManifest);
		v2 = new V2SourceGenerator(dest, csmap, knownCS);
		v3 = new V3SourceGenerator(dest, csmap, knownCS, externalProviders);
		fhirGenerator = new FHIRSourceGenerator(dest, csmap, knownCS);
		cdaGenerator = new CDASourceGenerator(dest, csmap, knownCS);
		
		v2.load(v2source);
		v3.load(v3source);
		fhirGenerator.load(fhirSourceUrl);
		cdaGenerator.load(cdaSourceFolder);
	}

	private void execute() throws Exception {
		ListResource v2Publishing = createManifestList("V2 Publishing Manifest", "v2-Publishing");
		ListResource v3Publishing = createManifestList("V3 Publishing Manifest", "v3-Publishing");
		ListResource unifiedManifest = createManifestList("Unified Rendering Manifest", "unified-Rendering");
		ListResource externalManifest = createManifestList("External Rendering Manifest", "external-Rendering");
		ListResource fhirManifest = createManifestList("FHIR Rendering Manifest", "fhir-Rendering");
		ListResource fhirNormativeManifest = createManifestList("FHIR Normative Manifest", "fhir-Normative");
		ListResource cdaManifest = createManifestList("CDA Rendering Manifest", "cda-Rendering");
		//ListResource nsManifest = createManifestList("Naming Systems Manifest", "namingSystems-Rendering");
		
		v2.loadTables();
		v3.loadMif();
		v2.process();
		v2.generateTables(v2Publishing);
		v2.generateCodeSystems(v2Publishing, externalManifest);

		//v3.generateCodeSystems(v3Publishing, externalManifest, nsManifest);
		v3.generateCodeSystems(v3Publishing, externalManifest);
		v3.generateValueSets(v3Publishing);
		generateConceptDomains(unifiedManifest);
		generateStaticUnifiedCodeSystems(unifiedManifest);

		fhirGenerator.generateCodeSystems(fhirManifest, fhirNormativeManifest);
		
		cdaGenerator.generateResources(cdaManifest);
		
		writeManifest(Utilities.path(dest, FolderNameConstants.CONTROL, "v2-Publishing.xml"), v2Publishing);
		writeManifest(Utilities.path(dest, FolderNameConstants.CONTROL, "v3-Publishing.xml"), v3Publishing);

		v2Publishing.setTitle("V2 Rendering Manifest");
		v2Publishing.setId("v2-rendering");
		writeManifest(Utilities.path(dest, FolderNameConstants.CONTROL, "v2-Rendering.xml"), v2Publishing);

		v3Publishing.setTitle("V3 Rendering Manifest");
		v3Publishing.setId("v3-rendering");
		writeManifest(Utilities.path(dest, FolderNameConstants.CONTROL, "v3-Rendering.xml"), v3Publishing);
		
		writeManifest(Utilities.path(dest, FolderNameConstants.CONTROL, "unified-Rendering.xml"), unifiedManifest);
		writeManifest(Utilities.path(dest, FolderNameConstants.CONTROL, "external-Rendering.xml"), externalManifest);
		writeManifest(Utilities.path(dest, FolderNameConstants.CONTROL, "fhir-Rendering.xml"), fhirManifest);
		writeManifest(Utilities.path(dest, FolderNameConstants.CONTROL, "fhir-Normative.xml"), fhirNormativeManifest);
		writeManifest(Utilities.path(dest, FolderNameConstants.CONTROL, "cda-Rendering.xml"), cdaManifest);
		//writeManifest(Utilities.path(dest, FolderNameConstants.CONTROL, "namingSystems-Rendering.xml"), nsManifest);

		writeExternalManifestFiles();
		
		System.out.println("finished");
	}

	private void generateConceptDomains(ListResource manifest) throws FileNotFoundException, IOException, Exception {
		CodeSystem cs = new CodeSystem();
		cs.setId("conceptdomains");
		cs.setUrl("http://terminology.hl7.org/CodeSystem/ConceptDomain");
		cs.setName("ConceptDomains");
		cs.setTitle("Concept Domains");
		cs.setStatus(PublicationStatus.ACTIVE);
		cs.setExperimental(false);

		cs.setDateElement(new DateTimeType(currentVersionDate, TemporalPrecisionEnum.DAY));
		cs.setPublisher("HL7, Inc");
		cs.addContact().addTelecom().setSystem(ContactPointSystem.URL).setValue("https://github.com/HL7/UTG");
		cs.setDescription("Concept Domains - includes both v2 abd v3 concept domains");
		cs.setCopyright("Copyright HL7. Licensed under creative commons public domain");
		cs.setCaseSensitive(true);
		cs.setHierarchyMeaning(CodeSystemHierarchyMeaning.ISA);
		cs.setCompositional(false);
		cs.setVersionNeeded(false);
		cs.setContent(CodeSystemContentMode.COMPLETE);

		// TODO - Fix these property URIs?
		cs.addProperty().setCode("source").setUri("http://terminology.hl7.org/CodeSystem/ConceptDomain/")
				.setType(PropertyType.CODE);
		cs.addProperty().setCode("ConceptualSpaceForClassCode").setUri("http://terminology.hl7.org/CodeSystem/ConceptDomain/")
				.setType(PropertyType.CODING);
		cs.addProperty().setCode("openIssue").setUri("http://terminology.hl7.org/CodeSystem/ConceptDomain/")
				.setType(PropertyType.STRING);
		cs.addProperty().setCode("deprecationInfo").setUri("http://terminology.hl7.org/CodeSystem/ConceptDomain/")
				.setType(PropertyType.STRING);

		
		for (String realm : BINDING_REALMS) {
			String propertyCodePrefix = CONTEXT_BINDING_PREFIX + realm;
			cs.addProperty().setCode(propertyCodePrefix + "-valueSet").setUri("http://terminology.hl7.org/CodeSystem/ConceptDomain/").setType(PropertyType.STRING);
			//cs.addProperty().setCode(propertyCodePrefix + "-codingStrength").setUri("http://terminology.hl7.org/CodeSystem/ConceptDomain/").setType(PropertyType.CODE);
			//cs.addProperty().setCode(propertyCodePrefix + "-effectiveDate").setUri("http://terminology.hl7.org/CodeSystem/ConceptDomain/").setType(PropertyType.DATETIME);
		}
		
		Map<String, String> codes = new HashMap<String, String>();

		int count = cs.getConcept().size() + v3.addConceptDomains(cs.getConcept(), codes);
		count = count + v2.addConceptDomains(cs, codes);

		new XmlParser().setOutputStyle(OutputStyle.PRETTY)
				.compose(new FileOutputStream(Utilities.path(dest, FolderNameConstants.UNIFIED, FolderNameConstants.CODESYSTEMS, "conceptdomains.xml")), cs);
		System.out.println("Save conceptdomains (" + Integer.toString(count) + " found)");
		
		manifest.addEntry(ListResourceExt.createCodeSystemListEntry(cs));
	}
	
	private void generateStaticUnifiedCodeSystems(ListResource manifest) {
		CodeSystem cs = new CodeSystem();
		cs.setId("hl7TermMaintInfra");
		cs.setUrl("http://terminology.hl7.org/CodeSystem/hl7TermMaintInfra");
		cs.setName("Hl7TermMaintInfra");
		cs.setTitle("HL7 Terminology Maintenance Infrastructure Vocabulary");
		cs.setStatus(PublicationStatus.ACTIVE);

		manifest.addEntry(ListResourceExt.createCodeSystemListEntry(cs));
	}

	private void createMissingOutputFolders() throws IOException {
		Files.createDirectories(Paths.get(Utilities.path(dest)));
		
		// Clear the output folders in case any already existed and contained prior data
		Utilities.clearDirectory(Utilities.path(dest, FolderNameConstants.V2, FolderNameConstants.CODESYSTEMS));
		Utilities.clearDirectory(Utilities.path(dest, FolderNameConstants.V2, FolderNameConstants.VALUESETS));
		Utilities.clearDirectory(Utilities.path(dest, FolderNameConstants.V2, FolderNameConstants.NAMINGSYSTEMS));
		
		Utilities.clearDirectory(Utilities.path(dest, FolderNameConstants.V3, FolderNameConstants.CODESYSTEMS));
		Utilities.clearDirectory(Utilities.path(dest, FolderNameConstants.V3, FolderNameConstants.VALUESETS));
		Utilities.clearDirectory(Utilities.path(dest, FolderNameConstants.V3, FolderNameConstants.NAMINGSYSTEMS));
		
		Utilities.clearDirectory(Utilities.path(dest, FolderNameConstants.EXTERNAL, FolderNameConstants.CODESYSTEMS));
		Utilities.clearDirectory(Utilities.path(dest, FolderNameConstants.EXTERNAL, FolderNameConstants.VALUESETS));
		Utilities.clearDirectory(Utilities.path(dest, FolderNameConstants.EXTERNAL, FolderNameConstants.NAMINGSYSTEMS));
		
		Utilities.clearDirectory(Utilities.path(dest, FolderNameConstants.DEPRECATED));

		Utilities.clearDirectory(Utilities.path(dest, FolderNameConstants.RELEASE));
		Utilities.clearDirectory(Utilities.path(dest, FolderNameConstants.CONTROL));
		Utilities.clearDirectory(Utilities.path(dest, FolderNameConstants.FHIR));
		Utilities.clearDirectory(Utilities.path(dest, FolderNameConstants.CDA));
		Utilities.clearDirectory(Utilities.path(dest, FolderNameConstants.CIMI));

		// Create output folders
		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.V2, FolderNameConstants.CODESYSTEMS)));
		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.V2, FolderNameConstants.VALUESETS)));
		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.V2, FolderNameConstants.NAMINGSYSTEMS)));
		
		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.V3, FolderNameConstants.CODESYSTEMS)));
		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.V3, FolderNameConstants.VALUESETS)));
		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.V3, FolderNameConstants.NAMINGSYSTEMS)));

		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.EXTERNAL, FolderNameConstants.V2, FolderNameConstants.CODESYSTEMS)));
		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.EXTERNAL, FolderNameConstants.V2, FolderNameConstants.VALUESETS)));

		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.EXTERNAL, FolderNameConstants.V3, FolderNameConstants.CODESYSTEMS)));
		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.EXTERNAL, FolderNameConstants.V3, FolderNameConstants.VALUESETS)));
		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.EXTERNAL, FolderNameConstants.V3, FolderNameConstants.NAMINGSYSTEMS)));
		
		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.DEPRECATED, FolderNameConstants.CODESYSTEMS)));
		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.DEPRECATED, FolderNameConstants.VALUESETS)));
		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.DEPRECATED, FolderNameConstants.NAMINGSYSTEMS)));

		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.UNIFIED, FolderNameConstants.CODESYSTEMS)));
		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.UNIFIED, FolderNameConstants.VALUESETS)));
		
		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.RELEASE)));
		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.CONTROL)));
		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.FHIR)));
		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.CDA)));
		Files.createDirectories(Paths.get(Utilities.path(dest, FolderNameConstants.CIMI)));

		// Create placeholder files
		PlaceHolderFile.create(Utilities.path(dest, FolderNameConstants.CDA));
		PlaceHolderFile.create(Utilities.path(dest, FolderNameConstants.CIMI));
		PlaceHolderFile.create(Utilities.path(dest, FolderNameConstants.FHIR));
		PlaceHolderFile.create(Utilities.path(dest, FolderNameConstants.CONTROL));
		PlaceHolderFile.create(Utilities.path(dest, FolderNameConstants.RELEASE));
		
		PlaceHolderFile.create(Utilities.path(dest, FolderNameConstants.DEPRECATED, FolderNameConstants.CODESYSTEMS));
		PlaceHolderFile.create(Utilities.path(dest, FolderNameConstants.DEPRECATED, FolderNameConstants.VALUESETS));
		PlaceHolderFile.create(Utilities.path(dest, FolderNameConstants.DEPRECATED, FolderNameConstants.NAMINGSYSTEMS));

		PlaceHolderFile.create(Utilities.path(dest, FolderNameConstants.UNIFIED, FolderNameConstants.CODESYSTEMS));
		PlaceHolderFile.create(Utilities.path(dest, FolderNameConstants.UNIFIED, FolderNameConstants.VALUESETS));

		PlaceHolderFile.create(Utilities.path(dest, FolderNameConstants.EXTERNAL, FolderNameConstants.V2, FolderNameConstants.CODESYSTEMS));
		PlaceHolderFile.create(Utilities.path(dest, FolderNameConstants.EXTERNAL, FolderNameConstants.V2, FolderNameConstants.VALUESETS));

		PlaceHolderFile.create(Utilities.path(dest, FolderNameConstants.EXTERNAL, FolderNameConstants.V3, FolderNameConstants.CODESYSTEMS));
		PlaceHolderFile.create(Utilities.path(dest, FolderNameConstants.EXTERNAL, FolderNameConstants.V3, FolderNameConstants.VALUESETS));
		PlaceHolderFile.create(Utilities.path(dest, FolderNameConstants.EXTERNAL, FolderNameConstants.V3, FolderNameConstants.NAMINGSYSTEMS));

	}
	
	private void writeExternalManifestFiles() throws Exception {
		String outputPath = Utilities.path(dest, FolderNameConstants.EXTERNAL);
		System.out.println("Writing " + externalProviders.size() + " External Provider Manifests");
		for (ExternalProvider provider : externalProviders.values()) {
			writeManifest(outputPath + File.separator + provider.getFilename(), provider.getManifest());
		}
		if (ExternalProvider.hasUnclassifiedCodeSystems()) {
			System.out.println("Writing Manifest for Unclassified External CodeSystems");
			ExternalProvider provider = ExternalProvider.getCatchAll();
			writeManifest(outputPath + File.separator + provider.getFilename(), provider.getManifest());
		}
		
	}

	private void writeManifest(String path, ListResource manifest) throws Exception {
		String manifestName = manifest.getId();
		new XmlParser().setOutputStyle(OutputStyle.PRETTY).compose(new FileOutputStream(path), manifest);
		System.out.println("Manifest '" + manifestName + "' saved");
	}

	private static ListResource createManifestList(String title, String id) throws Exception {
		ListResource manifest = ListResourceExt.createManifestList(title, id);
		
		manifest.setCode(new CodeableConcept().addCoding(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/hl7TermMaintInfra").setCode("UTGCTGManifest")));
		manifest.setOrderedBy(new CodeableConcept().addCoding(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/list-order").setCode("alphabetic")));
		
		return manifest;
	}
	
}
