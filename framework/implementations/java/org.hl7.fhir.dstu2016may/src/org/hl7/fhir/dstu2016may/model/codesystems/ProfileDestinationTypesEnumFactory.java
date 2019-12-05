package org.hl7.fhir.dstu2016may.model.codesystems;

/*
  Copyright (c) 2011+, HL7, Inc.
  All rights reserved.
  
  Redistribution and use in source and binary forms, with or without modification, 
  are permitted provided that the following conditions are met:
  
   * Redistributions of source code must retain the above copyright notice, this 
     list of conditions and the following disclaimer.
   * Redistributions in binary form must reproduce the above copyright notice, 
     this list of conditions and the following disclaimer in the documentation 
     and/or other materials provided with the distribution.
   * Neither the name of HL7 nor the names of its contributors may be used to 
     endorse or promote products derived from this software without specific 
     prior written permission.
  
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  POSSIBILITY OF SUCH DAMAGE.
  
*/

// Generated on Mon, Mar 21, 2016 12:03+1100 for FHIR v1.3.0


import org.hl7.fhir.dstu2016may.model.EnumFactory;

public class ProfileDestinationTypesEnumFactory implements EnumFactory<ProfileDestinationTypes> {

  public ProfileDestinationTypes fromCode(String codeString) throws IllegalArgumentException {
    if (codeString == null || "".equals(codeString))
      return null;
    if ("FHIR-DSTU2-Server".equals(codeString))
      return ProfileDestinationTypes.FHIRDSTU2SERVER;
    if ("FHIR-DSTU2-SDC-FormManager".equals(codeString))
      return ProfileDestinationTypes.FHIRDSTU2SDCFORMMANAGER;
    if ("FHIR-DSTU2-SDC-FormReceiver".equals(codeString))
      return ProfileDestinationTypes.FHIRDSTU2SDCFORMRECEIVER;
    if ("FHIR-DSTU2-SDC-FormProcessor".equals(codeString))
      return ProfileDestinationTypes.FHIRDSTU2SDCFORMPROCESSOR;
    throw new IllegalArgumentException("Unknown ProfileDestinationTypes code '"+codeString+"'");
  }

  public String toCode(ProfileDestinationTypes code) {
    if (code == ProfileDestinationTypes.FHIRDSTU2SERVER)
      return "FHIR-DSTU2-Server";
    if (code == ProfileDestinationTypes.FHIRDSTU2SDCFORMMANAGER)
      return "FHIR-DSTU2-SDC-FormManager";
    if (code == ProfileDestinationTypes.FHIRDSTU2SDCFORMRECEIVER)
      return "FHIR-DSTU2-SDC-FormReceiver";
    if (code == ProfileDestinationTypes.FHIRDSTU2SDCFORMPROCESSOR)
      return "FHIR-DSTU2-SDC-FormProcessor";
    return "?";
  }

    public String toSystem(ProfileDestinationTypes code) {
      return code.getSystem();
      }

}

