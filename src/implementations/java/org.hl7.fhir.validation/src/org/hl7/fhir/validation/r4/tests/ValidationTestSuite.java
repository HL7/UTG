package org.hl7.fhir.validation.r4.tests;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.convertors.VersionConvertor_10_40;
import org.hl7.fhir.convertors.VersionConvertor_14_40;
import org.hl7.fhir.convertors.VersionConvertor_30_40;
import org.hl7.fhir.exceptions.DefinitionException;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.exceptions.PathEngineException;
import org.hl7.fhir.r4.conformance.ProfileUtilities;
import org.hl7.fhir.r4.context.SimpleWorkerContext;
import org.hl7.fhir.r4.elementmodel.Element;
import org.hl7.fhir.r4.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r4.elementmodel.ObjectConverter;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Constants;
import org.hl7.fhir.r4.model.ExpansionProfile;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.TypeDetails;
import org.hl7.fhir.r4.test.support.TestingUtilities;
import org.hl7.fhir.r4.utils.FHIRPathEngine.IEvaluationContext;
import org.hl7.fhir.r4.utils.IResourceValidator.IValidatorResourceFetcher;
import org.hl7.fhir.r4.utils.IResourceValidator.ReferenceValidationPolicy;
import org.hl7.fhir.r4.validation.InstanceValidator;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.SAXException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@RunWith(Parameterized.class)
public class ValidationTestSuite implements IEvaluationContext, IValidatorResourceFetcher {

  @Parameters(name = "{index}: id {0}")
  public static Iterable<Object[]> data() throws ParserConfigurationException, SAXException, IOException {
    
    Map<String, JsonObject> examples = new HashMap<String, JsonObject>();
    JsonObject json =  (JsonObject) new com.google.gson.JsonParser().parse(TextFile.fileToString(Utilities.path(TestingUtilities.home(), "tests", "validation-examples", "manifest.json")));
    json = json.getAsJsonObject("validator-tests");
    for (Entry<String, JsonElement> e : json.getAsJsonObject("Json").entrySet()) {
      examples.put("Json."+e.getKey(), e.getValue().getAsJsonObject());
    }
    for (Entry<String, JsonElement> e : json.getAsJsonObject("Xml").entrySet()) {
      examples.put("Xml."+e.getKey(), e.getValue().getAsJsonObject());
    }

    List<String> names = new ArrayList<String>(examples.size());
    names.addAll(examples.keySet());
    Collections.sort(names);

    List<Object[]> objects = new ArrayList<Object[]>(examples.size());
    for (String id : names) {
      objects.add(new Object[] { id, examples.get(id)});
    }
    return objects;
  }

  private String name;
  private JsonObject content;
  
  public ValidationTestSuite(String name, JsonObject content) {
    this.name = name;
    this.content = content;
  }

