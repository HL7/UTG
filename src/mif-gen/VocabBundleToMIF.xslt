<?xml version="1.0" encoding="UTF-8"?>
<!-- TODO:
 - Fix concept domain URL
 - revamp contributor representation
 - 
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://hl7.org" xmlns:saxon="http://saxon.sf.net/" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="urn:hl7-org:v3/mif2" xpath-default-namespace="http://hl7.org/fhir" exclude-result-prefixes="fn saxon xs">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:param name="version" select="'todo'"/>
	<xsl:key name="resourceByUrl" match="/Bundle/entry" use="fullUrl/@value"/>
	<xsl:variable name="toolVersion" select="0.1"/>
	<xsl:variable name="conceptDomainSystemUrl" select="'http://terminology.hl7.org/CodeSystem/conceptdomains'"/>
	<xsl:variable name="urls" as="element(url)+">
    <xsl:apply-templates mode="url" select="Bundle/entry/resource/*[self::CodeSystem or self::ValueSet]"/>
	</xsl:variable>
	<xsl:template match="/Bundle">
    <xsl:if test="$version='todo'">
      <xsl:message select="'WARNING: No vocabulary package version specified.  Will set to ''todo'''"/>
    </xsl:if>
    <vocabularyModel name="{$version}" title="HL7 Vocabulary" packageKind="version" definitionKind="partial-publishing" schemaVersion="2.1.7">
      <packageLocation combinedId="DEFN=UV=VO={$version}" root="DEFN" artifact="VO" realmNamespace="UV" version="{$version}"/>
      <header>
        <renderingInformation renderingTime="{current-dateTime()}" application="UTG FHIR to MIF transform {$toolVersion}"/>
        <legalese copyrightOwner="Health Level Seven, Inc." copyrightYears="{substring(string(current-date()), 1, 4)}"/>
      </header>
