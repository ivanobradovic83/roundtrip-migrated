package helpers

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar

trait ScalaSpec extends AnyWordSpecLike with BeforeAndAfterEach with Matchers with MockitoSugar {}
