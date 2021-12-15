package service.common.logging

import akka.actor.ActorSystem
import nl.sdu.cwc.common.metrics.CwcEventLogger
import nl.sdu.cwc.common.metrics.CwcEventLogger.MetricsEvent
import org.joda.time.DateTime

import javax.inject.Inject

class LoggingService @Inject()(actorSystem: ActorSystem) {

  CwcEventLogger.init(actorSystem)

  def logEvent(eventType: String, duration: Long, documentKey: String, success: Boolean, message: String): Unit = {
    val metricsEvent = MetricsEvent(
      DateTime.now,
      "sdu-cwc-roundtrip-publishone",
      eventType,
      Some(duration),
      "processingType" -> "publishone",
      "documentKey" -> documentKey,
      "success" -> success,
      "message" -> message
    )
    CwcEventLogger.logEvent(metricsEvent)
  }

}