<!--      <xsl:call-template name="doConceptDomains"/>
      <xsl:call-template name="doCodeSystems"/>-->
      <xsl:call-template name="doValueSets"/>
      <xsl:call-template name="doBindingRealms"/>
      <!-- TODO: contextBindings -->
    </vocabularyModel>
	</xsl:template>
	<xsl:template name="doConceptDomains">
    <xsl:for-each select="key('resourceByUrl', $conceptDomainSystemUrl)/resource/CodeSystem//concept[property[code/@value='source']/valueCode/@value='v3']">
      <xsl:sort select="code/@value"/>
      <conceptDomain name="{code/@value}">
        <!-- TODO: <historyItem dateTime="2012-03-15" id="00000000-0000-0000-0000-000000000000"
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
          <!--TODO: <appInfo>
            <openIssue>
              <text>
                <xsl:copy-of select="fn:markdownToHTML(definition/@value, true())"/>
              </text>
            </openIssue>
            <deprecationInfo deprecationEffectiveVersion="1221-20130726">
              <text>
                <xsl:copy-of select="fn:markdownToHTML(definition/@value, true())"/>
              </text>
            </deprecationInfo>
          </appInfo>-->
        </annotations>
        <xsl:for-each select="parent::concept">
          <specializesDomain name="{code/@value}"/>
        </xsl:for-each>
        <xsl:for-each select="property[not(code/@value='source')]">
          <property name="{code/@value}" value="{valueCode/@value}"/>
        </xsl:for-each>
      </conceptDomain>
    </xsl:for-each>
	</xsl:template>
  <xsl:template name="doCodeSystems">
    <xsl:for-each select="key('resourceByUrl', 'http://terminology.hl7.org/List/v3-term-manifest')/resource/List/entry/item/reference">
      <xsl:variable name="url" select="substring-before(@value, '|')"/>
      <xsl:for-each select="key('resourceByUrl', $url)/resource/CodeSystem">
        <codeSystem name="{substring(id/@value, 4,1)}{substring(name/@value,2,300)}" title="{title/@value}" codeSystemId="{fn:getOID(.)}">
          <xsl:call-template name="doHeaderElements"/>
          <releasedVersion releaseDate="{date/@value}">
            <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/hl7-maintained-indicator']/valueString/@value">
              <xsl:attribute name="hl7MaintainedIndicator" select="."/>
            </xsl:for-each>
            <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/complete-codes-indicator']/valueString/@value">
              <xsl:attribute name="completeCodesIndicator" select="."/>
            </xsl:for-each>
            <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/hl7-approved-indicator']/valueString/@value">
              <xsl:attribute name="hl7ApprovedIndicator" select="."/>
            </xsl:for-each>
            <xsl:for-each select="language/@value">
              <supportedLanguage>
                <xsl:value-of select="."/>
              </supportedLanguage>
            </xsl:for-each>
            <xsl:if test="extension[@url='http://hl7.org/fhir/StructureDefinition/hl7-approved-indicator']/valueString/@value='true'">
              <supportedConceptRelationship relationshipKind="Specializes" name="Specializes" inverseName="Generalizes" isNavigable="true" reflexivity="irreflexive" symmetry="antisymmetric" transitivity="transitive">
                <description>The child code is a more narrow version of the concept represented by the parent code.  I.e. Every child concept is also a valid parent concept.  Used to allow determination of subsumption.  Must be transitive, irreflexive, antisymmetric.</description>
              </supportedConceptRelationship>
              <supportedConceptRelationship relationshipKind="Generalizes" name="Generalizes" inverseName="Specializes" isNavigable="true" reflexivity="irreflexive" symmetry="antisymmetric" transitivity="transitive">
                <description>Inverse of Specializes.  Only included as a derived relationship.</description>
              </supportedConceptRelationship>
            </xsl:if>
            <xsl:for-each select="property[extension/@url='http://hl7.org/fhir/StructureDefinition/codeSystem-relationshipKind']">
              <supportedConceptRelationship relationshipKind="{extension[@url='http://hl7.org/fhir/StructureDefinition/codeSystem-relationshipKind']/valueCode/@value}" name="{code/@value}">
                <xsl:for-each select="description">
                  <description>
                    <xsl:copy-of select="fn:markdownToHTML(@value, false())"/>
                  </description>
                </xsl:for-each>
              </supportedConceptRelationship>
            </xsl:for-each>
            <xsl:for-each select="property[not(extension/@url='http://hl7.org/fhir/StructureDefinition/codeSystem-relationshipKind' or code/@value='notSelectable')]">
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
              <supportedConceptProperty propertyName="{code/@value}" type="{$type}" isMandatoryIndicator="{extension[@url='http://hl7.org/fhir/StructureDefinition/codeSystem-mandatory']/valueBoolean/@value}">
                <xsl:for-each select="description">
                  <description>
                    <xsl:copy-of select="fn:markdownToHTML(@value, false())"/>
                  </description>
                </xsl:for-each>
              </supportedConceptProperty>
            </xsl:for-each>
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
                    <!-- TODO: 
                    <deprecationInfo/>-->
                    <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/codeSystem-openIssue']/valueString">
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
                <xsl:for-each select="property[valueCoding]">
                  <conceptRelationship relationshipName="{code/@value}">
                    <xsl:for-each select="valueCoding">
                      <targetConcept code="{code/@value}" codeSystem="{fn:urlToOID(system/@value)}"/>
                    </xsl:for-each>
                  </conceptRelationship>
                </xsl:for-each>
                <xsl:for-each select="property[not(valueCoding or code/@value='notSelectable' or (code/@value='status' and valueCode/@value!='retired') or (valueCode/@value='retired' and preceding-sibling::property[valueCode/@value='retired']))]">
                  <conceptProperty name="{code/@value}" value="{*[starts-with(local-name(.), 'value')]/@value}"/>
                </xsl:for-each>
                <xsl:for-each select="display">
                  <printName language="en" preferredForLanguage="true" text="{@value}"/>
                </xsl:for-each>
                <xsl:for-each select="designation[use[system/@value='http://something...?' and code/@value='deprecated alias']]">
                  <printName language="en" preferredForLanguage="false" text="{value/@value}"/>
                </xsl:for-each>
                <code code="{code/@value}" status="active"/>
                <!-- TODO: set code status -->
                <xsl:for-each select="designation[use[system/@value='http://something...?' and code/@value='synonym']]">
                  <code code="{value/@value}" status="active"/>
                  <!-- TODO: set alternate code status -->
                </xsl:for-each>
              </concept>
            </xsl:for-each>
          </releasedVersion>
        </codeSystem>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>
  <xsl:template name="doHeaderElements">
    <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/codesystem-history']">
      <historyItem dateTime="{extension[@url='date']/valueDateTime/@value}">
        <xsl:for-each select="extension[@url='id']/valueString">
          <xsl:attribute name="id" select="@value"/>
        </xsl:for-each>
        <xsl:for-each select="extension[@url='substantive']/valueBoolean">
          <xsl:attribute name="isSubstantiveChange" select="@value"/>
        </xsl:for-each>
        <xsl:for-each select="extension[@url='backwardCompatible']/valueBoolean">
          <xsl:attribute name="isBackwardCompatibleChange" select="@value"/>
        </xsl:for-each>
        <xsl:for-each select="extension[@url='id']/valueString">
          <xsl:attribute name="author" select="@value"/>
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
        <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/codeSystem-legalese']/valueString/@value">
          <notation>
            <xsl:value-of select="."/>
          </notation>
        </xsl:for-each>
        <xsl:for-each select="copyright/@value">
          <licenseTerms>
            <xsl:value-of select="."/>
          </licenseTerms>
        </xsl:for-each>
        <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/codeSystem-versioning']/valueString/@value">
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
      <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/codeSystem-contributor']/valueString">
        <contributor>
          <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/codeSystem-contributor-name']/valueString/@value">
            <role>
              <xsl:value-of select="."/>
            </role>
          </xsl:for-each>
          <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/codeSystem-contributor-role']/valueString/@value">
            <role>
              <xsl:value-of select="."/>
            </role>
          </xsl:for-each>
          <xsl:for-each select="extension[@url='http://hl7.org/fhir/StructureDefinition/codeSystem-contributor-notes']/valueString/@value">
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
      <!-- TODO: <xsl:for-each select="extension[@url='todo']/valueString">
        <appInfo>
          <deprecationInfo>
            <text>
              <xsl:copy-of select="fn:markdownToHTML(@value, true())"/>
            </text>
          </deprecationInfo>
        </appInfo>
      </xsl:for-each> -->
    </xsl:variable>
    <xsl:if test="count($annotations)!=0">
      <annotations>
        <xsl:copy-of select="$annotations"/>
      </annotations>
    </xsl:if>
  </xsl:template>
  <xsl:template name="doValueSets">
    <xsl:for-each select="key('resourceByUrl', 'http://terminology.hl7.org/List/v3-term-manifest')/resource/List/entry/item/reference">
      <xsl:variable name="url" select="substring-before(@value, '|')"/>
      <xsl:for-each select="key('resourceByUrl', $url)/resource/ValueSet">
        <valueSet name="{title/@value}" id="{fn:getOID(.)}">
          <xsl:call-template name="doHeaderElements"/>
          <version versionDate="{date/@value}">
            <xsl:for-each select="distinct-values(compose/include/system/@value)">
              <supportedCodeSystem>
                <xsl:value-of select="fn:urlToOID(.)"/>
              </supportedCodeSystem>
            </xsl:for-each>
            <xsl:for-each select="language">
              <supportedLanguage>
                <xsl:value-of select="@value"/>
              </supportedLanguage>
            </xsl:for-each>
            <!-- TODO: <associatedConceptProperty name="Name:Act:inboundRelationship:ActRelationship" value="relatedTo"/>-->
            <xsl:choose>
              <xsl:when test="count(include)=1 and not(exclude)">
                <content>
                  <xsl:call-template name="doContent"/>
                </content>
              </xsl:when>
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
          </version>
        </valueSet>
      </xsl:for-each>
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
  <xsl:template name="doContent">
    <xsl:for-each select="system">
      <xsl:attribute name="codeSystem" select="fn:urlToOID(@value)"/>
    </xsl:for-each>
    <xsl:choose>
      <xsl:when test="valueSet">
        <valueSetRef id="{fn:urlToOID(@value)}"/>
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
  </xsl:template>
  <xsl:template mode="url" match="CodeSystem|ValueSet">
    <url xmlns="http://hl7.org/fhir" value="{url/@value}" oid="{fn:getOID(.)}"/>
  </xsl:template>
	<xsl:function name="fn:urlToOID">
    <xsl:param name="url"/>
    <xsl:value-of select="$urls[@value=$url][1]/@oid"/>
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
