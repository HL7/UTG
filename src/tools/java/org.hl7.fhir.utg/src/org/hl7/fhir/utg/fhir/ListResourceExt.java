package org.hl7.fhir.utg.fhir;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.hl7.fhir.dstu3.model.Enumerations.ResourceType;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ListResource;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ListResource.ListEntryComponent;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.utilities.Utilities;;

public class ListResourceExt {
	public static ListResource createManifestList(String title) throws Exception {
		ListResource manifest = new ListResource();
		manifest.setId(UUID.randomUUID().toString());   
		manifest.setStatus(ListResource.ListStatus.CURRENT);
		manifest.setMode(ListResource.ListMode.WORKING);
		   
		if (!Utilities.noString(title))
			manifest.setTitle(title);
		
		return manifest;
	}
	
	public static ListEntryComponent createListEntry(String url, String version, String type) {
		if (Utilities.noString(url))
			return null;
	      
	    if (!Utilities.noString(version))
	        url += "|" + version;
	      
	    Reference referenceEntry = new Reference(url);
	    
	    if (!Utilities.noString(type))
	    	referenceEntry.setType(type);
	      
	    ListEntryComponent entry = new ListEntryComponent(referenceEntry);
	      
	    return entry;
	}	

	public static ListEntryComponent createCodeSystemListEntry(CodeSystem resource, String version) {	
		return createListEntry(resource.getUrl(), version, ResourceType.CODESYSTEM.getDisplay());
	}
	
	public static ListEntryComponent createCodeSystemListEntry(CodeSystem resource, Boolean useReleaseDate) {
		String version = null;
		
		if (useReleaseDate) {
			Date releaseDate = resource.getDate();
		      
		    if (releaseDate != null) {
		        DateFormat df = new SimpleDateFormat("YYYY-MM-dd");
		        version =  "|" + df.format(releaseDate);
		    }
		}
		else {
			version = resource.getVersion();
		}		
		
		return createListEntry(resource.getUrl(), version, ResourceType.CODESYSTEM.getDisplay());
	}	

	public static ListEntryComponent createValueSetListEntry(ValueSet resource, String version) {	
		return createListEntry(resource.getUrl(), version, ResourceType.VALUESET.getDisplay());
	}
	
	public static ListEntryComponent createValueSetListEntry(ValueSet resource, Boolean useReleaseDate) {
		String version = null;
		
		if (useReleaseDate) {
			Date releaseDate = resource.getDate();
		      
		    if (releaseDate != null) {
		        DateFormat df = new SimpleDateFormat("YYYY-MM-dd");
		        version =  "|" + df.format(releaseDate);
		    }
		}
		else {
			version = resource.getVersion();
		}
		
		return createListEntry(resource.getUrl(), version, ResourceType.VALUESET.getDisplay());
	}

}
