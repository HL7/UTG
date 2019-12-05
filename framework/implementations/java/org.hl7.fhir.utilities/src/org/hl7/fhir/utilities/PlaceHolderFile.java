package org.hl7.fhir.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PlaceHolderFile {
	
	private static final String defaultFileName = "README.md";
	private static final String defaultFileContent = "# UTG\r\nUnified Terminology Governance\r\n";
	
	public static void create(String filePath) throws IOException {
		create(filePath, defaultFileName, defaultFileContent);
	}
	
	public static void create(String filePath, String fileName, String content) throws IOException {
		String fullPath = Utilities.path(filePath, fileName); 
		if (Files.notExists(Paths.get(fullPath))) {
			TextFile.stringToFile(content, fullPath);
		}
	}
			
}
