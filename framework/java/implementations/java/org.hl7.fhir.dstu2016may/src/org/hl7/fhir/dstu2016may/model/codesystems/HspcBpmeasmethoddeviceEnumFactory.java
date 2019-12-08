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

public class HspcBpmeasmethoddeviceEnumFactory implements EnumFactory<HspcBpmeasmethoddevice> {

  public HspcBpmeasmethoddevice fromCode(String codeString) throws IllegalArgumentException {
    if (codeString == null || "".equals(codeString))
      return null;
    if ("2102".equals(codeString))
      return HspcBpmeasmethoddevice._2102;
    if ("2162".equals(codeString))
      return HspcBpmeasmethoddevice._2162;
    if ("31163".equals(codeString))
      return HspcBpmeasmethoddevice._31163;
    if ("50577434".equals(codeString))
      return HspcBpmeasmethoddevice._50577434;
    throw new IllegalArgumentException("Unknown HspcBpmeasmethoddevice code '"+codeString+"'");
  }

  public String toCode(HspcBpmeasmethoddevice code) {
    if (code == HspcBpmeasmethoddevice._2102)
      return "2102";
    if (code == HspcBpmeasmethoddevice._2162)
      return "2162";
    if (code == HspcBpmeasmethoddevice._31163)
      return "31163";
    if (code == HspcBpmeasmethoddevice._50577434)
      return "50577434";
    return "?";
  }

    public String toSystem(HspcBpmeasmethoddevice code) {
      return code.getSystem();
      }

}

