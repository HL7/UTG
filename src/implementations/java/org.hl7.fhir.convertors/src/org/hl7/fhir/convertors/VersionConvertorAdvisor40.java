package org.hl7.fhir.convertors;

import org.hl7.fhir.exceptions.FHIRException;

public interface VersionConvertorAdvisor40 {
  boolean ignoreEntry(org.hl7.fhir.r4.model.Bundle.BundleEntryComponent src);

  // called ?
  org.hl7.fhir.dstu2.model.Resource convertR2(org.hl7.fhir.r4.model.Resource resource) throws FHIRException;
  org.hl7.fhir.dstu2016may.model.Resource convertR2016May(org.hl7.fhir.r4.model.Resource resource) throws FHIRException;
  org.hl7.fhir.dstu3.model.Resource convertR3(org.hl7.fhir.r4.model.Resource resource) throws FHIRException;

  // called when an r2 value set has a codeSystem in it
  void handleCodeSystem(org.hl7.fhir.r4.model.CodeSystem tgtcs, org.hl7.fhir.r4.model.ValueSet source) throws FHIRException;

  org.hl7.fhir.r4.model.CodeSystem getCodeSystem(org.hl7.fhir.r4.model.ValueSet src) throws FHIRException;
}