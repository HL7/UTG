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

// Generated on Tue, Mar 22, 2016 08:00+1100 for FHIR v1.3.0


import org.hl7.fhir.dstu2016may.model.EnumFactory;

public class GuideResourcePurposeEnumFactory implements EnumFactory<GuideResourcePurpose> {

  public GuideResourcePurpose fromCode(String codeString) throws IllegalArgumentException {
    if (codeString == null || "".equals(codeString))
      return null;
    if ("example".equals(codeString))
      return GuideResourcePurpose.EXAMPLE;
    if ("terminology".equals(codeString))
      return GuideResourcePurpose.TERMINOLOGY;
    if ("profile".equals(codeString))
      return GuideResourcePurpose.PROFILE;
    if ("extension".equals(codeString))
      return GuideResourcePurpose.EXTENSION;
    if ("dictionary".equals(codeString))
      return GuideResourcePurpose.DICTIONARY;
    if ("logical".equals(codeString))
      return GuideResourcePurpose.LOGICAL;
    throw new IllegalArgumentException("Unknown GuideResourcePurpose code '"+codeString+"'");
  }

  public String toCode(GuideResourcePurpose code) {
    if (code == GuideResourcePurpose.EXAMPLE)
      return "example";
    if (code == GuideResourcePurpose.TERMINOLOGY)
      return "terminology";
    if (code == GuideResourcePurpose.PROFILE)
      return "profile";
    if (code == GuideResourcePurpose.EXTENSION)
      return "extension";
    if (code == GuideResourcePurpose.DICTIONARY)
      return "dictionary";
    if (code == GuideResourcePurpose.LOGICAL)
      return "logical";
    return "?";
  }

    public String toSystem(GuideResourcePurpose code) {
      return code.getSystem();
      }

}

