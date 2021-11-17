<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:cwc="urn:cwc"
                xmlns:keymap="urn:keymap-for-mapping" xmlns:p1="urn:p1"
                xmlns:exslt="http://exslt.org/common"
                xmlns:atom="http://www.w3.org/2005/Atom"
                xmlns:msxsl="urn:schemas-microsoft-com:xslt" exclude-result-prefixes="atom xs cwc p1 keymap msxsl exslt"
                version="1.0">

    <xsl:output method="xml" encoding="UTF-8"/>

    <xsl:variable name="upper-case">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
    <xsl:variable name="lower-case">abcdefghijklmnopqrstuvwxyz</xsl:variable>

    <xsl:variable name="collegeLookupList">
        <valuelist>
            <item><key>192139</key><value>ABN AMRO Geschillencommissie (Ontslagadviescommissie)</value></item>
            <item><key>173847</key><value>ABN AMRO Geschillencommissie</value></item>
            <item><key>151449</key><value>Accountantskamer</value></item>
            <item><key>168770</key><value>Afdeling bestuursrechtspraak Raad van State</value></item>
            <item><key>175577</key><value>Afdeling bestuursrechtspraak van de Raad van State (conclusie A-G)</value></item>
            <item><key>62633</key><value>Afdeling bestuursrechtspraak van de Raad van State</value></item>
            <item><key>62634</key><value>Afdeling rechtspraak van de Raad van State</value></item>
            <item><key>150092</key><value>AG Hof van Justitie EU</value></item>
            <item><key>149048</key><value>Arbeidsrechtbank</value></item>
            <item><key>183348</key><value>Augstaka tiesa (Letland)</value></item>
            <item><key>172085</key><value>Autoriteit Consument en Markt</value></item>
            <item><key>194328</key><value>Autoriteit Persoonsgegevens</value></item>
            <item><key>211361</key><value>beheer</value></item>
            <item><key>172883</key><value>Beklagcommissie Detentiecentrum Noord-Holland, locatie Schiphol</value></item>
            <item><key>183983</key><value>Beklagcommissie uit de Commissie van Toezicht bij de Rijks Justitiële Jeugdinrichting, locatie De Hartelborgt</value></item>
            <item><key>156211</key><value>Benelux-Gerechtshof</value></item>
            <item><key>164326</key><value>Beroepscommissie Financiële Dienstverlening</value></item>
            <item><key>169432</key><value>Beroepscommissie van de Raad voor Strafrechtstoepassing en Jeugdbescherming</value></item>
            <item><key>172940</key><value>Board of Appeal of the European Supervisory Authorities</value></item>
            <item><key>173260</key><value>Brief Minister van Binnenlandse Zaken en Koninkrijksrelaties</value></item>
            <item><key>145416</key><value>Bundesgerichtshof</value></item>
            <item><key>100237</key><value>Centraal Tuchtcollege voor de Gezondheidszorg</value></item>
            <item><key>10255</key><value>Centrale Raad van Beroep</value></item>
            <item><key>151848</key><value>Codecommissie KOAG/KAG</value></item>
            <item><key>177241</key><value>Codecommissie van de Stichting Code Geneesmiddelenreclame</value></item>
            <item><key>168163</key><value>College bescherming persoonsgegevens</value></item>
            <item><key>155433</key><value>College ter beoordeling van geneesmiddelen</value></item>
            <item><key>62649</key><value>College van Beroep Studiefinanciering</value></item>
            <item><key>175960</key><value>College van Beroep voor het Bedrijfsleven (Nederland)</value></item>
            <item><key>10257</key><value>College van Beroep voor het bedrijfsleven</value></item>
            <item><key>165946</key><value>College voor de Rechten van de Mens</value></item>
            <item><key>151559</key><value>Commissie Gelijke Behandeling</value></item>
            <item><key>172115</key><value>Commissie van Aanbestedingsexperts</value></item>
            <item><key>166947</key><value>Commissie van Beroep Financiële Dienstverlening</value></item>
            <item><key>212141</key><value>Commissie van Beroep Tuchtrecht Banken</value></item>
            <item><key>168317</key><value>Commissie van Beroep Voortgezet Onderwijs</value></item>
            <item><key>164115</key><value>Commissie van Toezicht P.I. Veenhuizen</value></item>
            <item><key>148159</key><value>Commissie voor bezwaar van de Raad voor Rechtsbijstand</value></item>
            <item><key>173272</key><value>Commissie voor Geschillen CAO BVE</value></item>
            <item><key>183981</key><value>Committee Against Torture (CAT)</value></item>
            <item><key>210889</key><value>Conclusie Advocaat-Generaal Hof van Justitie EU</value></item>
            <item><key>183331</key><value>Consiglio di Giustizia amministrativa per la Regione siciliana (Italië)</value></item>
            <item><key>172161</key><value>Consiglio di Stato (Italië)</value></item>
            <item><key>192501</key><value>Curtea de Apel Bacau (Roemenië)</value></item>
            <item><key>151847</key><value>CvB Stichting Code Geneesmiddelenreclame</value></item>
            <item><key>183258</key><value>De Kroon</value></item>
            <item><key>152545</key><value>EFTA Court</value></item>
            <item><key>166454</key><value>European Committee of Social Rights</value></item>
            <item><key>155096</key><value>Europees Hof voor de Rechten van de Mens (Grote kamer)</value></item>
            <item><key>152584</key><value>Europees Hof voor de Rechten van de Mens (ontvankelijkheidsbeslissing)</value></item>
            <item><key>10260</key><value>Europees Hof voor de Rechten van de Mens</value></item>
            <item><key>169314</key><value>Europese Commissie</value></item>
            <item><key>145415</key><value>Europese ombudsman</value></item>
            <item><key>176410</key><value>Externe Klachtencommissie regio Arnhem</value></item>
            <item><key>210800</key><value>Fovarosi Kozigazgatasi es Munkaugyi Birosag</value></item>
            <item><key>148933</key><value>Gemeenschappelijk Hof van Justitie van Aruba, Curaçao, Sint Maarten en van Bonaire, Sint Eustatius en Saba</value></item>
            <item><key>91527</key><value>Gemeenschappelijk Hof van Justitie van de Nederlandse Antillen en Aruba</value></item>
            <item><key>145644</key><value>Gerecht EU</value></item>
            <item><key>173284</key><value>Gerecht in Eerste Aanleg van Aruba</value></item>
            <item><key>149074</key><value>Gerecht in Eerste Aanleg van Curaçao</value></item>
            <item><key>100747</key><value>Gerecht in Eerste Aanleg van de Nederlandse Antillen</value></item>
            <item><key>149955</key><value>Gerecht in Eerste Aanleg van Sint Maarten</value></item>
            <item><key>155434</key><value>Gerecht van de Europese Unie</value></item>
            <item><key>62669</key><value>Gerecht van Eerste Aanleg EG</value></item>
            <item><key>149800</key><value>Gerecht van Eerste Aanleg EU</value></item>
            <item><key>173190</key><value>Gerecht van Eerste Aanleg</value></item>
            <item><key>10261</key><value>Gerechtshof</value></item>
            <item><key>151812</key><value>Geschillencommissie Financiële Dienstverlening</value></item>
            <item><key>167899</key><value>Geschillencommissie Fusiegedragsregels SER</value></item>
            <item><key>151789</key><value>Geschillencommissie RAS</value></item>
            <item><key>153134</key><value>Geschillencommissie Zorginstellingen</value></item>
            <item><key>100241</key><value>Geschillencommissie Zorgverzekeringen</value></item>
            <item><key>175351</key><value>Governancecommissie Gezondheidszorg</value></item>
            <item><key>175923</key><value>Grondwettelijk Hof België</value></item>
            <item><key>169325</key><value>Hanseatisches Oberlandesgericht Hamburg</value></item>
            <item><key>166953</key><value>High Court of Justice</value></item>
            <item><key>179488</key><value>Hof van Cassatie van België</value></item>
            <item><key>174932</key><value>Hof van Discipline</value></item>
            <item><key>153040</key><value>Hof van Justitie EG</value></item>
            <item><key>177103</key><value>Hof van Justitie EU (vijfde kamer)</value></item>
            <item><key>175291</key><value>Hof van Justitie EU, conclusie A-G</value></item>
            <item><key>100222</key><value>Hof van Justitie EU</value></item>
            <item><key>153050</key><value>Hof van Justitie Europese Unie</value></item>
            <item><key>10263</key><value>Hof van Justitie van de Europese Gemeenschappen</value></item>
            <item><key>154104</key><value>Hof van Justitie van de Europese Unie</value></item>
            <item><key>10264</key><value>Hoge Raad</value></item>
            <item><key>148156</key><value>Human Rights Committee</value></item>
            <item><key>164299</key><value>ILO Committee on Freedom of Association</value></item>
            <item><key>175636</key><value>Internationaal Gerechtshof</value></item>
            <item><key>183346</key><value>Juzgado de lo Contencioso Administrativo n°6 de Bilbao (Spanje)</value></item>
            <item><key>157053</key><value>Kamer van Toezicht over de notarissen en kandidaat-notarissen in het arrondissement Maastricht</value></item>
            <item><key>183351</key><value>Kinderombudsman</value></item>
            <item><key>176411</key><value>Klachtencommissie Cliënten Pro Persona</value></item>
            <item><key>152986</key><value>Klachtencommissie FPK De Kijvelanden</value></item>
            <item><key>174668</key><value>Klachtencommissie GGNet</value></item>
            <item><key>152140</key><value>Klachtencommissie GGZ inGeest, Arkin, AMC Psychiatrie</value></item>
            <item><key>201087</key><value>Klachtencommissie GGZ Rivierduinen</value></item>
            <item><key>168928</key><value>Klachtencommissie GGZ</value></item>
            <item><key>172829</key><value>Klachtencommissie Patiënten/cliënten GGz Centraal</value></item>
            <item><key>156802</key><value>Klachtencommissie Patiënten/Cliënten GGz Centraal</value></item>
            <item><key>100239</key><value>Klachtencommissie SCEN</value></item>
            <item><key>191707</key><value>Klagenævnet for Udbud (Commissie voor aanbestedingsgeschillen, Denemarken)</value></item>
            <item><key>191685</key><value>Koninklijk Besluit</value></item>
            <item><key>184111</key><value>Krajowa Izba Odwolawcza (Polen)</value></item>
            <item><key>170429</key><value>Landelijke Adviescommissie Lokaal Overleg</value></item>
            <item><key>176576</key><value>Landelijke Commissie voor Geschillen WMS</value></item>
            <item><key>149047</key><value>n.v.t.</value></item>
            <item><key>210798</key><value>Najvyssi sud Slovenskej republiky (Slowakije)</value></item>
            <item><key>100242</key><value>Nationale ombudsman</value></item>
            <item><key>153212</key><value>Nederlandse Mededingingsautoriteit</value></item>
            <item><key>168702</key><value>Oberlandesgericht Düsseldorf – Vergabesenat (Duitsland)</value></item>
            <item><key>184112</key><value>Oberlandesgericht Koblenz (Duitsland)</value></item>
            <item><key>168929</key><value>Officier van Justitie</value></item>
            <item><key>176262</key><value>Parket bij de Hoge Raad</value></item>
            <item><key>149134</key><value>President Europees Hof voor de Rechten van de Mens</value></item>
            <item><key>150065</key><value>Raad van Bestuur Nederlandse Mededingingsautoriteit</value></item>
            <item><key>150765</key><value>Raad van Bestuur Nederlandse Zorgautoriteit</value></item>
            <item><key>193293</key><value>Raad van State (België)</value></item>
            <item><key>172116</key><value>Raad van State</value></item>
            <item><key>150059</key><value>Raad van Tucht van de Koninklijke Nederlandse Maatschappij ter Bevordering der Pharmacie</value></item>
            <item><key>151235</key><value>Raad voor de tuchtrechtspraak KNMG</value></item>
            <item><key>148158</key><value>Raad voor Rechtsbijstand</value></item>
            <item><key>152532</key><value>Raad voor Strafrechtstoepassing en Jeugdbescherming</value></item>
            <item><key>163885</key><value>Rechtbank Amsterdam, sector kanton</value></item>
            <item><key>10272</key><value>Rechtbank</value></item>
            <item><key>151585</key><value>Reclame Code Commissie</value></item>
            <item><key>179993</key><value>Regionaal College voor het Opzicht</value></item>
            <item><key>171720</key><value>Regionaal Tuchtcollege voor de Gezondheidszorg</value></item>
            <item><key>100238</key><value>Regionaal Tuchtcollege</value></item>
            <item><key>179998</key><value>Regionale toetsingscommissies euthanasie</value></item>
            <item><key>100240</key><value>Scheidsgerecht Gezondheidszorg</value></item>
            <item><key>180477</key><value>Schweizerisches Bundesgericht</value></item>
            <item><key>175416</key><value>Supremo Tribunal Administrativo (Portugal)</value></item>
            <item><key>183772</key><value>Tribunal Català de Contractes del Sector Públic (Spanje)</value></item>
            <item><key>193292</key><value>Tribunal Central Administrativo Sul (Portugal)</value></item>
            <item><key>201225</key><value>Tribunal Superior de Justicia de Andalucía (Spanje)</value></item>
            <item><key>193291</key><value>Tribunale Amministrativo Regionale per la Lombardia</value></item>
            <item><key>194330</key><value>Tribunale Amministrativo Regionale per la Sardegna</value></item>
            <item><key>172157</key><value>Tribunale amministrativo regionale per le Marche (Italië)</value></item>
            <item><key>197870</key><value>Tribunale di Reggio Calabria (Italie)</value></item>
            <item><key>183332</key><value>Tribunale Regionale di Giustizia Amministrativa di Trento (Italië)</value></item>
            <item><key>207340</key><value>Tuchtcommissie Banken</value></item>
            <item><key>173005</key><value>UWV Arbeidsjuridische Dienst</value></item>
            <item><key>155011</key><value>UWV WERKbedrijf</value></item>
            <item><key>199906</key><value>Vergabekamer Südbayern</value></item>
            <item><key>175919</key><value>Verwaltungsgericht Darmstadt</value></item>
            <item><key>183347</key><value>Verwaltungsgerichtshof (Oostenrijk)</value></item>
            <item><key>153272</key><value>Veterinair Beroepscollege</value></item>
            <item><key>153325</key><value>Veterinair Tuchtcollege</value></item>
            <item><key>174981</key><value>Vicepresident van het Hof van Justitie van de EU</value></item>
            <item><key>212236</key><value>Vilniaus apygardos teismas (regionale rechter Vilnius, Litouwen)</value></item>
            <item><key>176804</key><value>VN-Mensenrechtencomité</value></item>
            <item><key>153285</key><value>Voorzieningenrechter College van Beroep voor het bedrijfsleven</value></item>
            <item><key>149953</key><value>Voorzitter Afdeling bestuursrechtspraak van de Raad van State</value></item>
            <item><key>100342</key><value>Voorzitter van de Afdeling bestuursrechtspraak van de Raad van State</value></item>
            <item><key>191706</key><value>Wojewódzki Sad Administracyjny w Warszawie (Polen)</value></item>
            <item><key>156209</key><value>WTO Appelate Body</value></item>
            <item><key>152956</key><value>WTO Panel</value></item>
        </valuelist>
    </xsl:variable>
    <xsl:variable name="plaatsLookupList">
        <valuelist>
            <item><key>10293</key><value>’s-Gravenhage</value></item>
            <item><key>156425</key><value>’s-Hertogenbosch</value></item>
            <item><key>10276</key><value>Alkmaar</value></item>
            <item><key>10277</key><value>Almelo</value></item>
            <item><key>163435</key><value>Almere</value></item>
            <item><key>62631</key><value>Amersfoort</value></item>
            <item><key>9882</key><value>Amsterdam</value></item>
            <item><key>62582</key><value>Apeldoorn</value></item>
            <item><key>10279</key><value>Arnhem</value></item>
            <item><key>156424</key><value>Arnhem-Leeuwarden</value></item>
            <item><key>10280</key><value>Assen</value></item>
            <item><key>62584</key><value>Bergen op Zoom</value></item>
            <item><key>10281</key><value>Breda</value></item>
            <item><key>10282</key><value>Den Haag</value></item>
            <item><key>10283</key><value>Dordrecht</value></item>
            <item><key>10284</key><value>Eindhoven</value></item>
            <item><key>62591</key><value>Enschede</value></item>
            <item><key>163873</key><value>Gelderland</value></item>
            <item><key>10285</key><value>Gouda</value></item>
            <item><key>10286</key><value>Groningen</value></item>
            <item><key>10287</key><value>Haarlem</value></item>
            <item><key>163437</key><value>Haarlemmermeer</value></item>
            <item><key>10288</key><value>Leeuwarden</value></item>
            <item><key>62603</key><value>Leiden</value></item>
            <item><key>62604</key><value>Lelystad</value></item>
            <item><key>167153</key><value>Limburg</value></item>
            <item><key>10289</key><value>Maastricht</value></item>
            <item><key>10290</key><value>Middelburg</value></item>
            <item><key>62607</key><value>Midden-Nederland</value></item>
            <item><key>62609</key><value>Nijmegen</value></item>
            <item><key>156417</key><value>Noord-Holland</value></item>
            <item><key>156420</key><value>Noord-Nederland</value></item>
            <item><key>156422</key><value>Oost-Brabant</value></item>
            <item><key>163874</key><value>Overijssel</value></item>
            <item><key>10291</key><value>Roermond</value></item>
            <item><key>10292</key><value>Rotterdam</value></item>
            <item><key>62621</key><value>Tilburg</value></item>
            <item><key>10296</key><value>Utrecht</value></item>
            <item><key>163439</key><value>Zaanstad</value></item>
            <item><key>156421</key><value>Zeeland-West-Brabant</value></item>
            <item><key>10297</key><value>Zutphen</value></item>
            <item><key>10298</key><value>Zwolle</value></item>
        </valuelist>
    </xsl:variable>
    <xsl:variable name="proceduresoortLookupList">
        <valuelist>
            <item><key>art80aro</key><value>Artikel 80a RO-zaken</value></item>
            <item><key>art81ro</key><value>Artikel 81 RO-zaken</value></item>
            <item><key>belemmeringenwet</key><value>Belemmeringenwet Privaatrecht</value></item>
            <item><key>beschikking</key><value>Beschikking</value></item>
            <item><key>bodemzaak</key><value>Bodemzaak</value></item>
            <item><key>cassatie</key><value>Cassatie</value></item>
            <item><key>cassatiebelangwet</key><value>Cassatie in het belang der wet</value></item>
            <item><key>conservatoiremaatr</key><value>Conservatoire maatregel</value></item>
            <item><key>eersteaanlegenkel</key><value>Eerste aanleg - enkelvoudig</value></item>
            <item><key>eersteaanlegmeer</key><value>Eerste aanleg - meervoudig</value></item>
            <item><key>eersteenigeaanleg</key><value>Eerste en enige aanleg</value></item>
            <item><key>geheimhoudingsbeslissing</key><value>Geheimhoudingsbeslissing</value></item>
            <item><key>herroeping</key><value>Herroeping</value></item>
            <item><key>herziening</key><value>Herziening</value></item>
            <item><key>hogerberoep</key><value>Hoger beroep</value></item>
            <item><key>hogerberoepkortgeding</key><value>Hoger beroep kort geding</value></item>
            <item><key>kortgeding</key><value>Kort geding</value></item>
            <item><key>mondelingeuitspraak</key><value>Mondelinge uitspraak</value></item>
            <item><key>optegenspraak</key><value>Op tegenspraak</value></item>
            <item><key>peek</key><value>Peek</value></item>
            <item><key>prejudicieelverzoek</key><value>Prejudicieel verzoek</value></item>
            <item><key>prejudicielebeslissing</key><value>Prejudiciële beslissing</value></item>
            <item><key>prejudicielespoedprocedure</key><value>Prejudiciële spoedprocedure (PPU)</value></item>
            <item><key>proceskostenveroordeling</key><value>Proceskostenveroordeling</value></item>
            <item><key>procesverbaal</key><value>Proces-verbaal</value></item>
            <item><key>raadkamer</key><value>Raadkamer</value></item>
            <item><key>rekestprocedure</key><value>Rekestprocedure</value></item>
            <item><key>schadevergoedingsuitspraak</key><value>Schadevergoedingsuitspraak</value></item>
            <item><key>tussenbeschikking</key><value>Tussenbeschikking</value></item>
            <item><key>tussenuitspraak</key><value>Tussenuitspraak</value></item>
            <item><key>tussenuitspraakbestlus</key><value>Tussenuitspraak bestuurlijke lus</value></item>
            <item><key>uitspraaknaprejudicielebesl</key><value>Uitspraak na prejudiciële beslissing</value></item>
            <item><key>vereenvoudigdebehandeling</key><value>Vereenvoudigde behandeling</value></item>
            <item><key>verschoning</key><value>Verschoning</value></item>
            <item><key>versneldebehandeling</key><value>Versnelde behandeling</value></item>
            <item><key>verstek</key><value>Verstek</value></item>
            <item><key>verwijzingnahogeraad</key><value>Verwijzing na Hoge Raad</value></item>
            <item><key>verzet</key><value>Verzet</value></item>
            <item><key>voorlopigevoorziening</key><value>Voorlopige voorziening</value></item>
            <item><key>voorlopigevoorzieningbodem</key><value>Voorlopige voorziening+bodemzaak</value></item>
            <item><key>wraking</key><value>Wraking</value></item>
            <item><key>ncc</key><value>NCC</value></item>
        </valuelist>
    </xsl:variable>
    <xsl:variable name="sectionLookupList">
        <valuelist>
            <item><key>sec001</key><value>Uitspraak</value></item>
            <item><key>sec002</key><value>Beleid</value></item>
            <item><key>sec003</key><value>CAOnieuws</value></item>
            <item><key>sec004</key><value>Column</value></item>
            <item><key>sec005</key><value>Kamerstukken</value></item>
            <item><key>sec006</key><value>Literatuur</value></item>
            <item><key>sec007</key><value>Opinie</value></item>
            <item><key>sec008</key><value>Opleidingen/cursussen</value></item>
            <item><key>sec009</key><value>Overig</value></item>
            <item><key>sec010</key><value>Persberichten</value></item>
            <item><key>sec011</key><value>Personalia</value></item>
            <item><key>sec012</key><value>Rapporten</value></item>
            <item><key>sec013</key><value>Specialistenvereniging</value></item>
            <item><key>sec014</key><value>Statistieken</value></item>
            <item><key>sec015</key><value>Tips &amp; trucs</value></item>
            <item><key>sec016</key><value>Vakbladen</value></item>
            <item><key>sec017</key><value>Wetgeving</value></item>
            <item><key>sec018</key><value>Advies</value></item>
            <item><key>sec019</key><value>Maak uw keuze</value></item>
        </valuelist>
    </xsl:variable>
    <xsl:variable name="RubriekLookupList">
        <valuelist>
            <item><key>rub001</key><value>Afval</value></item>
            <item><key>rub002</key><value>Bodem</value></item>
            <item><key>rub003</key><value>Dierenwelzijn</value></item>
            <item><key>rub004</key><value>Europa</value></item>
            <item><key>rub005</key><value>Geluid</value></item>
            <item><key>rub006</key><value>Geur</value></item>
            <item><key>rub007</key><value>Landbouw</value></item>
            <item><key>rub008</key><value>Luchtvaart</value></item>
            <item><key>rub009</key><value>Milieueffectrapportage</value></item>
            <item><key>rub010</key><value>Strafrecht</value></item>
            <item><key>rub011</key><value>Advocaten tuchtrecht</value></item>
            <item><key>rub012</key><value>Medisch tuchtrecht</value></item>
            <item><key>rub013</key><value>Materieel recht</value></item>
            <item><key>rub014</key><value>Bijzondere procedures</value></item>
            <item><key>rub015</key><value>Procesrecht</value></item>
            <item><key>rub016</key><value>Sanctierecht</value></item>
            <item><key>rub017</key><value>Grondbeleid</value></item>
            <item><key>rub018</key><value>Grondexploitatie</value></item>
            <item><key>rub019</key><value>Grondverwerving</value></item>
            <item><key>rub020</key><value>Gronduitgifte</value></item>
            <item><key>rub021</key><value>Fiscale aspecten</value></item>
            <item><key>rub022</key><value>Bodem</value></item>
            <item><key>rub023</key><value>Ruimtelijke ordening</value></item>
            <item><key>rub024</key><value>Agrarische regelgeving</value></item>
            <item><key>rub025</key><value>Ontgrondingen</value></item>
            <item><key>rub026</key><value>Planschade/nadeelcompensatie</value></item>
            <item><key>rub027</key><value>Bouw</value></item>
            <item><key>rub028</key><value>Monumenten</value></item>
            <item><key>rub029</key><value>Natuur</value></item>
            <item><key>rub030</key><value>Handhaving</value></item>
            <item><key>rub031</key><value>Staats- en bestuursrecht</value></item>
            <item><key>rub032</key><value>Formeel recht</value></item>
            <item><key>rub033</key><value>Aanbesteding</value></item>
            <item><key>rub034</key><value>Definitie geneesmiddel</value></item>
            <item><key>rub035</key><value>Diergeneesmiddelen</value></item>
            <item><key>rub036</key><value>Mededingingsrecht</value></item>
            <item><key>rub037</key><value>Medische hulpmiddelen</value></item>
            <item><key>rub038</key><value>Octrooirecht</value></item>
            <item><key>rub039</key><value>Ongeregistreerd geneesmiddel</value></item>
            <item><key>rub040</key><value>Overige</value></item>
            <item><key>rub041</key><value>Reclame</value></item>
            <item><key>rub042</key><value>Wet openbaarheid van bestuur</value></item>
            <item><key>rub043</key><value>Zorgverzekeraars</value></item>
            <item><key>rub044</key><value>Ondernemingsrecht</value></item>
            <item><key>rub045</key><value>Bank- en effectenrecht</value></item>
            <item><key>rub046</key><value>Financiering, zekerheden en insolventie</value></item>
            <item><key>rub047</key><value>Varia</value></item>
            <item><key>rub048</key><value>Uitspraken EHRM</value></item>
            <item><key>rub049</key><value>Ontvankelijkheidsbeslissingen EHRM</value></item>
            <item><key>rub050</key><value>Bedrijfs- en beroepsaansprakelijkheid</value></item>
            <item><key>rub051</key><value>Medische aansprakelijkheid</value></item>
            <item><key>rub052</key><value>Schadevergoeding en verjaring</value></item>
            <item><key>rub053</key><value>Verzekeringen</value></item>
            <item><key>rub054</key><value>Werkgeversaansprakelijkheid</value></item>
            <item><key>rub055</key><value>Deelgeschillen</value></item>
            <item><key>rub056</key><value>Relatievermogensrecht</value></item>
            <item><key>rub057</key><value>Echtscheiding en scheiding van tafel en bed</value></item>
            <item><key>rub058</key><value>Levensonderhoud voor de ex-partner en kinderalimentatie</value></item>
            <item><key>rub059</key><value>Afstamming en adoptie</value></item>
            <item><key>rub060</key><value>Minderjarigheid, gezag en omgang</value></item>
            <item><key>rub061</key><value>Jeugdhulp, jeugdbescherming</value></item>
            <item><key>rub062</key><value>Diversen</value></item>
            <item><key>rub063</key><value>Commissie van Aanbestedingsexperts</value></item>
            <item><key>rub065</key><value>Werkloosheid</value></item>
            <item><key>rub066</key><value>Bijstand</value></item>
            <item><key>rub067</key><value>Volksverzekeringen</value></item>
            <item><key>rub068</key><value>Ziektekosten en voorzieningen</value></item>
            <item><key>rub069</key><value>Verzekeringsplicht</value></item>
            <item><key>rub070</key><value>Boeten, maatregelen, terug- en invordering</value></item>
            <item><key>rub071</key><value>Internationaal</value></item>
            <item><key>rub072</key><value>Behandelingsovereenkomst en medische aansprakelijkheid</value></item>
            <item><key>rub073</key><value>Tuchtrecht</value></item>
            <item><key>rub074</key><value>Grondrechten</value></item>
            <item><key>rub075</key><value>Regulering</value></item>
            <item><key>rub076</key><value>Ziektekostenverzekeringen</value></item>
            <item><key>rub077</key><value>Regelgeving</value></item>
            <item><key>rub078</key><value>Redactioneel</value></item>
            <item><key>rub079</key><value>Rechtspraak</value></item>
            <item><key>rub080</key><value>Ziekte en re-integratie</value></item>
            <item><key>rub081</key><value>Arbeidsongeschiktheid</value></item>
            <item><key>rub082</key><value>Dagloon</value></item>
            <item><key>rub083</key><value>Premie</value></item>
            <item><key>rub084</key><value>Studiefinanciering</value></item>
            <item><key>rub085</key><value>Uitspraken HvJ EU</value></item>
            <item><key>rub086</key><value>Openbaarheid en privacy</value></item>
            <item><key>rub087</key><value>Governance</value></item>
            <item><key>rub088</key><value>Overeenkomsten</value></item>
            <item><key>rub089</key><value>Maatschappelijke ondersteuning</value></item>
            <item><key>rub090</key><value>Mededinging</value></item>
            <item><key>rub091</key><value>Arbeidsrecht</value></item>
            <item><key>rub092</key><value>Personen- en familierecht</value></item>
            <item><key>rub093</key><value>Civiel recht</value></item>
            <item><key>rub094</key><value>Bestuursrecht</value></item>
            <item><key>rub095</key><value>Verkeer en infrastructuur</value></item>
            <item><key>rub096</key><value>Dood/zwaar lichamelijk letsel door schuld</value></item>
            <item><key>rub097</key><value>Rijden onder invloed</value></item>
            <item><key>muk</key><value>Maak uw keuze</value></item>
            <item><key>rub098</key><value>Apotheker</value></item>
            <item><key>rub099</key><value>Preferentiebeleid</value></item>
            <item><key>rub100</key><value>Archeologie</value></item>
            <item><key>rub101</key><value>Asbest</value></item>
            <item><key>rub102</key><value>Bemesting</value></item>
            <item><key>rub103</key><value>Bodembepalingen in bestemmingsplannen</value></item>
            <item><key>rub104</key><value>Bodembepalingen in omgevingsvergunningen</value></item>
            <item><key>rub105</key><value>Bodembescherming: bestuurlijk</value></item>
            <item><key>rub106</key><value>Bodembescherming: civiel</value></item>
            <item><key>rub107</key><value>Bodemdaling</value></item>
            <item><key>rub108</key><value>Bodemsanering: bestuurlijk</value></item>
            <item><key>rub109</key><value>Bodemsanering: civiel</value></item>
            <item><key>rub110</key><value>Explosieven</value></item>
            <item><key>rub111</key><value>Ondergrondse trillingen, ondergronds lawaai en bodemvervuiling in de zin van trillingen en ondergronds geluid</value></item>
            <item><key>rub112</key><value>Onteigeningen m.b.t. bodemverontreiniging en bodemkwaliteit</value></item>
            <item><key>rub113</key><value>Onteigeningen m.b.t. omgevingsrecht, bestemmingsplannen, wegenbouw e.d.</value></item>
            <item><key>rub114</key><value>Strafzaken bodemsanering</value></item>
            <item><key>rub115</key><value>Strafzaken bodembescherming</value></item>
            <item><key>rub116</key><value>Verdroging</value></item>
            <item><key>rub117</key><value>Verzuring</value></item>
            <item><key>rub118</key><value>Warmte en koude</value></item>
            <item><key>rub119</key><value>Externe veiligheid</value></item>
            <item><key>rub120</key><value>Milieustrafrecht</value></item>
            <item><key>rub121</key><value>Milieustrafrecht/Afval</value></item>
            <item><key>rub122</key><value>Natuurbescherming/Milieueffectrapportage</value></item>
            <item><key>rub123</key><value>Strafrecht/Natuur</value></item>
            <item><key>rub124</key><value>Wet Mulder</value></item>
            <item><key>rub125</key><value>Europa/Energie</value></item>
            <item><key>rub126</key><value>Europa/Bestuursrecht</value></item>
            <item><key>rub128</key><value>Milieustrafrecht/Ontgronding</value></item>
            <item><key>rub129</key><value>Overheidsaansprakelijkheid</value></item>
            <item><key>rub130</key><value>Risicoaansprakelijkheid</value></item>
            <item><key>rub131</key><value>Bestuurs(proces)recht</value></item>
            <item><key>rub132</key><value>Omgevingsrecht</value></item>
            <item><key>rub127</key><value>Externe veiligheid/Geluid</value></item>
            <item><key>rub133</key><value>Gaswinning/Bestuursrecht</value></item>
            <item><key>rub134</key><value>Milieustrafrecht/Dierenwelzijn/Landbouw</value></item>
            <item><key>rub135</key><value>Milieustrafrecht/Gewasbeschermingsmiddelen en biociden</value></item>
            <item><key>rub136</key><value>Milieustrafrecht/Landbouw</value></item>
            <item><key>rub137</key><value>Bescherming van meerderjarigen</value></item>
            <item><key>rub138</key><value>Off-label gebruik</value></item>
            <item><key>rub139</key><value>(Poging tot) doodslag </value></item>
            <item><key>rub140</key><value>Biologische productie</value></item>
            <item><key>rub141</key><value>Microbiologische criteria levensmiddelen</value></item>
            <item><key>rub142</key><value>Toezicht en controle</value></item>
            <item><key>rub143</key><value>Voedings- en gezondheidsclaims</value></item>
            <item><key>rub144</key><value>Voedselfraude</value></item>
            <item><key>rub145</key><value>Voedselinformatie</value></item>
            <item><key>rub146</key><value>Natuurbescherming</value></item>
            <item><key>rub147</key><value>Bestrijdingsmiddelen</value></item>
            <item><key>rub148</key><value>Kernenergie</value></item>
            <item><key>rub149</key><value>Gevaarlijke stoffen</value></item>
            <item><key>rub150</key><value>Lucht</value></item>
            <item><key>rub151</key><value>Aanbestedingsrecht</value></item>
            <item><key>rub152</key><value>Arbeidsomstandigheden</value></item>
            <item><key>rub153</key><value>Visserij</value></item>
            <item><key>rub154</key><value>Voedselveiligheid</value></item>
            <item><key>rub155</key><value>Overige onderwerpen</value></item>
            <item><key>rub156</key><value>Vuurwerk</value></item>
            <item><key>rub157</key><value>Bestuurlijke boete</value></item>
            <item><key>rub158</key><value>Aanvullend beschermingscertificaat</value></item>
            <item><key>rub159</key><value>Handelsvergunning</value></item>
            <item><key>rub160</key><value>Hygiëne</value></item>
            <item><key>rub161</key><value>Beschermde benamingen</value></item>
            <item><key>rub162</key><value>(Poging tot) zware mishandeling</value></item>
            <item><key>rub163</key><value>Rijbewijs</value></item>
            <item><key>rub164</key><value>Gebruik van de weg</value></item>
            <item><key>rub165</key><value>Burgerlijke stand</value></item>
            <item><key>rub166</key><value>Huwelijk en geregistreerd partnerschap</value></item>
            <item><key>rub167</key><value>Controle</value></item>
            <item><key>rub168</key><value>Duurzaamheid</value></item>
            <item><key>rub169</key><value>Chemische stoffen</value></item>
            <item><key>rub170</key><value>Transport</value></item>
            <item><key>rub171</key><value>Mijnbouw</value></item>
            <item><key>rub172</key><value>Financieel recht</value></item>
            <item><key>rub173</key><value>Luchtkwaliteit</value></item>
            <item><key>rub174</key><value>BRZO</value></item>
            <item><key>rub175</key><value>Apotheekhoudende huisartsen</value></item>
            <item><key>rub176</key><value>Parallelimport</value></item>
            <item><key>rub177</key><value>Naamrecht</value></item>
            <item><key>rub178</key><value>Echtscheiding, scheiding van tafel en bed en ontbinding geregistreerd partnerschap</value></item>
            <item><key>rub179</key><value>Gewasbeschermingsmiddelen</value></item>
            <item><key>rub180</key><value>Openbare orde en veiligheid</value></item>
            <item><key>rub181</key><value>Wet geneesmiddelenprijzen</value></item>
            <item><key>rub182</key><value>Geneesmiddelenvergoedingssysteem</value></item>
            <item><key>rub183</key><value>Burgerlijk recht</value></item>
            <item><key>rub184</key><value>Klimaat</value></item>
            <item><key>rub185</key><value>GMO</value></item>
        </valuelist>
    </xsl:variable>
    <xsl:variable name="DoelgroepLookupList">
        <valuelist>
            <item><key>generalist</key><value>Generalist</value></item>
            <item><key>specialist</key><value>Specialist</value></item>
            <item><key>notariaat</key><value>Notariaat</value></item>
            <item><key>hse</key><value>HSE</value></item>
            <item><key>praktijk</key><value>Praktijk</value></item>
            <item><key>ondernemingsraad</key><value>Ondernemingsraad</value></item>
            <item><key>zeevaart</key><value>Zeevaart</value></item><item><key>gemeenten</key><value>Gemeenten</value></item>
            <item><key>salarisadministratie</key><value>Salarisadministratie</value></item>
            <item><key>onderwijsmanagement</key><value>Onderwijsmanagement</value></item>
            <item><key>sduarbeidsrecht</key><value>Sduarbeidsrecht</value></item>
            <item><key>fiscaalenmeer</key><value>FiscaalenMeer</value></item>
            <item><key>taxvice</key><value>Taxvice</value></item>
            <item><key>fiscalewettenbundel</key><value>Fiscalewettenbundel</value></item>
            <item><key>ndfr</key><value>NDFR</value></item>
            <item><key>guidancehuurrecht</key><value>GuidanceHuurrecht</value></item>
            <item><key>guidanceprivacy</key><value>GuidancePrivacy</value></item>
            <item><key>onlinewettenverzameling</key><value>Onlinewettenverzameling</value></item>
            <item><key>bzadvies</key><value>BelastingzakenAdvies</value></item>
            <item><key>transportzone</key><value>Transportzone</value></item>
            <item><key>financiallawhub</key><value>FinancialLawHub</value></item>
            <item><key>inenuitvoer</key><value>inenuitvoer</value></item>
            <item><key>covid19</key><value>COVID-19</value></item>
            <item><key>migratierecht</key><value>Migratierecht</value></item>
            <item><key>nationaliteitsrecht</key><value>Nationaliteitsrecht</value></item></valuelist>
    </xsl:variable>

    <!-- identity transform-->

    <xsl:template match="*">
        <xsl:element name="{local-name()}">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@*">
        <xsl:attribute name="{local-name()}"><xsl:value-of select="."/></xsl:attribute>
    </xsl:template>

    <!--logfile-->
    <xsl:template match="document[@documentTypeName='Lijmteksten']">
        <xsl:copy><xsl:copy-of select="@*|node()"/></xsl:copy>
    </xsl:template>
    <!-- end logfile-->

    <xsl:template match="document[@documentTypeName='Auteursbeschrijvingen']">
        <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
    </xsl:template>

    <xsl:template match="document[@documentTypeName='Auteursbeschrijvingen']//meta">
        <meta>
            <publicationName>
                <xsl:choose>
                    <xsl:when test="ancestor::folder//published/publicationName">
                        <xsl:for-each select="ancestor::folder//published/publicationName">
                            <item key="{translate(identifier,$upper-case,$lower-case)}">
                                <xsl:text> </xsl:text>
                            </item>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:otherwise>
                        <item key="muk"><xsl:text> </xsl:text></item>
                    </xsl:otherwise>
                </xsl:choose>
            </publicationName>
            <xsl:copy-of select="@*|node()"/>
        </meta>
    </xsl:template>

    <xsl:template match="folder[@documentTypeName='Jurisprudentie']/meta">
        <meta>
            <legalArea>
                <xsl:for-each select="parent::folder//legalArea">
                    <xsl:variable name="legalAreacode" select="translate(identifier,$upper-case,$lower-case)"/>
                    <item key="{identifier}"><xsl:text> </xsl:text></item>
                </xsl:for-each>
                <xsl:if test="not(parent::folder//legalArea)">
                    <item key="muk"><xsl:text> </xsl:text></item>
                </xsl:if>
            </legalArea>
            <audience><item key="specialist"><xsl:text> </xsl:text></item></audience>
            <dc-type><item key="jur.publ"><xsl:text> </xsl:text></item></dc-type>
            <isPartOf key="sdu-j"><xsl:text> </xsl:text></isPartOf>
            <prism-volume/>
            <year>
                <xsl:value-of select="parent::folder//published[1]/publicationYear"/>
            </year>
            <xsl:choose>
                <xsl:when test="parent::folder//published[1]/publicationDate">
                    <publicationDateFolio>
                        <xsl:value-of select="concat(parent::folder//published[1]/publicationDate,'T00:00:00')"/>
                    </publicationDateFolio>
                </xsl:when>
                <xsl:otherwise>
                    <publicationDateFolio/>
                </xsl:otherwise>
            </xsl:choose>
            <publicationName
                    key="{translate(parent::folder//published[1]/publicationName/identifier,$upper-case,$lower-case)}">
                <xsl:text> </xsl:text>
            </publicationName>
            <startpagina/>
            <prism-number>
                <xsl:value-of select="parent::folder//published[1]/number"/>
            </prism-number>
        </meta>
    </xsl:template>

    <xsl:template match="folder[@documentTypeName='Nieuws']/meta">
        <meta>
            <legalArea>
                <xsl:for-each select="parent::folder//legalArea">
                    <xsl:variable name="legalAreacode" select="translate(identifier,$upper-case,$lower-case)"/>
                    <item key="{identifier}"><xsl:text> </xsl:text></item>
                </xsl:for-each>
                <xsl:if test="not(parent::folder//legalArea)">
                    <item key="muk"><xsl:text> </xsl:text></item>
                </xsl:if>
            </legalArea>
            <audience>
                <xsl:variable name="DoelgroepLookupListImport">
                    <valuelist>
                        <xsl:for-each select="exslt:node-set($DoelgroepLookupList)//item">
                            <item>
                                <xsl:copy-of select="key"/>
                                <value><xsl:value-of select="translate(value,$upper-case,$lower-case)"/></value>
                            </item>
                        </xsl:for-each>
                    </valuelist>
                </xsl:variable>
                <xsl:for-each select="parent::folder//audience">
                    <xsl:variable name="check" select="text()"/>
                    <item key="{exslt:node-set($DoelgroepLookupListImport)//item[value=$check][1]/key}"><xsl:text> </xsl:text></item>
                </xsl:for-each>
                <xsl:if test="not(parent::folder//audience)">
                    <item key="specialist"><xsl:text> </xsl:text></item>
                </xsl:if>
            </audience>
            <year>
                <xsl:choose>
                    <xsl:when test="parent::folder//published[1]/publicationYear">
                        <xsl:value-of select="parent::folder//published[1]/publicationYear"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="'Vul in'"/>
                    </xsl:otherwise>
                </xsl:choose>
            </year>
        </meta>
    </xsl:template>


    <xsl:template match="metadata">
        <meta>
            <xsl:choose>
                <xsl:when test="creator[role='auteur'] and source='publishone'">
                    <dc-creator>
                        <xsl:for-each select="creator">
                            <item key="{identifier}">
                                <xsl:text> </xsl:text>
                            </item>
                        </xsl:for-each>
                    </dc-creator>
                </xsl:when>
                <xsl:when test="type='annotatie' or contains(type,'cmt.') or type='nieuws'">
                    <dc-creator>
                        <item key="muk">
                            <xsl:text> </xsl:text>
                        </item>
                    </dc-creator>
                </xsl:when>
            </xsl:choose>

            <xsl:if test="abstract and contains(type,'cmt.')">
                <abstract>
                    <xsl:apply-templates select="abstract" mode="markdown"/>
                </abstract>
            </xsl:if>

            <xsl:choose>
                <xsl:when test="type='pn' or substring(type,1,5)='tools'">
                    <legalArea>
                        <xsl:for-each select="legalArea">
                            <xsl:variable name="legalAreacode"
                                          select="translate(identifier,$upper-case,$lower-case)"/>
                            <item key="{identifier}">
                                <xsl:text> </xsl:text>
                            </item>
                        </xsl:for-each>
                    </legalArea>
                </xsl:when>
                <xsl:when test="type='annotatie' or type='nieuws'"/>
                <xsl:when test="source='jbs' or (type='jur.publ' and source='publishone')">
                    <add-legalArea/>
                </xsl:when>
                <xsl:otherwise>
                    <add-legalArea>
                        <xsl:for-each select="legalArea">
                            <xsl:variable name="legalAreacode"
                                          select="translate(identifier,$upper-case,$lower-case)"/>
                            <item key="{identifier}">
                                <xsl:text> </xsl:text>
                            </item>
                        </xsl:for-each>
                    </add-legalArea>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:if test="property[name[.='add-audience']]">
                <add-audience>
                    <xsl:for-each select="property[name[.='add-audience']]">
                        <item key="{key}">
                            <xsl:text> </xsl:text>
                        </item>
                    </xsl:for-each>
                </add-audience>
            </xsl:if>

            <xsl:if test="property[name[.='rechtsthema']] and contains(type,'cmt.') ">
                <rechtsthema>
                    <xsl:for-each select="property[name[.='rechtsthema']]">
                        <item key="{key}">
                            <xsl:text> </xsl:text>
                        </item>
                    </xsl:for-each>
                </rechtsthema>
            </xsl:if>

            <xsl:if test="type='nieuws'">
                <xsl:choose>
                    <xsl:when test="isPartOf[.='smt']">
                        <isPartOf key="smt"><xsl:text> </xsl:text></isPartOf>
                    </xsl:when>
                    <xsl:when test="isPartOf[.='nfx']">
                        <isPartOf key="nfx"><xsl:text> </xsl:text></isPartOf>
                    </xsl:when>
                    <xsl:when test="isPartOf[.='sdu-cao']">
                        <isPartOf key="sdu-cao"><xsl:text> </xsl:text></isPartOf>
                    </xsl:when>
                    <xsl:when test="isPartOf[.='transportzone']">
                        <isPartOf key="transportzone"><xsl:text> </xsl:text></isPartOf>
                    </xsl:when>
                    <xsl:when test="isPartOf[.='nd']">
                        <isPartOf key="nd"><xsl:text> </xsl:text></isPartOf>
                    </xsl:when>
                    <xsl:when test="isPartOf[.='njo']">
                        <isPartOf key="njo"><xsl:text> </xsl:text></isPartOf>
                    </xsl:when>
                    <xsl:when test="isPartOf[.='bz']">
                        <isPartOf key="bz"><xsl:text> </xsl:text></isPartOf>
                    </xsl:when>
                    <xsl:when test="isPartOf[.='sdu-nieuws'] and count(isPartOf) = 1">
                        <isPartOf key="algemeen"><xsl:text> </xsl:text></isPartOf>
                    </xsl:when>
                </xsl:choose>
            </xsl:if>

            <xsl:if test="property[name[.='type-bericht']] and not(type='nieuws')">
                <isPartOf key="{property[name[.='type-bericht']]/key}">
                    <xsl:text> </xsl:text>
                </isPartOf>
            </xsl:if>

            <xsl:if test="property[name[.='schoolmanagement-onderwijssoort']]">
                <schoolmanagement-onderwijssoort>
                    <xsl:for-each select="property[name[.='schoolmanagement-onderwijssoort']]">
                        <item key="{translate(key,$upper-case,$lower-case)}"><xsl:text> </xsl:text></item>
                    </xsl:for-each>
                </schoolmanagement-onderwijssoort>
            </xsl:if>

            <xsl:if test="language and contains(type,'cmt.')">
                <language key="{language}">
                    <xsl:text> </xsl:text>
                </language>
            </xsl:if>

            <xsl:if test="not(language) and contains(type,'cmt.')">
                <language key="nl">
                    <xsl:text> </xsl:text>
                </language>
            </xsl:if>

            <xsl:if test="not(type='jur.publ' and source='publishone') and published/section">
                <xsl:variable name="value" select="published/section"/>
                <xsl:variable name="check" select="exslt:node-set($sectionLookupList)//item[value=$value][1]/key"></xsl:variable>
                <xsl:variable name="key">
                    <xsl:choose>
                        <xsl:when test="$check!=''"><xsl:value-of select="$check"/></xsl:when>
                        <xsl:otherwise><xsl:value-of select="'muk'"/></xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <prism-section key="{$key}"><xsl:text> </xsl:text></prism-section>
            </xsl:if>

            <xsl:if test="(type='jur.publ' and source='publishone') and published/section">
                <xsl:variable name="value" select="published/section"/>
                <xsl:variable name="rubriekkey" select="exslt:node-set($RubriekLookupList)//item[value=$value]"/>
                <xsl:choose>
                    <xsl:when test="$rubriekkey/key!=''">
                        <prism-section>
                            <item key="{$rubriekkey/key}"><xsl:text> </xsl:text></item>
                        </prism-section>
                    </xsl:when>
                    <xsl:otherwise>
                        <prism-section>
                            <item key="muk"><xsl:text> </xsl:text></item>
                        </prism-section>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:if>


            <xsl:if test="published/publicationName and contains(type,'cmt.')">
                <publicationName>
                    <xsl:for-each select="published/publicationName">
                        <item key="{translate(identifier,$upper-case,$lower-case)}">
                            <xsl:text> </xsl:text>
                        </item>
                    </xsl:for-each>
                </publicationName>
            </xsl:if>

            <xsl:if test="published/publicationName and type='nieuws'">
                <publicationName>
                    <xsl:for-each select="published/publicationName">
                        <item key="{identifier}">
                            <xsl:text> </xsl:text>
                        </item>
                    </xsl:for-each>
                </publicationName>
            </xsl:if>

            <xsl:choose>
                <xsl:when test="published/publicationNumber">
                    <publicationNumber>
                        <xsl:value-of select="published/publicationNumber"/>
                    </publicationNumber>
                </xsl:when>
                <xsl:when test="type[.='jur.bron']">
                    <publicationNumber>?</publicationNumber>
                </xsl:when>
                <xsl:when test="type='jur.publ' and source='publishone'">
                    <publicationNumber>0</publicationNumber>
                </xsl:when>
            </xsl:choose>

            <xsl:if test="legalAspect">
                <legalAspect>
                    <xsl:for-each select="legalAspect">
                        <item key="{identifier}">
                            <xsl:text> </xsl:text>
                        </item>
                    </xsl:for-each>
                </legalAspect>
            </xsl:if>

            <xsl:choose>
                <xsl:when test="type[.='jur.bron'] or source='jbs' or (type='jur.publ' and source='publishone')"/>
                <xsl:when test="type='annotatie' or contains(type,'cmt.')"/>
                <xsl:otherwise>
                    <dc-type>
                        <xsl:for-each select="type[substring-after(.,'.')!= 'nieuws']">
                            <xsl:variable name="type">
                                <xsl:value-of select="."/>
                            </xsl:variable>
                            <item key="{$type}">
                                <xsl:text> </xsl:text>
                            </item>
                        </xsl:for-each>
                    </dc-type>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:if test="subject">
                <subject>
                    <xsl:for-each select="subject">
                        <item key="{identifier}">
                            <xsl:text> </xsl:text>
                        </item>
                    </xsl:for-each>
                </subject>
            </xsl:if>

            <xsl:if test="keyword">
                <dc-keyword>
                    <xsl:for-each select="keyword">
                        <item>
                            <xsl:value-of select="."/>
                        </item>
                    </xsl:for-each>
                </dc-keyword>
            </xsl:if>

            <xsl:if test="source='jbs' and altKey">
                <altKey>
                    <xsl:for-each select="altKey">
                        <item>
                            <xsl:value-of select="."/>
                        </item>
                    </xsl:for-each>
                </altKey>
            </xsl:if>

            <xsl:if test="(type='jur.publ' and source='publishone') and altKey">
                <altKey>
                    <xsl:for-each select="altKey">
                        <item>
                            <xsl:value-of select="."/>
                        </item>
                    </xsl:for-each>
                </altKey>
            </xsl:if>

            <xsl:if test="source='jbs' and rubriek">
                <xsl:variable name="rubriekkey" select="exslt:node-set($RubriekLookupList)//item[value=.]"/>
                <xsl:choose>
                    <xsl:when test="$rubriekkey/key!=''">
                        <prism-section>
                            <item key="{$rubriekkey/key}"><xsl:text> </xsl:text></item>
                        </prism-section>
                    </xsl:when>
                    <xsl:otherwise>
                        <prism-section>
                            <item key="muk"><xsl:text> </xsl:text></item>
                        </prism-section>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:if>

            <xsl:choose>
                <xsl:when test="type='pn' or substring(type,1,5)='tools'">
                    <prism-receptionDate>
                        <xsl:choose>
                            <xsl:when
                                    test="not(normalize-space(date[type[. ='editorial']]/value)='')">
                                <xsl:value-of
                                        select="concat(date[type[. ='editorial']]/value,'T00:00:00')"/>
                            </xsl:when>
                            <xsl:otherwise>0001-01-01T00:00:00</xsl:otherwise>
                        </xsl:choose>
                    </prism-receptionDate>
                </xsl:when>
                <xsl:otherwise>
                    <editorialDate>
                        <xsl:choose>
                            <xsl:when
                                    test="not(normalize-space(date[type[. ='editorial']]/value)='')">
                                <xsl:value-of
                                        select="concat(date[type[. ='editorial']]/value,'T00:00:00')"/>
                            </xsl:when>
                            <xsl:when test="not(documentDate='')">
                                <xsl:value-of select="concat(documentDate,'T00:00:00')"/>
                            </xsl:when>
                            <xsl:otherwise>0001-01-01T00:00:00</xsl:otherwise>
                        </xsl:choose>
                    </editorialDate>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:if test="type[.='nieuws'] and starts-with(about/altKey,'caonr:')">
                <aboutAltKey>
                    <xsl:for-each select="about/altKey[starts-with(.,'caonr:')]">
                        <item><xsl:value-of select="substring-after(.,'caonr:')"/></item>
                    </xsl:for-each>
                </aboutAltKey>
            </xsl:if>

            <xsl:if test="type[.='nieuws'] and teaser">
                <teaser>
                    <xsl:value-of select="teaser"/>
                </teaser>
            </xsl:if>

            <xsl:if test="type[.='jur.bron'] or source='jbs' or (type='jur.publ' and source='publishone')">
                <xsl:if test="//contributor/role[.='concaut']">
                    <concaut>
                        <xsl:value-of select="//contributor[role[.='concaut']]/name"/>
                    </concaut>
                </xsl:if>
                <aboutCaseDate>
                    <xsl:if test="not(normalize-space(about[1]/caseDate)='')">
                        <xsl:value-of select="concat(about[1]/caseDate,'T00:00:00')"/>
                    </xsl:if>
                </aboutCaseDate>
                <aboutIdentifier>
                    <xsl:for-each select="about/identifier">
                        <item>
                            <xsl:value-of select="translate(.,'_',':')"/>
                        </item>
                    </xsl:for-each>
                </aboutIdentifier>
                <xsl:if test="about[1]/caseNumber">
                    <aboutCaseNumber>
                        <xsl:for-each select="about/caseNumber">
                            <xsl:if test="not(. = preceding::about/caseNumber/.)">
                                <item>
                                    <xsl:value-of select="."/>
                                </item>
                            </xsl:if>
                        </xsl:for-each>
                    </aboutCaseNumber>
                </xsl:if>
                <xsl:if test="about[1]/caseType">
                    <aboutCaseType>
                        <xsl:for-each select="about[1]/caseType">
                            <item>
                                <xsl:variable name="value" select="normalize-space(.)"/>
                                <xsl:attribute name="key">
                                    <xsl:for-each
                                            select="exslt:node-set($proceduresoortLookupList)//item[value=$value][1]">
                                        <xsl:value-of select="key"/>
                                    </xsl:for-each>
                                </xsl:attribute>
                                <xsl:text> </xsl:text>
                            </item>
                        </xsl:for-each>
                    </aboutCaseType>
                </xsl:if>
            </xsl:if>

            <xsl:choose>
                <xsl:when test="type[.='jur.bron'] or source='jbs' or (type='jur.publ' and source='publishone')">
                    <xsl:call-template name="jurCreator">
                        <xsl:with-param name="naam" select="about/creator/name"/>
                    </xsl:call-template>
                    <xsl:variable name="zittingsplaats">
                        <xsl:for-each select="//p[starts-with(.,'Zittingsplaats ')][1]">
                            <xsl:value-of
                                    select="normalize-space(substring-after(.,'Zittingsplaats '))"/>
                        </xsl:for-each>
                    </xsl:variable>
                    <xsl:if test="not(normalize-space($zittingsplaats)='')">
                        <xsl:variable name="keyZittingsplaats">
                            <xsl:variable name="apos">'</xsl:variable>
                            <xsl:for-each
                                    select="exslt:node-set($plaatsLookupList)//item[value=translate($zittingsplaats,$apos,'’')][1]">
                                <xsl:value-of select="key"/>
                            </xsl:for-each>
                        </xsl:variable>
                        <xsl:if test="not(normalize-space($keyZittingsplaats)='')">
                            <zittingsplaats key="{$keyZittingsplaats}">
                                <xsl:value-of select="$zittingsplaats"/>
                            </zittingsplaats>
                        </xsl:if>
                    </xsl:if>
                </xsl:when>
            </xsl:choose>

            <xsl:choose>
                <xsl:when test="type[.='jur.bron'] or source='jbs' or contains(type,'cmt.') or (type='jur.publ' and source='publishone')"/>
                <xsl:when test="type='annotatie'"/>
                <xsl:otherwise>
                    <publication key="online">
                        <xsl:text> </xsl:text>
                    </publication>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:choose>
                <xsl:when test="type='annotatie'"/>
                <xsl:otherwise>
                    <documentKey>
                        <xsl:choose>
                            <xsl:when test="type[.='jur.bron']"/>
                            <xsl:otherwise>
                                <xsl:value-of select="identifier"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </documentKey>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:if test="html/head/meta[@name = 'description']">
                <description>
                    <xsl:value-of select="html/head/meta[@name = 'description']/@content"/>
                </description>
            </xsl:if>

        </meta>
    </xsl:template>

    <xsl:template name="jurCreator">
        <xsl:param name="naam"/>
        <xsl:param name="plaatsDeel"/>

        <xsl:variable name="apos">'</xsl:variable>

        <xsl:variable name="naamDeel"
                      select="normalize-space(substring($naam,1,string-length($naam)-string-length($plaatsDeel)))"/>
        <xsl:variable name="key">
            <xsl:for-each select="exslt:node-set($collegeLookupList)//item[value=$naamDeel][1]">
                <xsl:value-of select="key"/>
            </xsl:for-each>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="not(normalize-space($key)='')">
                <college key="{$key}">
                    <xsl:value-of select="$naamDeel"/>
                </college>
                <!--
                <xsl:if test="$iteratie=1 and normalize-space($plaatsDeel)=''">
                    <vestigingsplaats/>
                </xsl:if>
                -->
            </xsl:when>
            <xsl:when test="contains(normalize-space(substring($naam,1,string-length($naam)-string-length($plaatsDeel))),' ')">
                <xsl:variable name="plaats">
                    <xsl:call-template name="substring-after-last-space">
                        <xsl:with-param name="input" select="$naamDeel"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="keyPlaats">
                    <xsl:choose>
                        <xsl:when test="normalize-space($plaatsDeel)=''">
                            <xsl:for-each
                                    select="exslt:node-set($plaatsLookupList)//item[value=translate($plaats,$apos,'’')][1]">
                                <xsl:value-of select="key"/>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:for-each
                                    select="exslt:node-set($plaatsLookupList)//item[value=concat($plaats,' ',$plaatsDeel)][1]">
                                <xsl:value-of select="key"/>
                            </xsl:for-each>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="not(normalize-space($keyPlaats)='')">
                        <xsl:call-template name="jurCreator">
                            <xsl:with-param name="naam" select="$naam"/>
                            <xsl:with-param name="plaatsDeel">
                                <xsl:choose>
                                    <xsl:when test="normalize-space($plaatsDeel)=''">
                                        <xsl:value-of select="$plaats"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="concat($plaats,' ',$plaatsDeel)"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:with-param>
                        </xsl:call-template>
                        <vestigingsplaats key="{$keyPlaats}">
                            <xsl:choose>
                                <xsl:when test="normalize-space($plaatsDeel)=''">
                                    <xsl:value-of select="$plaats"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="concat($plaats,' ',$plaatsDeel)"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </vestigingsplaats>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="jurCreator">
                            <xsl:with-param name="naam" select="$naam"/>
                            <xsl:with-param name="plaatsDeel">
                                <xsl:choose>
                                    <xsl:when test="normalize-space($plaatsDeel)=''">
                                        <xsl:value-of select="$plaats"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="concat($plaats,' ',$plaatsDeel)"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="type[.='jur.bron']">
                    <xsl:message terminate="yes">Check college: <xsl:value-of select="$naam"/></xsl:message>
                </xsl:if>
                <college key="muk">
                    <xsl:text> </xsl:text>
                </college>
                <!-- <vestigingsplaats/> -->
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="substring-after-last-space">
        <xsl:param name="input"/>
        <xsl:choose>
            <xsl:when test="contains($input,' ')">
                <xsl:call-template name="substring-after-last-space">
                    <xsl:with-param name="input" select="substring-after($input,' ')"/>
                    <xsl:with-param name="marker" select="' '"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$input"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- mark down templates -->

    <xsl:template match="node()|@*" mode="markdown">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*" mode="markdown"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="abstract|div" mode="markdown">
        <xsl:apply-templates select="node()|@*" mode="markdown"/>
    </xsl:template>

    <xsl:template match="p" mode="markdown">
        <xsl:apply-templates select="node()|@*" mode="markdown"/>
    </xsl:template>

    <xsl:template match="em|i" mode="markdown">
        <xsl:text>_</xsl:text>
        <xsl:apply-templates select="* | text()" mode="markdown"/>
        <xsl:text>_</xsl:text>
    </xsl:template>

    <xsl:template match="strong|b" mode="markdown">
        <xsl:text>**</xsl:text>
        <xsl:apply-templates select="* | text()" mode="markdown"/>
        <xsl:text>**</xsl:text>
    </xsl:template>

    <xsl:template match="br" mode="markdown">
        <xsl:text>  </xsl:text>
    </xsl:template>

    <xsl:template match="ul|ol" mode="markdown">
        <xsl:if test="not(ancestor::ol) and not(ancestor::ul)">
            <xsl:text>&#xa;</xsl:text>
        </xsl:if>
        <xsl:apply-templates select="*" mode="markdown"/>
    </xsl:template>

    <xsl:template match="ul/li" mode="markdown">
        <xsl:text>* </xsl:text>
        <xsl:apply-templates select="* | text()" mode="markdown"/>
        <xsl:text>&#xa;</xsl:text>
    </xsl:template>

    <xsl:template match="*[self::ul|self::ol]/li/ul/li" mode="markdown">
        <xsl:text>&#xa;    * </xsl:text>
        <xsl:apply-templates select="* | text()" mode="markdown"/>
    </xsl:template>

    <xsl:template match="*[self::ul|self::ol]/li/*[self::ul|self::ol]/li/ul/li" mode="markdown">
        <xsl:text>&#xa;        * </xsl:text>
        <xsl:apply-templates select="* | text()" mode="markdown"/>
    </xsl:template>

    <xsl:template match="ol/li" mode="markdown">
        <xsl:value-of select="position()"/>
        <xsl:text>. </xsl:text>
        <xsl:apply-templates select="* | text()" mode="markdown"/>
        <xsl:text>&#xa;</xsl:text>
    </xsl:template>

    <xsl:template match="*[self::ul|self::ol]/li/ol/li" mode="markdown">
        <xsl:text>&#xa;    </xsl:text>
        <xsl:value-of select="position()"/>
        <xsl:text>. </xsl:text><xsl:apply-templates select="* | text()" mode="markdown"/>
    </xsl:template>

    <xsl:template match="*[self::ul|self::ol]/li/*[self::ul|self::ol]/li/ol/li" mode="markdown">
        <xsl:text>&#xa;        </xsl:text>
        <xsl:value-of select="position()"/>
        <xsl:text>. </xsl:text><xsl:apply-templates select="* | text()" mode="markdown"/>
    </xsl:template>

</xsl:stylesheet>