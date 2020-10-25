<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://hl7.org" xmlns:saxon="http://saxon.sf.net/" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:f="http://hl7.org/fhir" xmlns="urn:hl7-org:v3/mif2" xpath-default-namespace="http://hl7.org/fhir" exclude-result-prefixes="fn saxon xs f">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:param name="version" select="'2020-01-01'"/>
	<xsl:variable name="versionDate" select="substring-after($version, '-')"/>
	<xsl:key name="resourceByUrl" match="/Bundle/entry" use="fullUrl/@value"/>
	<xsl:key name="resourceByRef" match="/Bundle/entry" use="concat(local-name(resource/*),'/', resource/*/id/@value)"/>
	<xsl:key name="provenanceByUrl" match="/Bundle/entry/resource/Bundle/entry/resource/Provenance" use="target/reference/@value"/>
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
      <xsl:apply-templates mode="doDomain" select="."/>
      <xsl:for-each select="property[code/@value='synonymCode']">
<xsl:if test="normalize-space(valueCode/@value)=''">
<xsl:message terminate="yes" select="."/>
</xsl:if>
        <xsl:apply-templates mode="doDomain" select="parent::*">
          <xsl:with-param name="domain" select="valueCode/@value"/>
        </xsl:apply-templates>
      </xsl:for-each>
    </xsl:for-each>
	</xsl:template>
	<xsl:template mode="doDomain" match="concept">
    <xsl:param name="domain" select="code/@value"/>
