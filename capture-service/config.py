import os
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    # Server configuration
    SERVER_PORT: int = int(os.getenv("SERVER_PORT", "8085"))
    ENVIRONMENT: str = os.getenv("ENVIRONMENT", "development")
    LOG_LEVEL: str = os.getenv("LOG_LEVEL", "INFO")

    # Database connection
    DB_HOST: str = os.getenv("DB_HOST", "localhost")
    DB_PORT: int = int(os.getenv("DB_PORT", "5432"))
    DB_NAME: str = os.getenv("DB_NAME", "meeting_ops")
    DB_USER: str = os.getenv("DB_USER", "postgres")
    DB_PASSWORD: str = os.getenv("DB_PASSWORD", "postgres")

    # Kafka configuration
    KAFKA_BOOTSTRAP_SERVERS: str = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
    KAFKA_TOPIC_MEETING_CAPTURED: str = "meeting.captured"
    KAFKA_TOPIC_CAPTURE_ABORTED: str = "capture.aborted"

    # S3 / MinIO Ephemeral Audio Storage (NFR-4.2)
    S3_ENDPOINT_URL: str = os.getenv("S3_ENDPOINT_URL", "http://localhost:9000")
    S3_BUCKET_AUDIO: str = os.getenv("S3_BUCKET_AUDIO", "meeting-audio-recordings")
    S3_ACCESS_KEY: str = os.getenv("S3_ACCESS_KEY", "minioadmin")
    S3_SECRET_KEY: str = os.getenv("S3_SECRET_KEY", "minioadmin")
    S3_REGION: str = os.getenv("S3_REGION", "us-east-1")
    AUDIO_RETENTION_HOURS: int = 24  # Strict 24-hour TTL (NFR-4.2)

    # Bot Compliance & Provider Settings
    BOT_NAME_PREFIX: str = "MeetingOps Recording Bot"
    BOT_CHAT_DISCLAIMER: str = (
        "MeetingOps Recording Bot has joined to transcribe and extract action items. "
        "Audio is ephemeral (auto-purged in 24h). To opt out of analytics, visit your team portal."
    )
    BOT_PROVIDER_API_KEY: str = os.getenv("BOT_PROVIDER_API_KEY", "mock_bot_api_key")

    class Config:
        case_sensitive = True

settings = Settings()
