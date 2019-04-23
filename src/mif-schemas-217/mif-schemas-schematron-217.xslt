<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:saxon="http://saxon.sf.net/" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:schold="http://www.ascc.net/xml/schematron" xmlns:iso="http://purl.oclc.org/dsdl/schematron" xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:mif="urn:hl7-org:v3/mif" version="2.0">
  <!--Implementers: please note that overriding process-prolog or process-root is 
    the preferred method for meta-stylesheets to use where possible. -->
  <xsl:param name="archiveDirParameter"/>
  <xsl:param name="archiveNameParameter"/>
  <xsl:param name="fileNameParameter"/>
  <xsl:param name="fileDirParameter"/>
  <xsl:variable name="document-uri">
    <xsl:value-of select="document-uri(/)"/>
  </xsl:variable>
  <!--PHASES-->
  <!--PROLOG-->
  <xsl:output xmlns:svrl="http://purl.oclc.org/dsdl/svrl" method="xml" omit-xml-declaration="no" standalone="yes" indent="yes"/>
  <!--XSD TYPES FOR XSLT2-->
  <!--KEYS AND FUNCTIONS-->
  <!--DEFAULT RULES-->
  <!--MODE: SCHEMATRON-SELECT-FULL-PATH-->
  <!--This mode can be used to generate an ugly though full XPath for locators-->
  <xsl:template match="*" mode="schematron-select-full-path">
    <xsl:apply-templates select="." mode="schematron-get-full-path"/>
  </xsl:template>
  <!--MODE: SCHEMATRON-FULL-PATH-->
  <!--This mode can be used to generate an ugly though full XPath for locators-->
  <xsl:template match="*" mode="schematron-get-full-path">
    <xsl:apply-templates select="parent::*" mode="schematron-get-full-path"/>
    <xsl:text>/</xsl:text>
    <xsl:choose>
      <xsl:when test="namespace-uri()=''">
        <xsl:value-of select="name()"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>*:</xsl:text>
        <xsl:value-of select="local-name()"/>
        <xsl:text>[namespace-uri()='</xsl:text>
        <xsl:value-of select="namespace-uri()"/>
        <xsl:text>']</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:variable name="preceding" select="count(preceding-sibling::*[local-name()=local-name(current())                                   and namespace-uri() = namespace-uri(current())])"/>
    <xsl:text>[</xsl:text>
    <xsl:value-of select="1+ $preceding"/>
    <xsl:text>]</xsl:text>
  </xsl:template>
  <xsl:template match="@*" mode="schematron-get-full-path">
    <xsl:apply-templates select="parent::*" mode="schematron-get-full-path"/>
    <xsl:text>/</xsl:text>
    <xsl:choose>
      <xsl:when test="namespace-uri()=''">@<xsl:value-of select="name()"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>@*[local-name()='</xsl:text>
        <xsl:value-of select="local-name()"/>
        <xsl:text>' and namespace-uri()='</xsl:text>
        <xsl:value-of select="namespace-uri()"/>
        <xsl:text>']</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!--MODE: SCHEMATRON-FULL-PATH-2-->
  <!--This mode can be used to generate prefixed XPath for humans-->
  <xsl:template match="node() | @*" mode="schematron-get-full-path-2">
    <xsl:for-each select="ancestor-or-self::*">
      <xsl:text>/</xsl:text>
      <xsl:value-of select="name(.)"/>
      <xsl:if test="preceding-sibling::*[name(.)=name(current())]">
        <xsl:text>[</xsl:text>
        <xsl:value-of select="count(preceding-sibling::*[name(.)=name(current())])+1"/>
        <xsl:text>]</xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:if test="not(self::*)">
      <xsl:text/>/@<xsl:value-of select="name(.)"/>
    </xsl:if>
  </xsl:template>
  <!--MODE: SCHEMATRON-FULL-PATH-3-->
  <!--This mode can be used to generate prefixed XPath for humans 
	(Top-level element has index)-->
  <xsl:template match="node() | @*" mode="schematron-get-full-path-3">
    <xsl:for-each select="ancestor-or-self::*">
      <xsl:text>/</xsl:text>
      <xsl:value-of select="name(.)"/>
      <xsl:if test="parent::*">
        <xsl:text>[</xsl:text>
        <xsl:value-of select="count(preceding-sibling::*[name(.)=name(current())])+1"/>
        <xsl:text>]</xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:if test="not(self::*)">
      <xsl:text/>/@<xsl:value-of select="name(.)"/>
    </xsl:if>
  </xsl:template>
  <!--MODE: GENERATE-ID-FROM-PATH -->
  <xsl:template match="/" mode="generate-id-from-path"/>
  <xsl:template match="text()" mode="generate-id-from-path">
    <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
    <xsl:value-of select="concat('.text-', 1+count(preceding-sibling::text()), '-')"/>
  </xsl:template>
  <xsl:template match="comment()" mode="generate-id-from-path">
    <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
    <xsl:value-of select="concat('.comment-', 1+count(preceding-sibling::comment()), '-')"/>
  </xsl:template>
  <xsl:template match="processing-instruction()" mode="generate-id-from-path">
    <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
    <xsl:value-of select="concat('.processing-instruction-', 1+count(preceding-sibling::processing-instruction()), '-')"/>
  </xsl:template>
  <xsl:template match="@*" mode="generate-id-from-path">
    <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
    <xsl:value-of select="concat('.@', name())"/>
  </xsl:template>
  <xsl:template match="*" mode="generate-id-from-path" priority="-0.5">
    <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
    <xsl:text>.</xsl:text>
    <xsl:value-of select="concat('.',name(),'-',1+count(preceding-sibling::*[name()=name(current())]),'-')"/>
  </xsl:template>
  <!--MODE: GENERATE-ID-2 -->
  <xsl:template match="/" mode="generate-id-2">U</xsl:template>
  <xsl:template match="*" mode="generate-id-2" priority="2">
    <xsl:text>U</xsl:text>
    <xsl:number level="multiple" count="*"/>
  </xsl:template>
  <xsl:template match="node()" mode="generate-id-2">
    <xsl:text>U.</xsl:text>
    <xsl:number level="multiple" count="*"/>
    <xsl:text>n</xsl:text>
    <xsl:number count="node()"/>
  </xsl:template>
  <xsl:template match="@*" mode="generate-id-2">
    <xsl:text>U.</xsl:text>
    <xsl:number level="multiple" count="*"/>
    <xsl:text>_</xsl:text>
    <xsl:value-of select="string-length(local-name(.))"/>
    <xsl:text>_</xsl:text>
    <xsl:value-of select="translate(name(),':','.')"/>
  </xsl:template>
  <!--Strip characters-->
  <xsl:template match="text()" priority="-1"/>
  <!--SCHEMA SETUP-->
  <xsl:template match="/">
    <svrl:schematron-output xmlns:svrl="http://purl.oclc.org/dsdl/svrl" title="" schemaVersion="">
      <xsl:comment>
        <xsl:value-of select="$archiveDirParameter"/>   
		 <xsl:value-of select="$archiveNameParameter"/>  
		 <xsl:value-of select="$fileNameParameter"/>  
		 <xsl:value-of select="$fileDirParameter"/>
      </xsl:comment>
      <svrl:ns-prefix-in-attribute-values uri="urn:hl7-org:v3/mif" prefix="mif"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M1"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M2"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M3"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M4"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M5"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M6"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M7"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M8"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M9"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M10"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M11"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M12"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M13"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M14"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M15"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M16"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M17"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M18"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M19"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M20"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M21"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M22"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M23"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M24"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M25"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M26"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M27"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M28"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M29"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M30"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M31"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M32"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M33"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M34"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M35"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M36"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M37"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M38"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M39"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M40"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M41"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M42"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M43"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M44"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M45"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M46"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M47"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M48"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M49"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M50"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M51"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M52"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M53"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M54"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M55"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M56"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M57"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M58"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M59"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M60"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M61"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M62"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M63"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M64"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M65"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M66"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M67"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M68"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M69"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M70"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M71"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M72"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M73"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M74"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M75"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M76"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M77"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M78"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M79"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M80"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M81"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M82"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M83"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M84"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M85"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M86"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M87"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M88"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M89"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M90"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M91"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M92"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M93"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M94"/>
      <svrl:ns-prefix-in-attribute-values uri="urn:hl7-org:v3/mif" prefix="mif"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M96"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M97"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M98"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M99"/>
      <svrl:active-pattern>
        <xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M100"/>
    </svrl:schematron-output>
  </xsl:template>
  <!--SCHEMATRON PATTERNS-->
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:PackageRef" priority="1000" mode="M1">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:PackageRef"/>
    <!--REPORT -->
    <xsl:if test="contains(concat(';', @artifact, ';'), ';RIM;ITS;DT;VO;') and @domain">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="contains(concat(';', @artifact, ';'), ';RIM;ITS;DT;VO;') and @domain">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: RIM, ITS, Vocabulary and Datatypes must be global (they may not be within a domain).</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="contains(concat(';', @artifact, ';'), ';DIM;CIM;LIM;AR;TE;IN;TP;DAM;') and not(@domain)">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="contains(concat(';', @artifact, ';'), ';DIM;CIM;LIM;AR;TE;IN;TP;DAM;') and not(@domain)">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: DIMs, DAMs, CIMs, LIMs, Application Roles, Trigger Events, Interactions and Templates may only be defined in a domain.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="@subArtifact and @artifact!='VO'">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@subArtifact and @artifact!='VO'">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Only vocabulary artifacts may have sub-artifact types.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="contains(concat(';', @artifact, ';'), ';RIM;ITS;') and not(@realmNamespace)">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="contains(concat(';', @artifact, ';'), ';RIM;ITS;') and not(@realmNamespace)">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: All artifacts except RIM and ITS must be specific to a particular realm namespace (even if universal).</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="@root!='PROF' and not(@artifact)">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@root!='PROF' and not(@artifact)">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Must specify an artifact unless package represents a profile.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="@root='PROF' and not(@artifact)">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@root='PROF' and not(@artifact)">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Must specify an artifact unless package represents a profile.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="@id and @name">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@id and @name">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Artifacts can't have both name and id.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M1"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M1"/>
  <xsl:template match="@*|node()" priority="-2" mode="M1">
    <xsl:apply-templates select="*" mode="M1"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M2"/>
  <xsl:template match="@*|node()" priority="-2" mode="M2">
    <xsl:apply-templates select="*" mode="M2"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M3"/>
  <xsl:template match="@*|node()" priority="-2" mode="M3">
    <xsl:apply-templates select="*" mode="M3"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:historyItem" priority="1000" mode="M4">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:historyItem"/>
    <!--REPORT -->
    <xsl:if test="@id and preceding::mif:historyItem[@id=current/@id]">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@id and preceding::mif:historyItem[@id=current/@id]">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
								ERROR: HistoryItem ids must be unique across the document</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M4"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M4"/>
  <xsl:template match="@*|node()" priority="-2" mode="M4">
    <xsl:apply-templates select="*" mode="M4"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:realm" priority="1000" mode="M5">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:realm"/>
    <!--REPORT -->
    <xsl:if test="count(preceding-sibling::mif:*[name(.)=name(current())][@value=current()/@value])!=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(preceding-sibling::mif:*[name(.)=name(current())][@value=current()/@value])!=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
								ERROR: A given realm may only be listed once within a particular element.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M5"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M5"/>
  <xsl:template match="@*|node()" priority="-2" mode="M5">
    <xsl:apply-templates select="*" mode="M5"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M6"/>
  <xsl:template match="@*|node()" priority="-2" mode="M6">
    <xsl:apply-templates select="*" mode="M6"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:businessName" priority="1000" mode="M7">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:businessName"/>
    <!--REPORT -->
    <xsl:if test="count(preceding-sibling::mif:*[name(.)=name(current())][mif:context/@value=current()/mif:context/@value or (not(mif:context) and not(current()/mif:context))])!=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(preceding-sibling::mif:*[name(.)=name(current())][mif:context/@value=current()/mif:context/@value or (not(mif:context) and not(current()/mif:context))])!=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
								ERROR: Only one business name may exist for a particular context.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(preceding-sibling::*[name(.)=name(current())][@name=current()/@name])!=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(preceding-sibling::*[name(.)=name(current())][@name=current()/@name])!=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
								ERROR: Business names must be unique for a given element.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M7"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M7"/>
  <xsl:template match="@*|node()" priority="-2" mode="M7">
    <xsl:apply-templates select="*" mode="M7"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M8"/>
  <xsl:template match="@*|node()" priority="-2" mode="M8">
    <xsl:apply-templates select="*" mode="M8"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M9"/>
  <xsl:template match="@*|node()" priority="-2" mode="M9">
    <xsl:apply-templates select="*" mode="M9"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M10"/>
  <xsl:template match="@*|node()" priority="-2" mode="M10">
    <xsl:apply-templates select="*" mode="M10"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:graphElement[@name]" priority="1000" mode="M11">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:graphElement[@name]"/>
    <!--REPORT -->
    <xsl:if test="count(preceding-sibling::mif:*[name(.)=name(current())][@name=current()/@name])!=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(preceding-sibling::mif:*[name(.)=name(current())][@name=current()/@name])!=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
								ERROR: Only one diagram with a given name may exist within a single element.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M11"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M11"/>
  <xsl:template match="@*|node()" priority="-2" mode="M11">
    <xsl:apply-templates select="*" mode="M11"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:graphElement" priority="1000" mode="M12">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:graphElement"/>
    <xsl:apply-templates select="*" mode="M12"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M12"/>
  <xsl:template match="@*|node()" priority="-2" mode="M12">
    <xsl:apply-templates select="*" mode="M12"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:graphElement[mif:anchorage]" priority="1000" mode="M13">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:graphElement[mif:anchorage]"/>
    <xsl:apply-templates select="*" mode="M13"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M13"/>
  <xsl:template match="@*|node()" priority="-2" mode="M13">
    <xsl:apply-templates select="*" mode="M13"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M14"/>
  <xsl:template match="@*|node()" priority="-2" mode="M14">
    <xsl:apply-templates select="*" mode="M14"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M15"/>
  <xsl:template match="@*|node()" priority="-2" mode="M15">
    <xsl:apply-templates select="*" mode="M15"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M16"/>
  <xsl:template match="@*|node()" priority="-2" mode="M16">
    <xsl:apply-templates select="*" mode="M16"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M17"/>
  <xsl:template match="@*|node()" priority="-2" mode="M17">
    <xsl:apply-templates select="*" mode="M17"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:ballotComment" priority="1000" mode="M18">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:ballotComment"/>
    <!--REPORT -->
    <xsl:if test="not(ancestor::mif:*/mif:ballotInfo/mif:ballotResponse/@submissionId=current()/@submissionId)">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="not(ancestor::mif:*/mif:ballotInfo/mif:ballotResponse/@submissionId=current()/@submissionId)">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
											ERROR: Cannot have a ballot comment that is not part of an identified ballot response associated with the package.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="preceding::mif:*[name(.)=name(current())][@submissionId=current()/@submissionId][@name=current()/@name]">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="preceding::mif:*[name(.)=name(current())][@submissionId=current()/@submissionId][@name=current()/@name]">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
											ERROR: Cannot have multiple ballot comments with the same name within the same submission.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M18"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M18"/>
  <xsl:template match="@*|node()" priority="-2" mode="M18">
    <xsl:apply-templates select="*" mode="M18"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:changeRequest" priority="1000" mode="M19">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:changeRequest"/>
    <xsl:apply-templates select="*" mode="M19"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M19"/>
  <xsl:template match="@*|node()" priority="-2" mode="M19">
    <xsl:apply-templates select="*" mode="M19"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:deprecationInfo" priority="1000" mode="M20">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:deprecationInfo"/>
    <xsl:apply-templates select="*" mode="M20"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M20"/>
  <xsl:template match="@*|node()" priority="-2" mode="M20">
    <xsl:apply-templates select="*" mode="M20"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M21"/>
  <xsl:template match="@*|node()" priority="-2" mode="M21">
    <xsl:apply-templates select="*" mode="M21"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M22"/>
  <xsl:template match="@*|node()" priority="-2" mode="M22">
    <xsl:apply-templates select="*" mode="M22"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M23"/>
  <xsl:template match="@*|node()" priority="-2" mode="M23">
    <xsl:apply-templates select="*" mode="M23"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M24"/>
  <xsl:template match="@*|node()" priority="-2" mode="M24">
    <xsl:apply-templates select="*" mode="M24"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:text" priority="1000" mode="M25">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:text"/>
    <!--REPORT -->
    <xsl:if test="count(preceding-sibling::mif:*[name(.)=name(current())][@lang=current()/@lang or              ((@lang='EN' or not(@lang)) and (current()/@lang='EN' or not(current()/@lang)))])!=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(preceding-sibling::mif:*[name(.)=name(current())][@lang=current()/@lang or ((@lang='EN' or not(@lang)) and (current()/@lang='EN' or not(current()/@lang)))])!=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Each repetition of complex markup with language must be a different langage (no language is equivalent to 'EN').</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M25"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M25"/>
  <xsl:template match="@*|node()" priority="-2" mode="M25">
    <xsl:apply-templates select="*" mode="M25"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:text" priority="1000" mode="M26">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:text"/>
    <!--REPORT -->
    <xsl:if test="count(preceding-sibling::mif:*[name(.)=name(current())][@lang=current()/@lang or              ((@lang='EN' or not(@lang)) and (current()/@lang='EN' or not(current()/@lang)))])!=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(preceding-sibling::mif:*[name(.)=name(current())][@lang=current()/@lang or ((@lang='EN' or not(@lang)) and (current()/@lang='EN' or not(current()/@lang)))])!=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Each repetition of complex markup with language must be a different langage (no language is equivalent to 'EN').</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M26"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M26"/>
  <xsl:template match="@*|node()" priority="-2" mode="M26">
    <xsl:apply-templates select="*" mode="M26"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:sourceArtifact" priority="1000" mode="M27">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:sourceArtifact"/>
    <xsl:apply-templates select="*" mode="M27"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M27"/>
  <xsl:template match="@*|node()" priority="-2" mode="M27">
    <xsl:apply-templates select="*" mode="M27"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:text" priority="1000" mode="M28">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:text"/>
    <!--REPORT -->
    <xsl:if test="count(preceding-sibling::mif:*[name(.)=name(current())][@lang=current()/@lang or              ((@lang='EN' or not(@lang)) and (current()/@lang='EN' or not(current()/@lang)))])!=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(preceding-sibling::mif:*[name(.)=name(current())][@lang=current()/@lang or ((@lang='EN' or not(@lang)) and (current()/@lang='EN' or not(current()/@lang)))])!=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Each repetition of complex markup with language must be a different langage (no language is equivalent to 'EN').</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M28"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M28"/>
  <xsl:template match="@*|node()" priority="-2" mode="M28">
    <xsl:apply-templates select="*" mode="M28"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M29"/>
  <xsl:template match="@*|node()" priority="-2" mode="M29">
    <xsl:apply-templates select="*" mode="M29"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M30"/>
  <xsl:template match="@*|node()" priority="-2" mode="M30">
    <xsl:apply-templates select="*" mode="M30"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:packageLocation" priority="1000" mode="M31">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:packageLocation"/>
    <xsl:apply-templates select="*" mode="M31"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M31"/>
  <xsl:template match="@*|node()" priority="-2" mode="M31">
    <xsl:apply-templates select="*" mode="M31"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M32"/>
  <xsl:template match="@*|node()" priority="-2" mode="M32">
    <xsl:apply-templates select="*" mode="M32"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:packageLocation" priority="1000" mode="M33">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:packageLocation"/>
    <xsl:apply-templates select="*" mode="M33"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M33"/>
  <xsl:template match="@*|node()" priority="-2" mode="M33">
    <xsl:apply-templates select="*" mode="M33"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:replaces" priority="1000" mode="M34">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:replaces"/>
    <xsl:apply-templates select="*" mode="M34"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M34"/>
  <xsl:template match="@*|node()" priority="-2" mode="M34">
    <xsl:apply-templates select="*" mode="M34"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:targetPackage" priority="1000" mode="M35">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:targetPackage"/>
    <xsl:apply-templates select="*" mode="M35"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M35"/>
  <xsl:template match="@*|node()" priority="-2" mode="M35">
    <xsl:apply-templates select="*" mode="M35"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:legalese" priority="1000" mode="M36">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:legalese"/>
    <!--REPORT -->
    <xsl:if test="(@copyrightOwner and not(@copyrightYears)) or (not(@copyrightOwner) and @copyrightYears)">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="(@copyrightOwner and not(@copyrightYears)) or (not(@copyrightOwner) and @copyrightYears)">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
													ERROR: Either both copyrightYears and copyrightOwner must be specified or neither should be specified.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="@copyrightNotation and not(@copyrightYears)">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@copyrightNotation and not(@copyrightYears)">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
													ERROR: CopyrightNotation may only be specified when copyrightYears is present.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M36"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M36"/>
  <xsl:template match="@*|node()" priority="-2" mode="M36">
    <xsl:apply-templates select="*" mode="M36"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:responsibleGroup" priority="1000" mode="M37">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:responsibleGroup"/>
    <!--REPORT -->
    <xsl:if test="not(@groupId or @groupName or @organizationName)">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="not(@groupId or @groupName or @organizationName)">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
								ERROR: At least one of groupId, groupName or organizationName is required.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M37"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M37"/>
  <xsl:template match="@*|node()" priority="-2" mode="M37">
    <xsl:apply-templates select="*" mode="M37"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:approvalInfo" priority="1000" mode="M38">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:approvalInfo"/>
    <!--REPORT -->
    <xsl:if test="count(parent::mif:*/ancestor::mif:*/mif:header/mif:*[name(.)=name(current())])!=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(parent::mif:*/ancestor::mif:*/mif:header/mif:*[name(.)=name(current())])!=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
								ERROR: Cannot have a ballotInfo when a parent package has a ballotInfo.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M38"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M38"/>
  <xsl:template match="@*|node()" priority="-2" mode="M38">
    <xsl:apply-templates select="*" mode="M38"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M39"/>
  <xsl:template match="@*|node()" priority="-2" mode="M39">
    <xsl:apply-templates select="*" mode="M39"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M40"/>
  <xsl:template match="@*|node()" priority="-2" mode="M40">
    <xsl:apply-templates select="*" mode="M40"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M41"/>
  <xsl:template match="@*|node()" priority="-2" mode="M41">
    <xsl:apply-templates select="*" mode="M41"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:ballotSubmission" priority="1000" mode="M42">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:ballotSubmission"/>
    <!--REPORT -->
    <xsl:if test="contains(@vote, 'Negative') and not(parent::mif:*/parent::mif:header//mif:ballotComment[contains(@vote, 'Negative')] or mif:voterComments)">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="contains(@vote, 'Negative') and not(parent::mif:*/parent::mif:header//mif:ballotComment[contains(@vote, 'Negative')] or mif:voterComments)">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
								WARNING: You must have general comments or a negative ballotComment to have a negative vote.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="@vote!='Negative' and parent::mif:*/parent::header//mif:ballotComment[contains(@vote, 'Negative')]">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@vote!='Negative' and parent::mif:*/parent::header//mif:ballotComment[contains(@vote, 'Negative')]">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
								WARNING: Vote must be 'Negative' if there are any negative comments.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="@vote!='Negative' and @resolution">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@vote!='Negative' and @resolution">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
								WARNING: Resolutions should only be specified for negative votes.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="@resolution and parent::mif:*/parent::header//mif:ballotComment[not(mif:resolution)]">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@resolution and parent::mif:*/parent::header//mif:ballotComment[not(mif:resolution)]">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
								WARNING: Should only have a resolution for a ballot submission when all ballotcomments have been resolved.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M42"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M42"/>
  <xsl:template match="@*|node()" priority="-2" mode="M42">
    <xsl:apply-templates select="*" mode="M42"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M43"/>
  <xsl:template match="@*|node()" priority="-2" mode="M43">
    <xsl:apply-templates select="*" mode="M43"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:Uuid" priority="1000" mode="M44">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:Uuid"/>
    <!--REPORT -->
    <xsl:if test="count(//mif:Uuid[count(parent::mif:GraphicInformation)=0][.=current()]|//mif:Uuid[.=current()])&gt;1">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(//attribute(*, mif:Uuid)[count(parent::mif:GraphicInformation)=0][.=current()]|//mif:Uuid[.=current()])&gt;1">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: UUIDs should never be duplicated within a document.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M44"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M44"/>
  <xsl:template match="@*|node()" priority="-2" mode="M44">
    <xsl:apply-templates select="*" mode="M44"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M45"/>
  <xsl:template match="@*|node()" priority="-2" mode="M45">
    <xsl:apply-templates select="*" mode="M45"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M46"/>
  <xsl:template match="@*|node()" priority="-2" mode="M46">
    <xsl:apply-templates select="*" mode="M46"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M47"/>
  <xsl:template match="@*|node()" priority="-2" mode="M47">
    <xsl:apply-templates select="*" mode="M47"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M48"/>
  <xsl:template match="@*|node()" priority="-2" mode="M48">
    <xsl:apply-templates select="*" mode="M48"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M49"/>
  <xsl:template match="@*|node()" priority="-2" mode="M49">
    <xsl:apply-templates select="*" mode="M49"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M50"/>
  <xsl:template match="@*|node()" priority="-2" mode="M50">
    <xsl:apply-templates select="*" mode="M50"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:LibraryAnnotations" priority="1000" mode="M51">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:LibraryAnnotations"/>
    <!--REPORT -->
    <xsl:if test="*/*[not(cascadeInfo)]">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="*/*[not(cascadeInfo)]">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: 'cascadeInfo' must be specified on all annotations inside an annotation library.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M51"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M51"/>
  <xsl:template match="@*|node()" priority="-2" mode="M51">
    <xsl:apply-templates select="*" mode="M51"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="text()" priority="-1" mode="M52"/>
  <xsl:template match="@*|node()" priority="-2" mode="M52">
    <xsl:apply-templates select="*" mode="M52"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:datatypeModel" priority="1000" mode="M53">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:datatypeModel"/>
    <xsl:apply-templates select="*" mode="M53"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M53"/>
  <xsl:template match="@*|node()" priority="-2" mode="M53">
    <xsl:apply-templates select="*" mode="M53"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M54"/>
  <xsl:template match="@*|node()" priority="-2" mode="M54">
    <xsl:apply-templates select="*" mode="M54"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:datatype" priority="1000" mode="M55">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:datatype"/>
    <!--REPORT -->
    <xsl:if test="count(preceding::mif:*[name(.)=name(current())][@name=current()/@name][count(mif:parameter[@name=current()/mif:parameter/@name])=count(mif:parameter)])!=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(preceding::mif:*[name(.)=name(current())][@name=current()/@name][count(mif:parameter[@name=current()/mif:parameter/@name])=count(mif:parameter)])!=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: There must not be more than one datatype definition having the same name and parameters.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="ancestor::mif:datatypeModelLibrary//*[name(.)=name(current())][@name=current()/@name]&gt;1">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="ancestor::mif:datatypeModelLibrary//*[name(.)=name(current())][@name=current()/@name]&gt;1">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: There cannot be more than one datatype with the same name.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M55"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M55"/>
  <xsl:template match="@*|node()" priority="-2" mode="M55">
    <xsl:apply-templates select="*" mode="M55"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M56"/>
  <xsl:template match="@*|node()" priority="-2" mode="M56">
    <xsl:apply-templates select="*" mode="M56"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:operation" priority="1000" mode="M57">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:operation"/>
    <!--REPORT -->
    <xsl:if test="@operationKind!='conversion' and count(preceding-sibling::mif:*[name(.)=name(current())][@name=current()/@name][count(mif:parameter[@name=current()/mif:parameter/@name])=count(mif:parameter)])=1">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@operationKind!='conversion' and count(preceding-sibling::mif:*[name(.)=name(current())][@name=current()/@name][count(mif:parameter[@name=current()/mif:parameter/@name])=count(mif:parameter)]">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: There must not be more than one operation within a datatype definition having the same name and parameters.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="@defaultFrom and count(mif:type/descendant-or-self::mif:*[contains('CD;CE;CS;SC;BL;ST;INT;REAL;MO;PQ', @name)])!=1">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@defaultFrom and count(mif:type/descendant-or-self::mif:*[contains('CD;CE;CS;SC;BL;ST;INT;REAL;MO;PQ', @name)])!=1">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							WARNING: DefaultFrom may only be specified for codes and simple datatypes (CD, CE, CS, SC, BL, ST, INT, REAL, MO, PQ).</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(mif:vocabularySpecification)=0 and count(mif:type/descendant-or-self::mif:*[contains('CD;CE;CS;SC', @name)])!=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(mif:vocabularySpecification)=0 and count(mif:type/descendant-or-self::mif:*[contains('CD;CE;CS;SC', @name)])!=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							WARNING: Domain must be present for coded types (CD, CE, CS and SC).</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(mif:vocabularySpecification)!=0 and count(mif:type/descendant-or-self::mif:*[contains('CD;CE;CS;SC', @name)])=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(mif:vocabularySpecification)!=0 and count(mif:type/descendant-or-self::mif:*[contains('CD;CE;CS;SC', @name)])=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							WARNING: Domain may only be present for coded types (CD, CE, CS and SC).</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="@default and count(mif:type/descendant-or-self::mif:*[contains('CD;CE;CS;SC;BL;ST;INT;REAL;MO;PQ', @name)])!=1">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@default and count(mif:type/descendant-or-self::mif:*[contains('CD;CE;CS;SC;BL;ST;INT;REAL;MO;PQ', @name)])!=1">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							WARNING: Default may only be specified for codes and simple datatypes (CD, CE, CS, SC, BL, ST, INT, REAL, MO, PQ).</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="@minimumSupportedLength and count(mif:type/descendant-or-self::mif:*[contains('CD;CE;CS;SC;BL;ST;INT;REAL;MO;PQ', @name)])!=1">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@minimumSupportedLength and count(mif:type/descendant-or-self::mif:*[contains('CD;CE;CS;SC;BL;ST;INT;REAL;MO;PQ', @name)])!=1">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							WARNING: Minimum supported length may only be specified for codes and simple datatypes (CD, CE, CS, SC, BL, ST, INT, REAL, MO, PQ).</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(mif:type/descendant-or-self::mif:*[@name='CS'])!=0 and mif:vocabularySpecification/@codingStrength='CWE'">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(mif:type/descendant-or-self::mif:*[@name='CS'])!=0 and mif:vocabularySpecification/@codingStrength='CWE'">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: CodingStrength must not be CWE for CS datatype attributes.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(mif:derivedFrom)=0 and count(mif:businessName)=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(mif:derivedFrom)=0 and count(mif:businessName)=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Non-derived operations must have a business name.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(mif:derivedFrom)=0 and count(mif:annotations/mif:definition)=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(mif:derivedFrom)=0 and count(mif:annotations/mif:definition)=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Non-derived operations must have a definition annotation.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M57"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M57"/>
  <xsl:template match="@*|node()" priority="-2" mode="M57">
    <xsl:apply-templates select="*" mode="M57"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M58"/>
  <xsl:template match="@*|node()" priority="-2" mode="M58">
    <xsl:apply-templates select="*" mode="M58"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:operation/mif:derivedFrom" priority="1000" mode="M59">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:operation/mif:derivedFrom"/>
    <!--REPORT -->
    <xsl:if test="@operationName and count(mif:type)!=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@operationName and count(mif:type)!=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: A property reference may only have a name or a conversion datatype, not both.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="not(@operationName) and count(mif:type)=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="not(@operationName) and count(mif:type)=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: A property reference must have either a name or a conversion datatype.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M59"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M59"/>
  <xsl:template match="@*|node()" priority="-2" mode="M59">
    <xsl:apply-templates select="*" mode="M59"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:vocabularySpecification" priority="1000" mode="M60">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:vocabularySpecification"/>
    <xsl:apply-templates select="*" mode="M60"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M60"/>
  <xsl:template match="@*|node()" priority="-2" mode="M60">
    <xsl:apply-templates select="*" mode="M60"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M61"/>
  <xsl:template match="@*|node()" priority="-2" mode="M61">
    <xsl:apply-templates select="*" mode="M61"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:classTypeRef" priority="1000" mode="M62">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:classTypeRef"/>
    <!--REPORT -->
    <xsl:if test="count(ancestor::mif:staticModel//mif:classTypeRef[@typeId=current()/@typeId])!=1">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(ancestor::mif:staticModel//mif:classTypeRef[@typeId=current()/@typeId])!=1">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
										ERROR: Only one classTypeRef may exist in an staticModel with a given typeId.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M62"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M62"/>
  <xsl:template match="@*|node()" priority="-2" mode="M62">
    <xsl:apply-templates select="*" mode="M62"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:TermDefinition" priority="1000" mode="M63">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:TermDefinition"/>
    <!--REPORT -->
    <xsl:if test="count(seeAlso)+count(definition)=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(seeAlso)+count(definition)=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
              ERROR: Must have at least one of 'seeAlso' or 'definition'.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(termTranslations[@name=current()/@term])!=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(termTranslations[@name=current()/@term])!=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
              ERROR: Cannot have a term translation with the same name as the term.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M63"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M63"/>
  <xsl:template match="@*|node()" priority="-2" mode="M63">
    <xsl:apply-templates select="*" mode="M63"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:packageLocation" priority="1000" mode="M64">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:packageLocation"/>
    <xsl:apply-templates select="*" mode="M64"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M64"/>
  <xsl:template match="@*|node()" priority="-2" mode="M64">
    <xsl:apply-templates select="*" mode="M64"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M65"/>
  <xsl:template match="@*|node()" priority="-2" mode="M65">
    <xsl:apply-templates select="*" mode="M65"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M66"/>
  <xsl:template match="@*|node()" priority="-2" mode="M66">
    <xsl:apply-templates select="*" mode="M66"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:annotations/*" priority="1000" mode="M67">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:annotations/*"/>
    <!--REPORT -->
    <xsl:if test="count(mif:presentation)!=0 and not(parent::mif:annotations[parent::mif:class or parent::mif:classTypeRef or parent::mif:classStub or parent::mif:attribute or parent::mif:association])">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(mif:presentation)!=0 and not(parent::mif:annotations[parent::mif:class or parent::mif:classTypeRef or parent::mif:classStub or parent::mif:attribute or parent::mif:association])">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Annotations may only have presentation information when associated with a class, classTypeRef, classStub, attribute or association.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(mif:presentation)!=0 and            ((not(parent::mif:annotations[parent::mif:attribute or parent::mif:association]) and             count(parent::mif:annotations/parent::mif:*/mif:presentation[@shapeId=current()/mif:presentation/@connectionShapeId])=0) or           (parent::mif:annotations/parent::mif:attribute and             count(parent::mif:annotations/parent::mif:attribute/parent::mif:*/mif:presentation[@shapeId=current()/mif:presentation/@connectionShapeId])=0) or           (parent::mif:annotations/parent::mif:association and             count(parent::mif:annotations/parent::mif:association/parent::mif:*/mif:presentation[@shapeId=current()/mif:presentation/@connectionShapeId])=0))">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(mif:presentation)!=0 and ((not(parent::mif:annotations[parent::mif:attribute or parent::mif:association]) and count(parent::mif:annotations/parent::mif:*/mif:presentation[@shapeId=current()/mif:presentation/@connectionShapeId])=0) or (parent::mif:annotations/parent::mif:attribute and count(parent::mif:annotations/parent::mif:attribute/parent::mif:*/mif:presentation[@shapeId=current()/mif:presentation/@connectionShapeId])=0) or (parent::mif:annotations/parent::mif:association and count(parent::mif:annotations/parent::mif:association/parent::mif:*/mif:presentation[@shapeId=current()/mif:presentation/@connectionShapeId])=0))">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Connection shapeId on annotation does not point to a shape associated with the annotated class.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M67"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M67"/>
  <xsl:template match="@*|node()" priority="-2" mode="M67">
    <xsl:apply-templates select="*" mode="M67"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:staticModel" priority="1001" mode="M68">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:staticModel"/>
    <!--REPORT -->
    <xsl:if test="count(.//mif:displayInfo[@representationType='UML'])!=0 and count(.//mif:subjectArea) != count(.//mif:subjectArea[count(mif:displayInfo[@representationType='UML'])!=0])">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(.//mif:displayInfo[@representationType='UML'])!=0 and count(.//mif:subjectArea) != count(.//mif:subjectArea[count(mif:displayInfo[@representationType='UML'])!=0])">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: If any elements have UML displayInfo, all subjectAreas must have UML displayInfo.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(.//mif:displayInfo[@representationType='UML'])!=0 and count(.//mif:entryPoint) != count(.//mif:entryPoint[count(mif:displayInfo[@representationType='UML'])!=0])">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(.//mif:displayInfo[@representationType='UML'])!=0 and count(.//mif:entryPoint) != count(.//mif:entryPoint[count(mif:displayInfo[@representationType='UML'])!=0])">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: If any elements have UML displayInfo, all entryPoints must have UML displayInfo.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(.//mif:displayInfo[@representationType='UML'])!=0 and count(.//mif:class) != count(.//mif:class[count(mif:displayInfo[@representationType='UML'])!=0])">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(.//mif:displayInfo[@representationType='UML'])!=0 and count(.//mif:class) != count(.//mif:class[count(mif:displayInfo[@representationType='UML'])!=0])">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: If any elements have UML displayInfo, all classes must have UML displayInfo.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(.//mif:displayInfo[@representationType='UML'])!=0 and count(.//mif:classTypeRef) != count(.//mif:classTypeRef[count(mif:displayInfo[@representationType='UML'])!=0])">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(.//mif:displayInfo[@representationType='UML'])!=0 and count(.//mif:classTypeRef) != count(.//mif:classTypeRef[count(mif:displayInfo[@representationType='UML'])!=0])">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: If any elements have UML displayInfo, all classTypeRefs must have UML displayInfo.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(.//mif:displayInfo[@representationType='UML'])!=0 and count(.//mif:classStub) != count(.//mif:classStub[count(mif:displayInfo[@representationType='UML'])!=0])">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(.//mif:displayInfo[@representationType='UML'])!=0 and count(.//mif:classStub) != count(.//mif:classStub[count(mif:displayInfo[@representationType='UML'])!=0])">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: If any elements have UML displayInfo, all classStubs must have UML displayInfo.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(.//mif:displayInfo[@representationType='HL7Visio'])!=0 and count(.//mif:subjectArea) != count(.//mif:subjectArea[count(mif:displayInfo[@representationType='HL7Visio'])!=0])">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(.//mif:displayInfo[@representationType='HL7Visio'])!=0 and count(.//mif:subjectArea) != count(.//mif:subjectArea[count(mif:displayInfo[@representationType='HL7Visio'])!=0])">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: If any elements have HL7Visio displayInfo, all subjectAreas must have HL7Visio displayInfo.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(.//mif:displayInfo[@representationType='HL7Visio'])!=0 and count(.//mif:entryPoint) != count(.//mif:entryPoint[count(mif:displayInfo[@representationType='HL7Visio'])!=0])">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(.//mif:displayInfo[@representationType='HL7Visio'])!=0 and count(.//mif:entryPoint) != count(.//mif:entryPoint[count(mif:displayInfo[@representationType='HL7Visio'])!=0])">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: If any elements have HL7Visio displayInfo, all entryPoints must have HL7Visio displayInfo.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(.//mif:displayInfo[@representationType='HL7Visio'])!=0 and count(.//mif:class) != count(.//mif:class[count(mif:displayInfo[@representationType='HL7Visio'])!=0])">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(.//mif:displayInfo[@representationType='HL7Visio'])!=0 and count(.//mif:class) != count(.//mif:class[count(mif:displayInfo[@representationType='HL7Visio'])!=0])">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: If any elements have HL7Visio displayInfo, all classes must have HL7Visio displayInfo.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(.//mif:displayInfo[@representationType='HL7Visio'])!=0 and count(.//mif:classTypeRef) != count(.//mif:classTypeRef[count(mif:displayInfo[@representationType='HL7Visio'])!=0])">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(.//mif:displayInfo[@representationType='HL7Visio'])!=0 and count(.//mif:classTypeRef) != count(.//mif:classTypeRef[count(mif:displayInfo[@representationType='HL7Visio'])!=0])">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: If any elements have HL7Visio displayInfo, all classTypeRefs must have HL7Visio displayInfo.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(.//mif:displayInfo[@representationType='HL7Visio'])!=0 and count(.//mif:classStub) != count(.//mif:classStub[count(mif:displayInfo[@representationType='HL7Visio'])!=0])">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(.//mif:displayInfo[@representationType='HL7Visio'])!=0 and count(.//mif:classStub) != count(.//mif:classStub[count(mif:displayInfo[@representationType='HL7Visio'])!=0])">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: If any elements have HL7Visio displayInfo, all classStubs must have HL7Visio displayInfo.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M68"/>
  </xsl:template>
  <!--RULE -->
  <xsl:template match="mif:displayInfo" priority="1000" mode="M68">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:displayInfo"/>
    <!--REPORT -->
    <xsl:if test="preceding-sibling::mif:displayInfo[@representationType=current()/@representationType] and not(parent::mif:class or parent::mif:classTypeRef or parent::mif:classStub)">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="preceding-sibling::mif:displayInfo[@representationType=current()/@representationType] and not(parent::mif:class or parent::mif:classTypeRef or parent::mif:classStub)">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Only classes may have more than one display info for a given representationType.  (Classes may have 'shadow' shapes for hl7 representations).</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(parent::mif:*/mif:displayInfo[@representationType='UML'])&gt;1">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(parent::mif:*/mif:displayInfo[@representationType='UML'])&gt;1">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: There may not be more than one set of UML display info for a given element.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M68"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M68"/>
  <xsl:template match="@*|node()" priority="-2" mode="M68">
    <xsl:apply-templates select="*" mode="M68"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:staticModel/mif:derivationSupplier[@derivationType!='extension' and @derivationType='incompatible']" priority="1001" mode="M69">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:staticModel/mif:derivationSupplier[@derivationType!='extension' and @derivationType='incompatible']"/>
    <!--REPORT -->
    <xsl:if test="count(parent::mif:staticModel//mif:*[mif:derivationSupplier[@className]/@modelId=current()/@modelId])!=              count(parent::mif:staticModel//*[self::mif:class or self::mif:classTypeRef or self::mif:classStub])">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(parent::mif:staticModel//mif:*[mif:derivationSupplier[@className]/@modelId=current()/@modelId])!= count(parent::mif:staticModel//*[self::mif:class or self::mif:classTypeRef or self::mif:classStub])">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
										ERROR: If a derivationSupplier' element exists for a staticModel, all classes within that model must also have a derivationSupplier for the same modelId, unless the derivation type is 'extension'.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M69"/>
  </xsl:template>
  <!--RULE -->
  <xsl:template match="mif:derivationSupplier[parent::mif:class or parent::mif:classTypeRef or parent::mif:classStub][@derivationType!='extension' and @derivationType='incompatible']" priority="1000" mode="M69">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:derivationSupplier[parent::mif:class or parent::mif:classTypeRef or parent::mif:classStub][@derivationType!='extension' and @derivationType='incompatible']"/>
    <!--REPORT -->
    <xsl:if test="count(parent::mif:*/mif:attribute[mif:derivationSupplier/@modelId=current()/@modelId])!=count(parent::mif:*/mif:attribute)">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(parent::mif:*/mif:attribute[mif:derivationSupplier/@modelId=current()/@modelId])!=count(parent::mif:*/mif:attribute)">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
										ERROR: If a 'derivationSupplier' element exists for a staticModel, all attributes within that model must also have a derivationSupplier for the same modelId.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(parent::mif:*/mif:association[mif:derivationSupplier/@modelId=current()/@modelId])!=count(parent::mif:*/mif:association)">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(parent::mif:*/mif:association[mif:derivationSupplier/@modelId=current()/@modelId])!=count(parent::mif:*/mif:association)">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
										ERROR: If a 'derivationSupplier' element exists for a staticModel, all associations within that model must also have a derivationSupplier for the same modelId.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(parent::mif:*/mif:stateMachine/mif:state[mif:derivationSupplier/@modelId=current()/@modelId])!=count(parent::mif:*/mif:stateMachine/mif:state)">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(parent::mif:*/mif:stateMachine/mif:state[mif:derivationSupplier/@modelId=current()/@modelId])!=count(parent::mif:*/mif:stateMachine/mif:state)">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
										ERROR: If a 'derivationSupplier' element exists for a staticModel, all states within that model must also have a derivationSupplier for the same modelId.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(parent::mif:*/mif:stateMachine/mif:transition[mif:derivationSupplier/@modelId=current()/@modelId])!=count(parent::mif:*/mif:stateMachine/mif:transition)">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(parent::mif:*/mif:stateMachine/mif:transition[mif:derivationSupplier/@modelId=current()/@modelId])!=count(parent::mif:*/mif:stateMachine/mif:transition)">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
										ERROR: If a 'derivationSupplier' element exists for a staticModel, all transitions within that model must also have a derivationSupplier for the same modelId.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M69"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M69"/>
  <xsl:template match="@*|node()" priority="-2" mode="M69">
    <xsl:apply-templates select="*" mode="M69"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:derivationSupplier" priority="1000" mode="M70">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:derivationSupplier"/>
    <!--REPORT -->
    <xsl:if test="count(preceding-sibling::mif:derivationSupplier[@modelId=current()/@modelId])!=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(preceding-sibling::mif:derivationSupplier[@modelId=current()/@modelId])!=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
										GUIDELINE: An element may not be derived from two different elements within the same model.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="@derivationType='unchanged' and count(parent::mif:*//mif:derivationSupplier[@modelId=current()/@modelId][@derivationType!='unchanged'])">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@derivationType='unchanged' and count(parent::mif:*//mif:derivationSupplier[@modelId=current()/@modelId][@derivationType!='unchanged'])">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
										ERROR: An element derivation may not be marked as 'unchanged' unless all child elements are marked as 'unchanged'.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="@derivationType='annotation' and count(parent::mif:*//mif:derivationSupplier[@modelId=current()/@modelId][@derivationType!='unchanged' and @derivationType!='annotation'])">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@derivationType='annotation' and count(parent::mif:*//mif:derivationSupplier[@modelId=current()/@modelId][@derivationType!='unchanged' and @derivationType!='annotation'])">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
										ERROR: An element derivation may not be marked as 'annotation' unless all child elements are marked as 'annotation' or 'unchanged'.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="@derivationType='restriction' and count(parent::mif:*//mif:derivationSupplier[@modelId=current()/@modelId][@derivationType!='unchanged' and @derivationType!='annotation' and @derivationType!='restriction'])">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@derivationType='restriction' and count(parent::mif:*//mif:derivationSupplier[@modelId=current()/@modelId][@derivationType!='unchanged' and @derivationType!='annotation' and @derivationType!='restriction'])">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
										ERROR: An element derivation may not be marked as 'restriction' unless all child elements are marked as 'restriction', 'annotation' or 'unchanged'.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="@derivationType='extention' and count(parent::mif:*//mif:derivationSupplier[@modelId=current()/@modelId][@derivationType!='unchanged' and @derivationType!='annotation' and @derivationType!='restriction' and @derivationType!='extention'])">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@derivationType='extention' and count(parent::mif:*//mif:derivationSupplier[@modelId=current()/@modelId][@derivationType!='unchanged' and @derivationType!='annotation' and @derivationType!='restriction' and @derivationType!='extention'])">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
										ERROR: An element derivation may not be marked as 'extention' unless all child elements are marked as 'extention', 'restriction', 'annotation' or 'unchanged'.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M70"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M70"/>
  <xsl:template match="@*|node()" priority="-2" mode="M70">
    <xsl:apply-templates select="*" mode="M70"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:derivationSupplier[@stateName or @stateTransitionName]" priority="1001" mode="M71">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:derivationSupplier[@stateName or @stateTransitionName]"/>
    <!--REPORT -->
    <xsl:if test="count(parent::mif:stateMachine/parent::mif:*/mif:derivationSupplier[@modelId=current()/@modelId])=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(parent::mif:stateMachine/parent::mif:*/mif:derivationSupplier[@modelId=current()/@modelId])=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
										ERROR: An element may only be derived from a model that its parent is also derived from</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M71"/>
  </xsl:template>
  <!--RULE -->
  <xsl:template match="mif:derivationSupplier[@attributeName|@associationName|@className]" priority="1000" mode="M71">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:derivationSupplier[@attributeName|@associationName|@className]"/>
    <!--REPORT -->
    <xsl:if test="count(parent::mif:*/mif:derivationSupplier[@staticModelDerivationId=current()/@staticModelDerivationId])=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(parent::mif:*/mif:derivationSupplier[@staticModelDerivationId=current()/@staticModelDerivationId])=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
										ERROR: An element may only be derived from a model that its parent is also derived from</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M71"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M71"/>
  <xsl:template match="@*|node()" priority="-2" mode="M71">
    <xsl:apply-templates select="*" mode="M71"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M72"/>
  <xsl:template match="@*|node()" priority="-2" mode="M72">
    <xsl:apply-templates select="*" mode="M72"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:ballotInfo" priority="1000" mode="M73">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:ballotInfo"/>
    <xsl:apply-templates select="*" mode="M73"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M73"/>
  <xsl:template match="@*|node()" priority="-2" mode="M73">
    <xsl:apply-templates select="*" mode="M73"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:entryPoint" priority="1000" mode="M74">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:entryPoint"/>
    <!--REPORT -->
    <xsl:if test="parent::mif:staticModel/@serializable='true' and count(mif:annotations)!=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="parent::mif:staticModel/@serializable='true' and count(mif:annotations)!=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Annotations are only permitted on entry points for non-serializable models.  (For serializable models, the entry point and model are 1..1)</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="parent::mif:staticModel/@serializable='true' and count(mif:businessName/@name)!=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="parent::mif:staticModel/@serializable='true' and count(mif:businessName/@name)!=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Names are only permitted on entry points for non-serializable models.  (For serializable models, the entry point and model are 1..1)</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M74"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M74"/>
  <xsl:template match="@*|node()" priority="-2" mode="M74">
    <xsl:apply-templates select="*" mode="M74"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:class|mif:classTypeRef|mif:classStub" priority="1000" mode="M75">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:class|mif:classTypeRef|mif:classStub"/>
    <!--REPORT -->
    <xsl:if test="@sortKey and count(ancestor::mif:staticModel/descendant::mif:*[self::mif:class or self::mif:classTypeRef or self::mif:classStub][@sortKey=current()/@sortKey])!=1">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@sortKey and count(ancestor::mif:staticModel/descendant::mif:*[self::mif:class or self::mif:classTypeRef or self::mif:classStub][@sortKey=current()/@sortKey])!=1">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Sequence number must be unique across all classes in a model.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="not(@sortKey) and count(ancestor::mif:staticModel/descendant::mif:*[self::mif:class or self::mif:classTypeRef or self::mif:classStub][@sortKey])!=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="not(@sortKey) and count(ancestor::mif:staticModel/descendant::mif:*[self::mif:class or self::mif:classTypeRef or self::mif:classStub][@sortKey])!=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Sequence number must present on all classes in a model if it is present in any.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M75"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M75"/>
  <xsl:template match="@*|node()" priority="-2" mode="M75">
    <xsl:apply-templates select="*" mode="M75"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:derivationSupplier[@className]" priority="1000" mode="M76">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:derivationSupplier[@className]"/>
    <!--REPORT -->
    <xsl:if test="@className!=parent::mif:*/@name and count(ancestor::mif:model//mif:derivationSupplier[@className=current()/@className and @modelId=current()/@modelId and not(@attributeName or @stateName or @stateTransitionName)])=1">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@className!=parent::mif:*/@name and count(ancestor::mif:model//mif:derivationSupplier[@className=current()/@className and @modelId=current()/@modelId and not(@attributeName or @stateName or @stateTransitionName)])=1">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
											GUIDELINE: A class derivation must reference the same name as the derived-from class *unless* there are multiple classes derived from the same model class.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M76"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M76"/>
  <xsl:template match="@*|node()" priority="-2" mode="M76">
    <xsl:apply-templates select="*" mode="M76"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M77"/>
  <xsl:template match="@*|node()" priority="-2" mode="M77">
    <xsl:apply-templates select="*" mode="M77"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:class" priority="1000" mode="M78">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:class"/>
    <!--REPORT -->
    <xsl:if test="@abstract='true' and count(mif:*)=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@abstract='true' and count(mif:*)=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Abstract classes must have descendants.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(ancestor::mif:staticModel/mif:subjectArea)!=0 and not(mif:primarySubjectArea)">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(ancestor::mif:staticModel/mif:subjectArea)!=0 and not(mif:primarySubjectArea)">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: PrimarySubjectArea is required if a model contains subjectAreas.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(mif:attribute)=0 and count(mif:associations)=0 and count(mif:*)=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(mif:attribute)=0 and count(mif:associations)=0 and count(mif:*)=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							WARNING: Classes should have at least one attribute, association or descendant.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="contains(@name, '_') and count(mif:*)=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="contains(@name, '_') and count(mif:*)=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							WARNING: Class names should only contain '_' if they have descendants.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M78"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M78"/>
  <xsl:template match="@*|node()" priority="-2" mode="M78">
    <xsl:apply-templates select="*" mode="M78"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:attribute" priority="1000" mode="M79">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:attribute"/>
    <!--REPORT -->
    <xsl:if test="@isMandatory='true' and @conformance!='R'">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@isMandatory='true' and @conformance!='R'">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Conformance must be 'R' for mandatory elements.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="@isMandatory='true' and @minimumMultiplicity=0 and not(@defaultValue)">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@isMandatory='true' and @minimumMultiplicity=0 and not(@defaultValue)">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: MinimumMultiplicity must be at least 1 or there must be a default value when 'mandatory' is true.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="@fixedValue and @defaultValue">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@fixedValue and @defaultValue">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Can't have both a 'fixed' value and a 'default' value for a single element ABC.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="@defaultFrom and not(@defaultValue)">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@defaultFrom and not(@defaultValue)">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Can't have both a 'defaultFrom' value and a 'default' value for a single element.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="@updateModeDefault and not(contains(concat(';', translate(@allowedUpdateModes, ' ', ';'), ';'), concat(';', @updateModeDefault, ';')))">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@updateModeDefault and not(contains(concat(';', translate(@allowedUpdateModes, ' ', ';'), ';'), concat(';', @updateModeDefault, ';')))">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: DefaultUpdateMode must be part of allowedUpdateModes.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M79"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M79"/>
  <xsl:template match="@*|node()" priority="-2" mode="M79">
    <xsl:apply-templates select="*" mode="M79"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:attribute" priority="1000" mode="M80">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:attribute"/>
    <!--REPORT -->
    <xsl:if test="count(following-sibling::mif:associations/mif:association[@name=current()/@name])!=0">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(following-sibling::mif:associations/mif:association[@name=current()/@name])!=0">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
											ERROR: May not have attribute with the same name as a association in a single class.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M80"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M80"/>
  <xsl:template match="@*|node()" priority="-2" mode="M80">
    <xsl:apply-templates select="*" mode="M80"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:attribute" priority="1000" mode="M81">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:attribute"/>
    <!--REPORT -->
    <xsl:if test="mif:derivedFrom[@attributeName!=current()/@attributeName]">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="mif:derivedFrom[@attributeName!=current()/@attributeName]">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
											GUIDELINE: Attribute names must be the same as the names of the attribute being derived from.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M81"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M81"/>
  <xsl:template match="@*|node()" priority="-2" mode="M81">
    <xsl:apply-templates select="*" mode="M81"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:classTypeRef" priority="1000" mode="M82">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:classTypeRef"/>
    <!--REPORT -->
    <xsl:if test="count(ancestor::mif:staticModel//mif:classTypeRef[@typeId=current()/@typeId])!=1">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(ancestor::mif:staticModel//mif:classTypeRef[@typeId=current()/@typeId])!=1">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
										ERROR: Only one classTypeRef may exist in an staticModel with a given typeId.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M82"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M82"/>
  <xsl:template match="@*|node()" priority="-2" mode="M82">
    <xsl:apply-templates select="*" mode="M82"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:classTypeRef" priority="1000" mode="M83">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:classTypeRef"/>
    <!--REPORT -->
    <xsl:if test="count(ancestor::mif:staticModel//mif:classTypeRef[@typeId=current()/@typeId])!=1">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(ancestor::mif:staticModel//mif:classTypeRef[@typeId=current()/@typeId])!=1">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
										ERROR: Only one classTypeRef may exist in an staticModel with a given typeId.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M83"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M83"/>
  <xsl:template match="@*|node()" priority="-2" mode="M83">
    <xsl:apply-templates select="*" mode="M83"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:state" priority="1000" mode="M84">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:state"/>
    <!--REPORT -->
    <xsl:if test="mif:derivedFrom[@stateName!=current()/@name]">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="mif:derivedFrom[@stateName!=current()/@name]">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
														GUIDELINE: State names must be the same as the state names of the state being derived from.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M84"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M84"/>
  <xsl:template match="@*|node()" priority="-2" mode="M84">
    <xsl:apply-templates select="*" mode="M84"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:transition" priority="1000" mode="M85">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:transition"/>
    <!--REPORT -->
    <xsl:if test="mif:derivedFrom[@stateTransitionName!=current()/@name]">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="mif:derivedFrom[@stateTransitionName!=current()/@name]">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
														GUIDELINE: State transition names must be the same as the state names of the state being derived from.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M85"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M85"/>
  <xsl:template match="@*|node()" priority="-2" mode="M85">
    <xsl:apply-templates select="*" mode="M85"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M86"/>
  <xsl:template match="@*|node()" priority="-2" mode="M86">
    <xsl:apply-templates select="*" mode="M86"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M87"/>
  <xsl:template match="@*|node()" priority="-2" mode="M87">
    <xsl:apply-templates select="*" mode="M87"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:association" priority="1000" mode="M88">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:association"/>
    <!--REPORT -->
    <xsl:if test="ancestor::mif:staticModel/@type='RIM' and not(@linkAssociationName)">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="ancestor::mif:staticModel/@type='RIM' and not(@linkAssociationName)">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							GUIDELINE: RIM models must not have blocked associations (linkAssociationName must always be specified).</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="@name=@linkAssociationName">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@name=@linkAssociationName">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: The name of a association and the association linked to must not be the same.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M88"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M88"/>
  <xsl:template match="@*|node()" priority="-2" mode="M88">
    <xsl:apply-templates select="*" mode="M88"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:derivedFrom[@associationName]" priority="1000" mode="M89">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:derivedFrom[@associationName]"/>
    <!--REPORT -->
    <xsl:if test="@associationName!=parent::mif:*/@name and count(ancestor::mif:model//mif:derivedFrom[@className=current()/@className and @associationName=current()/@associationName and @modelId=current()/@modelId])=1">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@associationName!=parent::mif:*/@name and count(ancestor::mif:model//mif:derivedFrom[@className=current()/@className and @associationName=current()/@associationName and @modelId=current()/@modelId])=1">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
											GUIDELINE: An association derivation must reference the same name as the derived-from association *unless* there are multiple classes derived from the same model class.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M89"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M89"/>
  <xsl:template match="@*|node()" priority="-2" mode="M89">
    <xsl:apply-templates select="*" mode="M89"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:derivedFrom[@associationName]" priority="1000" mode="M90">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:derivedFrom[@associationName]"/>
    <!--REPORT -->
    <xsl:if test="@associationName!=parent::mif:*/@name and count(ancestor::mif:model//mif:derivedFrom[@className=current()/@className and @associationName=current()/@associationName and @modelId=current()/@modelId])=1">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@associationName!=parent::mif:*/@name and count(ancestor::mif:model//mif:derivedFrom[@className=current()/@className and @associationName=current()/@associationName and @modelId=current()/@modelId])=1">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
											GUIDELINE: An association derivation must reference the same name as the derived-from association *unless* there are multiple classes derived from the same model class.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M90"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M90"/>
  <xsl:template match="@*|node()" priority="-2" mode="M90">
    <xsl:apply-templates select="*" mode="M90"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="descendant" priority="1000" mode="M91">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="descendant"/>
    <!--REPORT -->
    <xsl:if test="preceding-sibling::descendant[@sortKey = current()/@sortKey]">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="preceding-sibling::descendant[@sortKey = current()/@sortKey]">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Sequence number must be unique across all descendants in a model.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M91"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M91"/>
  <xsl:template match="@*|node()" priority="-2" mode="M91">
    <xsl:apply-templates select="*" mode="M91"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:presentation[@shapeId]" priority="1000" mode="M92">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:presentation[@shapeId]"/>
    <!--REPORT -->
    <xsl:if test="count(ancestor::mif:staticModel//mif:presentation[@shapeId=current()/@shapeId])!=1">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(ancestor::mif:staticModel//mif:presentation[@shapeId=current()/@shapeId])!=1">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
              ERROR: Only one shape within a model can have a given shapeId.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M92"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M92"/>
  <xsl:template match="@*|node()" priority="-2" mode="M92">
    <xsl:apply-templates select="*" mode="M92"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="text()" priority="-1" mode="M93"/>
  <xsl:template match="@*|node()" priority="-2" mode="M93">
    <xsl:apply-templates select="*" mode="M93"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="text()" priority="-1" mode="M94"/>
  <xsl:template match="@*|node()" priority="-2" mode="M94">
    <xsl:apply-templates select="*" mode="M94"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:class|mif:classTypeRef|mif:classStub" priority="1000" mode="M96">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:class|mif:classTypeRef|mif:classStub"/>
    <!--REPORT -->
    <xsl:if test="count(ancestor::mif:staticModel/descendant::mif:*[@name=current()/@name and (self::mif:class or self::mif:classTypeRef or self::mif:classStub)])&gt;1">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(ancestor::mif:staticModel/descendant::mif:*[@name=current()/@name and (self::mif:class or self::mif:classTypeRef or self::mif:classStub)])&gt;1">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Only one class, classTypeRef or classStub may exist with a given name inside an staticModel.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(ancestor::mif:staticModel/descendant::mif:*[mif:businessName/@name=current()/mif:businessName/@name and (self::mif:class or self::mif:classTypeRef or self::mif:classStub)])&gt;1">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(ancestor::mif:staticModel/descendant::mif:*[mif:businessName/@name=current()/mif:businessName/@name and (self::mif:class or self::mif:classTypeRef or self::mif:classStub)])&gt;1">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Only one class, classTypeRef or classStub may exist with a given business name inside an staticModel.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M96"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M96"/>
  <xsl:template match="@*|node()" priority="-2" mode="M96">
    <xsl:apply-templates select="*" mode="M96"/>
  </xsl:template>
  <!--PATTERN -->
  <xsl:template match="text()" priority="-1" mode="M97"/>
  <xsl:template match="@*|node()" priority="-2" mode="M97">
    <xsl:apply-templates select="*" mode="M97"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="mif:class|mif:classTypeRef|mif:classStub" priority="1000" mode="M98">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="mif:class|mif:classTypeRef|mif:classStub"/>
    <!--REPORT -->
    <xsl:if test="count(ancestor::mif:staticModel/descendant::mif:*[@name=current()/@name and (self::mif:class or self::mif:classTypeRef or self::mif:classStub)])&gt;1">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(ancestor::mif:staticModel/descendant::mif:*[@name=current()/@name and (self::mif:class or self::mif:classTypeRef or self::mif:classStub)])&gt;1">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Only one class, classTypeRef or classStub may exist with a given name inside an staticModel.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <!--REPORT -->
    <xsl:if test="count(ancestor::mif:staticModel/descendant::mif:*[mif:businessName/@name=current()/mif:businessName/@name and (self::mif:class or self::mif:classTypeRef or self::mif:classStub)])&gt;1">
      <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(ancestor::mif:staticModel/descendant::mif:*[mif:businessName/@name=current()/mif:businessName/@name and (self::mif:class or self::mif:classTypeRef or self::mif:classStub)])&gt;1">
        <xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-select-full-path"/></xsl:attribute>
        <svrl:text>
							ERROR: Only one class, classTypeRef or classStub may exist with a given business name inside an staticModel.</svrl:text>
      </svrl:successful-report>
    </xsl:if>
    <xsl:apply-templates select="*" mode="M98"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M98"/>
  <xsl:template match="@*|node()" priority="-2" mode="M98">
    <xsl:apply-templates select="*" mode="M98"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="text()" priority="-1" mode="M99"/>
  <xsl:template match="@*|node()" priority="-2" mode="M99">
    <xsl:apply-templates select="*" mode="M99"/>
  </xsl:template>
  <!--PATTERN -->
  <!--RULE -->
  <xsl:template match="text()" priority="-1" mode="M100"/>
  <xsl:template match="@*|node()" priority="-2" mode="M100">
    <xsl:apply-templates select="*" mode="M100"/>
  </xsl:template>
</xsl:stylesheet>
