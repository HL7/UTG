package org.hl7.fhir.utg.v3;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r4.model.CodeSystem.ConceptPropertyComponent;
import org.hl7.fhir.r4.model.CodeSystem.PropertyComponent;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.xml.XMLUtil;
public class PropertyExport {

    public static void main(String[] args) throws Exception {

        // variables
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        String dest = "C:\\utg\\content-export\\v3\\codeSystems\\";
        final String NotSelectableProperty = "notSelectable";
                
        CodeSystem cs = initCodeSystem();

        // Code SYstem Node
        ConceptDefinitionComponent codeSystemCD = cs.addConcept();
        codeSystemCD.setCode("codeSystem");
		ConceptPropertyComponent cpc = codeSystemCD.addProperty();
		cpc.setCode(NotSelectableProperty);
		cpc.setValue(new BooleanType(true));
        
        // Value Set Node
        ConceptDefinitionComponent valueSetCD = cs.addConcept();
        valueSetCD.setCode("valueSet");
        cpc = valueSetCD.addProperty();
		cpc.setCode(NotSelectableProperty);
		cpc.setValue(new BooleanType(true));
        
		// Concept Domain Node
        ConceptDefinitionComponent conceptDomainCD = cs.addConcept();
        conceptDomainCD.setCode("conceptDomain");
        cpc = conceptDomainCD.addProperty();
        cpc.setCode(NotSelectableProperty);
		cpc.setValue(new BooleanType(true));
        
        // ConceptReletionshipType Node
		ConceptDefinitionComponent conceptRelationshipTypeCD = cs.addConcept();
        conceptRelationshipTypeCD.setCode("conceptRelationshipType");
        cpc = conceptRelationshipTypeCD.addProperty();
        cpc.setCode(NotSelectableProperty);
		cpc.setValue(new BooleanType(true));
		
		// Concept Node
        ConceptDefinitionComponent conceptCD = cs.addConcept();
        conceptCD.setCode("concept");
        cpc = conceptCD.addProperty();
        cpc.setCode(NotSelectableProperty);
		cpc.setValue(new BooleanType(true));
        
		// Code Node
        ConceptDefinitionComponent codeCD = cs.addConcept();
        codeCD.setCode("code");
        cpc = codeCD.addProperty();
        cpc.setCode(NotSelectableProperty);
		cpc.setValue(new BooleanType(true));
        

        // Step 1: Loading or registering Oracle JDBC driver class
        try {

            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        }
        catch(ClassNotFoundException cnfex) {

            System.out.println("Problem in loading or "
                    + "registering MS Access JDBC driver");
            cnfex.printStackTrace();
        }

        // Step 2: Opening database connection
        try {

            String msAccDB = "C:\\utg\\content-sources\\rim_none_Voc1418_20180401_Repository20180331.mdb";
            String dbURL = "jdbc:ucanaccess://" + msAccDB; 

            connection = DriverManager.getConnection(dbURL); 
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM VCS_property_definition"); 

            while(resultSet.next()) {
            	
            	String type = resultSet.getString("targetObjectType");
            	
            	switch(type) {
            	  case "codeSystem":  addSubConcept(codeSystemCD, resultSet);  break;
            	  case "valueSet": addSubConcept(valueSetCD, resultSet); break;
            	  case "conceptDomain": addSubConcept(conceptDomainCD, resultSet); break;
            	  case "conceptRelationshipType": addSubConcept(conceptRelationshipTypeCD, resultSet); break;
            	  case "concept": addSubConcept(conceptCD, resultSet); break;
            	  case "code": addSubConcept(codeCD, resultSet); break;
            	} 
         
            	
/*                System.out.println(resultSet.getString("targetObjectType") + "\t" + 
                        resultSet.getString("propertyName") + "\t" + 
                        resultSet.getString("propertyDescription") + "\t" +
                        resultSet.getString("propertyDT") + "\t" +
                        resultSet.getString("propertyAddTS") + "\t" +
                        resultSet.getString("propertyIsMandatory") + "\t" +
                        resultSet.getString("propertyDefaultValue") + "\t" +
                        resultSet.getString("usedInRepository") + "\t" +
                        resultSet.getString("usedInMif") + "\t" +
                        resultSet.getString("propertyActive") + "\t" +
                        resultSet.getString("propertyDefaultHandlingCode"));*/
            }
        }
        catch(SQLException sqlex){
            sqlex.printStackTrace();
        }
        finally {

            // Step 3: Closing database connection
            try {
                if(null != connection) {

                    // cleanup resources, once after processing
                    resultSet.close();
                    statement.close();

                    // and then finally close connection
                    connection.close();
                }
            }
            catch (SQLException sqlex) {
                sqlex.printStackTrace();
            }
        }
    	new XmlParser().setOutputStyle(OutputStyle.PRETTY).compose(new FileOutputStream(Utilities.path(dest, cs.getId())+".xml"), cs);
        System.out.println("Save property code system");
    }
    
    
    private static void addSubConcept(ConceptDefinitionComponent parentConcept, ResultSet rs) throws Exception {
    	ConceptDefinitionComponent cd = parentConcept.addConcept();
    	
    	cd.setCode(rs.getString("propertyName"));
    	cd.setDisplay(rs.getString("propertyDescription"));
    	
    	// Add propertyDT
    	ConceptPropertyComponent cpc = cd.addProperty();
    	cpc.setCode("propertyDT");
    	cpc.setValue(new StringType(rs.getString("propertyDT")));
    	
    	// Add propertyAddTS property
    	cpc = cd.addProperty();
    	cpc.setCode("propertyAddTS");
    	cpc.setValue(new StringType(rs.getString("propertyAddTS")));
    	
    	// Add propertyIsMandatory property
    	cpc = cd.addProperty();
    	cpc.setCode("propertyIsMandatory");
    	cpc.setValue(new StringType(rs.getString("propertyIsMandatory")));

    	// Add propertyDefaultValue property
    	cpc = cd.addProperty();
    	cpc.setCode("propertyDefaultValue");
    	cpc.setValue(new StringType(rs.getString("propertyDefaultValue")));

    	// Add propertyDefaultValue property
    	cpc = cd.addProperty();
    	cpc.setCode("usedInRepository");
    	cpc.setValue(new StringType(rs.getString("usedInRepository")));

    	// Add usedInMif property
    	cpc = cd.addProperty();
    	cpc.setCode("usedInMif");
    	cpc.setValue(new StringType(rs.getString("usedInMif")));
    	
    	// Add propertyActive property
    	cpc = cd.addProperty();
    	cpc.setCode("propertyActive");
    	cpc.setValue(new StringType(rs.getString("propertyActive")));

    	// Add propertyDefaultHandlingCode property
    	cpc = cd.addProperty();
    	cpc.setCode("propertyDefaultHandlingCode");
    	cpc.setValue(new StringType(rs.getString("propertyDefaultHandlingCode")));

    }
    
    
    private static CodeSystem initCodeSystem() throws Exception {
        CodeSystem cs = new CodeSystem();
        cs.setId("HL7 Terminology Properties");
        cs.setLanguage("en");
        cs.setUrl("http://terminology.hl7.org/codesystem/hl7TerminologyProperties");
        cs.setName("HL7 Terminology Properties");
        cs.setTitle("HL7 Terminology Properties");
        cs.getIdentifier().setSystem("urn:ietf:rfc:3986").setValue("urn:oid:"+ "2.16.840.1.113883.5.1141");
        cs.setStatus(PublicationStatus.ACTIVE);
        
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        today = sdf.parse(sdf.format(today));
        cs.setDate(today);
        
        cs.setPublisher("HL7");
        cs.setDescription("\r\n" + 
        		"	* Internal code system of the terminology objects (code systems, value sets, codes, concepts, concept domains, etc.) that are maintained in the Unified Terminology Governance (UTG) process and tooling.  Used for maintenance operations wholly within HL7 and the UTG tool suite, although many of the properties are surfaced as part of HL7 published code systems and value sets.hl7TermProperties.\r\n" + 
        		"Open Issue: As of July 2018 only the V3 terminology properties have been implemented.  Still remaining is to integrate the V2 and FHIR properties.\r\n" + 
        		"\r\n" + 
        		"");
        
        return cs;
    }
    
    protected static String csext(String name) {
        return "http://hl7.org/fhir/StructureDefinition/codeSystem-"+name;
      }
    

    
}
