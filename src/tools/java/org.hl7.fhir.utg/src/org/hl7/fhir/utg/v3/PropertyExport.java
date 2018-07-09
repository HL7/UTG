package org.hl7.fhir.utg.v3;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
    	cd.addExtension(csext("propertyDT"), new StringType(rs.getString("propertyDT")));
    	cd.addExtension(csext("propertyAddTS"), new StringType(rs.getString("propertyAddTS")));
    	cd.addExtension(csext("propertyIsMandatory"), new StringType(rs.getString("propertyIsMandatory")));
    	cd.addExtension(csext("propertyIsMandatory"), new StringType(rs.getString("propertyIsMandatory")));
    	cd.addExtension(csext("propertyDefaultValue"), new StringType(rs.getString("propertyDefaultValue")));
    	cd.addExtension(csext("usedInRepository"), new StringType(rs.getString("usedInRepository")));
    	cd.addExtension(csext("usedInMif"), new StringType(rs.getString("usedInMif")));
    	cd.addExtension(csext("propertyActive"), new StringType(rs.getString("propertyActive")));
    	cd.addExtension(csext("propertyDefaultHandlingCode"), new StringType(rs.getString("propertyDefaultHandlingCode")));
    	
    }
    
    
    private static CodeSystem initCodeSystem() throws Exception {
        CodeSystem cs = new CodeSystem();
        cs.setId("v3-Properties");
        cs.setUrl("http://hl7.org/fhir/ig/vocab-poc/CodeSystem/v3-Properties");
        cs.setName("v3-Properties");
        cs.setTitle("v3-Properties");
        //cs.setUserData("oid", item.getAttribute("codeSystemId"));
        cs.setStatus(PublicationStatus.ACTIVE);
        return cs;
    }
    
    protected static String csext(String name) {
        return "http://hl7.org/fhir/StructureDefinition/codeSystem-"+name;
      }
    

    
}
