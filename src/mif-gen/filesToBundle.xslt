<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:f="http://hl7.org/fhir">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/files">
    <Bundle xmlns="http://hl7.org/fhir">
      <type value="collection"/>
      <xsl:for-each select="*">
        <entry>
          <fullUrl value="http://terminology.hl7.org/{local-name(.)}/{*:id/@value}"/>
          <resource>
            <xsl:apply-templates select="."/>
          </resource>
        </entry>
      </xsl:for-each>
    </Bundle>
	</xsl:template>
	<xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
	</xsl:template>
	<xsl:template match="f:concept/f:property[not(*[starts-with(local-name(.), 'value')])]"/>
	<xsl:template match="@url[.='version annotation']">
    <xsl:attribute name="url" select="'annotation'"/>
	</xsl:template>
	<xsl:template match="f:id/@value[string-length(.)&gt;64]">
    <xsl:attribute name="value" select="substring(., 64)"/>
	</xsl:template>
</xsl:stylesheet>
