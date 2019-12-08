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

public class DeviceUseRequestPriorityEnumFactory implements EnumFactory<DeviceUseRequestPriority> {

  public DeviceUseRequestPriority fromCode(String codeString) throws IllegalArgumentException {
    if (codeString == null || "".equals(codeString))
      return null;
    if ("routine".equals(codeString))
      return DeviceUseRequestPriority.ROUTINE;
    if ("urgent".equals(codeString))
      return DeviceUseRequestPriority.URGENT;
    if ("stat".equals(codeString))
      return DeviceUseRequestPriority.STAT;
    if ("asap".equals(codeString))
      return DeviceUseRequestPriority.ASAP;
    throw new IllegalArgumentException("Unknown DeviceUseRequestPriority code '"+codeString+"'");
  }

  public String toCode(DeviceUseRequestPriority code) {
    if (code == DeviceUseRequestPriority.ROUTINE)
      return "routine";
    if (code == DeviceUseRequestPriority.URGENT)
      return "urgent";
    if (code == DeviceUseRequestPriority.STAT)
      return "stat";
    if (code == DeviceUseRequestPriority.ASAP)
      return "asap";
    return "?";
  }

    public String toSystem(DeviceUseRequestPriority code) {
      return code.getSystem();
      }

}