  @SuppressWarnings("deprecation")
  @Test
  public void test() throws Exception {
    if (TestingUtilities.context == null) {
      SimpleWorkerContext ctxt = SimpleWorkerContext.fromPack(Utilities.path(TestingUtilities.home(), "publish", "definitions.xml.zip"));
      TestingUtilities.context = ctxt;
      ctxt.setExpansionProfile(makeExpProfile());
      try {
        ctxt.connectToTSServer("http://tx.fhir.org/r4");
      } catch (Exception e) {
        ctxt.setCanRunWithoutTerminology(true);
      }
    }

    if (content.has("use-test") && !content.get("use-test").getAsBoolean())
      return;
    
    String path = Utilities.path(TestingUtilities.home(), "tests", "validation-examples", name.substring(name.indexOf(".")+1));
    InstanceValidator val = new InstanceValidator(TestingUtilities.context, this);
    if (content.has("allowed-extension-domain")) 
      val.getExtensionDomains().add(content.get("allowed-extension-domain").getAsString());
    val.setFetcher(this);
    if (content.has("profile")) {
      List<ValidationMessage> errors = new ArrayList<ValidationMessage>();
      JsonObject profile = content.getAsJsonObject("profile");
      String filename = Utilities.path(TestingUtilities.home(), "tests", "validation-examples", profile.get("source").getAsString());
      String v = content.has("version") ? content.get("version").getAsString() : Constants.VERSION;
      StructureDefinition sd = null;
      if (Constants.VERSION.equals(v))
        sd = (StructureDefinition) new XmlParser().parse(new FileInputStream(filename));
      else if (org.hl7.fhir.dstu3.model.Constants.VERSION.equals(v))
        sd = (StructureDefinition) VersionConvertor_30_40.convertResource(new org.hl7.fhir.dstu3.formats.XmlParser().parse(new FileInputStream(filename)));
      else if (org.hl7.fhir.dstu2016may.model.Constants.VERSION.equals(v))
        sd = (StructureDefinition) VersionConvertor_14_40.convertResource(new org.hl7.fhir.dstu2016may.formats.XmlParser().parse(new FileInputStream(filename)));
      else if (org.hl7.fhir.dstu2.model.Constants.VERSION.equals(v))
        sd = (StructureDefinition) new VersionConvertor_10_40(null).convertResource(new org.hl7.fhir.dstu2.formats.XmlParser().parse(new FileInputStream(filename)));
      if (!sd.hasSnapshot()) {
        ProfileUtilities pu = new ProfileUtilities(TestingUtilities.context, null, null);
        StructureDefinition base = TestingUtilities.context.fetchResource(StructureDefinition.class, sd.getBaseDefinition());
        pu.generateSnapshot(base, sd, sd.getUrl(), sd.getTitle());
      }
      if (name.startsWith("Json."))
        val.validate(null, errors, new FileInputStream(path), FhirFormat.JSON, sd);
      else
         val.validate(null, errors, new FileInputStream(path), FhirFormat.XML, sd);
      checkOutcomes(errors, profile);
    } else {
      List<ValidationMessage> errors = new ArrayList<ValidationMessage>();
      if (name.startsWith("Json."))
        val.validate(null, errors, new FileInputStream(path), FhirFormat.JSON);
      else
        val.validate(null, errors, new FileInputStream(path), FhirFormat.XML);
      checkOutcomes(errors, content);
    }
  }

  private void checkOutcomes(List<ValidationMessage> errors, JsonObject focus) {
    int ec = 0;
    int wc = 0;
    for (ValidationMessage vm : errors) {
      if (vm.getLevel() == IssueSeverity.FATAL || vm.getLevel() == IssueSeverity.ERROR) {
        ec++;
        System.out.println(vm.getDisplay());
      }
      if (vm.getLevel() == IssueSeverity.WARNING) 
        wc++;
    }
    if (TestingUtilities.context.isNoTerminologyServer() || !focus.has("tx-dependent")) {
      Assert.assertEquals("Expected "+Integer.toString(focus.get("errorCount").getAsInt())+" errors, but found "+Integer.toString(ec)+".", focus.get("errorCount").getAsInt(), ec);
      if (focus.has("warningCount"))
        Assert.assertEquals("Expected "+Integer.toString(focus.get("warningCount").getAsInt())+" warnings, but found "+Integer.toString(wc)+".", focus.get("warningCount").getAsInt(), wc);
    }
  }

  private ExpansionProfile makeExpProfile() {
    ExpansionProfile ep  = new ExpansionProfile();
    ep.setId("dc8fd4bc-091a-424a-8a3b-6198ef146891"); // change this to blow the cache
    ep.setUrl("http://hl7.org/fhir/ExpansionProfile/"+ep.getId());
    // all defaults....
    return ep;
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
    return false;
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
    if (url.equals("Patient/test"))
      return new Patient();
    return null;
  }

  @Override
  public Element fetch(Object appContext, String url) throws FHIRFormatError, DefinitionException, IOException, FHIRException {
    if (url.equals("Patient/test"))
      return new ObjectConverter(TestingUtilities.context).convert(new Patient());
    return null;
  }

  @Override
  public ReferenceValidationPolicy validationPolicy(Object appContext, String path, String url) {
    if (content.has("validate"))
      return ReferenceValidationPolicy.valueOf(content.get("validate").getAsString());
    else
      return ReferenceValidationPolicy.IGNORE;
  }

  @Override
  public boolean resolveURL(Object appContext, String path, String url) throws IOException, FHIRException {
    return true;
  }

}
