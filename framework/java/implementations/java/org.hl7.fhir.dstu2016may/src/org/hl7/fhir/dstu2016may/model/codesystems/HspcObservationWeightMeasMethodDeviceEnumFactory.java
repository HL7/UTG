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

public class HspcObservationWeightMeasMethodDeviceEnumFactory implements EnumFactory<HspcObservationWeightMeasMethodDevice> {

  public HspcObservationWeightMeasMethodDevice fromCode(String codeString) throws IllegalArgumentException {
    if (codeString == null || "".equals(codeString))
      return null;
    if ("1964".equals(codeString))
      return HspcObservationWeightMeasMethodDevice._1964;
    if ("2007".equals(codeString))
      return HspcObservationWeightMeasMethodDevice._2007;
    if ("2139".equals(codeString))
      return HspcObservationWeightMeasMethodDevice._2139;
    if ("2142".equals(codeString))
      return HspcObservationWeightMeasMethodDevice._2142;
    if ("2180".equals(codeString))
      return HspcObservationWeightMeasMethodDevice._2180;
    if ("2181".equals(codeString))
      return HspcObservationWeightMeasMethodDevice._2181;
    if ("2185".equals(codeString))
      return HspcObservationWeightMeasMethodDevice._2185;
    if ("50550851".equals(codeString))
      return HspcObservationWeightMeasMethodDevice._50550851;
    if ("521443011".equals(codeString))
      return HspcObservationWeightMeasMethodDevice._521443011;
    throw new IllegalArgumentException("Unknown HspcObservationWeightMeasMethodDevice code '"+codeString+"'");
  }

  public String toCode(HspcObservationWeightMeasMethodDevice code) {
    if (code == HspcObservationWeightMeasMethodDevice._1964)
      return "1964";
    if (code == HspcObservationWeightMeasMethodDevice._2007)
      return "2007";
    if (code == HspcObservationWeightMeasMethodDevice._2139)
      return "2139";
    if (code == HspcObservationWeightMeasMethodDevice._2142)
      return "2142";
    if (code == HspcObservationWeightMeasMethodDevice._2180)
      return "2180";
    if (code == HspcObservationWeightMeasMethodDevice._2181)
      return "2181";
    if (code == HspcObservationWeightMeasMethodDevice._2185)
      return "2185";
    if (code == HspcObservationWeightMeasMethodDevice._50550851)
      return "50550851";
    if (code == HspcObservationWeightMeasMethodDevice._521443011)
      return "521443011";
    return "?";
  }

    public String toSystem(HspcObservationWeightMeasMethodDevice code) {
      return code.getSystem();
      }

}

