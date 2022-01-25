package org.hl7.fhir.utg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Resource;

public class CodeSystemScanner {

	private static final String rootFolder = "C:\\Users\\dtriglianos\\Workspaces\\UTG\\input\\sourceOfTruth";
	
	public static void main(String[] args) {

		try {
			Stream<Path> codeSystemsMissingTitle = Files.find(Paths.get(rootFolder), 100, (path, basicFileAttributes) -> {
				try {
				    File file = path.toFile();
				    if (!file.isDirectory()) {
				    	if (file.getName().endsWith(".xml")) {
				    		InputStream inputStream = new FileInputStream(file);
				    		Resource resource = new XmlParser().parse(inputStream);
							inputStream.close();
							if (resource instanceof CodeSystem) {
				    			CodeSystem codeSystem = (CodeSystem) resource;
				    			if (!codeSystem.hasTitleElement() || codeSystem.getTitle().isEmpty()) {
				    				return true;
				    			}
				    		}
				    	}
				    }
				} catch (Exception e) {
					System.out.println("Could not parse " + path.toString() + ": " + e.getMessage() );
				}
				return false;
			});

			Stream<Path> filesWithFunnyValues = Files.find(Paths.get(rootFolder), 100, (path, basicFileAttributes) -> {
				try {
				    File file = path.toFile();
				    if (!file.isDirectory()) {
				    	if (file.getName().endsWith(".xml")) {
				    		boolean found = false;
				    		BufferedReader br = new BufferedReader(new FileReader(file)); 
				    		String st; 
							while ((st = br.readLine()) != null) { 
								if (st.contains("\"--\"")) {
									found = true;
									break;
								}
							}
							br.close();
							return found;
				    	}
				    }
				} catch (Exception e) {
					System.out.println("Could not parse " + path.toString() + ": " + e.getMessage() );
				}
				return false;
			});

			System.out.println("Resources missing title:");
			System.out.println("==================================");
			codeSystemsMissingTitle.forEach(path -> {
				System.out.println(path.toString());
			});
			System.out.println("==================================");
			codeSystemsMissingTitle.close();

			System.out.println("Resources with funny values:");
			System.out.println("==================================");
			filesWithFunnyValues.forEach(path -> {
				System.out.println(path.toString());
			});
			System.out.println("==================================");
			filesWithFunnyValues.close();
		
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
