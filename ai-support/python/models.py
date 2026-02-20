"""Pydantic models for Rendly AI Support API"""

from pydantic import BaseModel, Field
from typing import Optional, List
from datetime import datetime
from enum import Enum
import uuid


class MessageRole(str, Enum):
    USER = "user"
    AI = "ai"
    HUMAN_SUPPORT = "human_support"
    SYSTEM = "system"


class ResponseType(str, Enum):
    AI_RESPONSE = "ai_response"
    ESCALATED = "escalated"
    BLOCKED = "blocked"
    ERROR = "error"


class IntentCategory(str, Enum):
    PURCHASES = "compras"
    SALES = "ventas"
    ACCOUNT = "cuenta"
    PAYMENTS = "pagos"
    SHIPPING = "envios"
    SECURITY = "seguridad"
    APP = "app"
    GENERAL = "general"
    UNKNOWN = "unknown"


# Request Models
class SupportMessageRequest(BaseModel):
    user_id: str = Field(..., description="User UUID")
    message: str = Field(..., min_length=1, max_length=5000)
    session_id: Optional[str] = Field(None, description="Existing session ID")
    context: Optional[dict] = Field(None, description="Additional context")


class FeedbackRequest(BaseModel):
    message_id: str
    user_id: str
    helpful: bool
    feedback_text: Optional[str] = None


class EscalateRequest(BaseModel):
    conversation_id: str
    user_id: str
    reason: Optional[str] = None


# Response Models
class AnalysisDetails(BaseModel):
    confidence_score: int = Field(..., ge=0, le=100)
    detected_intent: str
    category: IntentCategory
    clarity_score: float
    completeness_score: float
    is_aggressive: bool = False
    is_confused: bool = False
    matched_keywords: List[str] = []


class ActionButton(BaseModel):
    """Button for navigation/actions in responses"""
    id: str
    label: str
    action: str  # "navigate", "open_url", "call_function"
    target: str  # Screen route or URL
    icon: Optional[str] = None  # Icon name


class SupportMessageResponse(BaseModel):
    message_id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    response_type: ResponseType
    content: str
    analysis: Optional[AnalysisDetails] = None
    session_id: str
    conversation_id: Optional[str] = None  # Real UUID from support_conversations table
    timestamp: datetime = Field(default_factory=datetime.utcnow)
    escalated: bool = False
    escalation_reason: Optional[str] = None
    suggested_actions: List[str] = []
    action_buttons: List[ActionButton] = []  # Navigation buttons


class ConversationMessage(BaseModel):
    id: str
    role: MessageRole
    content: str
    timestamp: datetime
    confidence_score: Optional[int] = None
    was_helpful: Optional[bool] = None


class ConversationResponse(BaseModel):
    conversation_id: str
    user_id: str
    messages: List[ConversationMessage]
    status: str  # "active", "resolved", "escalated"
    created_at: datetime
    updated_at: datetime
    resolved_by: Optional[str] = None  # "ai" or "human"


class FeedbackResponse(BaseModel):
    success: bool
    message: str


class HealthResponse(BaseModel):
    status: str
    version: str
    components: dict


# ═══ Training Pipeline Models ═══

class TrainingRunRequest(BaseModel):
    run_name: Optional[str] = None
    run_type: str = Field(default="full", description="full, incremental, intent_only, response_only")
    min_confidence: int = Field(default=0, ge=0, le=100)
    only_reviewed: bool = False


class CorrectionRequest(BaseModel):
    training_data_id: str = Field(..., description="UUID of the ai_training_data row")
    correct_intent: str = Field(..., min_length=1)
    correct_response: Optional[str] = None
    should_escalate: bool = False
    notes: Optional[str] = None
    corrector_id: Optional[str] = None


class CorrectionResponse(BaseModel):
    success: bool
    was_mismatch: Optional[bool] = None
    original_intent: Optional[str] = None
    correct_intent: Optional[str] = None


class TrainingRunResponse(BaseModel):
    run_id: str
    status: str
    training_samples: int = 0
    validation_samples: int = 0
    test_samples: int = 0
    total_intents: int = 0
    duration_seconds: float = 0.0
    improvement: dict = {}
    final_metrics: Optional[dict] = None
    baseline_metrics: Optional[dict] = None
    model_path: str = ""
    dataset_path: str = ""
    report_path: str = ""


class PredictResponse(BaseModel):
    message: str
    predicted_intent: str
    confidence: float
    top_intents: dict = {}
    model_trained: bool = False
