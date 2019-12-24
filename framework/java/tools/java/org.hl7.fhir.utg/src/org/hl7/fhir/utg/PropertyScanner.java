package org.hl7.fhir.utg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.PropertyComponent;
import org.hl7.fhir.r4.model.ListResource;
import org.hl7.fhir.r4.model.ListResource.ListEntryComponent;

@SuppressWarnings("serial")
public class PropertyScanner {

	private static List<String> sources = new LinkedList<String>() {{
		add("CDA");
		add("V2");
		add("V3");
	}};

	private static Map<String, String> folders = new HashMap<String, String>() {{
		put("CDA", "C:\\Users\\dtriglianos\\Workspaces\\UTG\\sourceOfTruth\\v3\\codeSystems");
		put("V2",  "C:\\Users\\dtriglianos\\Workspaces\\UTG\\sourceOfTruth\\v2\\codeSystems");
		put("V3",  "C:\\Users\\dtriglianos\\Workspaces\\UTG\\sourceOfTruth\\v3\\codeSystems");
	}};
	
	private static Map<String, String> manifests = new HashMap<String, String>() {{
		put("CDA", "C:\\Users\\dtriglianos\\Workspaces\\UTG\\sourceOfTruth\\control-manifests\\cda-Rendering.xml");
		put("V2",  "C:\\Users\\dtriglianos\\Workspaces\\UTG\\sourceOfTruth\\control-manifests\\v2-Rendering.xml");
		put("V3",  "C:\\Users\\dtriglianos\\Workspaces\\UTG\\sourceOfTruth\\control-manifests\\v3-Rendering.xml");
	}};
	
	private static Map<String, List<String>> properties = new HashMap<String, List<String>>() {{
		put("CDA", new LinkedList<String>());
		put("V2", new LinkedList<String>());
		put("V3", new LinkedList<String>());
	}};
	
	public static void main(String[] args) {
		
		for (String source : sources) {
			File manifestFile = new File(manifests.get(source));
			InputStream inputStream = null;
			ListResource manifest = null;

			try {
				inputStream = new FileInputStream(manifestFile);
				manifest = (ListResource) new XmlParser().parse(inputStream);
				inputStream.close();
			} catch (FileNotFoundException e) {
				System.out.println("File not found: '" + manifestFile.getName() + "'");
				return;
			} catch (FHIRFormatError e) {
				System.out.println("FHIR Format Error parsing '" + manifestFile.getName() + "'");
				return;
			} catch (IOException e) {
				System.out.println("I/O Error parsing '" + manifestFile.getName() + "'");
				return;
			}
			
			List<ListEntryComponent> entryList = manifest.getEntry();
			
			for (ListEntryComponent entry : entryList) {
				if (!entry.getItem().getType().equalsIgnoreCase("CodeSystem")) {
					continue;
				}
				String reference = entry.getItem().getReference();
				String[] parts = reference.split("/");
				String resourceName = parts[parts.length-1];
				String resourceFilename = folders.get(source) + "\\" + resourceName + ".xml";
				
				if (resourceName.equalsIgnoreCase("v2-tables")) {
					resourceFilename = "C:\\Users\\dtriglianos\\Workspaces\\UTG\\sourceOfTruth\\v2\\" + resourceName + ".xml";
				} else if (source.equalsIgnoreCase("V2")) {
					resourceFilename = folders.get(source) + "\\cs-" + resourceName + ".xml";
				} else {
					resourceFilename = folders.get(source) + "\\" + resourceName + ".xml";
				}
				
				File resourceFile = new File(resourceFilename);
				CodeSystem codeSystem = null;
				try {
					inputStream = new FileInputStream(resourceFile);
					codeSystem = (CodeSystem) new XmlParser().parse(inputStream);
					inputStream.close();
					
				} catch (FileNotFoundException e) {
					System.out.println("File not found for " + source + ": '" + resourceFilename + "'. Skipped");
					continue;
				} catch (FHIRFormatError e) {
					System.out.println("FHIR Format Error parsing '" + resourceFilename + "'");
					return;
				} catch (IOException e) {
					System.out.println("I/O Error parsing '" + resourceFilename + "'");
					return;
				}
				
				List<PropertyComponent> csProperties = codeSystem.getProperty();
				
				for (PropertyComponent property : csProperties) {
					if (!properties.get(source).contains(property.getCode())) {
						properties.get(source).add(property.getCode());
					}
				}
			}
			
		}
		
		for (String source : sources) {
			System.out.println(source + ":");
			List<String> sourcePropertyCodes = properties.get(source);
			Collections.sort(sourcePropertyCodes);
			for (String property : sourcePropertyCodes) {
				System.out.println("\t" + property);
			}
			System.out.println();
		}
	}

}
