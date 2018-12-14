package org.hl7.fhir.utg;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemContentMode;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemHierarchyMeaning;
import org.hl7.fhir.r4.model.CodeSystem.PropertyType;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.TemporalPrecisionEnum;
import org.hl7.fhir.utg.external.ExternalProvider;
import org.hl7.fhir.utg.v2.V2SourceGenerator;
import org.hl7.fhir.utg.v3.V3SourceGenerator;
import org.hl7.fhir.utilities.Utilities;
import org.xml.sax.SAXException;

public class UTGGenerator extends BaseGenerator {

	public static void main(String[] args) throws Exception {

		String problems = "";
		if (args.length > 4) {
			System.out.println("Warning: Only four arguments are required: Output Folder, V2 Access DB filename, V3 Coremif filename, External Provider Manifest filename. Additional arguments will be ignored.");
		}
		
		if (args.length < 3) {
			problems += "\t- Four arguments are required: Output Folder, V2 Access DB filename, V3 Coremif filename, External Provider Manifest filename.\n";
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
			if (Files.notExists(Paths.get(args[3]))) {
				problems += "\t- Specified External Provider Manifest file does not exist.\n";
			}
		}

		if (problems.isEmpty()) {
			String dest = args[0]; // the vocabulary repository to populate
			String v2source = args[1]; // access database name
			String v3source = args[2]; // MIF file name
			String externalProviderManifest = args[3]; // External Provider Manifest
			new UTGGenerator(dest, v2source, v3source, externalProviderManifest).execute();
		} else {
			System.out.println("One or more problems exist with the specified parameters:\n" + problems);
		}

	}

	private Date currentVersionDate;
	private V3SourceGenerator v3;
	private V2SourceGenerator v2;
	private Map<String, ExternalProvider> externalProviders;
	
	public UTGGenerator(String dest, String v2source, String v3source, String externalProviderManifest) throws IOException, ClassNotFoundException,
			SQLException, FHIRException, SAXException, ParserConfigurationException {
		super(dest, new HashMap<String, CodeSystem>(), new HashSet<String>());
		createMissingOutputFolders();

		externalProviders = ExternalProvider.getExternalProviders(externalProviderManifest);
		v2 = new V2SourceGenerator(dest, csmap, knownCS);
		v3 = new V3SourceGenerator(dest, csmap, knownCS, externalProviders);
		
		v2.load(v2source);
		v3.load(v3source);
	}

	private void execute() throws Exception {
		v2.loadTables();
		v3.loadMif();
		v2.process();
		generateConceptDomains();
		v2.generateTables();
		v2.generateCodeSystems();
		v2.mergeV2Manifests();
		v3.generateCodeSystems();
		v3.generateValueSets();
		v3.mergeV3Manifests();
		
		writeExternalManifestFiles();
		
		System.out.println("finished");
	}

	private void generateConceptDomains() throws FileNotFoundException, IOException {
		CodeSystem cs = new CodeSystem();
		cs.setId("conceptdomains");
		cs.setUrl("http://terminology.hl7.org/ConceptDomain/");
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

		cs.addProperty().setCode("source").setUri("http://terminology.hl7.org/ConceptDomain/")
				.setType(PropertyType.CODE);
		cs.addProperty().setCode("ConceptualSpaceForClassCode").setUri("http://somethingelse")
				.setType(PropertyType.CODE);

		Map<String, String> codes = new HashMap<String, String>();

		int count = cs.getConcept().size() + v3.addConceptDomains(cs.getConcept(), codes);
		count = count + v2.addConceptDomains(cs, codes);

		new XmlParser().setOutputStyle(OutputStyle.PRETTY)
				.compose(new FileOutputStream(Utilities.path(dest, "unified", "conceptdomains.xml")), cs);
		System.out.println("Save conceptdomains (" + Integer.toString(count) + " found)");
	}

	private void createMissingOutputFolders() throws IOException {
		Files.createDirectories(Paths.get(Utilities.path(dest, "unified")));
		Files.createDirectories(Paths.get(Utilities.path(dest, "release")));
		Files.createDirectories(Paths.get(Utilities.path(dest, "external")));
		Files.createDirectories(Paths.get(Utilities.path(dest, "v2", "codeSystems")));
		Files.createDirectories(Paths.get(Utilities.path(dest, "v2", "valueSets")));
		Files.createDirectories(Paths.get(Utilities.path(dest, "v3", "codeSystems")));
		Files.createDirectories(Paths.get(Utilities.path(dest, "v3", "valueSets")));

	}
	
	private void writeExternalManifestFiles() throws ParserConfigurationException, TransformerException, IOException {
		System.out.println("Writing " + externalProviders.size() + " External Provider Manifests");
		for (ExternalProvider provider : externalProviders.values()) {
			provider.writeXMLManifest(Utilities.path(dest, "external"));
		}
	}
}