<xsl:if test="normalize-space($domain)=''">
<xsl:message terminate="yes" select="."/>
</xsl:if>
    <conceptDomain name="{$domain}">
      <!-- No support for this, as we can only track history across all concept domains, not at the level of each domain
        <historyItem dateTime="2012-03-15" id="00000000-0000-0000-0000-000000000000"
                     isSubstantiveChange="true"
                     isBackwardCompatibleChange="false">
           <description>retiredAsOfRelease: 1130-20120315 Indicates that the Concet Domain was retired from any further use as of the release indicated.</description>
        </historyItem>
      -->
      <annotations>
        <documentation>
          <definition>
            <text>
              <xsl:copy-of select="fn:markdownToHTML(definition/@value, true())"/>
            </text>
          </definition>
          <xsl:for-each select="property[code/@value='HL7usageNotes']">
            <usageNotes>
              <text>
                <xsl:value-of select="valueString/@value"/>
              </text>
            </usageNotes>
          </xsl:for-each>
        </documentation>
        <xsl:variable name="appInfo" as="element()*">
          <xsl:for-each select="property[code/@value='openIssue']">
            <openIssue>
              <text>
                <xsl:copy-of select="fn:markdownToHTML(valueString/@value, true())"/>
              </text>
            </openIssue>
          </xsl:for-each>
          <xsl:if test="property[code/@value='DeprecationInfo']">
            <xsl:variable name="deprecationInfo" select="property[code/@value='DeprecationInfo']/valueString/@value"/>
            <xsl:variable name="text">
              <xsl:choose>
                <xsl:when test="contains($deprecationInfo, 'coremifText=')">
                  <xsl:value-of select="substring-after($deprecationInfo, 'coremifText=')"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$deprecationInfo"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <xsl:variable name="version">
              <xsl:choose>
                <xsl:when test="contains($deprecationInfo, 'deprecationEffectiveVersion=')">
                  <xsl:value-of select="substring-before(substring-after($deprecationInfo, 'deprecationEffectiveVersion='), ' ')"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="f:occurredPeriod/f:end/@value"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <deprecationInfo deprecationEffectiveVersion="{$version}">
              <text>
                <xsl:copy-of select="fn:markdownToHTML($text, true())"/>
              </text>
            </deprecationInfo>
          </xsl:if>
        </xsl:variable>
        <xsl:if test="count($appInfo)!=0">
          <appInfo>
            <xsl:copy-of select="$appInfo"/>
          </appInfo>
        </xsl:if>
      </annotations>
      <xsl:for-each select="parent::concept">
        <specializesDomain name="{code/@value}"/>
      </xsl:for-each>
      <xsl:for-each select="property[code/@value='subsumedBy']">
        <specializesDomain name="{valueCode/@value}"/>
      </xsl:for-each>
      <xsl:for-each select="property[not(code/@value=('source', 'deprecationInfo', 'openIssue', 'HL7usageNotes', 'subsumedBy', 'synonymCode', 'deprecated', 'status', 'DeprecationInfo') or starts-with(code/@value, 'contextBinding'))]">
        <property name="{code/@value}">
          <xsl:for-each select="valueCode">
            <xsl:attribute name="value" select="@value"/>
          </xsl:for-each>
          <xsl:for-each select="valueCoding">
            <xsl:attribute name="value" select="concat(substring-after(system/@value, 'http://terminology.hl7.org/CodeSystem/v3-'), '.', code/@value)"/>
          </xsl:for-each>
          <xsl:for-each select="valueString">
            <xsl:attribute name="value" select="@value"/>
          </xsl:for-each>
        </property>
      </xsl:for-each>
    </conceptDomain>
	</xsl:template>
  <xsl:template name="doCodeSystems">
    <xsl:for-each select="key('resourceByUrl', 'http://terminology.hl7.org/List/v3-Publishing')/resource/List/entry">
      <!-- Todo: yank this 'if' and collapse for-each loops once source is fixed -->
      <xsl:choose>
        <xsl:when test="not(preceding-sibling::entry[item/reference/@value=current()/item/reference/@value])">
          <xsl:for-each select="item/reference">
            <xsl:variable name="ref" select="@value"/>
            <xsl:choose>
              <xsl:when test="parent::item/type/@value='CodeSystem'">
                <xsl:for-each select="key('resourceByRef', $ref)/resource/CodeSystem">
                  <codeSystem name="{upper-case(substring(name/@value, 1,1))}{substring(name/@value,2,300)}" title="{title/@value}" codeSystemId="{fn:getOID(.)}">
                    <xsl:call-template name="doHeaderElements"/>
                    <releasedVersion releaseDate="{substring(date/@value,1,10)}" completeCodesIndicator="{if (content/@value='complete') then 'true' else 'false'}">
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
                      <xsl:for-each select="property[extension[@url='http://terminology.hl7.org/StructureDefinition/ext-mif-relationship-relationshipKind']]">
                        <xsl:variable name="name">
                          <xsl:choose>
                            <xsl:when test="starts-with(code/@value, 'rim-')">
                              <xsl:value-of select="substring-after(code/@value, 'rim-')"/>
                            </xsl:when>
                            <xsl:otherwise>
                              <xsl:value-of select="code/@value"/>
                            </xsl:otherwise>
                          </xsl:choose>
                        </xsl:variable>
                        <supportedConceptRelationship relationshipKind="{extension[@url='http://terminology.hl7.org/StructureDefinition/ext-mif-relationship-relationshipKind']/valueCode/@value}" name="{$name}">
                          <xsl:for-each select="extension[@url='http://terminology.hl7.org/StructureDefinition/ext-mif-relationship-inverseName']/valueString/@value">
                            <xsl:attribute name="inverseName" select="."/>
                          </xsl:for-each>
                          <xsl:for-each select="extension[@url='http://terminology.hl7.org/StructureDefinition/ext-mif-relationship-isNavigable']/valueBoolean/@value">
                            <xsl:attribute name="isNavigable" select="."/>
                          </xsl:for-each>
                          <xsl:for-each select="extension[@url='http://terminology.hl7.org/StructureDefinition/ext-mif-relationship-reflexivity']/valueCode/@value">
                            <xsl:attribute name="reflexivity" select="."/>
                          </xsl:for-each>
                          <xsl:for-each select="extension[@url='http://terminology.hl7.org/StructureDefinition/ext-mif-relationship-symmetry']/valueCode/@value">
                            <xsl:attribute name="symmetry" select="."/>
                          </xsl:for-each>
                          <xsl:for-each select="extension[@url='http://terminology.hl7.org/StructureDefinition/ext-mif-relationship-transitivity']/valueCode/@value">
                            <xsl:attribute name="transitivity" select="."/>
                          </xsl:for-each>
                          <xsl:for-each select="description">
                            <description>
                              <xsl:copy-of select="fn:markdownToHTML(@value, false())"/>
                            </description>
                          </xsl:for-each>
                        </supportedConceptRelationship>
                      </xsl:for-each>
                      <xsl:for-each select="property[not(extension[@url='http://terminology.hl7.org/StructureDefinition/ext-mif-relationship-relationshipKind'] or code/@value=('isMandatory','notSelectable','HL7usageNotes','subsumedBy','deprecationDate'))]">
                      <!-- NOTE: Need to handle deprecationDate stuff -->
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
                        <supportedConceptProperty propertyName="{code/@value}" type="{$type}" isMandatoryIndicator="false">
                          <!-- Support for this was dropped
                          <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/codesystem-mif-extended-properties']/extension[not(@url='isMandatory')]">
                            <xsl:attribute name="{@url}" select="*[starts-with(local-name(.), 'value')]/@value"/>
                          </xsl:for-each>-->
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
                                <xsl:if test="definition|property[code/@value='HL7usageNotes']">
                                  <documentation>
                                    <xsl:for-each select="definition">
                                      <definition>
                                        <text>
                                          <xsl:copy-of select="fn:markdownToHTML(@value, true())"/>
                                        </text>
                                      </definition>
                                    </xsl:for-each>
                                    <xsl:for-each select="property[code/@value='HL7usageNotes']">
                                      <usageNotes>
                                        <text>
                                          <xsl:value-of select="valueString/@value"/>
                                        </text>
                                      </usageNotes>
                                    </xsl:for-each>
                                  </documentation>
                                </xsl:if>
                                <xsl:variable name="appInfo" as="element()*">
                                  <xsl:call-template name="doDeprecation"/>
                                  <!-- Todo: drop the codesystem-openIssue extension -->
                                  <!-- Dropped support for this
                                  <xsl:for-each select="extension[@url=('http://hl7.org/fhir/StructureDefinition/resource-openIssue','http://hl7.org/fhir/StructureDefinition/codesystem-openIssue')]/valueString">
                                    <openIssue>
                                      <text>
                                        <xsl:copy-of select="fn:markdownToHTML(@value, true())"/>
                                      </text>
                                    </openIssue>
                                  </xsl:for-each>
