<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="urn:hl7-org:v3/mif2" xpath-default-namespace="urn:hl7-org:v3/mif2">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
	</xsl:template>
	<xsl:template match="vocabularyModel">
    <xsl:copy>
      <xsl:apply-templates select="@*|packageLocation|header"/>
      <xsl:for-each select="conceptDomain">
        <xsl:sort select="@name"/>
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:apply-templates select="codeSystem|valueSet"/>
    </xsl:copy>
	</xsl:template>
	<xsl:template match="releasedVersion">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()[not(self::concept)]"/>
      <xsl:for-each select="concept">
        <xsl:sort select="code[1]/@code"/>
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </xsl:copy>
	</xsl:template>
	<xsl:template match="annotations/documentation/*/text">
    <xsl:copy>
      <p>
        <xsl:value-of select="normalize-space(string-join(descendant::text(), ' '))"/>
      </p>
    </xsl:copy>
	</xsl:template>
	<xsl:template match="conceptDomain/annotations/appInfo|conceptDomain/historyItem"/>
	<xsl:template match="@publisherVersionId"/>
	<xsl:template match="supportedConceptProperty/@defaultValue|supportedConceptProperty/@defaultHandlingCode"/>
	<xsl:template match="concept/annotations/appInfo[not(openIssue)]"/>
	<xsl:template match="concept/annotations/appInfo/deprecationInfo"/>
</xsl:stylesheet>
