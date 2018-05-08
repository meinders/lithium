<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet 
    version="1.0" 
    xmlns="http://www.w3c.org/1999/xhtml" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:cat="urn:opwviewer:catalog">

  <xsl:template match="/cat:lyric">
    <html>
      <head>
        <title>
          <xsl:value-of select="@number" />.
          <xsl:value-of select="@title" />
        </title>
      </head>
      <body>
        <h1>
          <xsl:value-of select="@number" />.
          <xsl:value-of select="@title" />
        </h1>
        <p>
          <a href="index.html">Terug naar bundel</a>
        </p>
        <xsl:call-template name="plainToHTML">
          <xsl:with-param name="plain" select="cat:text" />
        </xsl:call-template>
        <font size="-1">
          <xsl:call-template name="plainToHTML">
            <xsl:with-param name="plain" select="cat:copyrights" />
          </xsl:call-template>
        </font>
      </body>
    </html>
  </xsl:template>

  <xsl:template name="plainToHTML">
    <xsl:param name="plain" />
    <xsl:call-template name="paragraphsToHTML">
      <xsl:with-param name="plain" select="$plain" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="paragraphsToHTML">
    <xsl:param name="plain" />
    <p>
      <xsl:choose>
        <xsl:when test="contains($plain, '&#10;&#10;')">
          <xsl:call-template name="linesToHTML">
            <xsl:with-param name="plain" select="substring-before($plain, '&#10;&#10;')" />
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="linesToHTML">
            <xsl:with-param name="plain" select="$plain" />
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </p>
    <xsl:if test="contains($plain, '&#10;&#10;')">
      <xsl:call-template name="paragraphsToHTML">
        <xsl:with-param name="plain" select="substring-after($plain, '&#10;&#10;')" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="linesToHTML">
    <xsl:param name="plain" />
    <xsl:choose>
      <xsl:when test="contains($plain, '&#10;')">
        <xsl:call-template name="lineToHTML">
          <xsl:with-param name="plain" select="substring-before($plain, '&#10;')" />
        </xsl:call-template>
        <br />
        <xsl:call-template name="linesToHTML">
          <xsl:with-param name="plain" select="substring-after($plain, '&#10;')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="lineToHTML">
          <xsl:with-param name="plain" select="$plain" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="lineToHTML">
    <xsl:param name="plain" />
    <xsl:choose>
      <xsl:when test="contains($plain, '&#9;')">
        <xsl:value-of select="normalize-space(substring-before($plain, '&#9;'))" />
        /
        <xsl:call-template name="lineToHTML">
          <xsl:with-param name="plain" select="substring-after($plain, '&#9;')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="normalize-space($plain)" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
