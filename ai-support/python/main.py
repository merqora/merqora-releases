"""
Rendly AI Support - FastAPI Application

Main entry point for the AI support system.
"""

from fastapi import FastAPI, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import structlog
import uvicorn

from config import get_settings, Settings
from models import (
    SupportMessageRequest,
    SupportMessageResponse,
    FeedbackRequest,
    FeedbackResponse,
    EscalateRequest,
    ConversationResponse,
    HealthResponse,
    TrainingRunRequest,
    CorrectionRequest,
    CorrectionResponse,
    TrainingRunResponse,
    PredictResponse,
)
from orchestrator import get_orchestrator, AIOrchestrator

# Configure structured logging
structlog.configure(
    processors=[
        structlog.stdlib.filter_by_level,
        structlog.stdlib.add_logger_name,
        structlog.stdlib.add_log_level,
        structlog.stdlib.PositionalArgumentsFormatter(),
        structlog.processors.TimeStamper(fmt="iso"),
        structlog.processors.StackInfoRenderer(),
        structlog.processors.format_exc_info,
        structlog.processors.UnicodeDecoder(),
        structlog.processors.JSONRenderer()
    ],
    wrapper_class=structlog.stdlib.BoundLogger,
    context_class=dict,
    logger_factory=structlog.stdlib.LoggerFactory(),
    cache_logger_on_first_use=True,
)

