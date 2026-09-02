import logging
import json
from datetime import datetime
from config import settings
from models import MeetingCapturedKafkaPayload, CaptureAbortedKafkaPayload

logger = logging.getLogger("capture-service.kafka")

class KafkaEventPublisher:
    """
    Decoupled Event Publisher for meeting capture lifecycle.
    Publishes to Kafka topics:
      - meeting.captured (FR-1.6)
      - capture.aborted (PRIV-2)
    """

    def __init__(self):
        self.bootstrap_servers = settings.KAFKA_BOOTSTRAP_SERVERS
        self.producer = None
        self._init_producer()

    def _init_producer(self):
        try:
            from confluent_kafka import Producer
            conf = {
                "bootstrap.servers": self.bootstrap_servers,
                "client.id": "capture-service-producer",
                "acks": "all",
                "retries": 3,
                "retry.backoff.ms": 250
            }
            self.producer = Producer(conf)
            logger.info(f"Connected to Kafka broker at {self.bootstrap_servers}")
        except Exception as e:
            logger.warning(f"Kafka client init failed ({e}). Fallback in-memory event logging will be used.")
            self.producer = None

    def publish_meeting_captured(self, payload: MeetingCapturedKafkaPayload) -> bool:
        """
        Publishes meeting.captured event (FR-1.6).
        """
        topic = settings.KAFKA_TOPIC_MEETING_CAPTURED
        event_dict = payload.model_dump()
        event_bytes = json.dumps(event_dict).encode("utf-8")

        if self.producer:
            try:
                self.producer.produce(
                    topic=topic,
                    key=payload.meetingId.encode("utf-8"),
                    value=event_bytes,
                    callback=self._delivery_callback
                )
                self.producer.poll(0)
                logger.info(f"Published meeting.captured event for meeting {payload.meetingId} to Kafka topic {topic}")
                return True
            except Exception as e:
                logger.error(f"Failed to produce to Kafka topic {topic}: {e}")
                return False
        else:
            logger.info(f"[Mock Kafka] Published to '{topic}': {event_dict}")
            return True

    def publish_capture_aborted(self, payload: CaptureAbortedKafkaPayload) -> bool:
        """
        Publishes capture.aborted event when consent check fails (PRIV-2).
        """
        topic = settings.KAFKA_TOPIC_CAPTURE_ABORTED
        event_dict = payload.model_dump()
        event_bytes = json.dumps(event_dict).encode("utf-8")

        if self.producer:
            try:
                self.producer.produce(
                    topic=topic,
                    key=payload.meetingId.encode("utf-8"),
                    value=event_bytes,
                    callback=self._delivery_callback
                )
                self.producer.poll(0)
                logger.info(f"Published capture.aborted event for meeting {payload.meetingId} to Kafka topic {topic}")
                return True
            except Exception as e:
                logger.error(f"Failed to produce to Kafka topic {topic}: {e}")
                return False
        else:
            logger.info(f"[Mock Kafka] Published to '{topic}': {event_dict}")
            return True

    def _delivery_callback(self, err, msg):
        if err:
            logger.error(f"Kafka delivery failed: {err}")
        else:
            logger.debug(f"Kafka message delivered to {msg.topic()} [{msg.partition()}] at offset {msg.offset()}")
