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

public class HspcObservationHeightMeasMethodDeviceEnumFactory implements EnumFactory<HspcObservationHeightMeasMethodDevice> {

  public HspcObservationHeightMeasMethodDevice fromCode(String codeString) throws IllegalArgumentException {
    if (codeString == null || "".equals(codeString))
      return null;
    if ("2057".equals(codeString))
      return HspcObservationHeightMeasMethodDevice._2057;
    if ("2058".equals(codeString))
      return HspcObservationHeightMeasMethodDevice._2058;
    if ("2061".equals(codeString))
      return HspcObservationHeightMeasMethodDevice._2061;
    if ("2063".equals(codeString))
      return HspcObservationHeightMeasMethodDevice._2063;
    if ("521363688".equals(codeString))
      return HspcObservationHeightMeasMethodDevice._521363688;
    throw new IllegalArgumentException("Unknown HspcObservationHeightMeasMethodDevice code '"+codeString+"'");
  }

  public String toCode(HspcObservationHeightMeasMethodDevice code) {
    if (code == HspcObservationHeightMeasMethodDevice._2057)
      return "2057";
    if (code == HspcObservationHeightMeasMethodDevice._2058)
      return "2058";
    if (code == HspcObservationHeightMeasMethodDevice._2061)
      return "2061";
    if (code == HspcObservationHeightMeasMethodDevice._2063)
      return "2063";
    if (code == HspcObservationHeightMeasMethodDevice._521363688)
      return "521363688";
    return "?";
  }

    public String toSystem(HspcObservationHeightMeasMethodDevice code) {
      return code.getSystem();
      }

}

