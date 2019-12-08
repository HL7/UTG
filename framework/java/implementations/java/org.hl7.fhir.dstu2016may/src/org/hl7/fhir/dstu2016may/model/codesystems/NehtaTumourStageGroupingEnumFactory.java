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

// Generated on Sun, May 8, 2016 03:05+1000 for FHIR v1.4.0


import org.hl7.fhir.dstu2016may.model.EnumFactory;

public class NehtaTumourStageGroupingEnumFactory implements EnumFactory<NehtaTumourStageGrouping> {

  public NehtaTumourStageGrouping fromCode(String codeString) throws IllegalArgumentException {
    if (codeString == null || "".equals(codeString))
      return null;
    if ("0".equals(codeString))
      return NehtaTumourStageGrouping._0;
    if ("i".equals(codeString))
      return NehtaTumourStageGrouping.I;
    if ("iia".equals(codeString))
      return NehtaTumourStageGrouping.IIA;
    if ("iib".equals(codeString))
      return NehtaTumourStageGrouping.IIB;
    if ("iic".equals(codeString))
      return NehtaTumourStageGrouping.IIC;
    if ("iiia".equals(codeString))
      return NehtaTumourStageGrouping.IIIA;
    if ("iiib".equals(codeString))
      return NehtaTumourStageGrouping.IIIB;
    if ("iiic".equals(codeString))
      return NehtaTumourStageGrouping.IIIC;
    if ("iva".equals(codeString))
      return NehtaTumourStageGrouping.IVA;
    if ("ivb".equals(codeString))
      return NehtaTumourStageGrouping.IVB;
    throw new IllegalArgumentException("Unknown NehtaTumourStageGrouping code '"+codeString+"'");
  }

  public String toCode(NehtaTumourStageGrouping code) {
    if (code == NehtaTumourStageGrouping._0)
      return "0";
    if (code == NehtaTumourStageGrouping.I)
      return "i";
    if (code == NehtaTumourStageGrouping.IIA)
      return "iia";
    if (code == NehtaTumourStageGrouping.IIB)
      return "iib";
    if (code == NehtaTumourStageGrouping.IIC)
      return "iic";
    if (code == NehtaTumourStageGrouping.IIIA)
      return "iiia";
    if (code == NehtaTumourStageGrouping.IIIB)
      return "iiib";
    if (code == NehtaTumourStageGrouping.IIIC)
      return "iiic";
    if (code == NehtaTumourStageGrouping.IVA)
      return "iva";
    if (code == NehtaTumourStageGrouping.IVB)
      return "ivb";
    return "?";
  }

    public String toSystem(NehtaTumourStageGrouping code) {
      return code.getSystem();
      }

}

