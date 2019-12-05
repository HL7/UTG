<?xml version="1.0" encoding="UTF-8"?>
<!-- edited with XMLSpy v2014 rel. 2 sp1 (x64) (http://www.altova.com) by Lloyd McKenzie (private) -->
<sch:schema xmlns="urn:hl7-org:v3/mif2" xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:sch="http://purl.oclc.org/dsdl/schematron" xmlns:xs="http://www.w3.org/2001/XMLSchema" queryBinding="xslt2">
  <sch:ns prefix="mif" uri="urn:hl7-org:v3/mif"/>
  <sch:pattern name="Validate PackageRef type">
    <sch:rule context="element(*, mif:PackageRef)">
      <sch:report test="contains(concat(';', @artifact, ';'), ';RIM;ITS;DT;VO;') and @domain">
							ERROR: RIM, ITS, Vocabulary and Datatypes must be global (they may not be within a domain).</sch:report>
      <sch:report test="contains(concat(';', @artifact, ';'), ';DIM;CIM;LIM;AR;TE;IN;TP;DAM;') and not(@domain)">
							ERROR: DIMs, DAMs, CIMs, LIMs, Application Roles, Trigger Events, Interactions and Templates may only be defined in a domain.</sch:report>
      <!-- DC, GL, IFC, SB can be domain or non-domain -->
      <sch:report test="@subArtifact and @artifact!='VO'">
							ERROR: Only vocabulary artifacts may have sub-artifact types.</sch:report>
      <sch:report test="contains(concat(';', @artifact, ';'), ';RIM;ITS;') and not(@realmNamespace)">
							ERROR: All artifacts except RIM and ITS must be specific to a particular realm namespace (even if universal).</sch:report>
      <sch:report test="@root!='PROF' and not(@artifact)">
							ERROR: Must specify an artifact unless package represents a profile.</sch:report>
      <sch:report test="@root='PROF' and not(@artifact)">
							ERROR: Must specify an artifact unless package represents a profile.</sch:report>
      <sch:report test="@id and @name">
							ERROR: Artifacts can't have both name and id.</sch:report>
      <!-- Todo: Add more constraints -->
      <!-- Todo: Can only have identifierId if realm is ZZ -->
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate ClassRef type">
    <sch:rule abstract="true" id="ClassRef">
      <sch:extends rule="PackageRef"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate TransitionRef type">
    <sch:rule abstract="true" id="TransitionRef">
      <sch:extends rule="ClassRef"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate historyItem element">
    <sch:rule context="mif:historyItem">
      <sch:extends rule="HistoryItem"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate realm element">
    <sch:rule context="mif:realm">
      <sch:extends rule="RealmElement"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate RealmElement type">
    <sch:rule abstract="true" id="RealmElement">
      <sch:report test="count(preceding-sibling::mif:*[name(.)=name(current())][@value=current()/@value])!=0">
								ERROR: A given realm may only be listed once within a particular element.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate businessName element">
    <sch:rule context="mif:businessName">
      <sch:extends rule="BusinessName"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate BusinessName type">
    <sch:rule abstract="true" id="BusinessName">
      <sch:report test="count(preceding-sibling::mif:*[name(.)=name(current())][mif:context/@value=current()/mif:context/@value or (not(mif:context) and not(current()/mif:context))])!=0">
								ERROR: Only one business name may exist for a particular context.</sch:report>
      <sch:report test="count(preceding-sibling::*[name(.)=name(current())][@name=current()/@name])!=0">
								ERROR: Business names must be unique for a given element.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate HistoryItem type">
    <sch:rule abstract="true" id="HistoryItem">
      <sch:report test="@id and preceding::mif:historyItem[@id=current/@id]">
								ERROR: HistoryItem ids must be unique across the document</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate Derivation type">
    <sch:rule abstract="true" id="Derivation">
      <sch:report test="@areAnnotationsReviewed='false' and @annotationsReviewedBy">
							ERROR: AnnotationsReviewedBy may only be specified if the annotations have been reviewed.</sch:report>
      <sch:report test="@areAnnotationsReviewed='false' and @relationship='unchanged'">
							ERROR: AnnotationsReviewed must be 'true' for 'unchanged' derivations.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate graphElement element">
    <sch:rule context="mif:graphElement[@name]">
      <sch:extends rule="Diagram"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate graphElement element">
    <sch:rule context="mif:graphElement">
      <sch:extends rule="ContainedGraphNode"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate graphElement element">
    <sch:rule context="mif:graphElement[mif:anchorage]">
      <sch:extends rule="GraphNodeWithOptionalConnection"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate GraphNode type">
    <sch:rule abstract="true" id="GraphNode">
      <sch:report test="@textWrappingWidth and @textWrappingWidth &gt; mif:size/@width">
							WARNING: Text wrapping width is larger than width of element.</sch:report>
      <sch:report test="@textWrappingWidth and @textWrappingWidth &gt; mif:size/@width">
							WARNING: Text wrapping width is larger than width of element.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate ContainedGraphNode type">
    <sch:rule abstract="true" id="ContainedGraphNode">
      <!--            <sch:report test="count(ancestor::mif:*/mif:graphElement[@name=current()/@containerDiagramName])!=1">
							ERROR: A node that claims to be part of a diagram must be contained by an element that has a diagram of that name.</sch:report>-->
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate GraphNodeWithOptionalConnection type">
    <sch:rule abstract="true" id="GraphNodeWithOptionalConnection">
      <sch:extends rule="ContainedGraphNode"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate Diagram type">
    <sch:rule abstract="true" id="Diagram">
      <sch:report test="count(preceding-sibling::mif:*[name(.)=name(current())][@name=current()/@name])!=0">
								ERROR: Only one diagram with a given name may exist within a single element.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate ballotComment element">
    <sch:rule context="mif:ballotComment">
      <sch:extends rule="BallotComment"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate changeRequest element">
    <sch:rule context="mif:changeRequest">
      <sch:extends rule="ChangeRequest"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate dprecationInfo element">
    <sch:rule context="mif:deprecationInfo">
      <sch:extends rule="DeprecationInfo"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate BasicAnnotation type">
    <sch:rule abstract="true" id="BasicAnnotation">
      <sch:report test="cascadeInfo and preceding::*[@name=current()/@name][@id=@prependAnnotationId or @id=@appendAnnotationId][cascadeInfo/@elementType=current()/cascadeInfo/@elementType or name(.)!=name(current())]">
							WARNING: Prepended and appended annotations must have the same elementType and name as the annotation to which they are prepended/annotated.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate AnnotationCascadeInfo type">
    <sch:rule abstract="true" id="AnnotationCascadeInfo">
      <sch:report test="todo">
							WARNING: RIM, DMIM and DIM models are not serializable.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate ComplexMarkupWithLanguage type">
    <sch:rule abstract="true" id="ComplexMarkupWithLanguage">
      <sch:report test="count(preceding-sibling::mif:*[name(.)=name(current())][@lang=current()/@lang or              ((@lang='EN' or not(@lang)) and (current()/@lang='EN' or not(current()/@lang)))])!=0">
							ERROR: Each repetition of complex markup with language must be a different langage (no language is equivalent to 'EN').</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate FreeFormMarkupWithLanguage type">
    <sch:rule abstract="true" id="FreeFormMarkupWithLanguage">
      <sch:report test="count(preceding-sibling::mif:*[name(.)=name(current())][@lang=current()/@lang or              ((@lang='EN' or not(@lang)) and (current()/@lang='EN' or not(current()/@lang)))])!=0">
							ERROR: Each repetition of complex markup with language must be a different langage (no language is equivalent to 'EN').</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate text element">
    <sch:rule context="mif:text">
      <sch:extends rule="FreeFormMarkupWithLanguage"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate text element">
    <sch:rule context="mif:text">
      <sch:extends rule="FreeFormMarkupWithLanguage"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate sourceArtifact element">
    <sch:rule context="mif:sourceArtifact">
      <sch:extends rule="PackageOrArtifactRef"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate text element">
    <sch:rule context="mif:text">
      <sch:extends rule="FreeFormMarkupWithLanguage"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate BallotComment type">
    <sch:rule abstract="true" id="BallotComment">
      <sch:report test="not(ancestor::mif:*/mif:ballotInfo/mif:ballotResponse/@submissionId=current()/@submissionId)">
											ERROR: Cannot have a ballot comment that is not part of an identified ballot response associated with the package.</sch:report>
      <sch:report test="preceding::mif:*[name(.)=name(current())][@submissionId=current()/@submissionId][@name=current()/@name]">
											ERROR: Cannot have multiple ballot comments with the same name within the same submission.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate ChangeRequest type">
    <sch:rule abstract="true" id="ChangeRequest">
      <!-- Todo: Fill in -->
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate packageLocation element">
    <sch:rule context="mif:packageLocation">
      <sch:extends rule="PackageRef"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate Package type">
    <sch:rule abstract="true" id="Package">
      <sch:report test="count(ancestor-or-self::mif:*/mif:header)=0">
							WARNING: This element or one of it's ancestors must have a header defined.</sch:report>
      <sch:report test="ancestor::mif:*/@name and mif:packageLocation">
							ERROR: Can't define a package location for an element that is located inside a package.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate packageLocation element">
    <sch:rule context="mif:packageLocation">
      <sch:extends rule="PackageRef"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate replaces element">
    <sch:rule context="mif:replaces">
      <sch:extends rule="PackageRef"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate targetPackage element">
    <sch:rule context="mif:targetPackage">
      <sch:extends rule="PackageRef"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate legalese element">
    <sch:rule context="mif:legalese">
      <sch:extends rule="Legalese"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate responsibleGroup element">
    <sch:rule context="mif:responsibleGroup">
      <sch:extends rule="ResponsibleGroup"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate approvalInfo element">
    <sch:rule context="mif:approvalInfo">
      <sch:extends rule="ApprovalInfo"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate Legalese type">
    <sch:rule abstract="true" id="Legalese">
      <sch:report test="(@copyrightOwner and not(@copyrightYears)) or (not(@copyrightOwner) and @copyrightYears)">
													ERROR: Either both copyrightYears and copyrightOwner must be specified or neither should be specified.</sch:report>
      <sch:report test="@copyrightNotation and not(@copyrightYears)">
													ERROR: CopyrightNotation may only be specified when copyrightYears is present.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate ResponsibleGroup type">
    <sch:rule abstract="true" id="ResponsibleGroup">
      <sch:report test="not(@groupId or @groupName or @organizationName)">
								ERROR: At least one of groupId, groupName or organizationName is required.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate ApprovalInfo type">
    <sch:rule abstract="true" id="ApprovalInfo">
      <sch:report test="count(parent::mif:*/ancestor::mif:*/mif:header/mif:*[name(.)=name(current())])!=0">
								ERROR: Cannot have a ballotInfo when a parent package has a ballotInfo.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate ballotSubmission element">
    <sch:rule context="mif:ballotSubmission">
      <sch:extends rule="BallotSubmission"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate BallotSubmission type">
    <sch:rule abstract="true" id="BallotSubmission">
      <sch:report test="contains(@vote, 'Negative') and not(parent::mif:*/parent::mif:header//mif:ballotComment[contains(@vote, 'Negative')] or mif:voterComments)">
								WARNING: You must have general comments or a negative ballotComment to have a negative vote.</sch:report>
      <sch:report test="@vote!='Negative' and parent::mif:*/parent::header//mif:ballotComment[contains(@vote, 'Negative')]">
								WARNING: Vote must be 'Negative' if there are any negative comments.</sch:report>
      <sch:report test="@vote!='Negative' and @resolution">
								WARNING: Resolutions should only be specified for negative votes.</sch:report>
      <sch:report test="@resolution and parent::mif:*/parent::header//mif:ballotComment[not(mif:resolution)]">
								WARNING: Should only have a resolution for a ballot submission when all ballotcomments have been resolved.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate Uuid type">
    <sch:rule context="mif:Uuid">
      <!-- Exclude guiids in graphicIds because these are referencing a guid elsewhere in the document -->
      <sch:report test="count(//attribute(*, mif:Uuid)[count(parent::element(*,mif:GraphicInformation))=0][.=current()]|//element(*, mif:Uuid)[.=current()])&gt;1">
							ERROR: UUIDs should never be duplicated within a document.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate MultiplicityRange type">
    <sch:rule abstract="true" id="MultiplicityRange">
      <sch:report test="@maximumMultiplicity!='*' and number(@minimumMultiplicity)&gt;number(@maximumMultiplicity)">
							ERROR: MinimumMultiplicity must be less than or equal to maximumMultiplicity.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate MultiplicityRange type">
    <sch:rule abstract="true" id="MultiplicityRange">
      <sch:report test="@maximumMultiplicity!='*' and number(@minimumMultiplicity)&gt;number(@maximumMultiplicity)">
							ERROR: MinimumMultiplicity must be less than or equal to maximumMultiplicity.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate DomainSpecification type">
    <sch:rule abstract="true" id="DomainSpecification">
      <!-- Todo: Check that package refs are appropriately populated based on type -->
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate Feature type">
    <sch:rule abstract="true" id="Feature">
      <sch:extends rule="Presence"/>
      <sch:report test="@fixedValue and @defaultValue">
							ERROR: Can't have both a 'fixed' value and a 'default' value for a single element ABC.</sch:report>
      <sch:report test="@defaultFrom and not(@defaultValue)">
							ERROR: Can't have both a 'defaultFrom' value and a 'default' value for a single element.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate UpdateMode type">
    <sch:rule abstract="true" id="UpdateMode">
      <sch:report test="@updateModeDefault and not(contains(concat(';', translate(@allowedUpdateModes, ' ', ';'), ';'), concat(';', @updateModeDefault, ';')))">
							ERROR: DefaultUpdateMode must be part of allowedUpdateModes.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate Presence type">
    <sch:rule abstract="true" id="Presence">
      <sch:report test="@isMandatory='true' and @conformance!='R'">
							ERROR: Conformance must be 'R' for mandatory elements.</sch:report>
      <sch:report test="@isMandatory='true' and @minimumMultiplicity=0 and not(@defaultValue)">
							ERROR: MinimumMultiplicity must be at least 1 or there must be a default value when 'mandatory' is true.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate LibraryAnnotations element">
    <sch:rule context="element(*, mif:LibraryAnnotations">
      <sch:report test="*/*[not(cascadeInfo)]">
							ERROR: 'cascadeInfo' must be specified on all annotations inside an annotation library.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate AppInfoLibrary element">
    <sch:rule context="element(*, mif:AppInfoLibrary">
      <sch:report test="*[preceding-sibling::*[name(.)=name(current())][deep-equals(cascadeInfo, current()/cascadeInfo)]]">
							ERROR: 'cascadeInfo' must be unique across all annotations of the same type within an appInfo library.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate datatypeModel element">
    <sch:rule context="mif:datatypeModel">
      <sch:extends rule="DatatypeModel"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate DatatypeModel type">
    <sch:rule abstract="true" id="DatatypeModelLibrary">
      <sch:extends rule="Package"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate datatype element">
    <sch:rule context="mif:datatype">
      <sch:extends rule="Datatype"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate Datatype type">
    <sch:rule abstract="true" id="Datatype">
      <sch:report test="count(preceding::mif:*[name(.)=name(current())][@name=current()/@name][count(mif:parameter[@name=current()/mif:parameter/@name])=count(mif:parameter)])!=0">
							ERROR: There must not be more than one datatype definition having the same name and parameters.</sch:report>
      <sch:report test="ancestor::mif:datatypeModelLibrary//*[name(.)=name(current())][@name=current()/@name]&gt;1">
							ERROR: There cannot be more than one datatype with the same name.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate operation element">
    <sch:rule context="mif:operation">
      <sch:extends rule="DatatypeOperation"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate DatatypeOperation type">
    <sch:rule abstract="true" id="DatatypeOperation">
      <sch:extends rule="Operation"/>
      <extends rule="CodeValidation"/>
      <sch:report test="@operationKind!='conversion' and count(preceding-sibling::mif:*[name(.)=name(current())][@name=current()/@name][count(mif:parameter[@name=current()/mif:parameter/@name])=count(mif:parameter)]">
							ERROR: There must not be more than one operation within a datatype definition having the same name and parameters.</sch:report>
      <sch:report test="@defaultFrom and count(mif:type/descendant-or-self::mif:*[contains('CD;CE;CS;SC;BL;ST;INT;REAL;MO;PQ', @name)])!=1">
							WARNING: DefaultFrom may only be specified for codes and simple datatypes (CD, CE, CS, SC, BL, ST, INT, REAL, MO, PQ).</sch:report>
      <sch:report test="count(mif:vocabularySpecification)=0 and count(mif:type/descendant-or-self::mif:*[contains('CD;CE;CS;SC', @name)])!=0">
							WARNING: Domain must be present for coded types (CD, CE, CS and SC).</sch:report>
      <sch:report test="count(mif:vocabularySpecification)!=0 and count(mif:type/descendant-or-self::mif:*[contains('CD;CE;CS;SC', @name)])=0">
							WARNING: Domain may only be present for coded types (CD, CE, CS and SC).</sch:report>--&gt;
						<sch:report test="@default and count(mif:type/descendant-or-self::mif:*[contains('CD;CE;CS;SC;BL;ST;INT;REAL;MO;PQ', @name)])!=1">
							WARNING: Default may only be specified for codes and simple datatypes (CD, CE, CS, SC, BL, ST, INT, REAL, MO, PQ).</sch:report>
      <sch:report test="@minimumSupportedLength and count(mif:type/descendant-or-self::mif:*[contains('CD;CE;CS;SC;BL;ST;INT;REAL;MO;PQ', @name)])!=1">
							WARNING: Minimum supported length may only be specified for codes and simple datatypes (CD, CE, CS, SC, BL, ST, INT, REAL, MO, PQ).</sch:report>
      <sch:report test="count(mif:type/descendant-or-self::mif:*[@name='CS'])!=0 and mif:vocabularySpecification/@codingStrength='CWE'">
							ERROR: CodingStrength must not be CWE for CS datatype attributes.</sch:report>
      <sch:report test="count(mif:derivedFrom)=0 and count(mif:businessName)=0">
							ERROR: Non-derived operations must have a business name.</sch:report>
      <!-- Todo: How come? . . . -->
      <sch:report test="count(mif:derivedFrom)=0 and count(mif:annotations/mif:definition)=0">
							ERROR: Non-derived operations must have a definition annotation.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate derivedFrom element">
    <sch:rule context="mif:operation/mif:derivedFrom">
      <sch:extends rule="OperationDerivation"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate vocabularySpecification element">
    <sch:rule context="mif:vocabularySpecification">
      <sch:extends rule="DomainSpecificationWithStrength"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate OperationDerivation type">
    <sch:rule abstract="true" id="OperationDerivation">
      <sch:report test="@operationName and count(mif:type)!=0">
							ERROR: A property reference may only have a name or a conversion datatype, not both.</sch:report>
      <sch:report test="not(@operationName) and count(mif:type)=0">
							ERROR: A property reference must have either a name or a conversion datatype.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Check classTypeRef information">
    <sch:rule context="mif:classTypeRef">
      <sch:report test="count(ancestor::mif:staticModel//mif:classTypeRef[@typeId=current()/@typeId])!=1">
										ERROR: Only one classTypeRef may exist in an staticModel with a given typeId.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" xmlns:xmi="http://www.eclipse.org/uml2/1.0.0/UML" name="Validate TermDefinition type">
    <sch:rule context="element(*, mif:TermDefinition)">
      <sch:report test="count(seeAlso)+count(definition)=0">
              ERROR: Must have at least one of 'seeAlso' or 'definition'.</sch:report>
      <sch:report test="count(termTranslations[@name=current()/@term])!=0">
              ERROR: Cannot have a term translation with the same name as the term.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" xmlns:xmi="http://www.eclipse.org/uml2/1.0.0/UML" name="Validate packageLocation element">
    <sch:rule context="mif:packageLocation">
      <sch:extends rule="PackageRef"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Validate SubSystem type">
    <sch:rule abstract="true" id="SubSystem">
      <sch:extends rule="Package"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Validate StaticModelBase type">
    <sch:rule abstract="true" id="StaticModelBase">
      <sch:extends rule="SubSystem"/>
      <sch:report test="@isSerializable='true' and ancestor-or-self::mif:*/mif:packageLocation[contains(';RIM;DIM;DMIM-deprecated;', concat(';', @artifactKind, ';'))]">
							WARNING: RIM, DMIM and DIM models are not serializable.</sch:report>
      <sch:report test="@isSerializable='true' and ancestor-or-self::mif:*[contains(';RIM;DIM;DMIM-deprecated;', concat(';', @packageKind, ';'))]">
							WARNING: RIM, DMIM and DIM models are not serializable.</sch:report>
      <sch:report test="@isSerializable='false' and ancestor-or-self::mif:*/mif:packageLocation[contains(';RIM;DIM;DMIM-deprecated;', concat(';', @artifactKind, ';'))]">
							WARNING: All model types except RIM, DMIM and DIM models are serializable.</sch:report>
      <sch:report test="@isSerializable='false' and ancestor-or-self::mif:*[contains(';RIM;DIM;DMIM-deprecated;', concat(';', @packageKind, ';'))]">
							WARNING: All model types except RIM, DMIM and DIM models are serializable.</sch:report>
      <sch:report test="@isSerializable='false' and @representationKind='serialized'">
							ERROR: Can't have a serialized representation of a non-serializable model.</sch:report>
      <sch:report test="@isSerializable='true' and @representationKind='flat' and              count(association/connections[count(traversableConnection)!=1])!=0">
							ERROR: Serializable model associations must be traverable in one and only one direction..</sch:report>
      <sch:report test="ancestor-or-self::mif:*[mif:packageLocation/@artifactKind='RIM' or @packageKind='RIM'] and count(mif:subjectArea)=0">
							GUIDELINE: RIM models must have at least one subjectArea.</sch:report>
      <sch:report test="@isSerializable='true' and count(mif:entryPoint)!=1">
							ERROR: Serializable models must have exactly one entry point.</sch:report>
      <sch:report test="ancestor-or-self::mif:*[mif:packageLocation/@artifactKind='RIM' or @packageKind='RIM'] and count(mif:derivationSupplier)!=0">
							ERROR: A RIM cannot be derived from another model.</sch:report>
      <sch:report test="not(ancestor-or-self::mif:*[mif:packageLocation/@artifactKind='RIM' or @packageKind='RIM']) and count(mif:derivationSupplier[mif:targetStaticModel/@artifactKind='RIM'])=0">
							ERROR: Non-RIM models must be derived from at least one other model (one of which must have a type of 'RIM').</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Check model annotations">
    <sch:rule context="mif:annotations/*">
      <sch:report test="count(mif:presentation)!=0 and not(parent::mif:annotations[parent::mif:class or parent::mif:classTypeRef or parent::mif:classStub or parent::mif:attribute or parent::mif:association])">
							ERROR: Annotations may only have presentation information when associated with a class, classTypeRef, classStub, attribute or association.</sch:report>
      <sch:report test="count(mif:presentation)!=0 and            ((not(parent::mif:annotations[parent::mif:attribute or parent::mif:association]) and             count(parent::mif:annotations/parent::mif:*/mif:presentation[@shapeId=current()/mif:presentation/@connectionShapeId])=0) or           (parent::mif:annotations/parent::mif:attribute and             count(parent::mif:annotations/parent::mif:attribute/parent::mif:*/mif:presentation[@shapeId=current()/mif:presentation/@connectionShapeId])=0) or           (parent::mif:annotations/parent::mif:association and             count(parent::mif:annotations/parent::mif:association/parent::mif:*/mif:presentation[@shapeId=current()/mif:presentation/@connectionShapeId])=0))">
							ERROR: Connection shapeId on annotation does not point to a shape associated with the annotated class.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Check displayInfo constraints">
    <sch:rule context="mif:staticModel">
      <sch:report test="count(.//mif:displayInfo[@representationType='UML'])!=0 and count(.//mif:subjectArea) != count(.//mif:subjectArea[count(mif:displayInfo[@representationType='UML'])!=0])">
							ERROR: If any elements have UML displayInfo, all subjectAreas must have UML displayInfo.</sch:report>
      <sch:report test="count(.//mif:displayInfo[@representationType='UML'])!=0 and count(.//mif:entryPoint) != count(.//mif:entryPoint[count(mif:displayInfo[@representationType='UML'])!=0])">
							ERROR: If any elements have UML displayInfo, all entryPoints must have UML displayInfo.</sch:report>
      <sch:report test="count(.//mif:displayInfo[@representationType='UML'])!=0 and count(.//mif:class) != count(.//mif:class[count(mif:displayInfo[@representationType='UML'])!=0])">
							ERROR: If any elements have UML displayInfo, all classes must have UML displayInfo.</sch:report>
      <sch:report test="count(.//mif:displayInfo[@representationType='UML'])!=0 and count(.//mif:classTypeRef) != count(.//mif:classTypeRef[count(mif:displayInfo[@representationType='UML'])!=0])">
							ERROR: If any elements have UML displayInfo, all classTypeRefs must have UML displayInfo.</sch:report>
      <sch:report test="count(.//mif:displayInfo[@representationType='UML'])!=0 and count(.//mif:classStub) != count(.//mif:classStub[count(mif:displayInfo[@representationType='UML'])!=0])">
							ERROR: If any elements have UML displayInfo, all classStubs must have UML displayInfo.</sch:report>
      <sch:report test="count(.//mif:displayInfo[@representationType='HL7Visio'])!=0 and count(.//mif:subjectArea) != count(.//mif:subjectArea[count(mif:displayInfo[@representationType='HL7Visio'])!=0])">
							ERROR: If any elements have HL7Visio displayInfo, all subjectAreas must have HL7Visio displayInfo.</sch:report>
      <sch:report test="count(.//mif:displayInfo[@representationType='HL7Visio'])!=0 and count(.//mif:entryPoint) != count(.//mif:entryPoint[count(mif:displayInfo[@representationType='HL7Visio'])!=0])">
							ERROR: If any elements have HL7Visio displayInfo, all entryPoints must have HL7Visio displayInfo.</sch:report>
      <sch:report test="count(.//mif:displayInfo[@representationType='HL7Visio'])!=0 and count(.//mif:class) != count(.//mif:class[count(mif:displayInfo[@representationType='HL7Visio'])!=0])">
							ERROR: If any elements have HL7Visio displayInfo, all classes must have HL7Visio displayInfo.</sch:report>
      <sch:report test="count(.//mif:displayInfo[@representationType='HL7Visio'])!=0 and count(.//mif:classTypeRef) != count(.//mif:classTypeRef[count(mif:displayInfo[@representationType='HL7Visio'])!=0])">
							ERROR: If any elements have HL7Visio displayInfo, all classTypeRefs must have HL7Visio displayInfo.</sch:report>
      <sch:report test="count(.//mif:displayInfo[@representationType='HL7Visio'])!=0 and count(.//mif:classStub) != count(.//mif:classStub[count(mif:displayInfo[@representationType='HL7Visio'])!=0])">
							ERROR: If any elements have HL7Visio displayInfo, all classStubs must have HL7Visio displayInfo.</sch:report>
    </sch:rule>
    <sch:rule context="mif:displayInfo">
      <sch:report test="preceding-sibling::mif:displayInfo[@representationType=current()/@representationType] and not(parent::mif:class or parent::mif:classTypeRef or parent::mif:classStub)">
							ERROR: Only classes may have more than one display info for a given representationType.  (Classes may have 'shadow' shapes for hl7 representations).</sch:report>
      <sch:report test="count(parent::mif:*/mif:displayInfo[@representationType='UML'])&gt;1">
							ERROR: There may not be more than one set of UML display info for a given element.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Validate derivationSupplier constraints">
    <sch:rule context="mif:staticModel/mif:derivationSupplier[@derivationType!='extension' and @derivationType='incompatible']">
      <sch:report test="count(parent::mif:staticModel//mif:*[mif:derivationSupplier[@className]/@modelId=current()/@modelId])!=              count(parent::mif:staticModel//*[self::mif:class or self::mif:classTypeRef or self::mif:classStub])">
										ERROR: If a derivationSupplier' element exists for a staticModel, all classes within that model must also have a derivationSupplier for the same modelId, unless the derivation type is 'extension'.</sch:report>
    </sch:rule>
    <sch:rule context="mif:derivationSupplier[parent::mif:class or parent::mif:classTypeRef or parent::mif:classStub][@derivationType!='extension' and @derivationType='incompatible']">
      <sch:report test="count(parent::mif:*/mif:attribute[mif:derivationSupplier/@modelId=current()/@modelId])!=count(parent::mif:*/mif:attribute)">
										ERROR: If a 'derivationSupplier' element exists for a staticModel, all attributes within that model must also have a derivationSupplier for the same modelId.</sch:report>
      <sch:report test="count(parent::mif:*/mif:association[mif:derivationSupplier/@modelId=current()/@modelId])!=count(parent::mif:*/mif:association)">
										ERROR: If a 'derivationSupplier' element exists for a staticModel, all associations within that model must also have a derivationSupplier for the same modelId.</sch:report>
      <sch:report test="count(parent::mif:*/mif:stateMachine/mif:state[mif:derivationSupplier/@modelId=current()/@modelId])!=count(parent::mif:*/mif:stateMachine/mif:state)">
										ERROR: If a 'derivationSupplier' element exists for a staticModel, all states within that model must also have a derivationSupplier for the same modelId.</sch:report>
      <sch:report test="count(parent::mif:*/mif:stateMachine/mif:transition[mif:derivationSupplier/@modelId=current()/@modelId])!=count(parent::mif:*/mif:stateMachine/mif:transition)">
										ERROR: If a 'derivationSupplier' element exists for a staticModel, all transitions within that model must also have a derivationSupplier for the same modelId.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Validate additional derivationSupplier constraints">
    <sch:rule context="mif:derivationSupplier">
      <sch:report test="count(preceding-sibling::mif:derivationSupplier[@modelId=current()/@modelId])!=0">
										GUIDELINE: An element may not be derived from two different elements within the same model.</sch:report>
      <sch:report test="@derivationType='unchanged' and count(parent::mif:*//mif:derivationSupplier[@modelId=current()/@modelId][@derivationType!='unchanged'])">
										ERROR: An element derivation may not be marked as 'unchanged' unless all child elements are marked as 'unchanged'.</sch:report>
      <sch:report test="@derivationType='annotation' and count(parent::mif:*//mif:derivationSupplier[@modelId=current()/@modelId][@derivationType!='unchanged' and @derivationType!='annotation'])">
										ERROR: An element derivation may not be marked as 'annotation' unless all child elements are marked as 'annotation' or 'unchanged'.</sch:report>
      <sch:report test="@derivationType='restriction' and count(parent::mif:*//mif:derivationSupplier[@modelId=current()/@modelId][@derivationType!='unchanged' and @derivationType!='annotation' and @derivationType!='restriction'])">
										ERROR: An element derivation may not be marked as 'restriction' unless all child elements are marked as 'restriction', 'annotation' or 'unchanged'.</sch:report>
      <sch:report test="@derivationType='extention' and count(parent::mif:*//mif:derivationSupplier[@modelId=current()/@modelId][@derivationType!='unchanged' and @derivationType!='annotation' and @derivationType!='restriction' and @derivationType!='extention'])">
										ERROR: An element derivation may not be marked as 'extention' unless all child elements are marked as 'extention', 'restriction', 'annotation' or 'unchanged'.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Validate still more derivationSupplier constraints">
    <sch:rule context="mif:derivationSupplier[@stateName or @stateTransitionName]">
      <sch:report test="count(parent::mif:stateMachine/parent::mif:*/mif:derivationSupplier[@modelId=current()/@modelId])=0">
										ERROR: An element may only be derived from a model that its parent is also derived from</sch:report>
    </sch:rule>
    <sch:rule context="mif:derivationSupplier[@attributeName|@associationName|@className]">
      <sch:report test="count(parent::mif:*/mif:derivationSupplier[@staticModelDerivationId=current()/@staticModelDerivationId])=0">
										ERROR: An element may only be derived from a model that its parent is also derived from</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Validate SubjectAreaPackage type">
    <sch:rule abstract="true" id="SubjectAreaPackage">
      <sch:report test="count(mif:subjectAreaPackage|mif:class)=0">
							WARNING: SubjectAreaPackages must contain either other subject areas or classes.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Validate ballotInfo element">
    <sch:rule context="mif:ballotInfo">
      <sch:extends rule="BallotInfo"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Validate entry-point constraints">
    <sch:rule context="mif:entryPoint">
      <sch:report test="parent::mif:staticModel/@serializable='true' and count(mif:annotations)!=0">
							ERROR: Annotations are only permitted on entry points for non-serializable models.  (For serializable models, the entry point and model are 1..1)</sch:report>
      <sch:report test="parent::mif:staticModel/@serializable='true' and count(mif:businessName/@name)!=0">
							ERROR: Names are only permitted on entry points for non-serializable models.  (For serializable models, the entry point and model are 1..1)</sch:report>
    </sch:rule>
    <!-- Todo: Ensure entrypoint name is unique within a model -->
    <!-- Todo: Don't allow abstract to be populated - we inherit it, but don't want it here -->
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Validate common class constraints">
    <sch:rule context="mif:class|mif:classTypeRef|mif:classStub">
      <sch:report test="@sortKey and count(ancestor::mif:staticModel/descendant::mif:*[self::mif:class or self::mif:classTypeRef or self::mif:classStub][@sortKey=current()/@sortKey])!=1">
							ERROR: Sequence number must be unique across all classes in a model.</sch:report>
      <sch:report test="not(@sortKey) and count(ancestor::mif:staticModel/descendant::mif:*[self::mif:class or self::mif:classTypeRef or self::mif:classStub][@sortKey])!=0">
							ERROR: Sequence number must present on all classes in a model if it is present in any.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Check class derivationSupplier constraints">
    <sch:rule context="mif:derivationSupplier[@className]">
      <sch:report test="@className!=parent::mif:*/@name and count(ancestor::mif:model//mif:derivationSupplier[@className=current()/@className and @modelId=current()/@modelId and not(@attributeName or @stateName or @stateTransitionName)])=1">
											GUIDELINE: A class derivation must reference the same name as the derived-from class *unless* there are multiple classes derived from the same model class.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Validate ClassDerivation type">
    <sch:rule abstract="true" id="ClassDerivation">
      <sch:report test="count(ancestor::mif:*/mif:derivedFrom[@staticModelDerivationId=current()/@staticModelDerivationId])=0">
							ERROR: Current derivation refers to a dervationId that is not found on the parent static model.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Check class attributes and elements">
    <sch:rule context="mif:class">
      <sch:report test="@abstract='true' and count(mif:*)=0">
							ERROR: Abstract classes must have descendants.</sch:report>
      <sch:report test="count(ancestor::mif:staticModel/mif:subjectArea)!=0 and not(mif:primarySubjectArea)">
							ERROR: PrimarySubjectArea is required if a model contains subjectAreas.</sch:report>
      <sch:report test="count(mif:attribute)=0 and count(mif:associations)=0 and count(mif:*)=0">
							WARNING: Classes should have at least one attribute, association or descendant.</sch:report>
      <sch:report test="contains(@name, '_') and count(mif:*)=0">
							WARNING: Class names should only contain '_' if they have descendants.</sch:report>
      <!-- Todo: Add rule that classes with a "choice" display variation must have specializations.  Also, need to decide if "conformance" applies to specialization relationships.  I.e. can some specializations be optional? -->
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Check attribute information.">
    <sch:rule context="mif:attribute">
      <sch:extends rule="Attribute"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Check for attribute/association duplication.">
    <sch:rule context="mif:attribute">
      <sch:report test="count(following-sibling::mif:associations/mif:association[@name=current()/@name])!=0">
											ERROR: May not have attribute with the same name as a association in a single class.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Check attribute derivations">
    <sch:rule context="mif:attribute">
      <sch:report test="mif:derivedFrom[@attributeName!=current()/@attributeName]">
											GUIDELINE: Attribute names must be the same as the names of the attribute being derived from.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Check classTypeRef information">
    <sch:rule context="mif:classTypeRef">
      <sch:report test="count(ancestor::mif:staticModel//mif:classTypeRef[@typeId=current()/@typeId])!=1">
										ERROR: Only one classTypeRef may exist in an staticModel with a given typeId.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Check classTypeRef information">
    <sch:rule context="mif:classTypeRef">
      <sch:report test="count(ancestor::mif:staticModel//mif:classTypeRef[@typeId=current()/@typeId])!=1">
										ERROR: Only one classTypeRef may exist in an staticModel with a given typeId.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Check state information">
    <sch:rule context="mif:state">
      <sch:report test="mif:derivedFrom[@stateName!=current()/@name]">
														GUIDELINE: State names must be the same as the state names of the state being derived from.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Check state information">
    <sch:rule context="mif:transition">
      <sch:report test="mif:derivedFrom[@stateTransitionName!=current()/@name]">
														GUIDELINE: State transition names must be the same as the state names of the state being derived from.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Validate StructuralFeature type">
    <sch:rule abstract="true" id="StructuralFeature">
      <sch:extends rule="Feature"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Validate Attribute type">
    <sch:rule abstract="true" id="Attribute">
      <sch:extends rule="StructuralFeature"/>
      <sch:extends rule="UpdateMode"/>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Check association information in a class">
    <sch:rule context="mif:association">
      <sch:report test="ancestor::mif:staticModel/@type='RIM' and not(@linkAssociationName)">
							GUIDELINE: RIM models must not have blocked associations (linkAssociationName must always be specified).</sch:report>
      <sch:report test="@name=@linkAssociationName">
							ERROR: The name of a association and the association linked to must not be the same.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Check association derivationSupplier constraints">
    <sch:rule context="mif:derivedFrom[@associationName]">
      <sch:report test="@associationName!=parent::mif:*/@name and count(ancestor::mif:model//mif:derivedFrom[@className=current()/@className and @associationName=current()/@associationName and @modelId=current()/@modelId])=1">
											GUIDELINE: An association derivation must reference the same name as the derived-from association *unless* there are multiple classes derived from the same model class.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Check association derivationSupplier constraints">
    <sch:rule context="mif:derivedFrom[@associationName]">
      <sch:report test="@associationName!=parent::mif:*/@name and count(ancestor::mif:model//mif:derivedFrom[@className=current()/@className and @associationName=current()/@associationName and @modelId=current()/@modelId])=1">
											GUIDELINE: An association derivation must reference the same name as the derived-from association *unless* there are multiple classes derived from the same model class.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Check descendant constraints">
    <sch:rule context="descendant">
      <sch:report test="preceding-sibling::descendant[@sortKey = current()/@sortKey]">
							ERROR: Sequence number must be unique across all descendants in a model.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:xhtml="urn:hl7-org:v3/mif2" name="Check class presentation constraints">
    <sch:rule context="mif:presentation[@shapeId]">
      <sch:report test="count(ancestor::mif:staticModel//mif:presentation[@shapeId=current()/@shapeId])!=1">
              ERROR: Only one shape within a model can have a given shapeId.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate DerivedStaticModel type">
    <sch:rule context="element(*, mif:DerivedStaticModel)">
      <sch:report test="mif:originalArtifact[contains('RIM','DIM','CIM','LIM','DMIM','RM','HD','MT'), @artifact)">
							ERROR: Can only derive a static model from another static model.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Check for duplicate attributes or associations in parent or child classes">
    <p>This check only works with the Saxon (6.5.2) or Xalan (2.4.1 or newer) because it makes use of the 'closure' functions</p>
    <sch:rule context="mif:attribute|mif:association">
      <sch:report test="(function-available('saxon:closure') and count(saxon:closure(parent::mif:class, saxon:expression('                     ancestor::mif:staticModel//*[self::mif:hl7Class or self::mif:classTypeRef or self::mif:classStub or self::mif:localClassReference]                     [@name=current()/mif:descendant/@className]'))/*[self::mif:attribute or self::mif:association][@name=current()/@name])!=1) or                     (function-available('dyn:closure') and count((parent::mif:class|dyn:closure(parent::mif:class,                      'ancestor::mif:staticModel//*[self::mif:class or self::mif:classTypeRef or self::mif:classStub or self::mif:localClassReference]                     [@name=current()/mif:descendant/@className]'))/*[self::mif:attribute or self::mif:association][@name=current()/@name])!=1)">
              ERROR: Class descendant (or descendant thereof) has an attribute or association with the same name.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:ns uri="urn:hl7-org:v3/mif" prefix="mif"/>
  <sch:pattern name="Validate class constraints">
    <sch:rule context="mif:class|mif:classTypeRef|mif:classStub">
      <sch:report test="count(ancestor::mif:staticModel/descendant::mif:*[@name=current()/@name and (self::mif:class or self::mif:classTypeRef or self::mif:classStub)])&gt;1">
							ERROR: Only one class, classTypeRef or classStub may exist with a given name inside an staticModel.</sch:report>
      <sch:report test="count(ancestor::mif:staticModel/descendant::mif:*[mif:businessName/@name=current()/mif:businessName/@name and (self::mif:class or self::mif:classTypeRef or self::mif:classStub)])&gt;1">
							ERROR: Only one class, classTypeRef or classStub may exist with a given business name inside an staticModel.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate Class type">
    <sch:rule abstract="true" id="Class">
      <sch:extends rule="ClassBase"/>
      <sch:report test="count(mif:specialization)!=0 and count(mif:attribute)!=0">
							ERROR: Must not have both specializations and attributes for a class.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern name="Validate class constraints">
    <sch:rule context="mif:class|mif:classTypeRef|mif:classStub">
      <sch:report test="count(ancestor::mif:staticModel/descendant::mif:*[@name=current()/@name and (self::mif:class or self::mif:classTypeRef or self::mif:classStub)])&gt;1">
							ERROR: Only one class, classTypeRef or classStub may exist with a given name inside an staticModel.</sch:report>
      <sch:report test="count(ancestor::mif:staticModel/descendant::mif:*[mif:businessName/@name=current()/mif:businessName/@name and (self::mif:class or self::mif:classTypeRef or self::mif:classStub)])&gt;1">
							ERROR: Only one class, classTypeRef or classStub may exist with a given business name inside an staticModel.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:hl7="urn:hl7-org:xhtml1-hl7-types" name="Validate Object type">
    <sch:rule context="element(*, xhtml:Object)">
      <sch:report test="@name='divRef' and (count(xhtml:param)!=1 or count(xhtml:param[@name='id'])!=1)">
											ERROR: divRef must have exactly one parameter which must have a type of 'id'.</sch:report>
      <sch:report test="@name='figureRef' and (count(xhtml:param)!=1 or count(xhtml:param[@name='id'])!=1)">
											ERROR: divRef must have exactly one parameter which must have a type of 'id'.</sch:report>
      <sch:report test="@name='tableRef' and (count(xhtml:param)!=1 or count(xhtml:param[@name='id'])!=1)">
											ERROR: divRef must have exactly one parameter which must have a type of 'id'.</sch:report>
      <sch:report test="@name='itemName' and (count(xhtml:param)&gt;1 or count(xhtml:param[@name!='item'])!=0)">
											ERROR: itemName is only allowed one parameter, which must be of type 'item' (if present)</sch:report>
      <sch:report test="@name='annotationRef' and (count(xhtml:param[@name='annotationKind'])=0 or count(xhtml:param[not(contains('annotationKind','item','id'),@name)])!=0)">
											ERROR: annotationRef must have an annotationKind parameter.  Only other allowed parameters are 'item' and 'id'</sch:report>
      <sch:report test="(@name='itemName' or @name='') and not(xhtml:Param[@name='item']) and (not(                 ancestor::mif:datatypeDefinition or ancestor::mif:property or ancestor::mif:class or ancestor::mif:attribute or                 ancestor::mif:relationship or ancestor::mif:triggerEvent or ancestor::mif:applicationRole))">
										ERROR: There is no ancestor of an appropriate type having a name.</sch:report>
    </sch:rule>
  </sch:pattern>
  <sch:pattern xmlns:hl7="urn:hl7-org:xhtml1-hl7-types" name="Validate Param type">
    <sch:rule context="element(*, xhtml:Param)">
      <sch:report test="@name='id' and not(@value castable as mif:BasicId)">
											ERROR: 'id' parameter must restricted to alpha-numeric with a limited set of punctuation and must be between 1 and 40 characters in length.</sch:report>
      <sch:report test="@name='item' and not(@value castable as mif:ItemName)">
											ERROR: 'item' parameter must be drawn from the set of allowed item values (refer to mif:referencedCodes.xsd/ParentArtifactKind)</sch:report>
      <sch:report test="@name='annotationKind' and not(@value castable as mif:AnnotationKind)">
											ERROR: 'annotationKind' parameter must be drawn from the set of allowed annotation types (refer to mif:referencedCodes.xsd/AnnotationKind)</sch:report>
      <sch:report test="@name='item' and (           (@value='datatype' and not(ancestor::mif:datatypeDefinition)) or                 (@value='property' and not(ancestor::mif:property)) or                 (@value='class' and not(ancestor::mif:class)) or                 (@value='attribute' and not(ancestor::mif:attribute)) or                 (@value='relationship' and not(ancestor::mif:relationship)) or                 (@value='trigger' and not(ancestor::mif:triggerEvent)) or                 (@value='appRole' and not(ancestor::mif:applicationRole))">
										ERROR: There is no ancestor of the specified type.</sch:report>
    </sch:rule>
  </sch:pattern>
</sch:schema>
