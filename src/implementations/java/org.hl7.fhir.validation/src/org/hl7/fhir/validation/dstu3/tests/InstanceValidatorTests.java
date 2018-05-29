package org.hl7.fhir.validation.dstu3.tests;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.Charsets;
import org.hl7.fhir.dstu3.context.SimpleWorkerContext;
import org.hl7.fhir.dstu3.elementmodel.Element;
import org.hl7.fhir.dstu3.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.dstu3.model.Base;
import org.hl7.fhir.dstu3.model.ExpansionProfile;
import org.hl7.fhir.dstu3.model.TypeDetails;
import org.hl7.fhir.dstu3.test.support.TestingUtilities;
import org.hl7.fhir.dstu3.utils.FHIRPathEngine.IEvaluationContext;
import org.hl7.fhir.dstu3.utils.IResourceValidator.IValidatorResourceFetcher;
import org.hl7.fhir.dstu3.utils.IResourceValidator.ReferenceValidationPolicy;
import org.hl7.fhir.dstu3.utils.formats.JsonTrackingParser;
import org.hl7.fhir.dstu3.utils.formats.JsonTrackingParser.LocationData;
import org.hl7.fhir.dstu3.validation.InstanceValidator;
import org.hl7.fhir.exceptions.DefinitionException;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.exceptions.PathEngineException;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class InstanceValidatorTests implements IEvaluationContext, IValidatorResourceFetcher {

	private void parse(String path) throws Exception {
		Map<JsonElement, LocationData> map = new HashMap<JsonElement, JsonTrackingParser.LocationData>();
		
		String source = TextFile.fileToString(Utilities.path("C:\\work\\org.hl7.fhir", path));
		JsonObject obj = JsonTrackingParser.parse(source, map);
		Assert.assertTrue(obj != null);
	}
		
	private void validate(String path, int errorCount, boolean json) throws Exception {
	  if (TestingUtilities.context == null) {
    	TestingUtilities.context = SimpleWorkerContext.fromPack(Utilities.path(TestingUtilities.home(), "publish", "igpack.zip"));
      ((SimpleWorkerContext) TestingUtilities.context).connectToTSServer("http://tx.fhir.org/r3");
      TestingUtilities.context.setExpansionProfile(makeExpProfile());
    }

    if (!TestingUtilities.silent)
    System.out.println("Test "+path);
    FileInputStream file = new FileInputStream(Utilities.path(TestingUtilities.home(), path));
		InstanceValidator val = new InstanceValidator(TestingUtilities.context, this);
		val.setFetcher(this);
		List<ValidationMessage> errors = new ArrayList<ValidationMessage>();
		val.validate(null, errors, file, json ? FhirFormat.JSON : FhirFormat.XML);
		int ec = 0;
		for (ValidationMessage m : errors) {
			if (m.getLevel() == IssueSeverity.ERROR || m.getLevel() == IssueSeverity.FATAL) {
				ec++;
				if (!TestingUtilities.silent)
	        System.out.println("  "+m.summary());
			}
		}
		Assert.assertTrue(ec == errorCount);
//		if (!TestingUtilities.silent)
//		System.out.println(val.reportTimes());
  }
		
  private void validateCnt(String cnt, int errorCount, boolean json) throws Exception {
    if (TestingUtilities.context == null) {
      TestingUtilities.context = SimpleWorkerContext.fromPack(Utilities.path(TestingUtilities.home(), "publish", "igpack.zip"));
      ((SimpleWorkerContext) TestingUtilities.context).connectToTSServer("http://tx.fhir.org/r3");
    }

    if (!TestingUtilities.silent)
    System.out.println("Test Content");
    ByteArrayInputStream file = new ByteArrayInputStream(cnt.getBytes(Charsets.UTF_8));
    InstanceValidator val = new InstanceValidator(TestingUtilities.context, null);
    List<ValidationMessage> errors = new ArrayList<ValidationMessage>();
    val.validate(null, errors, file, json ? FhirFormat.JSON : FhirFormat.XML);
    int ec = 0;
    for (ValidationMessage m : errors) {
      if (m.getLevel() == IssueSeverity.ERROR || m.getLevel() == IssueSeverity.FATAL) {
        ec++;
        if (!TestingUtilities.silent)
        System.out.println("  "+m.summary());
      }
    }
    Assert.assertTrue(ec == errorCount);
    if (!TestingUtilities.silent)
    System.out.println(val.reportTimes());
  }
    
//	@Test
//	public void testCustom() throws Exception {
//		validate("publish\\sdc\\questionnaire-sdc-profile-example.xml", 0, false);
//	}
	
	
	@Test
	public void testXmlListMinimal() throws Exception {
		validate("tests\\validation-examples\\list-minimal.xml", 0, false);
	}

	@Test
	public void testXmlListWrongOrder() throws Exception {
		validate("tests\\validation-examples\\list-wrong-order.xml", 1, false);
	}

	@Test
	public void testXmlListWrongCode() throws Exception {
		validate("tests\\validation-examples\\list-wrong-code.xml", 1, false);
	}

	@Test
	public void testXmlListWrongNS() throws Exception {
		validate("tests\\validation-examples\\list-wrong-ns.xml", 1, false);
	}
	
	@Test
	public void testXmlListWrongNS1() throws Exception {
		validate("tests\\validation-examples\\list-wrong-ns1.xml", 1, false);
	}

	@Test
	public void testXmlListWrongNS2() throws Exception {
		validate("tests\\validation-examples\\list-wrong-ns2.xml", 1, false);
	}

	@Test
	public void testXmlListEmpty1() throws Exception {
		validate("tests\\validation-examples\\list-empty1.xml", 3, false);
	}

	@Test
	public void testXmlListEmpty2() throws Exception {
		validate("tests\\validation-examples\\list-empty2.xml", 3, false);
	}

	@Test
	public void testXmlListUnknownAttr() throws Exception {
		validate("tests\\validation-examples\\list-unknown-attr.xml", 1, false);
	}

	@Test
	public void testXmlListUnknownElement() throws Exception {
		validate("tests\\validation-examples\\list-unknown-element.xml", 1, false);
	}

	@Test
	public void testXmlListText() throws Exception {
		validate("tests\\validation-examples\\list-text.xml", 1, false);
	}

	@Test
	public void testXmlListExtension() throws Exception {
		validate("tests\\validation-examples\\list-extension.xml", 0, false);
	}

	@Test
	public void testXmlListXhtml1() throws Exception {
		validate("tests\\validation-examples\\list-xhtml-correct1.xml", 0, false);
	}

	@Test
	public void testXmlListXhtml2() throws Exception {
		validate("tests\\validation-examples\\list-xhtml-correct2.xml", 0, false);
	}

	@Test
	public void testXmlListXXE() throws Exception {
		validate("tests\\validation-examples\\list-xhtml-xxe1.xml", 1, false);
	}

	@Test
	public void testXmlListXXE2() throws Exception {
		validate("tests\\validation-examples\\list-xhtml-xxe2.xml", 1, false);
	}

	@Test
	public void testXmlListXhtmlWrongNs1() throws Exception {
		validate("tests\\validation-examples\\list-xhtml-wrongns1.xml", 1, false);
	}

	@Test
	public void testXmlListXhtmlWrongNs2() throws Exception {
		validate("tests\\validation-examples\\list-xhtml-wrongns2.xml", 1, false);
	}

	@Test
	public void testXmlListXhtmlWrongNs3() throws Exception {
		validate("tests\\validation-examples\\list-xhtml-wrongns3.xml", 1, false);
	}

	@Test
	public void testXmlListXhtmlBadElement() throws Exception {
		validate("tests\\validation-examples\\list-xhtml-element.xml", 2, false);
	}

	@Test
	public void testXmlListXhtmlBadAttribute() throws Exception {
		validate("tests\\validation-examples\\list-xhtml-attribute.xml", 1, false);
	}

	@Test
	public void testXmlbadSyntax() throws Exception {
		validate("tests\\validation-examples\\list-bad-syntax.xml", 1, false);
	}
	
	@Test
	public void testXmlContained() throws Exception {
		validate("tests\\validation-examples\\list-contained.xml", 0, false);
	}

	@Test
	public void testXmlContainedBad() throws Exception {
		validate("tests\\validation-examples\\list-contained-bad.xml", 3, false); // broken reference will generate 3 errors
	}

	@Test
	public void testXmlBundle() throws Exception {
		validate("tests\\validation-examples\\bundle-good.xml", 0, false);
	}
	
	@Test
	public void testXmlGroupOk() throws Exception {
		validate("tests\\validation-examples\\group-minimal.xml", 0, false);
	}

	@Test
	public void testXmlGroupGood() throws Exception {
		validate("tests\\validation-examples\\group-choice-good.xml", 0, false);
	}

	@Test
	public void testXmlGroupBad1() throws Exception {
		validate("tests\\validation-examples\\group-choice-bad1.xml", 2, false);
	}

	@Test
	public void testXmlGroupBad2() throws Exception {
		validate("tests\\validation-examples\\group-choice-bad2.xml", 1, false);
	}

	@Test
	public void testXmlGroupBad3() throws Exception {
		validate("tests\\validation-examples\\group-choice-bad3.xml", 1, false);
	}

	@Test
	public void testXmlGroupEmpty() throws Exception {
		validate("tests\\validation-examples\\group-choice-empty.xml", 2, false); // empty elements generate a double warning (well, most of them)
	}

  @Test
  public void testParametersReference() throws Exception {
    validate("tests\\validation-examples\\params-reference.xml", 0, false);
  }

 // --- json --------------------------------------------------------------------------

	@Test
	public void testJsonListMinimal() throws Exception {
		validate("tests\\validation-examples\\list-minimal.json", 0, true);
	}

	@Test
	public void testJsonListWrongOrder() throws Exception {
		validate("tests\\validation-examples\\list-wrong-order.json", 0, true);
	}

	@Test
	public void testJsonListWrongCode() throws Exception {
		validate("tests\\validation-examples\\list-wrong-code.json", 1, true);
	}

	@Test
	public void testJsonListEmpty1() throws Exception {
		validate("tests\\validation-examples\\list-empty1.json", 3, true);
	}

	@Test
	public void testJsonListEmpty2() throws Exception {
		validate("tests\\validation-examples\\list-empty2.json", 0, true);
	}

	@Test
	public void testJsonListUnknownProp() throws Exception {
		validate("tests\\validation-examples\\list-unknown-prop.json", 1, true);
	}

	@Test
	public void testJsonListExtension1() throws Exception {
		validate("tests\\validation-examples\\list-extension1.json", 0, true);
	}

	@Test
	public void testJsonListExtension2() throws Exception {
		validate("tests\\validation-examples\\list-extension2.json", 1, true);
	}

	@Test
	public void testJsonListXhtmlCorrect1() throws Exception {
		validate("tests\\validation-examples\\list-xhtml-correct1.json", 0, true);
	}

	@Test
	public void testJsonListXhtmlCorrect2() throws Exception {
		validate("tests\\validation-examples\\list-xhtml-correct2.json", 0, true);
	}

	@Test
	public void testJsonListXhtmlXXE() throws Exception {
		validate("tests\\validation-examples\\list-xhtml-xxe.json", 1, true);
	}

	@Test
	public void testJsonListXhtmlBadSyntax() throws Exception {
		validate("tests\\validation-examples\\list-xhtml-syntax.json", 1, true);
	}

	@Test
	public void testJsonListXhtmlWrongNS1() throws Exception {
		validate("tests\\validation-examples\\list-xhtml-wrongns1.json", 1, true);
	}

	@Test
	public void testJsonListXhtmlWrongNS2() throws Exception {
		validate("tests\\validation-examples\\list-xhtml-wrongns2.json", 1, true);
	}

	@Test
	public void testJsonListXhtmlBadElement() throws Exception {
		validate("tests\\validation-examples\\list-xhtml-element.json", 2, true);
	}

	@Test
	public void testJsonListXhtmlBadAttribute() throws Exception {
		validate("tests\\validation-examples\\list-xhtml-attribute.json", 1, true);
	}

	@Test
	public void testJsonbadSyntax() throws Exception {
		validate("tests\\validation-examples\\list-bad-syntax.json", 1, true);
	}
	
	@Test
	public void testJsonContained() throws Exception {
		validate("tests\\validation-examples\\list-contained.json", 0, true);
	}

	@Test
	public void testJsonContainedBad() throws Exception {
		validate("tests\\validation-examples\\list-contained-bad.json", 3, true);
	}

	@Test
	public void testJsonBundle() throws Exception {
		validate("tests\\validation-examples\\bundle-good.json", 0, true);
	}
	
	@Test
	public void testJsonGroupOk() throws Exception {
		validate("tests\\validation-examples\\group-minimal.json", 0, true);
	}

	@Test
	public void testJsonGroupTiny() throws Exception {
		validate("tests\\validation-examples\\group-minimal-tiny.json", 0, true);
	}

	@Test
	public void testJsonGroupGood() throws Exception {
		validate("tests\\validation-examples\\group-choice-good.json", 0, true);
	}

	@Test
	public void testJsonGroupBad1() throws Exception {
		validate("tests\\validation-examples\\group-choice-bad1.json", 2, true);
	}

	@Test
	public void testJsonGroupBad2() throws Exception {
		validate("tests\\validation-examples\\group-choice-bad2.json", 2, true);
	}

	@Test
	public void testJsonGroupBad3() throws Exception {
		validate("tests\\validation-examples\\group-choice-bad3.json", 2, true);
	}

	@Test
	public void testJsonGroupEmpty() throws Exception {
		validate("tests\\validation-examples\\group-choice-empty.json", 1, true);
	}

	
  @Test
  public void testBuildPatientExampleB() throws Exception {
    validate("publish\\patient-example-b.xml", 0, false);
  }

  @Test
  public void testJsonS4S() throws Exception {
    validateCnt("{\r\n"+
        "  \"resourceType\": \"Bundle\",\r\n"+
        "  \"type\": \"searchset\",\r\n"+
        "  \"total\": 661,\r\n"+
        "  \"link\": [\r\n"+
        "    {\r\n"+
        "      \"relation\": \"self\",\r\n"+
        "      \"url\": \"https://fhir-open-api-dstu2.smarthealthit.org/Condition?_count=1&_format=json\"\r\n"+
        "    },\r\n"+
        "    {\r\n"+
        "      \"relation\": \"next\",\r\n"+
        "      \"url\": \"https://fhir-open-api-dstu2.smarthealthit.org/Condition?_format=json&_count=1&_skip=1\"\r\n"+
        "    }\r\n"+
        "  ],\r\n"+
        "  \"entry\": [\r\n"+
        "    {\r\n"+
        "      \"fullUrl\": \"https://fhir-open-api-dstu2.smarthealthit.org/Condition/119\",\r\n"+
        "      \"resource\": {\r\n"+
        "        \"resourceType\": \"Condition\",\r\n"+
        "        \"id\": \"119\",\r\n"+
        "        \"meta\": {\r\n"+
        "          \"versionId\": \"27\",\r\n"+
        "          \"lastUpdated\": \"2016-03-09T15:29:49.651+00:00\"\r\n"+
        "        },\r\n"+
        "        \"text\": {\r\n"+
        "          \"status\": \"generated\",\r\n"+
        "          \"div\": \"<div>Single liveborn, born in hospital, delivered without mention of cesarean section</div>\"\r\n"+
        "        },\r\n"+
        "        \"subject\": {\r\n"+
        "          \"reference\": \"Patient/1032702\"\r\n"+
        "        },\r\n"+
        "        \"code\": {\r\n"+
        "          \"coding\": [\r\n"+
        "            {\r\n"+
        "              \"system\": \"http://snomed.info/sct\",\r\n"+
        "              \"code\": \"442311008\",\r\n"+
        "              \"display\": \"Single liveborn, born in hospital, delivered without mention of cesarean section\"\r\n"+
        "            }\r\n"+
        "          ],\r\n"+
        "          \"text\": \"Single liveborn, born in hospital, delivered without mention of cesarean section\"\r\n"+
        "        },\r\n"+
        "        \"clinicalStatus\": \"active\",\r\n"+
        "        \"verificationStatus\": \"confirmed\",\r\n"+
        "        \"onsetDateTime\": \"2007-12-14\"\r\n"+
        "      },\r\n"+
        "      \"search\": {\r\n"+
        "        \"mode\": \"match\"\r\n"+
        "      }\r\n"+
        "    }\r\n"+
        "  ]\r\n"+
        "}\r\n", 1, true);
  }

  @Override
  public Base resolveConstant(Object appContext, String name) throws PathEngineException {
    return null;
  }

  @Override
  public TypeDetails resolveConstantType(Object appContext, String name) throws PathEngineException {
    return null;
  }

  @Override
  public boolean log(String argument, List<Base> focus) {
    System.out.println(argument);
    return true;
  }

  @Override
  public FunctionDetails resolveFunction(String functionName) {
    return null;
  }

  @Override
  public TypeDetails checkFunction(Object appContext, String functionName, List<TypeDetails> parameters) throws PathEngineException {
    return null;
  }

  @Override
  public List<Base> executeFunction(Object appContext, String functionName, List<List<Base>> parameters) {
    return null;
  }

  @Override
  public Base resolveReference(Object appContext, String url) {
    return null;
  }

  @Override
  public Element fetch(Object appContext, String url) throws FHIRFormatError, DefinitionException, IOException, FHIRException {
    return null;
  }

  @Override
  public ReferenceValidationPolicy validationPolicy(Object appContext, String path, String url) {
    return ReferenceValidationPolicy.IGNORE;
  }

  @Override
  public boolean resolveURL(Object appContext, String path, String url) throws IOException, FHIRException {
    return true;
  }
  
  private ExpansionProfile makeExpProfile() {
    ExpansionProfile ep  = new ExpansionProfile();
    ep.setId("dc8fd4bc-091a-424a-8a3b-6198ef146891"); // change this to blow the cache
    ep.setUrl("http://hl7.org/fhir/ExpansionProfile/"+ep.getId());
    // all defaults....
    return ep;
  }

}
