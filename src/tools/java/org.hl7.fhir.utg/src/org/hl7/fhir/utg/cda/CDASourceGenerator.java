package org.hl7.fhir.utg.cda;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ListResource;
import org.hl7.fhir.r4.model.ListResource.ListEntryComponent;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.utg.BaseGenerator;
import org.hl7.fhir.utg.fhir.ListResourceExt;
import org.hl7.fhir.utilities.FolderNameConstants;
import org.hl7.fhir.utilities.Utilities;

public class CDASourceGenerator extends BaseGenerator {

	private File[] sourceFiles = null;

	public CDASourceGenerator(String dest, Map<String, CodeSystem> csmap, Set<String> knownCS) {
		super(dest, csmap, knownCS);

	}

	public void load(String folder) {
		System.out.println("Reading CDA Source Folder");
		File dir = new File(folder);
		if (dir.isDirectory()) {
			sourceFiles = dir.listFiles();
		}
	}

	public void generateResources(ListResource cdaManifest) throws FHIRFormatError, IOException {
		System.out.println("Generating CDA Resources");
		if (sourceFiles != null) {
			for (File sourceFile : sourceFiles) {
				InputStream inputStream = new FileInputStream(sourceFile);
				Resource resource = new XmlParser().parse(inputStream);
				inputStream.close();
				
				// Do massaging here
				
				// Write resource file
				String resourcePath = Utilities.path(dest, FolderNameConstants.CDA, resource.getId()) + ".xml";
				new XmlParser().setOutputStyle(OutputStyle.PRETTY).compose(new FileOutputStream(resourcePath), resource);
				
				// Add manifest entry
				ListEntryComponent manifestEntry = null;
				
				if (resource instanceof CodeSystem) {
					manifestEntry = ListResourceExt.createCodeSystemListEntry((CodeSystem) resource);
				} else if (resource instanceof ValueSet) {
					manifestEntry = ListResourceExt.createValueSetListEntry((ValueSet) resource);
				}
				
				if (manifestEntry != null) {
					cdaManifest.addEntry(manifestEntry);
				}
				
				
			}
		} else {
			// Handle the case where dir is not really a directory.
			// Checking dir.isDirectory() above would not be sufficient
			// to avoid race conditions with another process that deletes
			// directories.
		}
	}

	public static void main(String[] args) {

	}

}
