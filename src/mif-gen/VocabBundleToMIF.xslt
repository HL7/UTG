<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://hl7.org" xmlns:saxon="http://saxon.sf.net/" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:f="http://hl7.org/fhir" xmlns="urn:hl7-org:v3/mif2" xpath-default-namespace="http://hl7.org/fhir" exclude-result-prefixes="fn saxon xs f">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:param name="version"/>
	<xsl:variable name="versionDate" select="substring-after($version, '-')"/>
	<xsl:variable name="bindingDate" select="concat(substring($versionDate, 1, 4), '-', substring($versionDate, 5, 2), '-', substring($versionDate, 7, 2))"/>
	<xsl:key name="resourceByUrl" match="/Bundle/entry" use="fullUrl/@value"/>
	<xsl:variable name="toolVersion" select="0.1"/>
	<xsl:variable name="conceptDomainSystemUrl" select="'http://terminology.hl7.org/CodeSystem/conceptdomains'"/>
	<xsl:variable name="urls" as="element(url)+">
    <xsl:apply-templates mode="url" select="Bundle/entry/resource/*[self::CodeSystem or self::ValueSet or self::NamingSystem]"/>
	</xsl:variable>
	<xsl:template match="/Bundle">
    <xsl:if test="$version='todo'">
      <xsl:message select="'WARNING: No vocabulary package version specified.  Will set to ''todo'''"/>
    </xsl:if>
    <vocabularyModel name="{$version}" title="HL7 Vocabulary" packageKind="version" definitionKind="partial-publishing" schemaVersion="2.1.7">
      <packageLocation combinedId="DEFN=UV=VO={$version}" root="DEFN" artifact="VO" realmNamespace="UV" version="{$version}"/>
      <header>
        <renderingInformation renderingTime="{substring(string(current-dateTime()),1,19)}" application="UTG FHIR to MIF transform {$toolVersion}"/>
        <legalese copyrightOwner="Health Level Seven, Inc." copyrightYears="{substring(string(current-date()), 1, 4)}"/>
      </header>
      <xsl:call-template name="doConceptDomains"/>
      <xsl:call-template name="doCodeSystems"/>
      <xsl:call-template name="doValueSets"/>
      <xsl:call-template name="doBindingRealms"/>
      <xsl:call-template name="doContextBindings"/>
    </vocabularyModel>
	</xsl:template>
	<xsl:template name="doConceptDomains">
    <xsl:for-each select="key('resourceByUrl', $conceptDomainSystemUrl)/resource/CodeSystem//concept[property[code/@value='source']/valueCode/@value='v3']">
      <xsl:sort select="code/@value"/>
      <conceptDomain name="{code/@value}">
        <!-- Not bothering with the following.  Only exists in one place and the fact it's deprecated is in the definition.
         <historyItem dateTime="2012-03-15" id="00000000-0000-0000-0000-000000000000"
                     isSubstantiveChange="true"
                     isBackwardCompatibleChange="false">
           <description>retiredAsOfRelease: 1130-20120315 Indicates that the Concet Domain was retired from any further use as of the release indicated.</description>
        </historyItem>-->
        <annotations>
          <documentation>
            <definition>
              <text>
                <xsl:copy-of select="fn:markdownToHTML(definition/@value, true())"/>
              </text>
            </definition>
          </documentation>
          <xsl:variable name="appInfo" as="element()*">
            <xsl:for-each select="property[code/@value='openIssue']">
              <openIssue>
                <text>
                  <xsl:copy-of select="fn:markdownToHTML(valueString/@value, true())"/>
                </text>
              </openIssue>
            </xsl:for-each>
