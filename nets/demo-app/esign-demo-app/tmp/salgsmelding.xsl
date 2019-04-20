<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" encoding="ISO-8859-1" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" indent="yes"/>
    <xsl:template match="/">
        <html>
            <head>
            </head>
            <body>
                <h2>Salgsmelding</h2>
                <p>Tidligere eier: <xsl:value-of select="dokument/salgsmelding/eier"/>.
                    Ny eier: <xsl:value-of select="dokument/salgsmelding/kjoper"/>.
                </p>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>