logger = structlog.get_logger()


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan handler"""
    logger.info("starting_ai_support_server")
    
    # Initialize orchestrator
    orchestrator = get_orchestrator()
    logger.info(
        "orchestrator_initialized",
        faq_count=len(orchestrator.knowledge_base.faq_entries)
    )
    
    yield
    
    logger.info("shutting_down_ai_support_server")


# Create FastAPI app
app = FastAPI(
    title="Rendly AI Support",
    description="AI-powered support system for Rendly app",
    version="1.0.0",
    lifespan=lifespan,
)

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Configure properly in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# Dependencies
def get_settings_dep() -> Settings:
    return get_settings()


def get_orchestrator_dep() -> AIOrchestrator:
    return get_orchestrator()


# ============== ENDPOINTS ==============

@app.get("/health", response_model=HealthResponse)
async def health_check():
    """Health check endpoint"""
    from scoring_service import CPP_AVAILABLE
    from orchestrator import RUST_AVAILABLE
    
    return HealthResponse(
        status="healthy",
        version="1.0.0",
        components={
            "python": "active",
            "cpp_scoring": "active" if CPP_AVAILABLE else "fallback",
            "rust_security": "active" if RUST_AVAILABLE else "fallback",
            "knowledge_base": "loaded"
        }
    )


@app.post("/ai/support/message", response_model=SupportMessageResponse)
async def process_support_message(
    request: SupportMessageRequest,
    orchestrator: AIOrchestrator = Depends(get_orchestrator_dep)
):
    """
    Process a user support message.
    
    The AI will:
    1. Validate the message (security, rate limiting)
    2. Analyze intent and confidence
    3. Either respond automatically or escalate to human
    """
    try:
        response = await orchestrator.process_message(request)
        
        logger.info(
            "message_processed",
            user_id=request.user_id,
            response_type=response.response_type,
            escalated=response.escalated,
            confidence=response.analysis.confidence_score if response.analysis else None
        )
        
        return response
        
    except Exception as e:
        logger.error("message_processing_error", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/ai/support/feedback", response_model=FeedbackResponse)
async def submit_feedback(
    request: FeedbackRequest,
    orchestrator: AIOrchestrator = Depends(get_orchestrator_dep)
):
    """
    Submit feedback on an AI response.
    
    This helps improve the AI over time by learning from user feedback.
    """
    try:
        # Record feedback in learning engine
        orchestrator.record_feedback(
            message_id=request.message_id,
            question=request.feedback_text or "",
            helpful=request.helpful
        )
        
        logger.info(
            "feedback_received",
            message_id=request.message_id,
            helpful=request.helpful,
            user_id=request.user_id
        )
        
        return FeedbackResponse(
            success=True,
            message="¡Gracias por tu feedback! Nos ayuda a mejorar."
        )
        
    except Exception as e:
        logger.error("feedback_error", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/ai/support/learning/stats")
async def get_learning_stats(
    orchestrator: AIOrchestrator = Depends(get_orchestrator_dep)
):
    """
    Get learning engine statistics.
    
    Shows how much the AI has learned from interactions.
    """
    return orchestrator.get_learning_stats()


@app.post("/ai/support/escalate")
async def escalate_conversation(
    request: EscalateRequest,
    settings: Settings = Depends(get_settings_dep)
):
    """
    Manually escalate a conversation to human support.
    """
    try:
        # TODO: Notify human support via Supabase realtime
        logger.info(
            "manual_escalation",
            conversation_id=request.conversation_id,
            user_id=request.user_id,
            reason=request.reason
        )
        
        return {
            "success": True,
            "message": "Conversación escalada a soporte humano",
            "support_user_id": settings.support_user_id
        }
        
    except Exception as e:
        logger.error("escalation_error", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/ai/support/conversation/{conversation_id}", response_model=ConversationResponse)
async def get_conversation(
    conversation_id: str,
    orchestrator: AIOrchestrator = Depends(get_orchestrator_dep)
):
    """
    Get conversation history.
    """
    # TODO: Fetch from Supabase
    raise HTTPException(status_code=404, detail="Conversation not found")


@app.get("/ai/support/stats")
async def get_stats(
    orchestrator: AIOrchestrator = Depends(get_orchestrator_dep)
):
    """
    Get AI support statistics and metrics.
    """
    return orchestrator.llm_service.get_metrics()


@app.get("/ai/support/metrics")
async def get_metrics(
    orchestrator: AIOrchestrator = Depends(get_orchestrator_dep)
):
    """
    Get detailed AI metrics for dashboard.
    """
    llm_metrics = orchestrator.llm_service.get_metrics()
    learning_stats = orchestrator.get_learning_stats()
    
    return {
        "llm": llm_metrics,
        "learning": learning_stats,
        "system": {
            "status": "online",
            "mode": "hybrid" if llm_metrics.get("llm_provider") else "local"
        }
    }


# ============== TRAINING PIPELINE ENDPOINTS ==============

@app.get("/ai/training/metrics")
async def get_training_metrics(
    hours: int = 24,
    orchestrator: AIOrchestrator = Depends(get_orchestrator_dep)
):
    """
    Get live training pipeline metrics.
    Includes intent accuracy, escalation rate, confidence distribution.
    """
    pipeline = orchestrator.training_pipeline
    return {
        "live_metrics": pipeline.get_live_metrics(hours=hours),
        "pipeline_stats": pipeline.get_stats(),
    }


@app.get("/ai/training/metrics/supabase")
async def get_training_metrics_from_supabase(
    days: int = 30,
    orchestrator: AIOrchestrator = Depends(get_orchestrator_dep)
):
    """
    Get training metrics from Supabase (historical data).
    """
    try:
        supabase = orchestrator.supabase
        if not supabase or not supabase.is_configured:
            return {"error": "Supabase not configured"}
        
        result = await supabase._request(
            "POST", "rpc/get_ai_training_metrics",
            {"p_days": days}
        )
        return result or {}
    except Exception as e:
        logger.error("training_metrics_error", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/ai/training/run", response_model=TrainingRunResponse)
async def trigger_training_run(
    request: TrainingRunRequest = None,
    orchestrator: AIOrchestrator = Depends(get_orchestrator_dep)
):
    """
    Trigger a training run. This will:
    1. Fetch training data from Supabase
    2. Clean and split the dataset
    3. Train a new intent classifier (TF-IDF + LinearSVC)
    4. Evaluate against baseline
    5. Deploy if improved
    6. Generate report
    """
    try:
        req = request or TrainingRunRequest()
        pipeline = orchestrator.training_pipeline
        result = await pipeline.run_training(
            run_name=req.run_name,
            run_type=req.run_type,
            min_confidence=req.min_confidence,
            only_reviewed=req.only_reviewed,
        )
        
        return TrainingRunResponse(
            run_id=result.run_id,
            status=result.status,
            training_samples=result.training_samples,
            validation_samples=result.validation_samples,
            test_samples=result.test_samples,
            total_intents=result.total_intents,
            duration_seconds=result.duration_seconds,
            improvement=result.improvement,
            final_metrics={
                "accuracy": result.final_metrics.accuracy if result.final_metrics else None,
                "f1": result.final_metrics.f1_macro if result.final_metrics else None,
                "precision": result.final_metrics.precision_macro if result.final_metrics else None,
                "recall": result.final_metrics.recall_macro if result.final_metrics else None,
                "total_intents": result.final_metrics.total_intents if result.final_metrics else 0,
                "misclassified_count": len(result.final_metrics.misclassified_samples) if result.final_metrics else 0,
            } if result.final_metrics else None,
            baseline_metrics={
                "accuracy": result.baseline_metrics.accuracy if result.baseline_metrics else None,
                "f1": result.baseline_metrics.f1_macro if result.baseline_metrics else None,
            } if result.baseline_metrics else None,
            model_path=result.model_path,
            dataset_path=result.dataset_path,
            report_path=result.report_path,
        )
    except Exception as e:
        logger.error("training_run_error", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/ai/training/predict", response_model=PredictResponse)
async def predict_intent(
    message: str,
    orchestrator: AIOrchestrator = Depends(get_orchestrator_dep)
):
    """
    Test the trained ML model's intent prediction on a message.
    Returns predicted intent, confidence, and top alternatives.
    """
    pipeline = orchestrator.training_pipeline
    intent, confidence, top_intents = pipeline.predict_intent(message)
    
    return PredictResponse(
        message=message,
        predicted_intent=intent,
        confidence=confidence,
        top_intents=top_intents,
        model_trained=pipeline.active_model.is_trained,
    )


@app.post("/ai/training/correction", response_model=CorrectionResponse)
async def submit_correction(
    request: CorrectionRequest,
    orchestrator: AIOrchestrator = Depends(get_orchestrator_dep)
):
    """
    Submit a human correction for a training sample.
    This is the core feedback loop - human reviewers correct misclassifications.
    """
    try:
        supabase = orchestrator.supabase
        if not supabase or not supabase.is_configured:
            raise HTTPException(status_code=503, detail="Supabase not configured")
        
        result = await supabase._request(
            "POST", "rpc/submit_intent_correction",
            {
                "p_training_data_id": request.training_data_id,
                "p_correct_intent": request.correct_intent,
                "p_correct_response": request.correct_response,
                "p_should_escalate": request.should_escalate,
                "p_notes": request.notes,
                "p_corrector_id": request.corrector_id,
            }
        )
        
        logger.info(
            "correction_submitted",
            training_data_id=request.training_data_id,
            correct_intent=request.correct_intent,
        )
        
        if isinstance(result, dict):
            return CorrectionResponse(
                success=result.get("success", True),
                was_mismatch=result.get("was_mismatch"),
                original_intent=result.get("original_intent"),
                correct_intent=result.get("correct_intent"),
            )
        return CorrectionResponse(success=True)
    except HTTPException:
        raise
    except Exception as e:
        logger.error("correction_error", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/ai/training/pending-review")
async def get_pending_review(
    limit: int = 50,
    offset: int = 0,
    orchestrator: AIOrchestrator = Depends(get_orchestrator_dep)
):
    """
    Get training samples pending human review.
    Sorted by lowest confidence first (most likely to be wrong).
    """
    try:
        supabase = orchestrator.supabase
        if not supabase or not supabase.is_configured:
            return {"data": [], "count": 0}
        
        endpoint = f"ai_training_data?dataset_status=eq.pending&order=confidence_score.asc,created_at.desc&limit={limit}&offset={offset}"
        result = await supabase._request("GET", endpoint)
        
        return {
            "data": result or [],
            "count": len(result) if result else 0,
        }
    except Exception as e:
        logger.error("pending_review_error", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/ai/training/errors")
async def get_classification_errors(
    limit: int = 50,
    orchestrator: AIOrchestrator = Depends(get_orchestrator_dep)
):
    """
    Get known classification errors (detected_intent != correct_intent).
    """
    try:
        supabase = orchestrator.supabase
        if not supabase or not supabase.is_configured:
            return {"data": [], "count": 0}
        
        endpoint = f"ai_training_data?intent_correct=eq.false&order=created_at.desc&limit={limit}"
        result = await supabase._request("GET", endpoint)
        
        return {
            "data": result or [],
            "count": len(result) if result else 0,
        }
    except Exception as e:
        logger.error("errors_fetch_error", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/ai/training/dataset/export")
async def export_dataset(
    format: str = "jsonl",
    min_confidence: int = 0,
    only_reviewed: bool = False,
    orchestrator: AIOrchestrator = Depends(get_orchestrator_dep)
):
    """
    Export the training dataset in JSONL or CSV format.
    """
    try:
        pipeline = orchestrator.training_pipeline
        
        # Fetch data
        pipeline.dataset_manager.samples.clear()
        await pipeline.fetch_training_dataset(
            min_confidence=min_confidence,
            only_reviewed=only_reviewed
        )
        
        if not pipeline.dataset_manager.samples:
            pipeline.build_dataset_from_buffer()
        
        pipeline.dataset_manager.clean_dataset()
        
        if format == "csv":
            path = pipeline.dataset_manager.export_csv()
        else:
            path = pipeline.dataset_manager.export_jsonl()
        
        from fastapi.responses import FileResponse
        return FileResponse(
            path,
            media_type="application/octet-stream",
            filename=path.split("/")[-1] if "/" in path else path.split("\\")[-1],
        )
    except Exception as e:
        logger.error("export_error", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/ai/training/dataset/stats")
async def get_dataset_stats(
    orchestrator: AIOrchestrator = Depends(get_orchestrator_dep)
):
    """
    Get dataset statistics (intent distribution, sample counts, etc.)
    """
    pipeline = orchestrator.training_pipeline
    return pipeline.dataset_manager.get_stats()


@app.get("/ai/training/runs")
async def get_training_runs(
    limit: int = 20,
    orchestrator: AIOrchestrator = Depends(get_orchestrator_dep)
):
    """
    Get history of training runs with metrics.
    """
    try:
        supabase = orchestrator.supabase
        if not supabase or not supabase.is_configured:
            return {"data": []}
        
        endpoint = f"ai_training_runs?order=created_at.desc&limit={limit}"
        result = await supabase._request("GET", endpoint)
        
        return {"data": result or []}
    except Exception as e:
        logger.error("training_runs_error", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/ai/training/flush")
async def flush_training_buffer(
    orchestrator: AIOrchestrator = Depends(get_orchestrator_dep)
):
    """
    Manually flush the training data buffer to Supabase.
    """
    try:
        pipeline = orchestrator.training_pipeline
        saved = await pipeline.flush_buffer_to_supabase()
        return {"flushed": saved, "remaining_buffer": len(pipeline._training_buffer)}
    except Exception as e:
        logger.error("flush_error", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/ai/training/snapshot")
async def create_snapshot(
    period_type: str = "daily",
    orchestrator: AIOrchestrator = Depends(get_orchestrator_dep)
):
    """
    Create a metrics snapshot for the specified period.
    """
    try:
        pipeline = orchestrator.training_pipeline
        snapshot_id = await pipeline.create_metrics_snapshot(period_type)
        return {"snapshot_id": snapshot_id, "period_type": period_type}
    except Exception as e:
        logger.error("snapshot_error", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/ai/training/intent-performance")
async def get_intent_performance(
    orchestrator: AIOrchestrator = Depends(get_orchestrator_dep)
):
    """
    Get performance metrics per intent (accuracy, confidence, corrections).
    """
    try:
        supabase = orchestrator.supabase
        if not supabase or not supabase.is_configured:
            return {"data": []}
        
        result = await supabase._request("GET", "v_intent_performance")
        return {"data": result or []}
    except Exception as e:
        logger.error("intent_performance_error", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))


# ============== MAIN ==============

if __name__ == "__main__":
    import os
    settings = get_settings()
    
    # Railway/Render inject PORT env var directly
    port = int(os.environ.get("PORT", settings.api_port))
    
    uvicorn.run(
        "main:app",
        host=settings.api_host,
        port=port,
        reload=settings.debug,
        log_level="info"
    )
