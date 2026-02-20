"""
AI Support Orchestrator - Main decision engine

This is the brain of the AI support system. It:
1. Receives user messages
2. Validates via Rust security layer
3. Analyzes via C++ scoring engine
4. Matches against knowledge base
5. Decides: respond or escalate
"""

from typing import Optional, Tuple
from datetime import datetime, timedelta
import uuid
import structlog

from models import (
    SupportMessageRequest,
    SupportMessageResponse,
    AnalysisDetails,
    ResponseType,
    IntentCategory,
    MessageRole,
    ActionButton,
)
from config import get_settings
from knowledge_base import get_knowledge_base, FAQEntry, ActionButton as KBActionButton
from scoring_service import analyze_message, AnalysisResult
from learning_engine import get_learning_engine
from reasoning_engine import get_reasoning_engine
from llm_service import get_llm_service
from supabase_service import get_supabase_service
from local_ai_model import get_local_ai
from training_pipeline import get_training_pipeline

# Try to import Rust security service
try:
    import rendly_security
    RUST_AVAILABLE = True
except ImportError:
    RUST_AVAILABLE = False

logger = structlog.get_logger()


class SecurityService:
    """Python fallback for security when Rust is unavailable"""
    
    def __init__(self):
        self.rate_limits = {}  # user_id -> (count, window_start)
        self.sessions = {}  # session_id -> user_id
        self.max_requests = 20
        self.window_seconds = 60
    
    def validate_message(self, user_id: str, message: str, session_id: str) -> dict:
        """Validate incoming message"""
        import time
        
        result = {
            "is_valid": True,
            "sanitized_message": message,
            "rate_limit_remaining": self.max_requests,
            "session_valid": True,
            "risk_score": 0.0,
            "blocked_reason": None
        }
        
        # Rate limiting
        now = time.time()
        if user_id in self.rate_limits:
            count, window_start = self.rate_limits[user_id]
            if now - window_start < self.window_seconds:
                if count >= self.max_requests:
                    result["is_valid"] = False
                    result["blocked_reason"] = "Rate limit exceeded"
                    return result
                self.rate_limits[user_id] = (count + 1, window_start)
                result["rate_limit_remaining"] = self.max_requests - count - 1
            else:
                self.rate_limits[user_id] = (1, now)
        else:
            self.rate_limits[user_id] = (1, now)
        
        # Basic sanitization
        sanitized = message.strip()
        if len(sanitized) > 5000:
            sanitized = sanitized[:5000]
        
        # Remove potential HTML/script
        import re
        sanitized = re.sub(r'<[^>]+>', '', sanitized)
        
        result["sanitized_message"] = sanitized
        
        # Risk assessment
        risk = 0.0
        if re.search(r'https?://', message):
            risk += 0.2
        if re.search(r'<script', message, re.I):
            risk += 0.5
        
        result["risk_score"] = min(1.0, risk)
        
        if risk > 0.8:
            result["is_valid"] = False
            result["blocked_reason"] = "Message flagged as potential abuse"
        
        return result
    
    def create_session(self, user_id: str) -> str:
        session_id = str(uuid.uuid4())
        self.sessions[session_id] = user_id
        return session_id


def get_security_service():
    """Get security service (Rust if available, else Python)"""
    if RUST_AVAILABLE:
        return rendly_security.SecurityService()
    return SecurityService()