<!--            <xsl:if test="property[code/@value='deprecationInfo']">
              <deprecationInfo deprecationEffectiveVersion="{property[code/@value='deprecationEffectiveVersion']/valueString/@value}">
                <text>
                  <xsl:copy-of select="fn:markdownToHTML(property[code/@value='deprecationInfo']/valueString/@value, true())"/>
                </text>
              </deprecationInfo>
            </xsl:if>-->
          </xsl:variable>
          <xsl:if test="count($appInfo)!=0">
            <appInfo>
              <xsl:copy-of select="$appInfo"/>
          <!--TODO:
            <deprecationInfo deprecationEffectiveVersion="1221-20130726">
              <text>
                <xsl:copy-of select="fn:markdownToHTML(definition/@value, true())"/>
              </text>
            </deprecationInfo>-->
            </appInfo>
          </xsl:if>
        </annotations>
        <xsl:for-each select="parent::concept">
          <specializesDomain name="{code/@value}"/>
        </xsl:for-each>
        <xsl:for-each select="property[not(code/@value=('source', 'deprecationInfo', 'openIssue') or starts-with(code/@value, 'contextBinding'))]">
          <property name="{code/@value}">
            <xsl:for-each select="valueCode">
              <xsl:attribute name="value" select="@value"/>
            </xsl:for-each>
            <xsl:for-each select="valueCoding">
              <xsl:attribute name="value" select="concat(substring-after(system/@value, 'http://terminology.hl7.org/CodeSystem/v3-'), '.', code/@value)"/>
            </xsl:for-each>
          </property>
        </xsl:for-each>
      </conceptDomain>
    </xsl:for-each>
	</xsl:template>
  <xsl:template name="doCodeSystems">
    <xsl:for-each select="key('resourceByUrl', 'http://terminology.hl7.org/List/v3-Manifest')/resource/List/entry">
      <!-- Todo: yank this 'if' and collapse for-each loops once source is fixed -->
      <xsl:choose>
        <xsl:when test="not(preceding-sibling::entry[item/reference/@value=current()/item/reference/@value])">-->
          <xsl:for-each select="item/reference">
            <xsl:variable name="url" select="if (contains(@value, '|')) then substring-before(@value, '|') else @value"/>
            <xsl:choose>
              <xsl:when test="parent::item/type/@value='CodeSystem'">
                <xsl:for-each select="key('resourceByUrl', $url)/resource/CodeSystem">
                  <codeSystem name="{substring(id/@value, 4,1)}{substring(name/@value,2,300)}" title="{title/@value}" codeSystemId="{fn:getOID(.)}">
                    <xsl:call-template name="doHeaderElements"/>
                    <releasedVersion releaseDate="{date/@value}" completeCodesIndicator="{if (content/@value='complete') then 'true' else 'false'}">
                      <!-- TODO: Figure out if these extensions are coming back
                      <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/hl7-maintained-indicator']/valueBoolean/@value">
                        <xsl:attribute name="hl7MaintainedIndicator" select="."/>
                      </xsl:for-each>
                      <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/hl7-approved-indicator']/valueBoolean/@value">
                        <xsl:attribute name="hl7ApprovedIndicator" select="."/>
                      </xsl:for-each>-->
                      <xsl:attribute name="hl7MaintainedIndicator">true</xsl:attribute>
                      <xsl:attribute name="hl7ApprovedIndicator">true</xsl:attribute>
                      <xsl:for-each select="language/@value">
                        <supportedLanguage>
                          <xsl:value-of select="."/>
                        </supportedLanguage>
                      </xsl:for-each>
                      <!-- Todo: fix this once the extension is back -->
                      <xsl:if test="true() or extension[@url='http://hl7.org/fhir/StructureDefinition/hl7-approved-indicator']/valueString/@value='true'">
                        <supportedConceptRelationship relationshipKind="Specializes" name="Specializes" inverseName="Generalizes" isNavigable="true" reflexivity="irreflexive" symmetry="antisymmetric" transitivity="transitive">
                          <description>The child code is a more narrow version of the concept represented by the parent code.  I.e. Every child concept is also a valid parent concept.  Used to allow determination of subsumption.  Must be transitive, irreflexive, antisymmetric.</description>
                        </supportedConceptRelationship>
                        <supportedConceptRelationship relationshipKind="Generalizes" name="Generalizes" inverseName="Specializes" isNavigable="true" reflexivity="irreflexive" symmetry="antisymmetric" transitivity="transitive">
                          <description>Inverse of Specializes.  Only included as a derived relationship.</description>
                        </supportedConceptRelationship>
                      </xsl:if>
                      <xsl:for-each select="property[extension[@url='http://hl7.org/fhir/StructureDefinition/codesystem-relationshipKind']]">
                        <supportedConceptRelationship relationshipKind="{extension[@url='http://hl7.org/fhir/StructureDefinition/codesystem-relationshipKind']/valueCode/@value}" name="{code/@value}">
                          <xsl:for-each select="description">
                            <description>
                              <xsl:copy-of select="fn:markdownToHTML(@value, false())"/>
                            </description>
                          </xsl:for-each>
                        </supportedConceptRelationship>
                      </xsl:for-each>
                      <xsl:for-each select="property[not(extension[@url='http://hl7.org/fhir/StructureDefinition/codesystem-relationshipKind'] or code/@value=('isMandatory','notSelectable'))]">
                        <xsl:variable name="type">
                          <xsl:choose>
                            <xsl:when test="type/@value='code'">Token</xsl:when>
                            <xsl:when test="type/@value='boolean'">Boolean</xsl:when>
                            <xsl:when test="type/@value='string'">String</xsl:when>
                            <xsl:otherwise>
                              <xsl:message terminate="yes" select="concat('Unrecognized property type: ', type/@value)"/>
                            </xsl:otherwise>
                          </xsl:choose>
                        </xsl:variable>
                        <xsl:variable name="mandatory" select="extension[@url='http://hl7.org/fhir/StructureDefinition/codesystem-mif-extended-properties']/extension[@url='isMandatory']/valueBoolean/@value"/>
                        <supportedConceptProperty propertyName="{code/@value}" type="{$type}" isMandatoryIndicator="{if ($mandatory!='') then $mandatory else 'false'}">
                          <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/codesystem-mif-extended-properties']/extension[not(@url='isMandatory')]">
                            <xsl:attribute name="{@url}" select="*[starts-with(local-name(.), 'value')]/@value"/>
                          </xsl:for-each>
                          <xsl:for-each select="description">
                            <description>
                              <xsl:copy-of select="fn:markdownToHTML(@value, false())"/>
                            </description>
                          </xsl:for-each>
                        </supportedConceptProperty>
                      </xsl:for-each>
                      <xsl:variable name="concepts" as="element(concepts)">
                        <f:concepts>
                          <xsl:for-each select="descendant::concept">
                            <xsl:sort select="code/@value"/>
                            <concept>
                              <xsl:for-each select="property[code/@value='notSelectable' and valueBoolean/@value='true']">
                                <xsl:attribute name="isSelectable" select="'false'"/>
                              </xsl:for-each>
                              <xsl:variable name="annotations" as="element()*">
                                <xsl:for-each select="definition">
                                  <documentation>
                                    <definition>
                                      <text>
                                        <xsl:copy-of select="fn:markdownToHTML(@value, true())"/>
                                      </text>
                                    </definition>
                                  </documentation>
                                </xsl:for-each>
                                <xsl:variable name="appInfo" as="element()*">
                                  <xsl:call-template name="doDeprecation"/>
                                  <!-- Todo: drop the codesystem-openIssue extension -->
                                  <xsl:for-each select="extension[@url=('http://hl7.org/fhir/StructureDefinition/resource-openIssue','http://hl7.org/fhir/StructureDefinition/codesystem-openIssue')]/valueString">
                                    <openIssue>
                                      <text>
                                        <xsl:copy-of select="fn:markdownToHTML(@value, true())"/>
                                      </text>
                                    </openIssue>
                                  </xsl:for-each>
                                </xsl:variable>
                                <xsl:if test="count($appInfo)!=0">
                                  <appInfo>
                                    <xsl:copy-of select="$appInfo"/>
                                  </appInfo>
                                </xsl:if>
                              </xsl:variable>
                              <xsl:if test="count($annotations)!=0">
                                <annotations>
                                  <xsl:copy-of select="$annotations"/>
                                </annotations>
                              </xsl:if>
                              <xsl:if test="parent::concept">
                                <conceptRelationship relationshipName="Specializes">
                                  <targetConcept code="{parent::concept/code/@value}"/>
                                </conceptRelationship>
                              </xsl:if>
                              <xsl:for-each select="property[code/@value='subsumedBy']/valueCode">
                                <conceptRelationship relationshipName="Specializes">
                                  <targetConcept code="{@value}"/>
                                </conceptRelationship>
                              </xsl:for-each>
                              <xsl:for-each select="property[valueCoding]">
                                <conceptRelationship relationshipName="{code/@value}">
                                  <xsl:for-each select="valueCoding">
                                    <targetConcept code="{code/@value}">
                                      <xsl:if test="system/@value!=ancestor::CodeSystem/url/@value">
                                        <xsl:attribute name="codeSystem" select="fn:urlToOID(system/@value)"/>
                                      </xsl:if>
                                    </targetConcept>
                                  </xsl:for-each>
                                </conceptRelationship>
                              </xsl:for-each>
              <!--                <xsl:for-each select="property[not(valueCoding or code/@value='notSelectable' or (code/@value='status' and valueCode/@value!='retired') or (valueCode/@value='retired' and preceding-sibling::property[valueCode/@value='retired']))]">-->
                              <xsl:for-each select="property[not(valueCoding or code/@value=('notSelectable', 'subsumedBy', 'synonymCode') or (code/@value='status' and string-length(valueCode/@value)!=1))]">
                                <conceptProperty name="{code/@value}" value="{*[starts-with(local-name(.), 'value')]/@value}"/>
                              </xsl:for-each>
                              <xsl:for-each select="display">
                                <printName language="en" preferredForLanguage="true" text="{@value}"/>
                              </xsl:for-each>
                              <xsl:for-each select="designation[use[system/@value='http://snomed.info/sct' and code/@value=('synonym','900000000000013009')]]">
                                <printName language="en" preferredForLanguage="false" text="{value/@value}"/>
                              </xsl:for-each>
                              <code code="{code/@value}">
                                <xsl:variable name="status">
                                  <xsl:choose>
                                    <xsl:when test="property[code/@value='status']">
                                      <xsl:choose>
                                        <xsl:when test="property[code/@value='status']/valueCode/@value='A'">active</xsl:when>
                                        <xsl:when test="property[code/@value='status']/valueCode/@value=('D', 'R', 'B', 'N')">retired</xsl:when>
                                        <xsl:otherwise>
                                          <xsl:value-of select="property[code/@value='status']/valueCode/@value"/>
                                        </xsl:otherwise>
                                      </xsl:choose>
                                    </xsl:when>
                                    <xsl:otherwise>active</xsl:otherwise>
                                  </xsl:choose>
                                </xsl:variable>
                                <xsl:attribute name="status" select="$status"/>
                              </code>
                              <xsl:for-each select="property[code/@value='synonymCode']">
                                <code code="{valueCode/@value}" status="active"/>
                                <!-- TODO: set alternate code status -->
                              </xsl:for-each>
                            </concept>
                          </xsl:for-each>
                        </f:concepts>
                      </xsl:variable>
                      <xsl:for-each select="$concepts/*:concept">
                        <xsl:if test="not(preceding-sibling::*:concept[*:conceptProperty[@name='internalId' and @value=current()/*:conceptProperty[@name='internalId']/@value]])">
                          <concept>
                            <xsl:apply-templates mode="mif" select="@*|node()"/>
                          </concept>
                        </xsl:if>
                      </xsl:for-each>
                    </releasedVersion>
                  </codeSystem>
                </xsl:for-each>
              </xsl:when>
              <xsl:otherwise>
                <xsl:for-each select="/Bundle/entry/resource/NamingSystem[uniqueId[type/@value='uri' and preferred/@value='true' and value/@value=$url]]">
  
                  <codeSystem name="{substring-after(id/@value, 'v3-')}" title="{title/@value}" codeSystemId="{uniqueId[type/@value='oid' and preferred/@value='true']/value/@value}">
                    <annotations>
                      <documentation>
                        <description>
                          <text>
                            <xsl:copy-of select="fn:markdownToHTML(description/@value, true())"/>
                          </text>
                        </description>
                      </documentation>
                    </annotations>
                    <releasedVersion releaseDate="{substring(date/@value, 1, 10)}" hl7MaintainedIndicator="false" completeCodesIndicator="false" hl7ApprovedIndicator="false">
                      <supportedLanguage>en</supportedLanguage>
                    </releasedVersion>
                  </codeSystem>
                </xsl:for-each>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:message select="concat('Duplicate item in v3-Manifest: ', item/reference/@value)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>
  <xsl:template mode="mif" match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates mode="mif" select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template mode="mif" priority="5" match="*">
    <xsl:element name="{local-name(.)}" xmlns="urn:hl7-org:v3/mif2">
      <xsl:apply-templates mode="mif" select="@*|node()"/>
    </xsl:element>
  </xsl:template>
  <xsl:template name="doHeaderElements">
    <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/resource-history']">
      <historyItem dateTime="{extension[@url='date']/valueDateTime/@value}">
        <xsl:for-each select="extension[@url='id']/valueString">
          <xsl:attribute name="id" select="@value"/>
        </xsl:for-each>
        <xsl:for-each select="extension[@url=('substantive','substantiative')]/valueBoolean">
          <xsl:attribute name="isSubstantiveChange" select="@value"/>
        </xsl:for-each>
        <xsl:for-each select="extension[@url='backwardCompatible']/valueBoolean">
          <xsl:attribute name="isBackwardCompatibleChange" select="@value"/>
        </xsl:for-each>
        <xsl:for-each select="extension[@url='author']/valueString">
          <xsl:attribute name="responsiblePersonName" select="@value"/>
        </xsl:for-each>
        <xsl:for-each select="extension[@url=('notes', 'annotation')]/valueString">
          <description>
            <xsl:value-of select="@value"/>
          </description>
        </xsl:for-each>
      </historyItem>
    </xsl:for-each>
    <xsl:variable name="headerElements" as="element()*">
      <xsl:variable name="legaleseElements" as="element()*">
        <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/codesystem-legalese']/valueString/@value">
          <notation>
            <xsl:value-of select="."/>
          </notation>
        </xsl:for-each>
        <xsl:for-each select="copyright/@value">
          <licenseTerms>
            <xsl:value-of select="."/>
          </licenseTerms>
        </xsl:for-each>
        <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/codesystem-versioning']/valueString/@value">
          <versioningPolicy>
            <xsl:value-of select="."/>
          </versioningPolicy>
        </xsl:for-each>
      </xsl:variable>
      <xsl:if test="count($legaleseElements)!=0">
        <legalese>
          <xsl:copy-of select="$legaleseElements"/>
        </legalese>
      </xsl:if>
      <xsl:for-each select="publisher/@value">
        <responsibleGroup organizationName="{.}"/>
      </xsl:for-each>
      <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/codesystem-contributor']">
        <contributor>
          <xsl:for-each select="extension[@url='role']/valueString/@value">
            <role>
              <xsl:value-of select="."/>
            </role>
          </xsl:for-each>
          <xsl:for-each select="extension[@url='name']/valueString/@value">
            <name name="{.}"/>
          </xsl:for-each>
          <xsl:for-each select="extension[@url='notes']/valueString/@value">
            <notes>
              <xsl:value-of select="."/>
            </notes>
          </xsl:for-each>
        </contributor>
      </xsl:for-each>
    </xsl:variable>
    <xsl:if test="count($headerElements)!=0">
      <header>
        <xsl:copy-of select="$headerElements"/>
      </header>
    </xsl:if>
    <xsl:variable name="annotations" as="element()*">
      <xsl:for-each select="description">
        <documentation>
          <description>
            <text>
              <xsl:copy-of select="fn:markdownToHTML(@value, true())"/>
            </text>
          </description>
        </documentation>
      </xsl:for-each>
      <xsl:variable name="appInfo" as="element()*">
        <xsl:call-template name="doDeprecation"/>
      </xsl:variable>
      <xsl:if test="count($appInfo)!=0">
        <appInfo>
          <xsl:copy-of select="$appInfo"/>
        </appInfo>
      </xsl:if>
    </xsl:variable>
    <xsl:if test="count($annotations)!=0">
      <annotations>
        <xsl:copy-of select="$annotations"/>
      </annotations>
    </xsl:if>
  </xsl:template>
  <xsl:template name="doDeprecation">
    <xsl:if test="extension[@url=('http://hl7.org/fhir/StructureDefinition/resource-deprecationInfo','http://hl7.org/fhir/StructureDefinition/resource-versionDeprecated')]/valueString">
      <deprecationInfo>
        <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/resource-versionDeprecated']/valueString">
          <xsl:attribute name="deprecationEffectiveVersion" select="@value"/>
        </xsl:for-each>
        <text>
          <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/resource-deprecationInfo']/valueString">
            <xsl:copy-of select="fn:markdownToHTML(@value, true())"/>
          </xsl:for-each>
          <xsl:if test="not(extension[@url='http://hl7.org/fhir/StructureDefinition/resource-deprecationInfo']/valueString)">This element was deprecated as of the release indicated.</xsl:if>
        </text>
      </deprecationInfo>
    </xsl:if>
  </xsl:template>
  <xsl:template name="doValueSets">
    <xsl:for-each select="key('resourceByUrl', 'http://terminology.hl7.org/List/v3-Manifest')/resource/List/entry/item/reference">
      <xsl:variable name="url" select="if (contains(@value, '|')) then substring-before(@value, '|') else @value"/>
      <xsl:for-each select="key('resourceByUrl', $url)/resource/ValueSet">
        <valueSet name="{title/@value}" id="{fn:getOID(.)}">
          <xsl:if test="immutable/@value='true'">
            <xsl:attribute name="isImmutable">true</xsl:attribute>
          </xsl:if>
          <xsl:call-template name="doHeaderElements"/>
          <version versionDate="{date/@value}">
            <xsl:variable name="codeSystems" as="xs:string+">
              <xsl:apply-templates mode="getSystems" select="compose"/>
            </xsl:variable>
            <xsl:for-each select="distinct-values($codeSystems)">
              <supportedCodeSystem>
                <xsl:value-of select="fn:urlToOID(.)"/>
              </supportedCodeSystem>
            </xsl:for-each>
            <xsl:for-each select="language">
              <supportedLanguage>
                <xsl:value-of select="@value"/>
              </supportedLanguage>
            </xsl:for-each>
            <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/valueset-hl7-assocConceptProp']">
              <associatedConceptProperty name="{extension[@url='name']/valueString/@value}" value="{extension[@url='value']/valueString/@value}"/>
            </xsl:for-each>
            <xsl:for-each select="compose">
              <xsl:choose>
                <xsl:when test="count(include)=1 and not(exclude)">
                  <content>
                    <xsl:for-each select="include">
                      <xsl:call-template name="doContent"/>
                    </xsl:for-each>
                  </content>
                </xsl:when>
                <!-- Todo - handle excludeHeadCode -->
                <xsl:otherwise>
                  <content>
                    <combinedContent>
                      <xsl:for-each select="include">
                        <unionWithContent>
                          <xsl:call-template name="doContent"/>
                        </unionWithContent>
                      </xsl:for-each>
                      <xsl:for-each select="exclude">
                        <excludeContent>
                          <xsl:call-template name="doContent"/>
                        </excludeContent>
                      </xsl:for-each>
                    </combinedContent>
                  </content>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
          </version>
        </valueSet>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>
  <xsl:template mode="getSystems" match="compose">
    <xsl:for-each select="include/system">
      <xsl:value-of select="@value"/>
    </xsl:for-each>
    <xsl:for-each select="include/valueSet">
      <xsl:apply-templates mode="getSystems" select="ancestor::Bundle/entry[fullUrl/@value=current()/@value]/resource/ValueSet/compose"/>
    </xsl:for-each>
  </xsl:template>
  <xsl:template name="doBindingRealms">
    <xsl:for-each select="key('resourceByUrl', 'http://terminology.hl7.org/CodeSystem/v3-hl7Realm')/resource/CodeSystem//concept[code/@value=('US', 'UV') or property[code/@value='OwningAffiliate']/valueCoding/code/@value='UV']">
      <xsl:variable name="owner" select="property[code/@value='OwningAffiliate']/valueCoding/code/@value"/>
      <bindingRealm name="{code/@value}" owningAffiliate="{if ($owner) then $owner else code/@value}">
        <xsl:for-each select="definition">
          <description>
            <xsl:value-of select="normalize-space(@value)"/>
          </description>
        </xsl:for-each>
      </bindingRealm>
    </xsl:for-each>
  </xsl:template>
	<xsl:template name="doContextBindings">
    <xsl:for-each select="key('resourceByUrl', $conceptDomainSystemUrl)/resource/CodeSystem//concept[property[code/@value='source']/valueCode/@value='v3']/property[starts-with(code/@value, 'contextBinding')]">
      <xsl:sort select="parent::code/@value"/>
      <xsl:variable name="realm" select="substring-after(substring-before(code/@value, '-valueSet'), 'contextBinding')"/>
      <xsl:variable name="strength" select="extension[@url='http://hl7.org/fhir/StructureDefinition/resource-concept-binding-strength']/valueCode/@value"/>
      <xsl:variable name="valueSet" select="valueString/@value"/>
      <xsl:variable name="domain" select="parent::concept/code/@value"/>
      <contextBinding bindingRealmName="{$realm}" valueSet="{$valueSet}" codingStrength="{$strength}" conceptDomain="{$domain}" effectiveDate="{$bindingDate}"/>
    </xsl:for-each>
	</xsl:template>
  <xsl:template name="doContent">
    <xsl:choose>
      <xsl:when test="exists(system) and normalize-space(fn:urlToOID(system/@value))=''">
        <xsl:message terminate="yes" select="concat('Unable to find OID for system: ', system/@value)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="system">
          <xsl:attribute name="codeSystem" select="fn:urlToOID(@value)"/>
        </xsl:for-each>
        <xsl:choose>
          <xsl:when test="valueSet">
            <valueSetRef id="{fn:urlToOID(valueSet/@value)}"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:for-each select="concept">
              <codeBasedContent code="{code/@value}"/>
            </xsl:for-each>
            <xsl:for-each select="filter">
              <codeBasedContent code="{value/@value}">
                <includeRelatedCodes relationshipName="Generalizes" relationshipTraversal="TransitiveClosure"/>
              </codeBasedContent>
            </xsl:for-each>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template mode="url" match="CodeSystem|ValueSet">
    <url xmlns="http://hl7.org/fhir" value="{url/@value}" oid="{fn:getOID(.)}"/>
  </xsl:template>
  <xsl:template mode="url" match="NamingSystem">
    <url xmlns="http://hl7.org/fhir" value="{uniqueId[preferred/@value='true' and type/@value='uri']/value/@value}" oid="{uniqueId[preferred/@value='true' and type/@value='oid']/value/@value}"/>
  </xsl:template>
	<xsl:function name="fn:urlToOID">
    <xsl:param name="url"/>
    <xsl:choose>
      <xsl:when test="starts-with($url, 'urn:oid:')">
        <xsl:value-of select="substring-after($url, 'urn:oid:')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$urls[@value=$url][1]/@oid"/>
      </xsl:otherwise>
    </xsl:choose>
	</xsl:function>
	<xsl:function name="fn:getOID">
    <xsl:param name="element" as="element()"/>
    <xsl:value-of select="substring-after($element/identifier[system/@value='urn:ietf:rfc:3986']/value/@value, 'urn:oid:')"/>
	</xsl:function>
	<xsl:function name="fn:markdownToHTML">
    <xsl:param name="markdown" as="xs:string"/>
    <xsl:param name="block" as="xs:boolean"/>
    <xsl:variable name="fixedMarkdown" select="replace(replace(replace(translate($markdown, '&#xd;', ''), '^\s+|\s+$', ''), '&amp;', '&amp;amp;'), '&lt;', '&amp;lt;')"/>
    <xsl:copy-of select="fn:markdownToHTMLInternal($fixedMarkdown, $block)"/>
	</xsl:function>
	<xsl:function name="fn:markdownToHTMLInternal">
    <xsl:param name="fixedMarkdown" as="xs:string"/>
    <xsl:param name="block" as="xs:boolean"/>
    <!--sub br -->    
    <xsl:choose>
      <xsl:when test="$block and contains($fixedMarkdown, '&#xa;&#xa;')">
        <!-- paragraphs -->
        <xsl:variable name="paragraphs" as="xs:string+" select="tokenize($fixedMarkdown, '\n\n')"/>
        <xsl:for-each select="$paragraphs">
          <xsl:choose>
            <xsl:when test="starts-with(., '* ') or starts-with(., '1. ')">
              <xsl:copy-of select="fn:markdownToHTMLInternal(., $block)"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:copy-of select="fn:markdownToHTMLInternal(., $block)"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:when>
      <xsl:when test="$block and starts-with($fixedMarkdown, '* ')">
        <!-- <ul/> -->
        <xsl:variable name="listItems" as="xs:string+" select="tokenize(substring-after($fixedMarkdown, '* '), '\n\* ')"/>
        <ul>
          <xsl:for-each select="$listItems">
            <li>
              <xsl:copy-of select="fn:markdownToHTMLInternal(., $block)"/>
            </li>
          </xsl:for-each>
        </ul>
      </xsl:when>
      <xsl:when test="$block and starts-with($fixedMarkdown, '1. ')">
        <!-- <ol/> -->
        <xsl:variable name="listItems" as="xs:string+" select="tokenize(substring-after($fixedMarkdown, '1. '), '\n[0-9]+\. ')"/>
        <ul>
          <xsl:for-each select="$listItems">
            <li>
              <xsl:copy-of select="fn:markdownToHTMLInternal(., $block)"/>
            </li>
          </xsl:for-each>
        </ul>
      </xsl:when>
      <xsl:when test="matches($fixedMarkdown, '^(.*\s)?\*\*\*([^\s\*].*[^\s\*])\*\*\*(\s.*)?$')">
        <!-- <b><i/></b> -->
        <xsl:copy-of select="fn:markdownToHTMLInternal(replace($fixedMarkdown, '^(.*\s)?\*\*\*([^\s\*].*[^\s\*])\*\*\*(\s.*)?$', '$1&lt;b&gt;&lt;i&gt;$2&lt;/i&gt;&lt;/b&gt;$3'), $block)"/>
      </xsl:when>
      <xsl:when test="matches($fixedMarkdown, '^(.*\s)?\*\*([^\s\*].*[^\s\*])\*\*(\s.*)?$')">
        <!-- <b/> -->
        <xsl:copy-of select="fn:markdownToHTMLInternal(replace($fixedMarkdown, '^(.*\s)?\*\*([^\s\*].*[^\s\*])\*\*(\s.*)?$', '$1&lt;b&gt;$2&lt;/b&gt;$3'), $block)"/>
      </xsl:when>
      <xsl:when test="matches(concat(' ', $fixedMarkdown, ' '), '^(.*\s)?\*([^\s\*].*[^\s\*])\*(\s.*)?$')">
        <!-- <i/> -->
        <xsl:copy-of select="fn:markdownToHTMLInternal(replace($fixedMarkdown, '^(.*\s)?\*([^\s\*].*[^\s\*])\*(\s.*)?$', '$1&lt;i&gt;$2&lt;/i&gt;$3'), $block)"/>
      </xsl:when>
      <xsl:when test="matches($fixedMarkdown, '^(.*\s)?___([^\s\*].*[^\s\*])___(\s.*)?$')">
        <!-- <b><i/></b> -->
        <xsl:copy-of select="fn:markdownToHTMLInternal(replace($fixedMarkdown, '^(.*\s)?___([^\s\*].*[^\s\*])___(\s.*)?$', '$1&lt;b&gt;&lt;i&gt;$2&lt;/i&gt;&lt;/b&gt;$3'), $block)"/>
      </xsl:when>
      <xsl:when test="matches($fixedMarkdown, '^(.*\s)?__([^\s\*].*[^\s\*])__(\s.*)?$')">
        <!-- <b/> -->
        <xsl:copy-of select="fn:markdownToHTMLInternal(replace($fixedMarkdown, '^(.*\s)?__([^\s\*].*[^\s\*])__(\s.*)?$', '$1&lt;b&gt;$2&lt;/b&gt;$3'), $block)"/>
      </xsl:when>
      <xsl:when test="matches($fixedMarkdown, '^(.*\s)?_([^\s\*].*[^\s\*])_(\s.*)?$')">
        <!-- <i/> -->
        <xsl:copy-of select="fn:markdownToHTMLInternal(replace($fixedMarkdown, '^(.*\s)?_([^\s\*].*[^\s\*])_(\s.*)?$', '$1&lt;i&gt;$2&lt;/i&gt;$3'), $block)"/>
      </xsl:when>
      <xsl:when test="matches($fixedMarkdown, '(.*)?\[(.+)\]\((.+)\)(.*)?')">
        <!-- <a href=""></a> -->
        <xsl:copy-of select="fn:markdownToHTMLInternal(replace($fixedMarkdown, '(.*)?\[(.+)\]\((.+)\)(.*)?', '$1&lt;a href=&quot;$2&quot;&gt;$3&lt;/a&gt;$4'), $block)"/>
      </xsl:when>
      <xsl:when test="contains($fixedMarkdown, '\&#xa;')">
        <xsl:copy-of select="fn:markdownToHTMLInternal(concat(substring-before($fixedMarkdown, '\*#a;'), '&lt;br/&gt;&#xa;', substring-after($fixedMarkdown, '\*#a;')), $block)"/>
      </xsl:when>
      <xsl:otherwise>
<!--<xsl:message select="$fixedMarkdown"/>-->
        <xsl:variable name="nodes" as="node()*" select="saxon:parse(concat('&lt;content&gt;', $fixedMarkdown, '&lt;/content&gt;'))/*/node()"/>
        <xsl:choose>
          <xsl:when test="$block and not($nodes[self::*])">
            <p>
              <xsl:apply-templates mode="fixNS" select="$nodes"/>
            </p>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates mode="fixNS" select="$nodes"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
	</xsl:function>
	<xsl:template mode="fixNS" match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates mode="fixNS" select="@*|node()"/>
    </xsl:copy>
	</xsl:template>
	<xsl:template mode="fixNS" priority="5" match="text()">
    <xsl:value-of select="normalize-space(.)"/>
	</xsl:template>
	<xsl:template mode="fixNS" priority="5" match="*">
    <xsl:element name="{local-name(.)}" xmlns="urn:hl7-org:v3/mif2">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
	</xsl:template>
</xsl:stylesheet>
