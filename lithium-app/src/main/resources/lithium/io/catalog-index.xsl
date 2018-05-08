<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet 
    version="1.0" 
    xmlns="http://www.w3c.org/1999/xhtml" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:cat="urn:opwviewer:catalog">

  <xsl:template match="/">
    <html>
      <head>
        <title>Inhoud</title>
      </head>
      <body>
        <h1>Inhoud</h1>
        <ul>
          <xsl:apply-templates />
        </ul>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="cat:group[cat:lyric/@title]">
    <li>
      <xsl:element name="a">
        <xsl:attribute name="href"><xsl:value-of select="@name" />/index.html</xsl:attribute>
        <xsl:value-of select="@name" />
      </xsl:element>
    </li>
  </xsl:template>
</xsl:stylesheet>
