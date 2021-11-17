<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:atom="http://www.w3.org/2005/Atom"
                xmlns:cwc="urn:cwc"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:prism="http://prismstandard.org/namespaces/basic/2.0"
                xmlns:sdu="urn:sdu-nl:legal:metadata-1_0"
                xmlns:pcv="http://prismstandard.org/namespaces/1.2/pcv/"
                version="2.0" exclude-result-prefixes="rdf cwc prism sdu pcv dc atom">

    <xsl:output method="xml" encoding="UTF-8"/>

    <xsl:strip-space elements="section li tr ul ol"/>

    <xsl:variable name="upper-case">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
    <xsl:variable name="lower-case">abcdefghijklmnopqrstuvwxyz</xsl:variable>

    <xsl:variable name="DocTypes">
        <xsl:text>;</xsl:text>
        <xsl:for-each select="//metadata/type">
            <xsl:value-of select="concat(.,';')"/>
        </xsl:for-each>
    </xsl:variable>

    <!-- identity transform -->
    <xsl:template match="*">
        <xsl:element name="{local-name()}">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@*">
        <xsl:attribute name="{local-name()}"><xsl:value-of select="."/></xsl:attribute>
    </xsl:template>

    <!-- logfile true/false-->
    <xsl:variable name="log">false</xsl:variable>

    <xsl:template match="document[.//metadata/source[.='jbs']]">
        <folder documentTypeName="Jurisprudentie" document-type-key="jurisprudentie">
            <naam><xsl:value-of select=".//metadata/title"/></naam>
            <meta/>
            <document id="jbs_jur"><xsl:apply-templates/></document>
            <xsl:if test=".//div[@id='annotatie']">
                <document id="annotatie">
                    <metadata>
                        <title><xsl:value-of select="concat('Noot bij ',.//metadata/title)"/></title>
                        <xsl:copy-of select=".//metadata/creator"/>
                        <xsl:copy-of select=".//metadata/documentDate"/>
                        <type>annotatie</type>
                    </metadata>
                    <html>
                        <xsl:apply-templates select="html/head"/>
                        <body><xsl:apply-templates select=".//div[@id='annotatie']/*"/></body></html>
                </document>
                <xsl:for-each select=".//metadata/creator[role='annotator']">
                    <document documentTypeName="Auteursbeschrijvingen" document-type-key="auteursbeschrijvingen">
                        <naam><xsl:value-of select="name"/></naam>
                        <meta>
                            <role><item key="annotator">Annotator</item></role>
                            <publication key="online">Online</publication>
                            <documentKey/>
                        </meta>
                        <document track-changes="false" version="1">
                            <section orientation="portrait">
                                <p class="Auteur"><xsl:value-of select="name"/></p>
                                <xsl:if test="function">
                                    <p class="Auteurinfo"><xsl:value-of select="function"/></p>
                                </xsl:if>
                            </section>
                        </document>
                    </document>
                </xsl:for-each>
            </xsl:if>
        </folder>
    </xsl:template>

    <xsl:template match="div[@id='annotatie']|section[@class='sws-annotation']"/>

    <xsl:template match="document[not(.//metadata/source[.='jbs'])]">
        <xsl:variable name="docType">
            <xsl:choose>
                <xsl:when test="contains($DocTypes,';pn;')">pn</xsl:when>
                <xsl:when test="contains($DocTypes,';nieuws;')">nieuws</xsl:when>
                <xsl:when test="contains($DocTypes,';tools.')">tools</xsl:when>
                <xsl:when test="contains($DocTypes,';jur.publ')">p1jur</xsl:when>
                <xsl:when test="contains($DocTypes,';jur.bron')">jurisprudentie</xsl:when>
                <xsl:when test="contains($DocTypes,';cmt.')">commentaar</xsl:when>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$docType='nieuws'">
                <folder documentTypeName="Nieuws" document-type-key="nieuws">
                    <naam><xsl:value-of select=".//metadata/title"/></naam>
                    <meta/>
                    <document id="{$docType}"><xsl:apply-templates/></document>
                </folder>
            </xsl:when>
            <xsl:when test="$docType='p1jur'">
                <folder documentTypeName="Jurisprudentie" document-type-key="jurisprudentie">
                    <naam><xsl:value-of select=".//metadata/title"/></naam>
                    <meta/>
                    <document id="p1jur"><xsl:apply-templates/></document>
                    <xsl:if test=".//section[@class='sws-annotation']">
                        <xsl:for-each select="//section[@class='sws-annotation']">
                            <document id="annotatie">
                                <metadata>
                                    <title><xsl:value-of select="concat('Noot bij ',ancestor::document//metadata/title)"/></title>
                                    <xsl:copy-of select="ancestor::document//metadata/creator"/>
                                    <xsl:copy-of select="ancestor::document//metadata/documentDate"/>
                                    <type>annotatie</type>
                                </metadata>
                                <html>
                                    <xsl:apply-templates select="ancestor::document//html/head"/>
                                    <body><xsl:apply-templates select="./*"/></body></html>
                            </document>
                            <xsl:for-each select=".//div[@class='Auteur']">
                                <document documentTypeName="Auteursbeschrijvingen" document-type-key="auteursbeschrijvingen">
                                    <naam><xsl:value-of select="p[@class='Auteur']"/></naam>
                                    <meta>
                                        <role><item key="annotator">Annotator</item></role>
                                        <publication key="online">Online</publication>
                                        <documentKey/>
                                    </meta>
                                    <document track-changes="false" version="1">
                                        <section orientation="portrait">
                                            <xsl:apply-templates select="./*" mode="auteursbeschrijving"/>
                                        </section>
                                    </document>
                                </document>
                            </xsl:for-each>
                        </xsl:for-each>
                    </xsl:if>
                </folder>
            </xsl:when>
            <xsl:otherwise>
                <document id="{$docType}"><xsl:apply-templates/></document>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="div[@class='Auteur']//*" mode="auteursbeschrijving">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="div[@class='Auteur']//p[@class='sws-author-affiliates']" mode="auteursbeschrijving">
        <p class="Auteurinfo">
            <xsl:apply-templates select="node()"/>
        </p>
    </xsl:template>

    <xsl:template match="section[@class='sws-annotation']//div[@class='Auteur']"/>

    <!-- clean footnotes not publishone bronnen-->

    <xsl:template match="a[starts-with(@href,'#_')][ancestor::document/metadata/type[. = 'jur.bron']]">
        <xsl:copy>
            <xsl:attribute name="class">sws-note-ref</xsl:attribute>
            <xsl:attribute name="href"><xsl:value-of select="@href"/></xsl:attribute>
            <xsl:attribute name="id"><xsl:value-of select="@href"/></xsl:attribute>
            <xsl:attribute name="data-sws-note-id"><xsl:value-of select="substring-after(@href,'#')"/></xsl:attribute>
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="a[@class='sws-note-ref'][ancestor::document/metadata/source[. = 'jbs'] or ancestor::document/metadata/type[contains(.,'cmt.')]]">
        <xsl:copy>
            <xsl:attribute name="class">sws-note-ref</xsl:attribute>
            <xsl:attribute name="href"><xsl:value-of select="@href"/></xsl:attribute>
            <xsl:attribute name="id"><xsl:value-of select="@href"/></xsl:attribute>
            <xsl:attribute name="data-sws-note-id"><xsl:value-of select="substring-after(@href,'#')"/></xsl:attribute>
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="div[@class='sws-notes'][ancestor::document/metadata/type[. = 'jur.bron' or contains(.,'cmt.')] or ancestor::document/metadata/source[. = 'jbs']]">
        <section class="sws-notes">
            <xsl:apply-templates select="div"/>
        </section>
    </xsl:template>

    <xsl:template match="div[@class='sws-note'][ancestor::document/metadata/type[. = 'jur.bron' or contains(.,'cmt.')] or ancestor::document/metadata/source[. = 'jbs']]">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:if test="not(@id)">
                <xsl:attribute name="id">
                    <xsl:value-of select="span[@class='sws-note-nr sws-generated']/a/@id"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:choose>
                <xsl:when test="not(p)">
                    <p><xsl:apply-templates /></p>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="div[@class='sws-note'][ancestor::document/metadata/type[. = 'jur.bron' or contains(.,'cmt.')] or ancestor::document/metadata/source[. = 'jbs']]/div">
        <xsl:apply-templates />
    </xsl:template>

    <xsl:template match="span[@class='sws-note-nr sws-generated'][ancestor::document/metadata/type[. = 'jur.bron' or contains(.,'cmt.')] or ancestor::document/metadata/source[. = 'jbs']]">
        <span class="nr">
            <xsl:apply-templates mode="jurbron"/>
        </span>
    </xsl:template>

    <xsl:template match="a" mode="jurbron">
        <xsl:apply-templates />
    </xsl:template>

    <!-- mark non referenced sws links in caselaw -->

    <xsl:template match="section[@class='sws-jurisprudence-meta']//dd[preceding-sibling::dt[1] = 'Regelgeving']">
        <xsl:copy>
            <xsl:if test="not(.//a)">
                <xsl:attribute name='class'>no_reference_link</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>

    <!-- remove header layout in jbs -->

    <xsl:template match="i|b|u|em[ancestor::document/metadata/source[. = 'jbs']]">
        <xsl:choose>
            <xsl:when test="parent::h1|parent::h2|parent::h3|parent::h4 and not(parent::*[text()])">
                <xsl:apply-templates />
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- parties to code in jbs -->

    <xsl:template match="parties[ancestor::document/metadata/source[. = 'jbs']]">
        <xsl:copy><xsl:value-of select="." disable-output-escaping="yes"/></xsl:copy>
    </xsl:template>

    <!-- parties to code in p1 -->

    <xsl:template match="parties[ancestor::document/metadata/source[. = 'publishone']]">
        <xsl:copy><xsl:value-of select="." disable-output-escaping="yes"/></xsl:copy>
    </xsl:template>

    <!-- joined case recognition in jbs-->

    <xsl:template match="div[@id='beslissingbesluit'][ancestor::document/metadata/source[. = 'jbs']]">
        <xsl:choose>
            <xsl:when test="ancestor::html//div[@id='conclusie']">
                <div id="samengevoegd"><xsl:apply-templates select="node()"/></div>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- old dl/dt/dd structure to p in jbs-->

    <xsl:template match="dl[@class='sws-list-explicit']|dl[@class='sws-list-explicit']/dd">
        <xsl:apply-templates select="node()"/>
    </xsl:template>

    <xsl:template match="dl[@class='sws-list-explicit']/dt"/>

    <xsl:template match="dl[@class='sws-list-explicit']/dd/p">
        <xsl:copy>
            <xsl:if test="count(preceding-sibling::p) = 0 and parent::dd[preceding-sibling::*[1][self::dt]]">
                <xsl:value-of select="concat(parent::dd/preceding-sibling::dt[1],' ')"/>
            </xsl:if>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- case law - rename colleges -->

    <xsl:template match="about/creator[role='instantie'][ancestor::document/metadata/type[. = 'jur.bron'] or ancestor::document/metadata/source[. = 'jbs']]">
        <xsl:copy>
            <xsl:choose>
                <xsl:when test="name='Gerecht in eerste aanleg van Curaçao'">
                    <identifier/><role>instantie</role><name>Gerecht in Eerste Aanleg van Curaçao</name>
                </xsl:when>
                <xsl:when test="name='Gerecht in eerste aanleg van Sint Maarten'">
                    <identifier/><role>instantie</role><name>Gerecht in Eerste Aanleg van Sint Maarten</name>
                </xsl:when>
                <xsl:when test="name='Raad van State'">
                    <identifier/><role>instantie</role><name>Afdeling bestuursrechtspraak van de Raad van State</name>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>

    <!-- new id for main div when Conclusie - changed to work with step specifiek -->

    <xsl:template match="html/body/div[@id='conclusie']">
        <div id="uitspraak" class="sws-content">
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <!-- remove id's from ecli headers -->

    <xsl:template match="*[self::h1|self::h2|self::h3|self::h4|self::h5|self::h6][ancestor::document/metadata/type[. = 'jur.bron']]/@id"/>

    <!-- join possible multiple <aside>, as in next template they are moved together to the next <p> -->

    <xsl:template match="document[metadata/type[contains(.,'cmt.')]]//aside">
        <asides>
            <aside><xsl:apply-templates/></aside>
            <xsl:if test="following-sibling::*[1][self::aside]">
                <xsl:apply-templates select="following-sibling::*[1][self::aside]" mode="commentaarkantnoten"/>
            </xsl:if>
        </asides>
    </xsl:template>

    <xsl:template match="aside" mode="commentaarkantnoten">
        <aside><xsl:apply-templates/></aside>
        <xsl:if test="following-sibling::*[1][self::aside]">
            <xsl:apply-templates select="following-sibling::*[1][self::aside]" mode="commentaarkantnoten"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="document[metadata/type[contains(.,'cmt.')]]//aside[preceding-sibling::*[1][self::aside]]"/>


    <!-- decode urlencoded url in img and assets to match reference and filename-->

    <xsl:template match="img/@src[contains(.,'%')]">
        <xsl:attribute name="src">
            <xsl:call-template name="decode">
                <xsl:with-param name="str" select="."/>
            </xsl:call-template>
        </xsl:attribute>
    </xsl:template>

    <xsl:template match="a[@class = 'asset']/@href[contains(.,'%')]">
        <xsl:attribute name="src">
            <xsl:call-template name="decode">
                <xsl:with-param name="str" select="."/>
            </xsl:call-template>
        </xsl:attribute>
    </xsl:template>

    <xsl:variable name="hex" select="'0123456789ABCDEF'"/>
    <xsl:variable name="ascii"> !"#$%&amp;'()*+,-./0123456789:;&lt;=&gt;?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~</xsl:variable>
    <xsl:variable name="safe">!'()*-.0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz~</xsl:variable>
    <xsl:variable name="latin1">&#160;&#161;&#162;&#163;&#164;&#165;&#166;&#167;&#168;&#169;&#170;&#171;&#172;&#173;&#174;&#175;&#176;&#177;&#178;&#179;&#180;&#181;&#182;&#183;&#184;&#185;&#186;&#187;&#188;&#189;&#190;&#191;&#192;&#193;&#194;&#195;&#196;&#197;&#198;&#199;&#200;&#201;&#202;&#203;&#204;&#205;&#206;&#207;&#208;&#209;&#210;&#211;&#212;&#213;&#214;&#215;&#216;&#217;&#218;&#219;&#220;&#221;&#222;&#223;&#224;&#225;&#226;&#227;&#228;&#229;&#230;&#231;&#232;&#233;&#234;&#235;&#236;&#237;&#238;&#239;&#240;&#241;&#242;&#243;&#244;&#245;&#246;&#247;&#248;&#249;&#250;&#251;&#252;&#253;&#254;&#255;</xsl:variable>

    <xsl:template name="decode">
        <xsl:param name="str"/>
        <xsl:choose>
            <xsl:when test="contains($str,'%')">
                <xsl:value-of select="substring-before($str,'%')"/>
                <xsl:variable name="hexpair" select="translate(substring(substring-after($str,'%'),1,2),'abcdef','ABCDEF')"/>
                <xsl:variable name="decimal" select="(string-length(substring-before($hex,substring($hexpair,1,1))))*16 + string-length(substring-before($hex,substring($hexpair,2,1)))"/>
                <xsl:choose>
                    <xsl:when test="$decimal &lt; 127 and $decimal &gt; 31">
                        <xsl:value-of select="substring($ascii,$decimal - 31,1)"/>
                    </xsl:when>
                    <xsl:when test="$decimal &gt; 159">
                        <xsl:value-of select="substring($latin1,$decimal - 159,1)"/>
                    </xsl:when>
                    <xsl:otherwise>?</xsl:otherwise>
                </xsl:choose>
                <xsl:call-template name="decode">
                    <xsl:with-param name="str" select="substring(substring-after($str,'%'),3)"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$str"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>