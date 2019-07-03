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
        <xsl:sort select="conceptProperty[@name='internalId']/@value"/>
<!--        <xsl:sort select="code[1]/@code"/>-->
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="concept">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:if test="not(annotations)">
        <annotations>
          <documentation>
            <definition>
              <text>
                <p>text</p>
              </text>
            </definition>
          </documentation>
        </annotations>
      </xsl:if>
      <xsl:apply-templates select="node()[not(self::printName or self::code)]"/>
      <xsl:for-each select="printName">
        <xsl:sort select="@text"/>
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="code">
        <xsl:sort select="@code"/>
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </xsl:copy>
  </xsl:template>
  <!-- Todo: remove these filters -->
  <xsl:template match="annotations/*/*/text">
    <xsl:copy>
      <p>
        <xsl:text>text</xsl:text>
        <!--        <xsl:value-of select="normalize-space(string-join(descendant::text(), ' '))"/>-->
      </p>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="conceptDomain/annotations/appInfo|conceptDomain/historyItem"/>
  <xsl:template match="@publisherVersionId"/>
  <xsl:template match="conceptProperty[@name='status' and @value='retired']"/>
<!--  <xsl:template match="codeSystem[releasedVersion/@hl7MaintainedIndicator='false']"/>
  <xsl:template match="valueSet[not(descendant::valueSetRef)]"/>-->
  <xsl:template priority="5" match="comment()"/>
  <xsl:template match="@includeHeadCode|valueSetRef/@name|content[valueSetRef]/@codeSystem|unionWithContent[valueSetRef]/@codeSystem|content[combinedContent]/@codeSystem"/>
<!--  <xsl:template match="@includeHeadCode|valueSetRef/@name|unionWithContent[valueSetRef]/@codeSystem|content[combinedContent]/@codeSystem"/>-->
</xsl:stylesheet>
