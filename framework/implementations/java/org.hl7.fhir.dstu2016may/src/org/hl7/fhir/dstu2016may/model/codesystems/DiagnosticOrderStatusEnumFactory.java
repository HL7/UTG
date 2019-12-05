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

public class DiagnosticOrderStatusEnumFactory implements EnumFactory<DiagnosticOrderStatus> {

  public DiagnosticOrderStatus fromCode(String codeString) throws IllegalArgumentException {
    if (codeString == null || "".equals(codeString))
      return null;
    if ("proposed".equals(codeString))
      return DiagnosticOrderStatus.PROPOSED;
    if ("draft".equals(codeString))
      return DiagnosticOrderStatus.DRAFT;
    if ("planned".equals(codeString))
      return DiagnosticOrderStatus.PLANNED;
    if ("requested".equals(codeString))
      return DiagnosticOrderStatus.REQUESTED;
    if ("received".equals(codeString))
      return DiagnosticOrderStatus.RECEIVED;
    if ("accepted".equals(codeString))
      return DiagnosticOrderStatus.ACCEPTED;
    if ("in-progress".equals(codeString))
      return DiagnosticOrderStatus.INPROGRESS;
    if ("review".equals(codeString))
      return DiagnosticOrderStatus.REVIEW;
    if ("completed".equals(codeString))
      return DiagnosticOrderStatus.COMPLETED;
    if ("cancelled".equals(codeString))
      return DiagnosticOrderStatus.CANCELLED;
    if ("suspended".equals(codeString))
      return DiagnosticOrderStatus.SUSPENDED;
    if ("rejected".equals(codeString))
      return DiagnosticOrderStatus.REJECTED;
    if ("failed".equals(codeString))
      return DiagnosticOrderStatus.FAILED;
    if ("entered-in-error".equals(codeString))
      return DiagnosticOrderStatus.ENTEREDINERROR;
    throw new IllegalArgumentException("Unknown DiagnosticOrderStatus code '"+codeString+"'");
  }

  public String toCode(DiagnosticOrderStatus code) {
    if (code == DiagnosticOrderStatus.PROPOSED)
      return "proposed";
    if (code == DiagnosticOrderStatus.DRAFT)
      return "draft";
    if (code == DiagnosticOrderStatus.PLANNED)
      return "planned";
    if (code == DiagnosticOrderStatus.REQUESTED)
      return "requested";
    if (code == DiagnosticOrderStatus.RECEIVED)
      return "received";
    if (code == DiagnosticOrderStatus.ACCEPTED)
      return "accepted";
    if (code == DiagnosticOrderStatus.INPROGRESS)
      return "in-progress";
    if (code == DiagnosticOrderStatus.REVIEW)
      return "review";
    if (code == DiagnosticOrderStatus.COMPLETED)
      return "completed";
    if (code == DiagnosticOrderStatus.CANCELLED)
      return "cancelled";
    if (code == DiagnosticOrderStatus.SUSPENDED)
      return "suspended";
    if (code == DiagnosticOrderStatus.REJECTED)
      return "rejected";
    if (code == DiagnosticOrderStatus.FAILED)
      return "failed";
    if (code == DiagnosticOrderStatus.ENTEREDINERROR)
      return "entered-in-error";
    return "?";
  }

    public String toSystem(DiagnosticOrderStatus code) {
      return code.getSystem();
      }

}

