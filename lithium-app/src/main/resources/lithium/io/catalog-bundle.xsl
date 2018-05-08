<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet 
    version="1.0" 
    xmlns="http://www.w3c.org/1999/xhtml" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:cat="urn:opwviewer:catalog">

  <xsl:template match="/cat:group">
    <html>
      <head>
        <title>
          <xsl:value-of select="@name" />
        </title>
      </head>
      <body>
        <h1>
          <xsl:value-of select="@name" />
        </h1>
        <p>
          <a href="../index.html">Terug naar inhoudsopgave</a>
        </p>
        <ul>
          <xsl:apply-templates>
            <xsl:sort select="@number" data-type="number" />
          </xsl:apply-templates>
        </ul>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template match="cat:lyric[@title]">
    <li>
      <xsl:element name="a">
        <xsl:attribute name="href"><xsl:value-of select="@number" />.html</xsl:attribute>
        <xsl:value-of select="@number" />.
        <xsl:value-of select="@title" />
      </xsl:element>
    </li>
  </xsl:template>
</xsl:stylesheet>