class AIOrchestrator:
    """Main AI orchestration engine"""
    
    def __init__(self):
        self.settings = get_settings()
        self.knowledge_base = get_knowledge_base()
        self.security = get_security_service()
        self.learning_engine = get_learning_engine()
        self.reasoning_engine = get_reasoning_engine()
        self.llm_service = get_llm_service()
        self.supabase = get_supabase_service()
        self.local_ai = get_local_ai()
        self.training_pipeline = get_training_pipeline(self.supabase)
        self.conversations = {}  # In-memory fallback
    
    def _intent_to_category(self, intent: str) -> IntentCategory:
        """Map detected intent to category"""
        category_map = {
            "purchase_": IntentCategory.PURCHASES,
            "payment_": IntentCategory.PAYMENTS,
            "refund": IntentCategory.PAYMENTS,
            "wallet_": IntentCategory.PAYMENTS,
            "account_": IntentCategory.ACCOUNT,
            "notification_": IntentCategory.ACCOUNT,
            "privacy_": IntentCategory.ACCOUNT,
            "social_": IntentCategory.ACCOUNT,
            "reputation_": IntentCategory.ACCOUNT,
            "language_": IntentCategory.ACCOUNT,
            "shipping_": IntentCategory.SHIPPING,
            "return_": IntentCategory.PURCHASES,
            "sell_": IntentCategory.SALES,
            "product_": IntentCategory.SALES,
            "offer_": IntentCategory.PURCHASES,
            "review_": IntentCategory.PURCHASES,
            "security_": IntentCategory.SECURITY,
            "app_": IntentCategory.APP,
            "stories_": IntentCategory.APP,
            "rends_": IntentCategory.APP,
            "chat_": IntentCategory.APP,
            "interaction_": IntentCategory.APP,
            "highlights_": IntentCategory.APP,
            "livestream_": IntentCategory.APP,
            "zone_": IntentCategory.APP,
            "handshake_": IntentCategory.PURCHASES,
            "seller_": IntentCategory.PURCHASES,
            "escalation_": IntentCategory.GENERAL,
            "farewell": IntentCategory.GENERAL,
            "greeting": IntentCategory.GENERAL,
        }
        
        for prefix, category in category_map.items():
            if intent.startswith(prefix) or intent == prefix.rstrip("_"):
                return category
        
        return IntentCategory.UNKNOWN
    
    def _generate_escalation_message(self, analysis: AnalysisResult) -> str:
        """Generate message when escalating to human"""
        if analysis.is_aggressive:
            return """Entiendo que estás frustrado/a. Voy a transferirte con un agente de soporte humano que podrá ayudarte mejor con tu situación.

Un momento por favor, alguien del equipo de Rendly se comunicará contigo en breve. 🙏"""
        
        if analysis.is_confused:
            return """Veo que tienes varias dudas. Para darte la mejor ayuda posible, te voy a conectar con un agente de soporte.

En unos momentos alguien del equipo te atenderá personalmente. ¡Gracias por tu paciencia! 🙏"""
        
        return """Para brindarte la mejor asistencia con tu consulta, te voy a conectar con un agente de soporte humano.

Un miembro del equipo Rendly te atenderá en breve. ¡Gracias por contactarnos! 🙏"""
    
    def _generate_fallback_response(self, analysis: AnalysisResult) -> str:
        """Generate fallback when no FAQ match"""
        category = self._intent_to_category(analysis.detected_intent)
        
        fallbacks = {
            IntentCategory.PURCHASES: """No encontré información específica sobre tu consulta de compras.

¿Podrías darme más detalles? Por ejemplo:
• ¿Es sobre el estado de un pedido?
• ¿Necesitas cancelar o devolver?
• ¿Hay algún problema con lo recibido?

También puedes revisar **Perfil → Historial de pedidos** para ver tus compras.""",

            IntentCategory.PAYMENTS: """No encontré una respuesta exacta para tu consulta de pagos.

¿Podrías especificar si es sobre:
• Agregar o cambiar método de pago
• Un pago rechazado o con error
• Un reembolso pendiente

Revisa **Perfil → Métodos de pago** para gestionar tus tarjetas.""",

            IntentCategory.ACCOUNT: """No encontré información específica sobre tu cuenta.

¿Tu consulta es sobre:
• Cambiar contraseña o datos
• Verificar tu cuenta
• Problemas para acceder

Revisa **Perfil → Configuración** para opciones de cuenta.""",

            IntentCategory.SHIPPING: """No encontré una respuesta exacta sobre envíos.

¿Necesitas ayuda con:
• Agregar o cambiar dirección
• Tiempos de entrega
• Un paquete perdido o demorado

Revisa **Perfil → Direcciones** para gestionar tus direcciones.""",

            IntentCategory.SALES: """No encontré información específica sobre ventas.

¿Tu consulta es sobre:
• Cómo publicar un producto
• Comisiones y cobros
• Problemas con una venta

Revisa **Perfil → Mis ventas** para ver el estado de tus ventas.""",

            IntentCategory.SECURITY: """No encontré una respuesta exacta sobre seguridad.

¿Necesitas ayuda con:
• Activar verificación en dos pasos
• Reportar un usuario o fraude
• Proteger tu cuenta

Revisa **Perfil → Configuración → Seguridad** para opciones.""",
        }
        
        return fallbacks.get(
            category,
            """No estoy seguro de entender tu consulta.

¿Podrías reformularla o darme más detalles? Puedo ayudarte con:
• 🛒 Compras y pedidos
• 💰 Pagos y reembolsos
• 👤 Tu cuenta
• 📦 Envíos
• 🏪 Ventas
• 🔒 Seguridad

También puedes explorar el **Centro de ayuda** para más información."""
        )
    
    async def process_message(
        self,
        request: SupportMessageRequest
    ) -> SupportMessageResponse:
        """Process an incoming support message"""
        import time
        start_time = time.time()
        
        message_id = str(uuid.uuid4())
        session_id = request.session_id or str(uuid.uuid4())
        
        # Get or create conversation_id early for realtime subscriptions
        conversation_id = None
        if self.supabase and self.supabase.is_configured:
            try:
                conversation_id = await self.supabase.get_or_create_conversation(
                    request.user_id, session_id
                )
                
                # Check if conversation is already escalated to human agent
                if conversation_id:
                    is_escalated = await self.supabase.is_conversation_escalated(conversation_id)
                    if is_escalated:
                        logger.info("conversation_escalated_skipping_ai", conversation_id=conversation_id)
                        return SupportMessageResponse(
                            message_id=message_id,
                            response_type=ResponseType.ESCALATED,
                            content="Un agente humano está atendiendo tu consulta. Por favor, espera su respuesta.",
                            session_id=session_id,
                            conversation_id=conversation_id,
                            escalated=True,
                            escalation_reason="already_escalated"
                        )
                
            except Exception as e:
                logger.warn("conversation_creation_error", error=str(e))
        
        logger.info(
            "processing_message",
            user_id=request.user_id,
            message_length=len(request.message),
            conversation_id=conversation_id
        )
        
        # 1. Security validation (Rust layer)
        if RUST_AVAILABLE:
            validation = self.security.validate_message(
                request.user_id,
                request.message,
                session_id
            )
        else:
            validation = self.security.validate_message(
                request.user_id,
                request.message,
                session_id
            )
        
        if not validation.get("is_valid", validation.is_valid if hasattr(validation, 'is_valid') else True):
            blocked_reason = validation.get("blocked_reason") or getattr(validation, 'blocked_reason', 'Blocked')
            logger.warn("message_blocked", reason=blocked_reason)
            return SupportMessageResponse(
                message_id=message_id,
                response_type=ResponseType.BLOCKED,
                content=f"Lo siento, no puedo procesar tu mensaje: {blocked_reason}",
                session_id=session_id,
                conversation_id=conversation_id,
                escalated=False
            )
        
        sanitized_message = validation.get("sanitized_message") or getattr(validation, 'sanitized_message', request.message)
        
        # 2. Check for learned agent responses (non-blocking, with timeout)
        learned_response = None
        if self.supabase and self.supabase.is_configured:
            try:
                import asyncio
                learned_response = await asyncio.wait_for(
                    self.supabase.check_for_similar_query(sanitized_message),
                    timeout=2.0  # Max 2 seconds for learning lookup
                )
            except asyncio.TimeoutError:
                logger.warn("learning_lookup_timeout")
            except Exception as e:
                logger.warn("learning_lookup_error", error=str(e))
        
        if learned_response:
            logger.info("learned_response_found", source="agent_learning")
            response_time_ms = int((time.time() - start_time) * 1000)
            
            await self._save_to_supabase(
                user_id=request.user_id,
                session_id=session_id,
                user_message=request.message,
                ai_response=learned_response,
                analysis=AnalysisDetails(
                    confidence_score=95,
                    detected_intent="learned_from_agent",
                    category=IntentCategory.GENERAL,
                    clarity_score=0.8,
                    completeness_score=0.8,
                ),
                response_time_ms=response_time_ms,
                escalated=False,
                response_source="agent_learning"
            )
            
            return SupportMessageResponse(
                message_id=message_id,
                response_type=ResponseType.AI_RESPONSE,
                content=learned_response,
                analysis=AnalysisDetails(
                    confidence_score=95,
                    detected_intent="learned_from_agent",
                    category=IntentCategory.GENERAL,
                    clarity_score=0.8,
                    completeness_score=0.8,
                ),
                session_id=session_id,
                conversation_id=conversation_id,
                escalated=False
            )
        
        # 3. Check learning engine cache (fast path)
        cached = self.learning_engine.find_cached_response(sanitized_message)
        if cached:
            logger.info("cache_hit", intent=cached.intent, confidence=cached.confidence_score)
            
            # Convert cached buttons to ActionButton models
            action_buttons = [
                ActionButton(
                    id=btn.get('id', ''),
                    label=btn.get('label', ''),
                    action=btn.get('action', 'navigate'),
                    target=btn.get('target', ''),
                    icon=btn.get('icon')
                )
                for btn in cached.action_buttons
            ] if cached.action_buttons else []
            
            return SupportMessageResponse(
                message_id=message_id,
                response_type=ResponseType.AI_RESPONSE,
                content=cached.response_text,
                analysis=AnalysisDetails(
                    confidence_score=cached.confidence_score,
                    detected_intent=cached.intent,
                    category=IntentCategory(cached.category) if cached.category in [e.value for e in IntentCategory] else IntentCategory.GENERAL,
                    clarity_score=0.8,
                    completeness_score=0.8,
                    is_aggressive=False,
                    is_confused=False,
                    matched_keywords=[]
                ),
                session_id=session_id,
                conversation_id=conversation_id,
                escalated=False,
                action_buttons=action_buttons
            )
        
        # 3. Analyze message (C++ scoring engine)
        analysis = analyze_message(sanitized_message)
        
        logger.info(
            "message_analyzed",
            confidence=analysis.confidence_score,
            intent=analysis.detected_intent,
            recommendation=analysis.recommendation
        )
        
        analysis_details = AnalysisDetails(
            confidence_score=analysis.confidence_score,
            detected_intent=analysis.detected_intent,
            category=self._intent_to_category(analysis.detected_intent),
            clarity_score=analysis.clarity_score,
            completeness_score=analysis.completeness_score,
            is_aggressive=analysis.is_aggressive,
            is_confused=analysis.is_confused,
            matched_keywords=analysis.matched_keywords
        )
        
        # 3. Check if aggressive - always escalate aggressive users
        if analysis.is_aggressive:
            logger.info("escalating_aggressive_user")
            response_time_ms = int((time.time() - start_time) * 1000)
            escalation_content = self._generate_escalation_message(analysis)
            
            # Guardar en Supabase
            await self._save_to_supabase(
                user_id=request.user_id,
                session_id=session_id,
                user_message=request.message,
                ai_response=escalation_content,
                analysis=analysis_details,
                response_time_ms=response_time_ms,
                escalated=True,
                escalation_reason="aggressive_user",
                response_source="escalation"
            )
            
            return SupportMessageResponse(
                message_id=message_id,
                response_type=ResponseType.ESCALATED,
                content=escalation_content,
                analysis=analysis_details,
                session_id=session_id,
                conversation_id=conversation_id,
                escalated=True,
                escalation_reason="aggressive_user"
            )
        
        # 4. Find matching FAQ response
        faq_entry, match_score = self.knowledge_base.find_best_match(
            sanitized_message,
            analysis.detected_intent,
            analysis.matched_keywords
        )
        
        # If we have a good FAQ match, respond directly (regardless of confidence score)
        if faq_entry and match_score >= 0.5:
            logger.info(
                "faq_matched",
                faq_id=faq_entry.id,
                match_score=match_score
            )
            
            # Convert FAQ buttons to response buttons
            action_buttons = self._convert_action_buttons(faq_entry.action_buttons)
            
            # Cache this response for future fast retrieval
            self.learning_engine.cache_response(
                question=sanitized_message,
                response=faq_entry.answer,
                intent=analysis.detected_intent,
                category=str(analysis_details.category.value),
                confidence=analysis.confidence_score,
                action_buttons=[{
                    'id': btn.id,
                    'label': btn.label,
                    'action': btn.action,
                    'target': btn.target,
                    'icon': btn.icon
                } for btn in faq_entry.action_buttons] if faq_entry.action_buttons else []
            )
            
            # Guardar en Supabase
            response_time_ms = int((time.time() - start_time) * 1000)
            await self._save_to_supabase(
                user_id=request.user_id,
                session_id=session_id,
                user_message=request.message,
                ai_response=faq_entry.answer,
                analysis=analysis_details,
                response_time_ms=response_time_ms,
                escalated=False,
                response_source="faq"
            )
            
            return SupportMessageResponse(
                message_id=message_id,
                response_type=ResponseType.AI_RESPONSE,
                content=faq_entry.answer,
                analysis=analysis_details,
                session_id=session_id,
                conversation_id=conversation_id,
                escalated=False,
                suggested_actions=self._get_suggested_actions(faq_entry),
                action_buttons=action_buttons
            )
        
        # 5. No exact FAQ match - use hybrid LLM + reasoning approach
        logger.info(
            "using_hybrid_ai",
            intent=analysis.detected_intent,
            confidence=analysis.confidence_score
        )
        
        # First get local reasoning response as fallback
        reasoning_result = self.reasoning_engine.reason(
            message=sanitized_message,
            detected_intent=analysis.detected_intent,
            keywords=analysis.matched_keywords,
            confidence_score=analysis.confidence_score
        )
        
        # Use hybrid LLM service for intelligent response
        llm_response = await self.llm_service.generate_response(
            query=sanitized_message,
            intent=analysis.detected_intent,
            keywords=analysis.matched_keywords,
            session_id=session_id,
            local_response=reasoning_result.response,
            local_confidence=reasoning_result.confidence
        )
        
        logger.info(
            "hybrid_response",
            source=llm_response.source,
            model=llm_response.model,
            confidence=llm_response.confidence,
            latency_ms=llm_response.latency_ms
        )
        
        # Update analysis with response confidence
        analysis_details.confidence_score = llm_response.confidence
        
        # Check if should escalate
        should_escalate = llm_response.should_escalate or reasoning_result.should_escalate
        
        if should_escalate:
            escalation_reason = llm_response.escalation_reason or reasoning_result.escalation_reason
            logger.info("escalating", reason=escalation_reason)
            
            response_time_ms = int((time.time() - start_time) * 1000)
            escalation_content = self._generate_escalation_message(analysis)
            
            # Guardar en Supabase
            await self._save_to_supabase(
                user_id=request.user_id,
                session_id=session_id,
                user_message=request.message,
                ai_response=escalation_content,
                analysis=analysis_details,
                response_time_ms=response_time_ms,
                escalated=True,
                escalation_reason=escalation_reason,
                response_source="escalation"
            )
            
            return SupportMessageResponse(
                message_id=message_id,
                response_type=ResponseType.ESCALATED,
                content=escalation_content,
                analysis=analysis_details,
                session_id=session_id,
                conversation_id=conversation_id,
                escalated=True,
                escalation_reason=escalation_reason
            )
        
        # Return hybrid AI response
        response = SupportMessageResponse(
            message_id=message_id,
            response_type=ResponseType.AI_RESPONSE,
            content=llm_response.content,
            analysis=analysis_details,
            session_id=session_id,
            conversation_id=conversation_id,
            escalated=False,
            suggested_actions=reasoning_result.suggested_actions
        )
        
        # Guardar en Supabase y aprender automáticamente
        response_time_ms = int((time.time() - start_time) * 1000)
        await self._save_to_supabase(
            user_id=request.user_id,
            session_id=session_id,
            user_message=request.message,
            ai_response=llm_response.content,
            analysis=analysis_details,
            response_time_ms=response_time_ms,
            escalated=False,
            response_source="llm" if llm_response.source == "llm" else "reasoning"
        )
        
        return response
    
    def _get_suggested_actions(self, faq_entry: FAQEntry) -> list:
        """Get suggested follow-up actions based on FAQ"""
        actions_map = {
            "purchase_track": ["Ver historial de pedidos", "Contactar vendedor"],
            "purchase_cancel": ["Ver historial de pedidos", "Solicitar reembolso"],
            "payment_methods": ["Agregar método de pago", "Ver billetera"],
            "account_password": ["Cambiar contraseña", "Activar 2FA"],
            "shipping_address": ["Gestionar direcciones"],
            "sell_how": ["Publicar producto", "Ver mis ventas"],
            "security_2fa": ["Configurar seguridad"],
        }
        
        return actions_map.get(faq_entry.id, [])
    
    def _convert_action_buttons(self, kb_buttons: list) -> list:
        """Convert knowledge base buttons to API response buttons"""
        if not kb_buttons:
            return []
        
        return [
            ActionButton(
                id=btn.id,
                label=btn.label,
                action=btn.action,
                target=btn.target,
                icon=btn.icon if btn.icon else None
            )
            for btn in kb_buttons
        ]
    
    async def _save_to_supabase(
        self,
        user_id: str,
        session_id: str,
        user_message: str,
        ai_response: str,
        analysis: AnalysisDetails,
        response_time_ms: int,
        escalated: bool,
        escalation_reason: Optional[str] = None,
        response_source: str = "unknown"
    ):
        """Guarda la interacción en Supabase, entrena el modelo, y alimenta el training pipeline"""
        try:
            category_str = analysis.category.value if hasattr(analysis.category, 'value') else str(analysis.category)
            
            # 1. Obtener o crear conversación
            conv_id = await self.supabase.get_or_create_conversation(user_id, session_id)
            
            if conv_id:
                # 2. Guardar mensaje del usuario
                msg_id_result = await self.supabase.save_user_message(
                    conversation_id=conv_id,
                    content=user_message,
                    analysis={
                        "confidence_score": analysis.confidence_score,
                        "detected_intent": analysis.detected_intent,
                        "category": category_str,
                        "clarity_score": analysis.clarity_score,
                        "completeness_score": analysis.completeness_score,
                        "is_aggressive": analysis.is_aggressive,
                        "is_confused": analysis.is_confused,
                        "matched_keywords": analysis.matched_keywords
                    }
                )
                
                # 3. Guardar respuesta de IA
                await self.supabase.save_ai_response(
                    conversation_id=conv_id,
                    content=ai_response,
                    response_time_ms=response_time_ms
                )
                
                # 4. Si fue escalada, crear registro de escalación
                if escalated:
                    await self.supabase.create_escalation(
                        conversation_id=conv_id,
                        user_id=user_id,
                        reason=escalation_reason or "low_confidence",
                        confidence_score=analysis.confidence_score,
                        detected_intent=analysis.detected_intent
                    )
                
                logger.info("interaction_saved_to_supabase", conversation_id=conv_id)
            
            # 5. TRAINING PIPELINE - Record interaction for ML training
            self.training_pipeline.record_interaction(
                user_message=user_message,
                detected_intent=analysis.detected_intent or "unknown",
                ai_response=ai_response,
                confidence_score=analysis.confidence_score,
                category=category_str,
                escalated=escalated,
                escalation_reason=escalation_reason or "",
                response_source=response_source,
                response_time_ms=response_time_ms,
                conversation_id=str(conv_id) if conv_id else "",
                message_id=str(msg_id_result) if conv_id and msg_id_result else "",
                user_id=user_id,
                session_id=session_id,
                matched_keywords=analysis.matched_keywords or [],
                clarity_score=analysis.clarity_score,
                completeness_score=analysis.completeness_score,
            )
            
            # 6. APRENDIZAJE CONTINUO - Aprender de CADA interacción
            if analysis.detected_intent and analysis.detected_intent != "unknown":
                was_helpful = not escalated and analysis.confidence_score >= 50
                self.local_ai.learn_from_feedback(
                    query=user_message,
                    intent=analysis.detected_intent,
                    response=ai_response,
                    was_helpful=was_helpful
                )
            
            # 7. Descubrir nuevos patrones
            if analysis.matched_keywords:
                self.learning_engine.learn_new_pattern(
                    question=user_message,
                    intent=analysis.detected_intent or "unknown",
                    keywords=analysis.matched_keywords
                )
            
            # 8. Cachear respuestas exitosas
            if not escalated and analysis.confidence_score >= 50:
                self.learning_engine.cache_response(
                    question=user_message,
                    response=ai_response,
                    intent=analysis.detected_intent or "unknown",
                    category=category_str,
                    confidence=analysis.confidence_score
                )
            
            # 9. Si fue escalada, registrar el patrón
            if escalated:
                self.learning_engine.learn_new_pattern(
                    question=user_message,
                    intent=f"escalated_{analysis.detected_intent or 'unknown'}",
                    keywords=analysis.matched_keywords or []
                )
            
            # 10. Flush training buffer periodically to Supabase
            if len(self.training_pipeline._training_buffer) >= 20:
                try:
                    await self.training_pipeline.flush_buffer_to_supabase()
                except Exception as flush_err:
                    logger.warn("training_buffer_flush_error", error=str(flush_err))
            
            # 11. Check if auto-retraining should trigger
            if self.training_pipeline.should_retrain():
                logger.info("auto_retrain_triggered", samples=self.training_pipeline._samples_since_last_train)
                # Don't block the response - schedule async retraining
                import asyncio
                asyncio.create_task(self._auto_retrain())
                
        except Exception as e:
            logger.error("supabase_save_error", error=str(e))
    
    async def _auto_retrain(self):
        """Run auto-retraining in background"""
        try:
            result = await self.training_pipeline.run_training(
                run_name=f"auto_{datetime.utcnow().strftime('%Y%m%d_%H%M%S')}",
                run_type="incremental",
            )
            logger.info(
                "auto_retrain_completed",
                status=result.status,
                accuracy=result.final_metrics.accuracy if result.final_metrics else 0,
                samples=result.training_samples,
            )
        except Exception as e:
            logger.error("auto_retrain_error", error=str(e))
    
    async def record_feedback(self, message_id: str, user_id: str, question: str, helpful: bool):
        """Record user feedback to improve learning"""
        self.learning_engine.record_feedback(message_id, question, helpful)
        
        # Guardar feedback en Supabase
        await self.supabase.save_feedback(message_id, user_id, helpful)
        
        # Entrenar modelo local con feedback
        if helpful:
            self.local_ai.learn_from_feedback(
                query=question,
                intent="unknown",
                response="",
                was_helpful=helpful
            )
    
    def get_learning_stats(self) -> dict:
        """Get learning engine statistics"""
        stats = self.learning_engine.get_stats()
        stats["local_ai"] = self.local_ai.get_stats()
        stats["training_pipeline"] = self.training_pipeline.get_stats()
        stats["live_metrics"] = self.training_pipeline.get_live_metrics(hours=24)
        return stats


# Singleton
_orchestrator: Optional[AIOrchestrator] = None


def get_orchestrator() -> AIOrchestrator:
    global _orchestrator
    if _orchestrator is None:
        _orchestrator = AIOrchestrator()
    return _orchestrator