-->
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
                                <xsl:variable name="name">
                                  <xsl:choose>
                                    <xsl:when test="starts-with(code/@value, 'rim-')">
                                      <xsl:value-of select="substring-after(code/@value, 'rim-')"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                      <xsl:value-of select="code/@value"/>
                                    </xsl:otherwise>
                                  </xsl:choose>
                                </xsl:variable>
                                <conceptRelationship relationshipName="{$name}">
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
                              <xsl:for-each select="property[not(valueCoding or code/@value=('notSelectable', 'subsumedBy', 'synonymCode', 'HL7usageNotes') or (code/@value='status' and string-length(valueCode/@value)!=1))]">
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
                                        <xsl:when test="property[code/@value='status']/valueCode/@value=('deprecated', 'inactive')">active</xsl:when>
                                        <!-- Todo - figure out what to do about 'inactive' -->
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
                <xsl:for-each select="key('resourceByRef', $ref)/resource/NamingSystem">
<!--                <xsl:for-each select="/Bundle/entry/resource/NamingSystem[uniqueId[type/@value='uri' and preferred/@value='true' and value/@value=$url]]">-->
                  <xsl:variable name="title">
                    <xsl:choose>
                      <xsl:when test="title">
                        <xsl:value-of select="title/@value"/>
                      </xsl:when>
                      <xsl:when test="extension[@url='http://terminology.hl7.org/StructureDefinition/ext-namingsystem-title']">
                        <xsl:value-of select="extension[@url='http://terminology.hl7.org/StructureDefinition/ext-namingsystem-title']/valueString/@value"/>
                      </xsl:when>
                      <xsl:otherwise>foo</xsl:otherwise>
                    </xsl:choose>
                  </xsl:variable>
                  <codeSystem name="{substring-after(id/@value, 'v3-')}" title="{$title}" codeSystemId="{uniqueId[type/@value='oid' and preferred/@value='true']/value/@value}">
                    <xsl:if test="description/@value">
                      <annotations>
                        <documentation>
                          <description>
                            <text>
                              <xsl:copy-of select="fn:markdownToHTML(description/@value, true())"/>
                            </text>
                          </description>
                        </documentation>
                      </annotations>
                    </xsl:if>
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
          <xsl:message select="concat('Duplicate item in v3-Publishing: ', item/reference/@value)"/>
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
    <xsl:for-each select="key('provenanceByUrl', f:url/@value)">
      <xsl:if test="not(f:activity/f:coding[f:system/@value='http://terminology.hl7.org/CodeSystem/v3-DataOperation']/f:code/@value='DEPRECATE')">
        <historyItem dateTime="{f:occurredPeriod/f:end/@value}" id="{f:id/@value}">
          <!-- Not supported
          <xsl:for-each select="extension[@url=('substantive','substantiative')]/valueBoolean">
            <xsl:attribute name="isSubstantiveChange" select="@value"/>
          </xsl:for-each>
          <xsl:for-each select="extension[@url='backwardCompatible']/valueBoolean">
            <xsl:attribute name="isBackwardCompatibleChange" select="@value"/>
          </xsl:for-each>
          -->
          <xsl:for-each select="f:agent[f:type[f:coding[f:system/@value='http://terminology.hl7.org/CodeSystem/provenance-participant-type' and f:code/@value='author']]][1]/f:who/f:display">
            <xsl:attribute name="responsiblePersonName" select="@value"/>
          </xsl:for-each>
          <description>
            <xsl:value-of select="concat(f:activity/f:coding[f:system/@value='http://terminology.hl7.org/CodeSystem/v3-DataOperation']/f:code/@value, ': ')"/>
            <xsl:value-of select="fn:markdownToHTML(reason/f:text/@value, true())"/>
          </description>
        </historyItem>
      </xsl:if>
    </xsl:for-each>
    <xsl:variable name="headerElements" as="element()*">
      <xsl:variable name="legaleseElements" as="element()*">
        <!-- Not supported
        <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/codesystem-legalese']/valueString/@value">
          <notation>
            <xsl:value-of select="."/>
          </notation>-
        </xsl:for-each>-->
        <xsl:for-each select="copyright/@value">
          <licenseTerms>
            <xsl:value-of select="."/>
          </licenseTerms>
        </xsl:for-each>
        <!-- Not supported
        <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/codesystem-versioning']/valueString/@value">
          <versioningPolicy>
            <xsl:value-of select="."/>
          </versioningPolicy>
        </xsl:for-each>-->
      </xsl:variable>
      <xsl:if test="count($legaleseElements)!=0">
        <legalese>
          <xsl:copy-of select="$legaleseElements"/>
        </legalese>
      </xsl:if>
      <xsl:for-each select="publisher/@value">
        <responsibleGroup organizationName="{substring(., 1, 80)}"/>
      </xsl:for-each>
      <!-- Not supported
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
      -->
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
    <xsl:for-each select="key('provenanceByUrl', f:url/@value)">
      <xsl:if test="f:activity/f:coding[f:system/@value='http://terminology.hl7.org/CodeSystem/v3-DataOperation']/f:code/@value='DEPRECATE'">
        <deprecationInfo deprecationEffectiveVersion="{f:occurredPeriod/f:end/@value}">
          <text>
            <xsl:value-of select="fn:markdownToHTML(reason/f:text/@value, true())"/>
          </text>
        </deprecationInfo>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
  <xsl:template name="doValueSets">
    <xsl:for-each select="key('resourceByUrl', 'http://terminology.hl7.org/List/v3-Publishing')/resource/List/entry/item/reference">
      <xsl:variable name="url" select="if (contains(@value, '|')) then substring-before(@value, '|') else @value"/>
      <xsl:for-each select="key('resourceByRef', $url)/resource/ValueSet">
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
            <xsl:for-each select="extension[@url='http://terminology.hl7.org/StructureDefinition/ext-mif-assocConceptProp']">
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
    <xsl:for-each select="key('resourceByUrl', $conceptDomainSystemUrl)/resource/CodeSystem//concept[property[code/@value='source']/valueCode/@value='v3']/property[starts-with(code/@value, 'contextBinding') and ends-with(code/@value, '-valueSet')]">
      <xsl:sort select="parent::code/@value"/>
      <xsl:variable name="realm" select="substring-after(substring-before(code/@value, '-valueSet'), 'contextBinding')"/>
      <xsl:variable name="realm" select="$realm"/>
      <xsl:variable name="strength" select="parent::*/property[code/@value=concat('contextBinding', $realm, '-strength')]/valueCode/@value"/>
      <xsl:variable name="valueSet" select="valueString/@value"/>
      <xsl:variable name="domain" select="parent::concept/code/@value"/>
      <xsl:variable name="bindingDate" select="parent::*/property[code/@value=concat('contextBinding', $realm, '-effectiveDate')]/valueDateTime/@value"/>
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
    <xsl:for-each select="uniqueId[type/@value='uri']">
      <url xmlns="http://hl7.org/fhir" value="{value/@value}" oid="{parent::NamingSystem/uniqueId[preferred/@value='true' and type/@value='oid']/value/@value}"/>
    </xsl:for-each>
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
