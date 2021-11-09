package components

import mockws.MockWSHelpers
import org.scalatest._

class SwsApiSpec extends AsyncWordSpec with MustMatchers with MockWSHelpers with BeforeAndAfterAll {

//  override def afterAll(): Unit = {
//    shutdownHelpers()
//  }
//
//  private def atomFeed(baseUrl: String, docs: Seq[String], nextPage: Option[String] = None) =
//    <feed>
//      { nextPage.map(np => <link rel="next" href={ np }/>).orNull }
//      { docs.map { d => <entry>
//            <id>{ baseUrl }/{ d }</id>
//            <category term="testFormat" scheme="http://schema.sdu.nl/2014/07/cwc-vocabulary/documentFormat"/>
//          </entry> } }
//    </feed>
//
//  "SwsApi" should {
//
//    "search and stream documents when there is only one page" in {
//      val baseUrl = "http://test.sdu.nl"
//      val fakeConfig = Configuration.from(Map("cwc.sws.url" -> baseUrl))
//      val mockWs = MockWS {
//        case (GET, "http://test.sdu.nl/?type=pn") => Action { Ok(atomFeed(baseUrl, Seq("1", "2", "3")).toString) }
//      }
//
//      val swsApi = new SwsApi(fakeConfig, mockWs)
//      val source = swsApi.searchAndStreamDocs("?type=pn")
//
//      source.runWith(Sink.seq).map(_ must have length 3)
//    }
//
//    "search and stream documents when there are mulitple atom feed pages" in {
//      val baseUrl = "http://test.sdu.nl"
//      val fakeConfig = Configuration.from(Map("cwc.sws.url" -> baseUrl))
//      val mockWs = MockWS {
//        case (GET, "http://test.sdu.nl/?type=pn") => Action { Ok(atomFeed(baseUrl, Seq("1", "2", "3"), Some("http://test.sdu.nl/1")).toString) }
//        case (GET, "http://test.sdu.nl/1")        => Action { Ok(atomFeed(baseUrl, Seq("4", "5", "6"), Some("http://test.sdu.nl/2")).toString) }
//        case (GET, "http://test.sdu.nl/2")        => Action { Ok(atomFeed(baseUrl, Seq("7", "8", "9")).toString) }
//      }
//
//      val swsApi = new SwsApi(fakeConfig, mockWs)
//      val source = swsApi.searchAndStreamDocs("?type=pn")
//
//      source.runWith(Sink.seq).map(_.map(_.docKey) must equal(Seq("1", "2", "3", "4", "5", "6", "7", "8", "9")))
//    }
//
//    "search and stream documents when there are mulitple atom feed pages and the last one is empty" in {
//      val baseUrl = "http://test.sdu.nl"
//      val fakeConfig = Configuration.from(Map("cwc.sws.url" -> baseUrl))
//      val mockWs = MockWS {
//        case (GET, "http://test.sdu.nl/?type=pn") => Action { Ok(atomFeed(baseUrl, Seq("1", "2", "3"), Some("http://test.sdu.nl/1")).toString) }
//        case (GET, "http://test.sdu.nl/1")        => Action { Ok(atomFeed(baseUrl, Seq("4", "5", "6"), Some("http://test.sdu.nl/2")).toString) }
//        case (GET, "http://test.sdu.nl/2")        => Action { Ok(atomFeed(baseUrl, Seq("7", "8", "9"), Some("http://test.sdu.nl/3")).toString) }
//        case (GET, "http://test.sdu.nl/3")        => Action { Ok(atomFeed(baseUrl, Seq()).toString) }
//      }
//
//      val swsApi = new SwsApi(fakeConfig, mockWs)
//      val source = swsApi.searchAndStreamDocs("?type=pn")
//
//      source.runWith(Sink.seq).map(_ must have length 9)
//    }
//  }

}
