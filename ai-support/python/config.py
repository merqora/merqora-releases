"""Configuration for Rendly AI Support"""

from pydantic_settings import BaseSettings
from functools import lru_cache

class Settings(BaseSettings):
    # API
    api_host: str = "0.0.0.0"
    api_port: int = 8000
    debug: bool = False
    
    # Supabase
    supabase_url: str = ""
    supabase_key: str = ""
    supabase_service_key: str = ""
    supabase_anon_key: str = ""
    
    # AI Settings
    confidence_threshold: int = 70  # Below this, escalate to human
    max_message_length: int = 2000
    response_cache_ttl: int = 3600  # 1 hour
    
    # Rate Limiting
    rate_limit_requests: int = 20
    rate_limit_window: int = 60  # seconds
    
    # Escalation
    support_user_id: str = ""  # Your user ID for receiving escalations
    escalation_notification: bool = True
    
    class Config:
        env_file = ".env"
        env_prefix = "RENDLY_AI_"


@lru_cache()
def get_settings() -> Settings:
    return Settings()
