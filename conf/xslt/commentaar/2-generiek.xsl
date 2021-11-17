<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:cwc="urn:cwc"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:prism="http://prismstandard.org/namespaces/basic/2.0"
                xmlns:sdu="urn:sdu-nl:legal:metadata-1_0"
                xmlns:pcv="http://prismstandard.org/namespaces/1.2/pcv/"
                version="2.0" exclude-result-prefixes="rdf cwc prism sdu pcv dc">

    <xsl:output method="xml" encoding="UTF-8"/>
    <xsl:strip-space elements="*"/>

    <xsl:variable name="swspath" select="concat('http://sws.sdu.nl/',//metadata/identifier,'/')"/>

    <xsl:key name="footnote" match="div[@class = 'sws-note']" use="@id"/>

    <!-- identity transform -->

    <xsl:template match="*">
        <xsl:element name="{local-name()}">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@*">
        <xsl:attribute name="{local-name()}"><xsl:value-of select="."/></xsl:attribute>
    </xsl:template>

    <!-- sws-non-hierchical-head or tussenkop -->
    <xsl:template match="p[@class='sws-non-hierarchical-head']">
        <p class="Tussenkop_Vet">
            <xsl:apply-templates/>
        </p>
    </xsl:template>

    <xsl:template match="p[@class='sws-non-hierarchical-head']/b">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="p[@class='tussenkop']">
        <p class="Tussenkop_Vet">
            <xsl:apply-templates/>
        </p>
    </xsl:template>

    <xsl:template match="p[@class='tussenkop']/b">
        <xsl:apply-templates/>
    </xsl:template>

    <!-- footnotes -->

    <xsl:template match="a[@class='sws-note-ref']">
        <xsl:variable name="fnid">
            <xsl:value-of select="@data-sws-note-id"/>
        </xsl:variable>
        <xsl:apply-templates select="key('footnote', $fnid)"/>
    </xsl:template>

    <xsl:template match="div[@class='sws-note']">
        <footnote>
            <xsl:apply-templates select="node()"/>
        </footnote>
    </xsl:template>

    <xsl:template match="div[@class='sws-note']/div[p]">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="div[@class='sws-note']//p">
        <p class="footnote_text">
            <xsl:for-each select="node()">
                <xsl:choose>
                    <xsl:when test="self::p">
                        <br/>
                        <xsl:apply-templates/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="."/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </p>
    </xsl:template>

    <xsl:template match="document[@id='commentaar']//*[self::h1|self::h2|self::h3|self::h4|self::h5|self::h6|self::h7|self::h8|self::h9|self::h10|self::h11|self::h12]/span[@class='nr']">
        <span class="Paragraafnummer">
            <xsl:apply-templates />
        </span>
    </xsl:template>

    <xsl:template match="div[@class='sws-note']//span[@class='nr']"/>

    <xsl:template match="section[@class='sws-notes']"/>

    <!-- aside\kantnoten -->

    <xsl:template match="asides[following-sibling::*[1][self::p|self::ol|self::ul|self::div[@class='sws-custom-list']|self::blockquote]]"/>

    <xsl:template match="p[preceding-sibling::*[1][self::asides]]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates select="preceding-sibling::*[1][self::asides]/aside"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="asides/aside">
        <span class="Margetekst"><xsl:apply-templates select="node()"/></span>
    </xsl:template>

    <!-- lists -->

    <xsl:template match="li">
        <li>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </li>
    </xsl:template>

    <xsl:template match="li/p">
        <p class="List_Paragraph">
            <xsl:if test="ancestor::*[self::ul|self::ol|self::div[@class='sws-custom-list']][1][preceding-sibling::*[1][self::asides]] and count(parent::li[preceding-sibling::li]) = 0 and count(preceding-sibling::p) = 0">
                <xsl:apply-templates select="ancestor::*[self::ul|self::ol|self::div[@class='sws-custom-list']][1]/preceding-sibling::*[1][self::asides]/aside"/>
            </xsl:if>
            <xsl:apply-templates/>
        </p>
    </xsl:template>

    <xsl:template match="ul">
        <list type="bullet" format="">
            <xsl:apply-templates/>
        </list>
    </xsl:template>

    <xsl:template match="ol[@type='1' or @class='arabic']">
        <list type="number" format="%{count(ancestor::*[ol|ul|self::div[@class='sws-custom-list']])}.">
            <xsl:apply-templates/>
        </list>
    </xsl:template>

    <xsl:template match="ol[@type=('a') or @class='loweralpha']">
        <list type="letter" format="%{count(ancestor::*[ol|ul|self::div[@class='sws-custom-list']])}.">
            <xsl:apply-templates/>
        </list>
    </xsl:template>

    <xsl:template match="ol[@type=('i') or @class='lowerroman']">
        <list type="lower-roman" format="%{count(ancestor::*[ol|ul|self::div[@class='sws-custom-list']])}.">
            <xsl:apply-templates/>
        </list>
    </xsl:template>

    <xsl:template match="ol[@type='I' or @class='upperroman']">
        <list type="upper-roman" format="%{count(ancestor::*[ol|ul|self::div[@class='sws-custom-list']])}.">
            <xsl:apply-templates/>
        </list>
    </xsl:template>

    <xsl:template match="ol[@type='A' or @class='upperalpha']">
        <list type="upper-alpha" format="%{count(ancestor::*[ol|ul|self::div[@class='sws-custom-list']])}.">
            <xsl:apply-templates/>
        </list>
    </xsl:template>

    <xsl:template match="div[@class='sws-custom-list']">
        <list type="free">
            <xsl:apply-templates/>
        </list>
    </xsl:template>

    <xsl:template match="div[@class='sws-custom-list-item']">
        <li>
            <xsl:apply-templates select="node()"/>
        </li>
    </xsl:template>

    <xsl:template match="div[@class='sws-custom-list-content']">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="div[@class='sws-custom-list-nr']">
        <num><xsl:apply-templates/></num>
    </xsl:template>

    <xsl:template match="div[@class='sws-custom-list-content']/p">
        <p class="List_Paragraph">
            <xsl:if test="ancestor::*[self::ul|self::ol][1][preceding-sibling::*[1][self::asides]] and count(parent::li[preceding-sibling::li]) = 0 and count(preceding-sibling::p) = 0">
                <xsl:apply-templates select="ancestor::*[self::ul|self::ol|self::div[@class='sws-custom-list']][1]/preceding-sibling::*[1][self::asides]/aside"/>
            </xsl:if>
            <xsl:apply-templates/>
        </p>
    </xsl:template>

    <!-- links -->

    <xsl:template match="a[@class = 'asset']">
        <xsl:variable name="filename">
            <xsl:choose>
                <xsl:when test="contains(@href,$swspath)">
                    <xsl:value-of select="substring-before(substring-after(@href,$swspath) ,concat('.',substring-after(.,'.')))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="substring-before(@href ,concat('.',substring-after(.,'.')))"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <a href="{$filename}">
            <span class="Hyperlink"><xsl:apply-templates/></span></a>
    </xsl:template>

    <xsl:template match="a">
        <a>
            <xsl:apply-templates select="@href"/>
            <span class="Hyperlink"><xsl:apply-templates/></span>
        </a>
    </xsl:template>

    <xsl:template match="a[starts-with(@data-sws-link-id,'ECLI')]">
        <a href="{concat('http://sws.sdu.nl/?altKey=',@data-sws-link-id)}">
            <span class="Hyperlink"><xsl:apply-templates/></span>
        </a>
    </xsl:template>

    <xsl:template match="a[starts-with(@data-sws-link-type,'cao')]">
        <xsl:variable name="linkref">
            <xsl:value-of select="concat('http://sws.sdu.nl/?type=',@data-sws-link-type)"/>
            <xsl:if test="@data-sws-link-title">
                <xsl:value-of select="concat('&amp;title=',@data-sws-link-title)"/>
            </xsl:if>
        </xsl:variable>
        <a href="{$linkref}"><span class="Hyperlink"><xsl:apply-templates/></span></a>
    </xsl:template>

    <xsl:template match="a[@data-sws-link-altKey]">
        <xsl:variable name="linkref">
            <xsl:value-of select="concat('http://sws.sdu.nl/?altKey=',@data-sws-link-altKey)"/>
            <xsl:if test="@data-sws-part-article">
                <xsl:value-of select="concat('&amp;about.part.article=',@data-sws-part-article)"/>
            </xsl:if>
            <xsl:if test="@data-sws-link-valid-date">
                <xsl:value-of select="concat('&amp;valid.date=',@data-sws-link-valid-date)"/>
            </xsl:if>
        </xsl:variable>
        <a href="{$linkref}"><span class="Hyperlink"><xsl:apply-templates/></span></a>
    </xsl:template>

    <xsl:template match="a[@data-sws-link-key]">
        <a href="{concat('http://sws.sdu.nl/?identifier=',@data-sws-link-key)}">
            <span class="Hyperlink"><xsl:apply-templates/></span>
        </a>
    </xsl:template>

    <xsl:template match="a[@data-sws-link-type='law']">
        <xsl:variable name="linkref">
            <xsl:if test="@data-sws-link-altKey">
                <xsl:value-of select="concat('http://sws.sdu.nl/?altKey=',@data-sws-link-altKey)"/>
            </xsl:if>
            <xsl:if test="not(@data-sws-link-altKey) and @data-sws-link-id">
                <xsl:value-of select="concat('http://sws.sdu.nl/?identifier=',@data-sws-link-id)"/>
            </xsl:if>
            <xsl:if test="@data-sws-part-article">
                <xsl:value-of select="concat('&amp;about.part.article=',@data-sws-part-article)"/>
            </xsl:if>
        </xsl:variable>
        <a href="{$linkref}"><span class="Hyperlink"><xsl:apply-templates/></span></a>
    </xsl:template>

    <xsl:template match="a[@data-sws-link-type='op'][@data-sws-link-id]">
        <xsl:variable name="linkref">
            <xsl:value-of select="concat('http://sws.sdu.nl/?identifier=',@data-sws-link-id)"/>
        </xsl:variable>
        <a href="{$linkref}"><span class="Hyperlink"><xsl:apply-templates/></span></a>
    </xsl:template>

    <xsl:template match="a[@data-sws-link-type='eur'][@data-sws-link-id]">
        <xsl:variable name="linkref">
            <xsl:value-of select="concat('http://sws.sdu.nl/?identifier=',@data-sws-link-id)"/>
        </xsl:variable>
        <a href="{$linkref}"><span class="Hyperlink"><xsl:apply-templates/></span></a>
    </xsl:template>

    <xsl:template match="a[@data-sws-link-type='cmt'][@data-sws-link-id]">
        <xsl:variable name="linkref">
            <xsl:value-of select="concat('http://sws.sdu.nl/?identifier=',@data-sws-link-id)"/>
        </xsl:variable>
        <a href="{$linkref}"><span class="Hyperlink"><xsl:apply-templates/></span></a>
    </xsl:template>

    <xsl:template match="a[@data-sws-link-publicationName]">
        <xsl:variable name="href">
            <xsl:value-of select="concat('http://sws.sdu.nl/?published.publicationName=',@data-sws-link-publicationName)"/>
            <xsl:if test="@data-sws-link-publicationYear">
                <xsl:value-of select="concat('&amp;published.publicationYear=',@data-sws-link-publicationYear)"/>
            </xsl:if>
            <xsl:if test="@data-sws-link-publicationNumber">
                <xsl:value-of select="concat('&amp;published.publicationNumber=',@data-sws-link-publicationNumber)"/>
            </xsl:if>
        </xsl:variable>
        <a href="{$href}"><span class="Hyperlink"><xsl:apply-templates/></span></a>
    </xsl:template>

    <xsl:template match="a[@class='sws-link-unresolved']">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="img">
        <xsl:variable name="filename">
            <xsl:choose>
                <xsl:when test="contains(@src,$swspath)">
                    <xsl:value-of select="substring-before(substring-after(@src,$swspath) ,concat('.',substring-after(.,'.')))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="substring-before(@src ,concat('.',substring-after(.,'.')))"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <img href="{$filename}"/>
    </xsl:template>

    <!-- inline images -->
    <xsl:template match="p/figure[@class='sws-image']">
        <xsl:apply-templates/>
    </xsl:template>

    <!-- not inline images? -->
    <xsl:template match="*[not(self::p)]/figure[@class='sws-image']">
        <p><xsl:apply-templates/></p>
    </xsl:template>

    <xsl:template match="figcaption">
        <p class="caption"><xsl:apply-templates select="node()" mode="figcaption"/></p>
    </xsl:template>

    <xsl:template match="text()[1][starts-with(.,'Figuur ')]" mode="figcaption">
        <xsl:call-template name="AddField">
            <xsl:with-param name="CaptionType">Figuur </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="text()[1][starts-with(.,'Grafiek ')]" mode="figcaption">
        <xsl:call-template name="AddField">
            <xsl:with-param name="CaptionType">Grafiek </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="text()[1][starts-with(.,'Tabel ')]" mode="figcaption">
        <xsl:call-template name="AddField">
            <xsl:with-param name="CaptionType">Tabel </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="AddField">
        <xsl:param name="CaptionType"/>
        <xsl:variable name="Caption" select="substring-after(.,$CaptionType)"/>
        <xsl:variable name="CaptionNr" select="substring-before($Caption,' ')"/>
        <xsl:variable name="CaptionTitle" select="substring-after($Caption, concat($CaptionNr, ' '))"/>
        <xsl:choose>
            <xsl:when test="$CaptionNr = '' and translate($Caption, '1234567890', '') = ''">
                <xsl:value-of select="$CaptionType"/>
                <field code=" SEQ {$CaptionType}\* ARABIC ">
                    <xsl:value-of select="$Caption"/>
                </field>
            </xsl:when>
            <xsl:when test="number($CaptionNr) and translate($CaptionNr, '1234567890', '') = ''">
                <xsl:value-of select="$CaptionType"/>
                <field code=" SEQ {$CaptionType}\* ARABIC ">
                    <xsl:value-of select="$CaptionNr"/>
                </field>
                <xsl:value-of select="$CaptionTitle"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="."/>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <!-- inline elements -->

    <xsl:template match="em[@class='smallcaps']">
        <xsl:element name="{@class}"><xsl:apply-templates/></xsl:element>
    </xsl:template>

    <xsl:template match="em[@class= 'underline']|u">
        <underline><xsl:apply-templates/></underline>
    </xsl:template>

    <xsl:template match="em[@class= 'bold italic']">
        <bold><italic><xsl:apply-templates/></italic></bold>
    </xsl:template>

    <xsl:template match="em[@class= 'bold']">
        <bold><xsl:apply-templates/></bold>
    </xsl:template>

    <xsl:template match="i|em[@class='italic']"><italic><xsl:apply-templates/></italic></xsl:template>

    <xsl:template match="b"><bold><xsl:apply-templates/></bold></xsl:template>

    <xsl:template match="s"><strikethrough><xsl:apply-templates/></strikethrough></xsl:template>

    <!-- tables -->

    <xsl:template match="figure[@class='sws-table']">
        <xsl:apply-templates select="table"/>
        <xsl:apply-templates select="figcaption"/>
    </xsl:template>

    <xsl:template match="table">
        <table>
            <xsl:if test="contains(@class, 'sws-cals-colsep-yes')">
                <xsl:attribute name="colsep">1</xsl:attribute>
            </xsl:if>
            <xsl:if test="contains(@class, 'sws-cals-rowsep-yes')">
                <xsl:attribute name="rowsep">1</xsl:attribute>
            </xsl:if>
            <xsl:if test="contains(@class, 'sws-cals-frame-')">
                <xsl:attribute name="frame">
                    <xsl:choose>
                        <xsl:when test="contains(substring-after(@class,'sws-cals-frame-'), ' ')">
                            <xsl:value-of select="substring-before(substring-after(@class,'sws-cals-frame-'),' ')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="substring-after(@class,'sws-cals-frame-')"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="contains(@class, 'sws-cals-align-')">
                <xsl:attribute name="align">
                    <xsl:choose>
                        <xsl:when test="contains(substring-after(@class,'sws-cals-align-'), ' ')">
                            <xsl:value-of select="substring-before(substring-after(@class,'sws-cals-align-'),' ')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="substring-after(@class,'sws-cals-align-')"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </xsl:if>
            <xsl:attribute name="class">Table_Grid</xsl:attribute>
            <xsl:attribute name="width">100%</xsl:attribute>
            <tgroup>
                <xsl:apply-templates select="tgroup/thead|thead"/>
                <xsl:apply-templates select="tgroup/tbody|tbody"/>
            </tgroup>
        </table>
    </xsl:template>

    <xsl:template match="td|th">
        <entry>
            <xsl:if test="@morerows">
                <xsl:attribute name="rowspan"><xsl:value-of select="number(@morerows) + 1"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="@rowspan">
                <xsl:attribute name="rowspan"><xsl:value-of select="@rowspan"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="@colspan">
                <xsl:attribute name="colspan"><xsl:value-of select="@colspan"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="contains(@class, 'sws-cals-colsep-yes')">
                <xsl:attribute name="colsep">1</xsl:attribute>
            </xsl:if>
            <xsl:if test="contains(@class, 'sws-cals-colsep-no')">
                <xsl:attribute name="colsep">0</xsl:attribute>
            </xsl:if>
            <xsl:if test="contains(@class, 'sws-cals-rowsep-yes')">
                <xsl:attribute name="rowsep">1</xsl:attribute>
            </xsl:if>
            <xsl:if test="contains(@class, 'sws-cals-rowsep-no')">
                <xsl:attribute name="rowsep">0</xsl:attribute>
            </xsl:if>
            <xsl:if test="contains(@class, 'sws-cals-valign-')">
                <xsl:attribute name="valign">
                    <xsl:choose>
                        <xsl:when test="contains(substring-after(@class,'sws-cals-valign-'), ' ')">
                            <xsl:value-of select="translate(substring-before(substring-after(@class,'sws-cals-valign-'),' '),';','')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="translate(substring-after(@class,'sws-cals-valign-'),';','')"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="contains(@class, 'sws-cals-align-')">
                <xsl:attribute name="align">
                    <xsl:choose>
                        <xsl:when test="contains(substring-after(@class,'sws-cals-align-'), ' ')">
                            <xsl:value-of select="translate(substring-before(substring-after(@class,'sws-cals-align-'),' '),';','')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="translate(substring-after(@class,'sws-cals-align-'),';','')"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </xsl:if>
            <xsl:apply-templates/>
        </entry>
    </xsl:template>

    <xsl:template match="tr">
        <row><xsl:apply-templates/></row>
    </xsl:template>

    <xsl:template match="thead">
        <thead><xsl:apply-templates/></thead>
    </xsl:template>

    <xsl:template match="tbody">
        <tbody><xsl:apply-templates/></tbody>
    </xsl:template>

    <xsl:template match="blockquote">
        <p class="Quote">
            <xsl:if test="preceding-sibling::*[1][self::asides]">
                <xsl:apply-templates select="preceding-sibling::*[1][self::asides]/aside"/>
            </xsl:if>
            <xsl:apply-templates select="node()"/>
        </p>
    </xsl:template>

    <xsl:template match="span[@data-side-note]">
        <span class="Intense_Emphasis"><xsl:value-of select="@data-side-note"/></span>
    </xsl:template>

</xsl:stylesheet>
