<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:atom="http://www.w3.org/2005/Atom"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:prism="http://prismstandard.org/namespaces/basic/2.0"
                xmlns:sdu="urn:sdu-nl:legal:metadata-1_0"
                xmlns:pcv="http://prismstandard.org/namespaces/1.2/pcv/"
                version="2.0" exclude-result-prefixes="rdf dc prism sdu pcv atom">

    <xsl:output method="xml" encoding="UTF-8"/>
    <xsl:strip-space elements="*"/>

    <xsl:variable name="upper-case">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
    <xsl:variable name="lower-case">abcdefghijklmnopqrstuvwxyz</xsl:variable>

    <xsl:variable name="abstract">
        <xsl:call-template name="string-replace">
            <xsl:with-param name="string" select="//metadata/abstract" />
            <xsl:with-param name="replace" select="'&lt;p&gt;'" />
            <xsl:with-param name="with" select="'&lt;p class=&quot;Samenvatting&quot;&gt;'" />
        </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="rubriek" select="//document[@id='jbs_jur']//div[@class='sws-metadata sws-generated']//table//row[*[self::th|self::entry][.='Rubriek']]/entry"/>

    <!--logfile-->
    <xsl:template match="document[@documentTypeName='Lijmteksten']">
        <xsl:copy>
            <xsl:copy-of select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <!-- end logfile-->

    <xsl:template match="document[@documentTypeName='Auteursbeschrijvingen']">
        <xsl:copy>
            <xsl:copy-of select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="document">

        <xsl:variable name="DocTemplate">
            <xsl:choose>
                <xsl:when test="@id='pn'">practicenotes</xsl:when>
                <xsl:when test="@id='nieuws'">nieuws</xsl:when>
                <xsl:when test="@id='tools'">tools</xsl:when>
                <xsl:when test="@id='jurisprudentie'">jurisprudentie</xsl:when>
                <xsl:when test="@id='jbs_jur'">jbsjur</xsl:when>
                <xsl:when test="@id='p1jur'">p1jur</xsl:when>
                <xsl:when test="@id='annotatie'">annotatie</xsl:when>
                <xsl:when test="@id='commentaar'">commentaar</xsl:when>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="DocTypeName">
            <xsl:choose>
                <xsl:when test="@id='pn'">PracticeNotes</xsl:when>
                <xsl:when test="@id='nieuws'">Nieuws</xsl:when>
                <xsl:when test="@id='tools'">Toolbeschrijvingen</xsl:when>
                <xsl:when test="@id='jurisprudentie'">Jurisprudentie</xsl:when>
                <xsl:when test="@id='jbs_jur'">Jurisprudentie</xsl:when>
                <xsl:when test="@id='p1jur'">Jurisprudentie</xsl:when>
                <xsl:when test="@id='annotatie'">Annotaties</xsl:when>
                <xsl:when test="@id='commentaar'">Commentaar</xsl:when>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="DocTypeKey">
            <xsl:choose>
                <xsl:when test="@id='pn'">10_PracticeNotes</xsl:when>
                <xsl:when test="@id='nieuws'">nieuws</xsl:when>
                <xsl:when test="@id='tools'">toolbeschrijvingen</xsl:when>
                <xsl:when test="@id='jurisprudentie'">jurisprudentie</xsl:when>
                <xsl:when test="@id='jbs_jur'">jurisprudentie</xsl:when>
                <xsl:when test="@id='p1jur'">jurisprudentie</xsl:when>
                <xsl:when test="@id='annotatie'">annotatie</xsl:when>
                <xsl:when test="@id='commentaar'">commentaar</xsl:when>
            </xsl:choose>
        </xsl:variable>

        <document documentTypeName="{$DocTypeName}" document-type-key="{$DocTypeKey}">
            <naam><xsl:value-of select="metadata/title"/></naam>
            <lastmodified/>
            <xsl:apply-templates select="metadata"/>
            <document version="1" track-changes="false">
                <section orientation="portrait">
                    <xsl:if test="$DocTemplate='practicenotes'">
                        <xsl:apply-templates select="html"/>
                    </xsl:if>
                    <xsl:if test="$DocTemplate='tools'">
                        <!--xsl:apply-templates select="html"/-->
                        <xsl:for-each select="//a">
                            <p><xsl:copy-of select="."/></p>
                        </xsl:for-each>
                    </xsl:if>
                    <xsl:if test="$DocTemplate='annotatie'">
                        <xsl:apply-templates select="html"/>
                    </xsl:if>
                    <xsl:if test="$DocTemplate='commentaar'">
                        <xsl:apply-templates select="html"/>
                    </xsl:if>
                    <xsl:if test="$DocTemplate='nieuws'">
                        <xsl:apply-templates select="html" />
                        <xsl:call-template name="swslinks">
                            <xsl:with-param name="input" select="metadata/references" />
                            <xsl:with-param name="output" select="'Verwijzingen'" />
                        </xsl:call-template>
                        <xsl:call-template name="swslinks">
                            <xsl:with-param name="input" select="metadata/about" />
                            <xsl:with-param name="output" select="'Bronnen'" />
                        </xsl:call-template>
                        <xsl:call-template name="assets">
                            <xsl:with-param name="input" select="metadata/hasFormat" />
                            <xsl:with-param name="output" select="'Link_versie'" />
                        </xsl:call-template>
                    </xsl:if>
                    <xsl:if test="$DocTemplate='p1jur'">
                        <h1 class="Redactionele_titel"><xsl:value-of select="html/body//h1"/></h1>
                        <table width="auto" class="Table_Grid" frame="all" colsep="1" rowsep="1">
                            <tgroup>
                                <colspec colname="col0" colwidth="1248"/>
                                <colspec colname="col1" colwidth="1255"/>
                                <colspec colname="col2" colwidth="2886"/>
                                <thead>
                                    <row>
                                        <entry colsep="1" rowsep="1" colspan="3" namest="col0" nameend="col2"
                                               align="center" btop="1" bleft="1"><p align="center"><bold>Rechters</bold></p></entry>
                                    </row>
                                    <row>
                                        <entry colsep="1" rowsep="1" bleft="1"><p><italic>Titulatuur</italic></p></entry>
                                        <entry colsep="1" rowsep="1"><p><italic>Voorletters</italic></p></entry>
                                        <entry colsep="1" rowsep="1"><p><italic>Achternaam</italic></p></entry>
                                    </row>
                                </thead>
                                <tbody>
                                    <xsl:for-each select="metadata/about/contributor[role='rechter']">
                                        <row>
                                            <entry colsep="1" rowsep="1" bleft="1">
                                                <xsl:choose>
                                                    <xsl:when test="translate(substring(name,1,4),$upper-case,$lower-case)='mr. '">
                                                        <p><xsl:value-of select="substring(name,1,3)"/></p>
                                                    </xsl:when>
                                                    <xsl:otherwise><p/></xsl:otherwise>
                                                </xsl:choose>
                                            </entry>
                                            <entry colsep="1" rowsep="1"><p/></entry>
                                            <entry colsep="1" rowsep="1" bleft="1">
                                                <xsl:choose>
                                                    <xsl:when test="substring(name,1,4)='mr. '">
                                                        <p><xsl:value-of select="substring-after(name,'mr. ')"/></p>
                                                    </xsl:when>
                                                    <xsl:when test="substring(name,1,4)='Mr. '">
                                                        <p><xsl:value-of select="substring-after(name,'Mr. ')"/></p>
                                                    </xsl:when>
                                                    <xsl:otherwise><p><xsl:value-of select="name"/></p></xsl:otherwise>
                                                </xsl:choose>
                                            </entry>
                                        </row>
                                    </xsl:for-each>
                                </tbody>
                            </tgroup>
                        </table>
                        <xsl:call-template name="swslinks">
                            <xsl:with-param name="input" select="metadata/references" />
                            <xsl:with-param name="output" select="'Verwijzingen'" />
                        </xsl:call-template>
                        <xsl:call-template name="sws_no_reference_links">
                            <xsl:with-param name="input" select="//dd[@class='no_reference_link']" />
                            <xsl:with-param name="output" select="'Verwijzingen'" />
                        </xsl:call-template>
                        <xsl:apply-templates select="html/body//section[@class='sws-abstract']/*"/>
                        <xsl:apply-templates select=".//metadata//parties"/>
                        <xsl:apply-templates select="html/body//section[@class='sws-verdict']/*|html/body//section[@class='sws-conclusion']/*"/>
                    </xsl:if>
                    <xsl:if test="$DocTemplate='jbsjur'">
                        <h1 class="Redactionele_titel"><xsl:value-of select="html/body//h1"/></h1>
                        <table width="auto" class="Table_Grid" frame="all" colsep="1" rowsep="1">
                            <tgroup>
                                <colspec colname="col0" colwidth="1248"/>
                                <colspec colname="col1" colwidth="1255"/>
                                <colspec colname="col2" colwidth="2886"/>
                                <thead>
                                    <row>
                                        <entry colsep="1" rowsep="1" colspan="3" namest="col0" nameend="col2"
                                               align="center" btop="1" bleft="1"><p align="center"><bold>Rechters</bold></p></entry>
                                    </row>
                                    <row>
                                        <entry colsep="1" rowsep="1" bleft="1"><p><italic>Titulatuur</italic></p></entry>
                                        <entry colsep="1" rowsep="1"><p><italic>Voorletters</italic></p></entry>
                                        <entry colsep="1" rowsep="1"><p><italic>Achternaam</italic></p></entry>
                                    </row>
                                </thead>
                                <tbody>
                                    <xsl:for-each select="metadata/about/contributor[role='rechter']">
                                        <row>
                                            <entry colsep="1" rowsep="1" bleft="1">
                                                <xsl:choose>
                                                    <xsl:when test="translate(substring(name,1,4),$upper-case,$lower-case)='mr. '">
                                                        <p><xsl:value-of select="substring(name,1,3)"/></p>
                                                    </xsl:when>
                                                    <xsl:otherwise><p/></xsl:otherwise>
                                                </xsl:choose>
                                            </entry>
                                            <entry colsep="1" rowsep="1"><p/></entry>
                                            <entry colsep="1" rowsep="1" bleft="1">
                                                <xsl:choose>
                                                    <xsl:when test="substring(name,1,4)='mr. '">
                                                        <p><xsl:value-of select="substring-after(name,'mr. ')"/></p>
                                                    </xsl:when>
                                                    <xsl:when test="substring(name,1,4)='Mr. '">
                                                        <p><xsl:value-of select="substring-after(name,'Mr. ')"/></p>
                                                    </xsl:when>
                                                    <xsl:otherwise><p><xsl:value-of select="name"/></p></xsl:otherwise>
                                                </xsl:choose>
                                            </entry>
                                        </row>
                                    </xsl:for-each>
                                </tbody>
                            </tgroup>
                        </table>
                        <xsl:call-template name="swslinks">
                            <xsl:with-param name="input" select="metadata/references" />
                            <xsl:with-param name="output" select="'Verwijzingen'" />
                        </xsl:call-template>
                        <xsl:apply-templates select="html/body/div[@id='content']/div[@id='samenvatting']"/>
                        <xsl:if test="not(.//metadata//parties) and .//row[th[starts-with(.,'Partij')]]">
                            <p class="Partijen">
                                <xsl:apply-templates select=".//row[th[starts-with(.,'Partij')]]/entry/node()"/>
                            </p>
                        </xsl:if>
                        <xsl:apply-templates select=".//metadata//parties"/>
                        <xsl:apply-templates select="html/body/div[@id='content']/div[@id='uitspraak']/div"/>
                    </xsl:if>
                    <xsl:if test="$DocTemplate='jurisprudentie'">
                        <h1 class="Redactionele_titel">[Vul hier een redactionele titel in]</h1>
                        <table width="auto" class="Table_Grid" frame="all" colsep="1" rowsep="1">
                            <tgroup>
                                <colspec colname="col0" colwidth="1248"/>
                                <colspec colname="col1" colwidth="1255"/>
                                <colspec colname="col2" colwidth="2886"/>
                                <thead>
                                    <row>
                                        <entry colsep="1" rowsep="1" colspan="3" namest="col0" nameend="col2"
                                               align="center" btop="1" bleft="1"><p align="center"><bold>Rechters</bold></p></entry>
                                    </row>
                                    <row>
                                        <entry colsep="1" rowsep="1" bleft="1"><p><italic>Titulatuur</italic></p></entry>
                                        <entry colsep="1" rowsep="1"><p><italic>Voorletters</italic></p></entry>
                                        <entry colsep="1" rowsep="1"><p><italic>Achternaam</italic></p></entry>
                                    </row>
                                </thead>
                                <tbody>
                                    <row>
                                        <entry colsep="1" rowsep="1" bleft="1"><p/></entry>
                                        <entry colsep="1" rowsep="1"><p/></entry>
                                        <entry colsep="1" rowsep="1"><p/></entry>
                                    </row>
                                    <row>
                                        <entry colsep="1" rowsep="1" bleft="1"><p/></entry>
                                        <entry colsep="1" rowsep="1"><p/></entry>
                                        <entry colsep="1" rowsep="1"><p/></entry>
                                    </row>
                                    <row>
                                        <entry colsep="1" rowsep="1" bleft="1"><p/></entry>
                                        <entry colsep="1" rowsep="1"><p/></entry>
                                        <entry colsep="1" rowsep="1"><p/></entry>
                                    </row>
                                    <row>
                                        <entry colsep="1" rowsep="1" bleft="1"><p/></entry>
                                        <entry colsep="1" rowsep="1"><p/></entry>
                                        <entry colsep="1" rowsep="1"><p/></entry>
                                    </row>
                                    <row>
                                        <entry colsep="1" rowsep="1" bleft="1"><p/></entry>
                                        <entry colsep="1" rowsep="1"><p/></entry>
                                        <entry colsep="1" rowsep="1"><p/></entry>
                                    </row>
                                    <row>
                                        <entry colsep="1" rowsep="1" bleft="1"><p/></entry>
                                        <entry colsep="1" rowsep="1"><p/></entry>
                                        <entry colsep="1" rowsep="1"><p/></entry>
                                    </row>
                                </tbody>
                            </tgroup>
                        </table>
                        <xsl:for-each select="//entry[preceding-sibling::*[1][self::th[.='Datum publicatie']]]">
                            <xsl:if test="translate(.,'0123456789-','')">
                                <p><xsl:value-of select="."/></p>
                            </xsl:if>
                        </xsl:for-each>
                        <xsl:for-each select="metadata/relation[displayTitle]">
                            <p class="Formele_relaties"><xsl:value-of select="displayTitle"/></p>
                        </xsl:for-each>
                        <xsl:call-template name="verwijzingen"/>
                        <xsl:apply-templates select="html"/>
                    </xsl:if>
                </section>
            </document>
        </document>
    </xsl:template>

    <xsl:template name="verwijzingen">
        <xsl:for-each select="metadata/references">
            <p class="Verwijzingen">
                <xsl:variable name="jcicheckversion">
                    <xsl:choose>
                        <xsl:when test="contains(identifier,'&amp;g=')">
                            <xsl:value-of select="substring-before(identifier,'&amp;g=')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="identifier"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="jci">
                    <xsl:choose>
                        <xsl:when test="contains($jcicheckversion,':BWB')">
                            <xsl:value-of select="concat('BWB',substring-after($jcicheckversion,':BWB'))"/>
                        </xsl:when>
                    </xsl:choose>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="altKey='wkey:LEEG'">
                        <xsl:value-of select="displayTitle"/>
                    </xsl:when>
                    <xsl:when test="not(identifier) and starts-with(altIdentifier,'http')">
                        <a href="{altIdentifier}">
                            <span class="Hyperlink"><xsl:value-of select="displayTitle"/></span>
                        </a>
                    </xsl:when>
                    <xsl:when test="contains($jci,'&amp;artikel=') or not(contains($jci,'&amp;'))">
                        <a>
                            <xsl:attribute name="href">
                                <xsl:text>http://sws.sdu.nl/?altKey=</xsl:text>
                                <xsl:choose>
                                    <xsl:when test="contains($jci,'&amp;artikel=')">
                                        <xsl:value-of select="substring-before($jci,'&amp;artikel=')"/>
                                        <xsl:text>&amp;about.part.article=</xsl:text>
                                        <xsl:choose>
                                            <xsl:when test="contains(substring-after($jci,'&amp;artikel='),', ')">
                                                <xsl:value-of select="substring-before(substring-after($jci,'&amp;artikel='),', ')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="substring-after($jci,'&amp;artikel=')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$jci"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                            <span class="Hyperlink"><xsl:value-of select="displayTitle"/></span>
                        </a>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="displayTitle"/>
                    </xsl:otherwise>
                </xsl:choose>
            </p>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="assets">
        <xsl:param name="input" />
        <xsl:param name="output"/>
        <xsl:for-each select="$input">
            <p class="{$output}"><a href="{substring-before(resource/identifier,'.')}"><span class="Hyperlink">pdf</span></a></p>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="swslinks">
        <xsl:param name="input" />
        <xsl:param name="output"/>
        <xsl:for-each select="$input">
            <xsl:variable name="href">
                <xsl:choose>
                    <xsl:when test="identifier">
                        <xsl:value-of select="'http://sws.sdu.nl/'"/>
                        <xsl:choose>
                            <xsl:when test="substring(identifier,1,3)='BWB'">
                                <xsl:value-of select="'?altKey='"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="'?identifier='"/>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:choose>
                            <xsl:when test="substring(identifier,1,3)='BWB' and contains(identifier,'-')">
                                <xsl:value-of select="substring-before(identifier,'-')"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="identifier"/>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:if test="part/article">
                            <xsl:value-of select="concat('&amp;about.part.article=',part/article)"/>
                        </xsl:if>
                        <xsl:if test="part/label-id">
                            <xsl:value-of select="concat('&amp;about.part.label-id=',part/label-id)"/>
                        </xsl:if>
                        <xsl:if test="part/anchor">
                            <xsl:value-of select="concat('&amp;about.part.anchor=',part/anchor)"/>
                        </xsl:if>
                    </xsl:when>
                    <xsl:when test="altKey and $output = 'Commentaarobject'">
                        <xsl:value-of select="'http://sws.sdu.nl/?altKey='"/>
                        <xsl:value-of select="altKey[1]"/>
                        <xsl:if test="part/article">
                            <xsl:value-of select="concat('&amp;about.part.article=',part/article)"/>
                        </xsl:if>
                        <xsl:if test="part/label-id">
                            <xsl:value-of select="concat('&amp;about.part.label-id=',part/label-id)"/>
                        </xsl:if>
                        <xsl:if test="part/anchor">
                            <xsl:value-of select="concat('&amp;about.part.anchor=',part/anchor)"/>
                        </xsl:if>
                    </xsl:when>
                    <xsl:when test="altIdentifier">
                        <xsl:value-of select="altIdentifier"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="displayTitle"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:variable name="linktekst">
                <xsl:choose>
                    <xsl:when test="$output = 'Commentaarobject'">
                        <xsl:if test="part/article">
                            <xsl:value-of select="concat('Art. ',part/article,' ')"/>
                        </xsl:if>
                        <xsl:value-of select="title"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="displayTitle"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:variable name="caocheck">
                <xsl:for-each select="$input//altKey"><xsl:value-of select="."/></xsl:for-each>
            </xsl:variable>
            <xsl:if test="not(contains($caocheck,'caonr:'))">
                <p class="{$output}">
                    <xsl:choose>
                        <xsl:when test="starts-with($href,'http')">
                            <a href="{$href}">
                                <span class="Hyperlink"><xsl:value-of select="$linktekst"/></span>
                            </a>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$linktekst"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </p>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="sws_no_reference_links">
        <xsl:param name="input" />
        <xsl:param name="output"/>
        <xsl:for-each select="$input">
            <p class="{$output}"><xsl:value-of select="."/></p>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="html">
        <xsl:apply-templates />
    </xsl:template>

    <xsl:template match="html/head"/>

    <xsl:template match="body/section">
        <xsl:apply-templates />
    </xsl:template>

    <xsl:template match="metadata">
        <xsl:copy>
            <xsl:if test="$rubriek!=''">
                <rubriek><xsl:value-of select="$rubriek"/></rubriek>
            </xsl:if>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="section[@class='sws-authors']"/>

    <xsl:template match="body|section[@class='sws-level']">
        <xsl:apply-templates />
    </xsl:template>

    <xsl:template match="h2|h3|h4|h5|h6|h7|h8|h9">
        <xsl:copy>
            <xsl:if test="@class">
                <xsl:attribute name="class"><xsl:value-of select="@class"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="parent::*/@id and not(ancestor::section[@class='sws-pn-related-info'])">
                <xsl:attribute name="id"><xsl:value-of select="parent::*/@id"/></xsl:attribute>
            </xsl:if>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="h1[@class='practicenote']">
        <h1>
            <xsl:attribute name="class">heading_1</xsl:attribute>
            <xsl:if test="parent::*/@id">
                <xsl:attribute name="id"><xsl:value-of select="parent::*/@id"/></xsl:attribute>
            </xsl:if>
            <xsl:apply-templates />
        </h1>
    </xsl:template>

    <xsl:template match="h1[@class='toolbeschrijving']">
        <h1>
            <xsl:attribute name="class">heading_1</xsl:attribute>
            <xsl:if test="parent::*/@id">
                <xsl:attribute name="id"><xsl:value-of select="parent::*/@id"/></xsl:attribute>
            </xsl:if>
            <xsl:apply-templates />
        </h1>
    </xsl:template>

    <xsl:template match="h1[@class='nieuws']">
        <h1>
            <xsl:attribute name="class">Kop_1</xsl:attribute>
            <xsl:if test="parent::*/@id">
                <xsl:attribute name="id"><xsl:value-of select="parent::*/@id"/></xsl:attribute>
            </xsl:if>
            <xsl:apply-templates />
        </h1>
        <xsl:if test="$abstract != ''">
            <xsl:value-of select="$abstract" disable-output-escaping="yes"/>
        </xsl:if>
    </xsl:template>

    <!-- specifiek practicenotes and tools-->

    <xsl:template match="section[@class=('sws-pn-abstract')]">
        <div class="samenvatting">
            <xsl:apply-templates />
        </div>
    </xsl:template>

    <xsl:template match="section[@class='sws-pn-related-info']">
        <div class="kader">
            <xsl:choose>
                <xsl:when test="not(div[@class='sws-pn-law'])">
                    <h2 class="heading_2">Tab wetgeving/wettelijk kader</h2><p/><p/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:element name="h2">
                        <xsl:attribute name="class">heading_2</xsl:attribute>
                        <xsl:if test="div[@class='sws-pn-law']/@id">
                            <xsl:attribute name="id"><xsl:value-of select="div[@class='sws-pn-law']/@id"/></xsl:attribute>
                        </xsl:if>Tab wetgeving/wettelijk kader</xsl:element>
                    <xsl:apply-templates select="div[@class='sws-pn-law']"/>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:choose>
                <xsl:when test="not(div[@class='sws-pn-caselaw'])">
                    <h2 class="heading_2">Tab jurisprudentie</h2><p/><p/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:element name="h2">
                        <xsl:attribute name="class">heading_2</xsl:attribute>
                        <xsl:if test="div[@class='sws-pn-caselaw']/@id">
                            <xsl:attribute name="id"><xsl:value-of select="div[@class='sws-pn-caselaw']/@id"/></xsl:attribute>
                        </xsl:if>Tab jurisprudentie</xsl:element>
                    <xsl:apply-templates select="div[@class='sws-pn-caselaw']"/>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:choose>
                <xsl:when test="not(div[@class='sws-pn-best-practices'])">
                    <h2 class="heading_2">Tab best practices</h2><p/><p/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:element name="h2">
                        <xsl:attribute name="class">heading_2</xsl:attribute>
                        <xsl:if test="div[@class='sws-pn-best-practices']/@id">
                            <xsl:attribute name="id"><xsl:value-of select="div[@class='sws-pn-best-practices']/@id"/></xsl:attribute>
                        </xsl:if>Tab best practices</xsl:element>
                    <xsl:apply-templates select="div[@class='sws-pn-best-practices']"/>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:choose>
                <xsl:when test="not(div[@class='sws-pn-tools'])">
                    <h2 class="heading_2">Tab tools</h2><p/><p/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:element name="h2">
                        <xsl:attribute name="class">heading_2</xsl:attribute>
                        <xsl:if test="div[@class='sws-pn-tools']/@id">
                            <xsl:attribute name="id"><xsl:value-of select="div[@class='sws-pn-tools']/@id"/></xsl:attribute>
                        </xsl:if>Tab tools</xsl:element>
                    <xsl:apply-templates select="div[@class='sws-pn-tools']"/>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:choose>
                <xsl:when test="not(div[@class='sws-pn-elaboration']) and not(div[@class='sws-pn-literature'])">
                    <h2 class="heading_2">Tab verdieping</h2><p/><p/>
                </xsl:when>
                <xsl:when test="div[@class='sws-pn-literature']">
                    <xsl:element name="h2">
                        <xsl:attribute name="class">heading_2</xsl:attribute>
                        <xsl:if test="div[@class='sws-pn-literature']/@id">
                            <xsl:attribute name="id"><xsl:value-of select="div[@class='sws-pn-literature']/@id"/></xsl:attribute>
                        </xsl:if>Tab literatuur</xsl:element>
                    <xsl:apply-templates select="div[@class='sws-pn-literature']"/>
                </xsl:when>
                <xsl:when test="div[@class='sws-pn-elaboration']">
                    <xsl:element name="h2">
                        <xsl:attribute name="class">heading_2</xsl:attribute>
                        <xsl:if test="div[@class='sws-pn-elaboration']/@id">
                            <xsl:attribute name="id"><xsl:value-of select="div[@class='sws-pn-elaboration']/@id"/></xsl:attribute>
                        </xsl:if>Tab verdieping</xsl:element>
                    <xsl:apply-templates select="div[@class='sws-pn-elaboration']"/>
                </xsl:when>
            </xsl:choose>

            <xsl:choose>
                <xsl:when test="not(div[@class='sws-pn-discussion'])">
                    <h2 class="heading_2">Tab forum</h2><p/><p/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:element name="h2">
                        <xsl:attribute name="class">heading_2</xsl:attribute>
                        <xsl:if test="div[@class='sws-pn-discussion']/@id">
                            <xsl:attribute name="id"><xsl:value-of select="div[@class='sws-pn-discussion']/@id"/></xsl:attribute>
                        </xsl:if>Tab forum</xsl:element>
                    <xsl:apply-templates select="div[@class='sws-pn-discussion']"/>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:choose>
                <xsl:when test="not(div[@class='sws-pn-related-topics'])">
                    <h2 class="heading_2">Tab gerelateerde topics</h2><p/><p/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:element name="h2">
                        <xsl:attribute name="class">heading_2</xsl:attribute>
                        <xsl:if test="div[@class='sws-pn-related-topics']/@id">
                            <xsl:attribute name="id"><xsl:value-of select="div[@class='sws-pn-related-topics']/@id"/></xsl:attribute>
                        </xsl:if>Tab gerelateerde topics</xsl:element>
                    <xsl:apply-templates select="div[@class='sws-pn-related-topics']"/>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:choose>
                <xsl:when test="not(div[@class='sws-pn-news'])">
                    <h2 class="heading_2">Tab nieuws</h2><p/><p/>
                </xsl:when>

                <xsl:otherwise>
                    <xsl:element name="h2">
                        <xsl:attribute name="class">heading_2</xsl:attribute>
                        <xsl:if test="div[@class='sws-pn-news']/@id">
                            <xsl:attribute name="id"><xsl:value-of select="div[@class='sws-pn-news']/@id"/></xsl:attribute>
                        </xsl:if>Tab nieuws</xsl:element>
                    <xsl:apply-templates select="div[@class='sws-pn-news']"/>
                </xsl:otherwise>

            </xsl:choose>
        </div>

        <xsl:if test="not(following::section[@class='sws-level-recent']) and ancestor::document[@id='pn']">
            <div class="letop">
                <h2 class="heading_2">Actueel</h2>
                <p>{Omschrijf hier de meest relevante jurisprudentie behorend bij de practice note}</p>
            </div>
        </xsl:if>

    </xsl:template>

    <xsl:template match="section[@class='sws-level-recent']">
        <div class="letop">
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template match="section[@class='sws-pn-related-info']/div">
        <xsl:apply-templates select="node()"/>
    </xsl:template>

    <xsl:template match="aside">
        <div class="note">
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <!-- specifiek nieuws -->

    <xsl:template match="about" mode="nieuws">
        <p class="Bronnen">
            <xsl:value-of select="displayTitle"/>
        </p>
    </xsl:template>

    <xsl:template match="references" mode="nieuws">
        <p class="Verwijzingen">
            <xsl:value-of select="displayTitle"/>
        </p>
    </xsl:template>

    <xsl:template match="section[@class='sws-impact']">
        <xsl:apply-templates select="node()"/>
    </xsl:template>

    <xsl:template match="section[@class='sws-introduction']">
        <xsl:apply-templates select="node()"/>
    </xsl:template>

    <xsl:template match="section[@class='sws-introduction']//p" priority="1">
        <p class="Inleiding">
            <xsl:apply-templates select="node()"/>
        </p>
    </xsl:template>

    <xsl:template match="section[@class='sws-impact']//p" priority="1">
        <p class="Kader_tekst">
            <xsl:apply-templates select="node()"/>
        </p>
    </xsl:template>

    <xsl:template match="section[@class='sws-impact']//h2" priority="1">
        <p class="Kader_titel">
            <xsl:apply-templates select="node()"/>
        </p>
    </xsl:template>

    <!-- specifiek p1jur -->

    <xsl:template match="document[@id='p1jur']//section[@class='sws-abstract']/h2"/>

    <xsl:template match="document[@id='p1jur']//section[@class='sws-abstract']/div">
        <xsl:apply-templates select="p"/>
    </xsl:template>

    <xsl:template match="document[@id='p1jur']//section[@class='sws-abstract']//p">
        <p class="Samenvatting">
            <xsl:apply-templates select="node()"/>
        </p>
    </xsl:template>

    <xsl:template match="document[@id='p1jur']//section//h3">
        <xsl:choose>
            <xsl:when test="not(*) and normalize-space(.)=''"/>
            <xsl:when test="parent::section[@class='sws-decision']">
                <h3 class="KopBeslissing">
                    <xsl:if test="ancestor::section[1]/@id">
                        <xsl:attribute name="id">
                            <xsl:value-of select="ancestor::section[1]/@id"></xsl:value-of>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:apply-templates/></h3>
            </xsl:when>
            <xsl:otherwise>
                <h3 class="KopUitspraak">
                    <xsl:if test="ancestor::section[1]/@id">
                        <xsl:attribute name="id">
                            <xsl:value-of select="ancestor::section[1]/@id"></xsl:value-of>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:apply-templates/>
                </h3>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="document[@id='p1jur']//section//h4">
        <h4 class="SubkopUitspraak">
            <xsl:if test="ancestor::section[1]/@id">
                <xsl:attribute name="id">
                    <xsl:value-of select="ancestor::section[1]/@id"></xsl:value-of>
                </xsl:attribute>
            </xsl:if>
            <xsl:apply-templates/>
        </h4>
    </xsl:template>

    <xsl:template match="document[@id='p1jur']//section[@class='sws-decision']">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="document[@id='p1jur']//header">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="document[@id='p1jur']//header/h2"/>

    <xsl:template match="document[@id='p1jur']//header/p">
        <xsl:choose>
            <xsl:when test="ancestor::section[@class='sws-verdict']">
                <h2 class="KopHogeRaad">
                    <xsl:if test="ancestor::section[@class='sws-verdict']/@id">
                        <xsl:attribute name="id">
                            <xsl:value-of select="ancestor::section[@class='sws-verdict']/@id"></xsl:value-of>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:apply-templates/></h2>
            </xsl:when>
            <xsl:when test="ancestor::section[@class='sws-conclusion']">
                <h2 class="KopConclusie">
                    <xsl:if test="ancestor::section[@class='sws-conclusion']/@id">
                        <xsl:attribute name="id">
                            <xsl:value-of select="ancestor::section[@class='sws-conclusion']/@id"></xsl:value-of>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:apply-templates/></h2>
            </xsl:when>
            <xsl:otherwise>
                <p><xsl:apply-templates/></p>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="document[@id='p1jur']//section[@class='sws-conclusion']//div[@class='Auteur']">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="document[@id='p1jur']//section[@class='sws-conclusion']//div[@class='Auteur']/p[@class='Auteur']">
        <p class="Conclusie_auteur"><xsl:apply-templates/></p>
    </xsl:template>

    <!-- specifiek jbsjurisprudentie -->


    <xsl:template match="document[@id='jbs_jur']//div">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="document[@id='jbs_jur']//div/text()">
        <p><xsl:value-of select="."/></p>
    </xsl:template>

    <xsl:template match="document[@id='jbs_jur']//div/br">
        <xsl:if test="not(preceding-sibling::*[1][self::br])">
            <p/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="parties">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="parties//div">
        <xsl:choose>
            <xsl:when test="not(p)">
                <p class="Partijen"><xsl:apply-templates/></p>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="p"/>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <xsl:template match="parties//p">
        <p class="Partijen"><xsl:apply-templates/></p>
    </xsl:template>

    <xsl:template match="document[@id='jbs_jur']//div[@id='samenvatting']">
        <xsl:apply-templates select="p"/>
    </xsl:template>

    <xsl:template match="document[@id='jbs_jur']//div[@id='samenvatting']/p">
        <p class="Samenvatting"><xsl:apply-templates/></p>
    </xsl:template>

    <xsl:template match="document[@id='jbs_jur']//div[@id='uitspraak']//h2">
        <xsl:choose>
            <xsl:when test="not(*) and normalize-space(.)=''"/>
            <xsl:when test="parent::div[parent::div[@id='conclusie']][not(preceding-sibling::div)]">
                <h2 class="KopConclusie"><xsl:apply-templates/></h2>
            </xsl:when>
            <xsl:when test="parent::div[@id='samengevoegd']">
                <h2 class="KopHogeRaad"><xsl:apply-templates/></h2>
            </xsl:when>
            <xsl:when test="parent::div[@id='beslissingbesluit']">
                <h3 class="KopBeslissing"><xsl:apply-templates/></h3>
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="count(ancestor::div) > 4 and ancestor::*[self::div[@id='samengevoegd']|self::div[@id='conclusie']]">
                        <h4 class="SubkopUitspraak"><xsl:apply-templates/></h4>
                    </xsl:when>
                    <xsl:when test="count(ancestor::div) > 3 and ancestor::*[self::div[@id='samengevoegd']|self::div[@id='conclusie']]">
                        <h3 class="KopUitspraak"><xsl:apply-templates/></h3>
                    </xsl:when>
                    <xsl:when test="count(ancestor::div) > 3">
                        <h4 class="SubkopUitspraak"><xsl:apply-templates/></h4>
                    </xsl:when>
                    <xsl:otherwise>
                        <h3 class="KopUitspraak"><xsl:apply-templates/></h3>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="document[@id='jbs_jur']//div[@id='uitspraak']//h3">
        <h4 class="SubkopUitspraak"><xsl:apply-templates/></h4>
    </xsl:template>

    <xsl:template match="document[@id='jbs_jur']//div[@id='uitspraak']//div[@id='samengevoegd']//h3">
        <h3 class="KopUitspraak"><xsl:apply-templates/></h3>
    </xsl:template>

    <!-- specifiek annotaties -->

    <xsl:template match="document[@id='annotatie']//div">
        <xsl:choose>
            <xsl:when test="@class='Auteur'"/>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="document[@id='annotatie']//div/text()">
        <p><xsl:value-of select="."/></p>
    </xsl:template>

    <xsl:template match="document[@id='annotatie']//h2">
        <xsl:choose>
            <xsl:when test=".='Noot' and count(preceding-sibling::h2) = 0"/>
            <xsl:otherwise>
                <h2 class="KopNoot"><xsl:apply-templates/></h2>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="document[@id='annotatie']//h3">
        <h3 class="KopNoot"><xsl:apply-templates/></h3>
    </xsl:template>

    <xsl:template match="document[@id='annotatie']//h4">
        <h4 class="SubkopNoot"><xsl:apply-templates/></h4>
    </xsl:template>

    <xsl:template match="document[@id='annotatie']//p[@class='auteur']"/>

    <!-- specifiek commentaar -->

    <xsl:template match="document[@id='commentaar']//metadata/abstract">
        <abstract><xsl:value-of select="." disable-output-escaping="yes"/></abstract>
    </xsl:template>

    <xsl:template match="document[@id='commentaar']//aside">
        <div class="data-side-note">
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template match="header">
        <xsl:apply-templates select="h1"/>
        <p class="Subtitel">
            <xsl:value-of select="p"/>
        </p>
    </xsl:template>

    <xsl:template match="section[@class='sws-commentary']//h1">
        <xsl:copy>
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="section[@class='sws-amendments-description']"/>

    <xsl:template match="section[@class='sws-related-legislation']"/>

    <xsl:template match="section[@class='sws-commentary-object-info']|div[@class='sws-notification']">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="div[@class='sws-notification']/p">
        <p class="Rode_regel"><xsl:apply-templates/></p>
    </xsl:template>

    <xsl:template match="section[@class='sws-full-commentary']//section">
        <xsl:apply-templates />
    </xsl:template>

    <xsl:template match="section[@class='sws-full-commentary']//h2|section[@class='sws-commentary-object-info']//h2">
        <xsl:variable name="sectionType">
            <xsl:choose>
                <xsl:when test="parent::section[@class='sws-essence']">Kop_kern</xsl:when>
                <xsl:when test="parent::section[@class='sws-commentary-text']">Kop_commentaar</xsl:when>
                <xsl:when test="parent::section[@class='sws-references-caselaw']">Kop_jurisprudentie</xsl:when>
                <xsl:when test="parent::section[@class='sws-references-literature']">Kop_literatuur</xsl:when>
                <xsl:when test="parent::section[@class='sws-related-legislation']">Kop_besluiten</xsl:when>
                <xsl:when test="parent::section[@class='sws-references-official-publications']">Kop_parlementaire geschiedenis</xsl:when>
                <xsl:when test="parent::section[@class='sws-amendments-description']">Kop_wijzigingen</xsl:when>
            </xsl:choose>
        </xsl:variable>
        <xsl:copy>
            <xsl:copy-of select="parent::section/@id"/>
            <xsl:if test="$sectionType!=''">
                <xsl:attribute name="class"><xsl:value-of select="$sectionType"/></xsl:attribute>
            </xsl:if>
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="section[@class='sws-amendments-description']" mode="commentaar">
        <xsl:apply-templates />
    </xsl:template>

    <xsl:template match="section[@class='sws-full-commentary']">

        <xsl:call-template name="swslinks">
            <xsl:with-param name="input" select="ancestor::document//metadata/about" />
            <xsl:with-param name="output" select="'Commentaarobject'" />
        </xsl:call-template>

        <xsl:choose>
            <xsl:when test="preceding-sibling::div[@class='sws-letop']">
                <xsl:apply-templates select="preceding-sibling::div[@class='sws-letop']" mode="commentaar"/>
            </xsl:when>
            <xsl:otherwise/>
            <!--div class="letop">
              <h2>Uitgelicht</h2><p/><p/>
            </div-->
        </xsl:choose>

        <xsl:choose>
            <xsl:when test="section[@class='sws-essence']">
                <xsl:apply-templates select="section[@class='sws-essence']"/>
            </xsl:when>
            <xsl:otherwise>
                <h2 class="Kop_kern">Kern</h2><p/><p/>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:choose>
            <xsl:when test="section[@class='sws-commentary-text']">
                <xsl:apply-templates select="section[@class='sws-commentary-text']"/>
            </xsl:when>
        </xsl:choose>

        <xsl:choose>
            <xsl:when test="//metadata/audience[.='specialist'] and section[@class='sws-references-caselaw']">
                <xsl:apply-templates select="section[@class='sws-references-caselaw']"/>
            </xsl:when>
        </xsl:choose>

        <xsl:choose>
            <xsl:when test="section[@class='sws-references-literature']">
                <xsl:apply-templates select="section[@class='sws-references-literature']"/>
            </xsl:when>
        </xsl:choose>

        <xsl:choose>
            <xsl:when test="section[@class='sws-references-legislation']">
                <xsl:apply-templates select="section[@class='sws-references-legislation']"/>
            </xsl:when>
        </xsl:choose>

        <xsl:choose>
            <xsl:when test="//metadata/audience[.='ndfr'] and section[@class='sws-references-official-publications']">
                <xsl:apply-templates select="section[@class='sws-references-official-publications']"/>
            </xsl:when>
        </xsl:choose>

        <xsl:choose>
            <xsl:when test="following-sibling::section[@class='sws-commentary-object-info'][section[@class='sws-amendments-description']]">
                <xsl:apply-templates select="following-sibling::section[@class='sws-commentary-object-info']/section[@class='sws-amendments-description']" mode="commentaar"/>
            </xsl:when>
        </xsl:choose>

    </xsl:template>

    <xsl:template match="div[@class='sws-parablock']">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="div[@class='sws-parablock']/p[preceding-sibling::*[1][not(self::p)]]">
        <p>
            <xsl:apply-templates/>
            <xsl:if test="following-sibling::*[1][self::p]">
                <xsl:apply-templates select="following-sibling::*[1][self::p]" mode="parablock"/>
            </xsl:if>
        </p>
    </xsl:template>

    <xsl:template match="div[@class='sws-parablock']/p[1]">
        <p><xsl:apply-templates/>
            <xsl:if test="following-sibling::*[1][self::p]">
                <xsl:apply-templates select="following-sibling::*[1][self::p]" mode="parablock"/>
            </xsl:if></p>
    </xsl:template>

    <xsl:template match="div[@class='sws-parablock']/p[preceding-sibling::*[1][self::p]]"/>

    <xsl:template match="div[@class='sws-parablock']/p" mode="parablock">
        <br/><xsl:apply-templates/>
        <xsl:if test="following-sibling::*[1][self::p]">
            <xsl:apply-templates select="following-sibling::*[1][self::p]" mode="parablock"/>
        </xsl:if>
    </xsl:template>

    <!-- specifiek jurisprudentieoff -->

    <xsl:template match="a[not(@*) and normalize-space(.)='']"/>

    <xsl:template match="strong">
        <xsl:param name="IsPartijen" />
        <xsl:choose>
            <xsl:when test="$IsPartijen='true'">
                <xsl:apply-templates select="node()"/>
            </xsl:when>
            <xsl:otherwise>
                <bold>
                    <xsl:apply-templates select="node()|@*"/>
                </bold>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="em">
        <xsl:call-template name="startStyleTests" />
    </xsl:template>

    <xsl:template name="startStyleTests">
        <xsl:call-template name="testBold" />
    </xsl:template>

    <xsl:template name="testBold">
        <xsl:choose>
            <xsl:when test="contains(./@class, 'bold')">
                <xsl:element name="bold"><xsl:call-template name="testItalic" /></xsl:element>
            </xsl:when>
            <xsl:otherwise><xsl:call-template name="testItalic" /></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="testItalic">
        <xsl:choose>
            <xsl:when test="contains(./@class, 'italic')">
                <xsl:element name="italic">
                    <xsl:call-template name="testUnderline" />
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="testUnderline" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="testUnderline">
        <xsl:choose>
            <xsl:when test="contains(./@class, 'underline')">
                <xsl:element name="underline">
                    <xsl:call-template name="endStyleTests" />
                </xsl:element>
            </xsl:when>
            <xsl:otherwise><xsl:call-template name="endStyleTests" /></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="endStyleTests">
        <xsl:apply-templates />
    </xsl:template>

    <xsl:template match="body[starts-with(@data-sws-documentkey,'ECLI')]/div[@class='sws-metadata sws-generated']"/>

    <xsl:template match="body[starts-with(@data-sws-documentkey,'ECLI')]/h1[@class='jurisprudentie']"/>

    <xsl:template match="body[starts-with(@data-sws-documentkey,'ECLI')]//div[@class='parablock' or @class='paragroup']">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="body[starts-with(@data-sws-documentkey,'ECLI')]//div[@id='uitspraak' and @class='sws-content']">
        <xsl:apply-templates/>
    </xsl:template>


    <xsl:template match="body[starts-with(@data-sws-documentkey,'ECLI')]//div[@id='uitspraak' and @class='sws-content']/div[@class='uitspraak.info' or @class='conclusie.info']/h2">
        <p>
            <xsl:apply-templates/>
        </p>
    </xsl:template>

    <xsl:template match="body[starts-with(@data-sws-documentkey,'ECLI')]//h2">
        <xsl:variable name="kopGenormaliseerd" select="normalize-space(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.','abcdefghijklmnopqrstuvwxyz'))"/>
        <xsl:choose>
            <xsl:when test="parent::div[@id='uitspraak' and @class='sws-content'] and (@id='uitspraak' or normalize-space(.)='Uitspraak' or normalize-space(.)='Conclusie')"/>
            <xsl:when test="$kopGenormaliseerd='conclusie'">
                <h2 class="KopConclusie">
                    <xsl:for-each select="node()">
                        <xsl:choose>
                            <xsl:when test="not(preceding-sibling::node()) and self::text()">
                                <xsl:call-template name="strip-leading-blanks">
                                    <xsl:with-param name="s" select="."/>
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:apply-templates select="."/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </h2>
            </xsl:when>
            <xsl:when test="$kopGenormaliseerd='de beslissing' or $kopGenormaliseerd='beslissing'">
                <h3 class="KopBeslissing">
                    <xsl:for-each select="node()">
                        <xsl:choose>
                            <xsl:when test="not(preceding-sibling::node()) and self::text()">
                                <xsl:call-template name="strip-leading-blanks">
                                    <xsl:with-param name="s" select="."/>
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:apply-templates select="."/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </h3>
            </xsl:when>
            <xsl:otherwise>
                <h3 class="KopUitspraak">
                    <xsl:for-each select="node()">
                        <xsl:choose>
                            <xsl:when test="not(preceding-sibling::node()) and self::text()">
                                <xsl:call-template name="strip-leading-blanks">
                                    <xsl:with-param name="s" select="."/>
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:apply-templates select="."/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </h3>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="body[starts-with(@data-sws-documentkey,'ECLI')]//h3">
        <xsl:choose>
            <xsl:when test="strong and normalize-space(.)=normalize-space(strong)">
                <h3 class="KopUitspraak">
                    <xsl:for-each select="strong">
                        <xsl:for-each select="node()">
                            <xsl:choose>
                                <xsl:when test="not(preceding-sibling::node()) and self::text()">
                                    <xsl:call-template name="strip-leading-blanks">
                                        <xsl:with-param name="s" select="."/>
                                    </xsl:call-template>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:apply-templates select="."/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each>
                    </xsl:for-each>
                </h3>
            </xsl:when>
            <xsl:when test="em and normalize-space(.)=normalize-space(em)">
                <h3 class="KopUitspraak">
                    <xsl:for-each select="em">
                        <xsl:for-each select="node()">
                            <xsl:choose>
                                <xsl:when test="not(preceding-sibling::node()) and self::text()">
                                    <xsl:call-template name="strip-leading-blanks">
                                        <xsl:with-param name="s" select="."/>
                                    </xsl:call-template>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:apply-templates select="."/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each>
                    </xsl:for-each>
                </h3>
            </xsl:when>
            <xsl:otherwise>
                <h4 class="SubkopUitspraak">
                    <xsl:for-each select="node()">
                        <xsl:choose>
                            <xsl:when test="not(preceding-sibling::node()) and self::text()">
                                <xsl:call-template name="strip-leading-blanks">
                                    <xsl:with-param name="s" select="."/>
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:apply-templates select="."/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </h4>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="body[starts-with(@data-sws-documentkey,'ECLI')]//div/p[normalize-space(string(.)) = '']"/>

    <xsl:template match="body[starts-with(@data-sws-documentkey,'ECLI')]//div[@id='uitspraak' and @class='sws-content']/div[@class='uitspraak.info' or @class='conclusie.info']">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="body[starts-with(@data-sws-documentkey,'ECLI')]//div[@id='uitspraak' and @class='sws-content']/*[last() and self::h2 and .='Voetnoten']"/>

    <xsl:template match="div[@class='uitspraak.info' or @class='conclusie.info']//table[not(descendant::row/entry[position()&gt;1 and not(normalize-space(.)='')]) and not(descendant::row/entry[*[not(self::p)]]) and not(descendant::row/entry[text()[not(normalize-space(.)='')]])]">
        <xsl:for-each select="descendant::row">
            <xsl:for-each select="entry/p">
                <p class="Partijen">
                    <xsl:apply-templates>
                        <xsl:with-param name="IsPartijen">true</xsl:with-param>
                    </xsl:apply-templates>
                </p>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="div[@class='uitspraak.info' or @class='conclusie.info']//p[normalize-space(string(.)) != ''] | div[@class='uitspraak.info' or @class='conclusie.info']//h3[normalize-space(string(.)) != '']">
        <xsl:variable name="tekstGenormaliseerd" select="normalize-space(translate(.,',ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.[',' abcdefghijklmnopqrstuvwxyz'))"/>
        <p>
            <xsl:attribute name="class">Partijen</xsl:attribute>
            <xsl:for-each select="node()">
                <xsl:choose>
                    <xsl:when test="normalize-space(string(.)) = ''"/>
                    <xsl:when test="not(preceding-sibling::node()) and self::text()">
                        <xsl:call-template name="strip-leading-blanks">
                            <xsl:with-param name="s" select="."/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select=".">
                            <xsl:with-param name="IsPartijen">true</xsl:with-param>
                        </xsl:apply-templates>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </p>
    </xsl:template>

    <xsl:template match="body[starts-with(@data-sws-documentkey,'ECLI')]//div[not(@class)]">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="div[@class='paragroup']/*[1][self::span and following-sibling::*[1][self::p]]"/>

    <xsl:template match="div[@class='paragroup']/*[2][self::p and preceding-sibling::*[1][self::span]]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:for-each select="preceding-sibling::*[1]">
                <xsl:apply-templates/>
            </xsl:for-each>
            <xsl:text> </xsl:text>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="div[@class='paragroup']/*[1][self::span and following-sibling::*[1][self::div[@class='parablock' and *[1][self::p]]]]"/>

    <xsl:template match="div[@class='parablock' and preceding-sibling::*[1][self::span]]/*[1][self::p]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:for-each select="../preceding-sibling::*[1]">
                <xsl:apply-templates/>
            </xsl:for-each>
            <xsl:text> </xsl:text>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="dl[dt='-']">
        <xsl:for-each select="dd">
            <p>
                <xsl:if test="ancestor::div[@class='uitspraak.info' or @class='conclusie.info']">
                    <xsl:attribute name="class">Partijen</xsl:attribute>
                </xsl:if>
                <xsl:text>- </xsl:text>
                <xsl:for-each select="node()">
                    <xsl:choose>
                        <xsl:when test="self::p">
                            <xsl:apply-templates/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:apply-templates select="."/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </p>
        </xsl:for-each>
    </xsl:template>

    <!-- functions -->

    <xsl:template name="string-replace">
        <xsl:param name="string" />
        <xsl:param name="replace" />
        <xsl:param name="with" />
        <xsl:choose>
            <xsl:when test="contains($string, $replace)">
                <xsl:value-of select="substring-before($string, $replace)" />
                <xsl:value-of select="$with" />
                <xsl:call-template name="string-replace">
                    <xsl:with-param name="string" select="substring-after($string,$replace)" />
                    <xsl:with-param name="replace" select="$replace" />
                    <xsl:with-param name="with" select="$with" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$string" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="strip-leading-blanks">
        <xsl:param name="s"/>
        <xsl:choose>
            <xsl:when test="normalize-space(substring($s,1,1))=''">
                <xsl:call-template name="strip-leading-blanks">
                    <xsl:with-param name="s" select="substring($s,2)"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$s"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- identity transform -->

    <xsl:template match="*">
        <xsl:element name="{local-name()}">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@*">
        <xsl:attribute name="{local-name()}"><xsl:value-of select="."/></xsl:attribute>
    </xsl:template>

</xsl:stylesheet>