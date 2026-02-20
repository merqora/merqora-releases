"""
LLM Service - Sistema de IA híbrido local para Rendly

Este servicio provee:
- Modelo de IA local entrenable (sin APIs externas)
- Métricas automáticas
- Aprendizaje continuo con feedback
- Caché inteligente
"""

import os
import json
import time
import hashlib
from typing import Optional, Dict, List, Tuple
from dataclasses import dataclass, field
from datetime import datetime
import structlog
from functools import lru_cache

logger = structlog.get_logger()

# Importar modelo local
from local_ai_model import get_local_ai, LocalAIModel


@dataclass
class LLMResponse:
    """Response from LLM service"""
    content: str
    confidence: int
    source: str  # "llm", "local", "cached"
    model: str
    tokens_used: int = 0
    latency_ms: int = 0
    should_escalate: bool = False
    escalation_reason: Optional[str] = None


@dataclass
class ConversationMessage:
    """Message in conversation history"""
    role: str  # "user", "assistant", "system"
    content: str
    timestamp: float = field(default_factory=time.time)


@dataclass
class MetricEntry:
    """Metric entry for analytics"""
    timestamp: float
    query: str
    intent: str
    source: str
    confidence: int
    latency_ms: int
    helpful: Optional[bool] = None
    escalated: bool = False


class LLMMetrics:
    """Automatic metrics collection"""
    
    def __init__(self):
        self.metrics: List[MetricEntry] = []
        self.response_times: List[int] = []
        self.confidence_scores: List[int] = []
        self.escalation_count: int = 0
        self.total_queries: int = 0
        self.helpful_count: int = 0
        self.unhelpful_count: int = 0
        self.llm_calls: int = 0
        self.local_calls: int = 0
        self.cache_hits: int = 0
    
    def record(self, entry: MetricEntry):
        """Record a metric entry"""
        self.metrics.append(entry)
        self.total_queries += 1
        self.response_times.append(entry.latency_ms)
        self.confidence_scores.append(entry.confidence)
        
        if entry.escalated:
            self.escalation_count += 1
        
        if entry.source == "llm":
            self.llm_calls += 1
        elif entry.source == "local":
            self.local_calls += 1
        else:
            self.cache_hits += 1
        
        # Keep only last 1000 entries
        if len(self.metrics) > 1000:
            self.metrics = self.metrics[-1000:]
    
    def record_feedback(self, helpful: bool):
        """Record user feedback"""
        if helpful:
            self.helpful_count += 1
        else:
            self.unhelpful_count += 1
    
    def get_stats(self) -> Dict:
        """Get current metrics stats"""
        avg_latency = sum(self.response_times[-100:]) / max(len(self.response_times[-100:]), 1)
        avg_confidence = sum(self.confidence_scores[-100:]) / max(len(self.confidence_scores[-100:]), 1)
        
        return {
            "total_queries": self.total_queries,
            "avg_latency_ms": round(avg_latency, 2),
            "avg_confidence": round(avg_confidence, 2),
            "escalation_rate": round(self.escalation_count / max(self.total_queries, 1) * 100, 2),
            "llm_usage_rate": round(self.llm_calls / max(self.total_queries, 1) * 100, 2),
            "cache_hit_rate": round(self.cache_hits / max(self.total_queries, 1) * 100, 2),
            "helpful_rate": round(self.helpful_count / max(self.helpful_count + self.unhelpful_count, 1) * 100, 2),
            "llm_calls": self.llm_calls,
            "local_calls": self.local_calls,
            "cache_hits": self.cache_hits,
        }


class ResponseCache:
    """Cache for LLM responses with learning"""
    
    def __init__(self, max_size: int = 500):
        self.cache: Dict[str, Tuple[str, int, float]] = {}  # hash -> (response, confidence, timestamp)
        self.max_size = max_size
        self.learned_patterns: Dict[str, str] = {}  # intent+keywords -> best_response
    
    def _hash_query(self, query: str, intent: str) -> str:
        """Create hash for query"""
        normalized = query.lower().strip()
        key = f"{intent}:{normalized}"
        return hashlib.md5(key.encode()).hexdigest()[:16]
    
    def get(self, query: str, intent: str) -> Optional[Tuple[str, int]]:
        """Get cached response"""
        h = self._hash_query(query, intent)
        if h in self.cache:
            response, confidence, ts = self.cache[h]
            # Cache valid for 1 hour
            if time.time() - ts < 3600:
                return response, confidence
            del self.cache[h]
        return None
    
    def set(self, query: str, intent: str, response: str, confidence: int):
        """Cache a response"""
        if len(self.cache) >= self.max_size:
            # Remove oldest entries
            sorted_cache = sorted(self.cache.items(), key=lambda x: x[1][2])
            for key, _ in sorted_cache[:100]:
                del self.cache[key]
        
        h = self._hash_query(query, intent)
        self.cache[h] = (response, confidence, time.time())
    
    def learn(self, intent: str, keywords: List[str], response: str, was_helpful: bool):
        """Learn from feedback"""
        if was_helpful:
            key = f"{intent}:{','.join(sorted(keywords[:5]))}"
            self.learned_patterns[key] = response
    
    def get_learned(self, intent: str, keywords: List[str]) -> Optional[str]:
        """Get learned response pattern"""
        key = f"{intent}:{','.join(sorted(keywords[:5]))}"
        return self.learned_patterns.get(key)


class HybridLLMService:
    """
    Hybrid AI service that combines:
    - Real LLM (OpenAI/Anthropic) for intelligent responses
    - Local reasoning engine for fast/offline responses
    - Caching and learning for improvement over time
    """
    
    # Rendly-specific system prompt
    SYSTEM_PROMPT = """Eres el asistente de soporte de Rendly, una aplicación de compra-venta similar a Wallapop/Mercado Libre.

CONOCIMIENTO DE RENDLY:
- Comisión por venta: 10%
- Tiempo de envío estándar: 5-10 días hábiles
- Tiempo de envío express: 2-4 días hábiles
- Reembolso en tarjeta: 5-10 días hábiles
- Ventana de devolución: 7 días desde entrega
- Soporte: 24/7 por chat

NAVEGACIÓN EN LA APP:
- Pedidos: Perfil → Historial de pedidos
- Direcciones: Perfil → Direcciones
- Pagos: Perfil → Métodos de pago
- Billetera: Perfil → Billetera
- Configuración: Perfil → Configuración
- Seguridad: Perfil → Configuración → Seguridad
- Publicar: Botón + → Publicación
- Ventas: Perfil → Mis ventas

REGLAS:
1. Responde en español, de forma amigable pero profesional
2. Sé conciso pero completo
3. Usa formato con **negritas** para rutas y acciones importantes
4. Incluye emojis relevantes pero no excesivos
5. Si no puedes ayudar, indica que transferirás a un agente humano
6. NUNCA inventes información que no conozcas
7. Para problemas complejos (cobros dobles, fraudes, etc.) sugiere escalación

RESPONDE SOLO con la respuesta al usuario, sin explicaciones adicionales."""

    def __init__(self):
        self.metrics = LLMMetrics()
        self.cache = ResponseCache()
        self.conversations: Dict[str, List[ConversationMessage]] = {}
        
        # Usar modelo local entrenado
        self.local_ai = get_local_ai()
        self.llm_provider = "local_trained"
        self.model = "rendly_ai_v1"
        
        logger.info(
            "local_ai_initialized",
            examples=self.local_ai.get_stats()['total_examples'],
            intents=self.local_ai.get_stats()['unique_intents']
        )
    
    def _get_conversation_history(self, session_id: str, limit: int = 5) -> List[Dict]:
        """Get recent conversation history for context"""
        if session_id not in self.conversations:
            return []
        
        messages = self.conversations[session_id][-limit:]
        return [{"role": m.role, "content": m.content} for m in messages]
    
    def _add_to_conversation(self, session_id: str, role: str, content: str):
        """Add message to conversation history"""
        if session_id not in self.conversations:
            self.conversations[session_id] = []
        
        self.conversations[session_id].append(ConversationMessage(role=role, content=content))
        
        # Keep only last 20 messages per session
        if len(self.conversations[session_id]) > 20:
            self.conversations[session_id] = self.conversations[session_id][-20:]
    
    async def _call_openai(self, messages: List[Dict]) -> Tuple[str, int]:
        """Call OpenAI API"""
        try:
            response = await self.http_client.post(
                "https://api.openai.com/v1/chat/completions",
                headers={
                    "Authorization": f"Bearer {self.openai_key}",
                    "Content-Type": "application/json"
                },
                json={
                    "model": self.model,
                    "messages": messages,
                    "max_tokens": 500,
                    "temperature": 0.7,
                }
            )
            
            if response.status_code == 200:
                data = response.json()
                content = data["choices"][0]["message"]["content"]
                tokens = data.get("usage", {}).get("total_tokens", 0)
                return content, tokens
            else:
                logger.error("openai_error", status=response.status_code)
                return None, 0
                
        except Exception as e:
            logger.error("openai_exception", error=str(e))
            return None, 0
    
    async def _call_anthropic(self, messages: List[Dict]) -> Tuple[str, int]:
        """Call Anthropic API"""
        try:
            # Convert messages format for Anthropic
            system_msg = ""
            chat_messages = []
            
            for msg in messages:
                if msg["role"] == "system":
                    system_msg = msg["content"]
                else:
                    chat_messages.append(msg)
            
            response = await self.http_client.post(
                "https://api.anthropic.com/v1/messages",
                headers={
                    "x-api-key": self.anthropic_key,
                    "Content-Type": "application/json",
                    "anthropic-version": "2023-06-01"
                },
                json={
                    "model": self.model,
                    "max_tokens": 500,
                    "system": system_msg,
                    "messages": chat_messages,
                }
            )
            
            if response.status_code == 200:
                data = response.json()
                content = data["content"][0]["text"]
                tokens = data.get("usage", {}).get("input_tokens", 0) + data.get("usage", {}).get("output_tokens", 0)
                return content, tokens
            else:
                logger.error("anthropic_error", status=response.status_code)
                return None, 0
                
        except Exception as e:
            logger.error("anthropic_exception", error=str(e))
            return None, 0
    
    async def generate_response(
        self,
        query: str,
        intent: str,
        keywords: List[str],
        session_id: str,
        local_response: str,
        local_confidence: int
    ) -> LLMResponse:
        """
        Genera respuesta usando modelo local entrenado:
        1. Revisa caché
        2. Usa modelo local entrenado
        3. Aprende de las interacciones
        """
        start_time = time.time()
        
        # 1. Check cache first
        cached = self.cache.get(query, intent)
        if cached:
            response, confidence = cached
            latency = int((time.time() - start_time) * 1000)
            
            self.metrics.record(MetricEntry(
                timestamp=time.time(),
                query=query,
                intent=intent,
                source="cached",
                confidence=confidence,
                latency_ms=latency
            ))
            
            return LLMResponse(
                content=response,
                confidence=confidence,
                source="cached",
                model="cache",
                latency_ms=latency
            )
        
        # 2. Usar modelo local entrenado
        prediction = self.local_ai.predict(query)
        latency = int((time.time() - start_time) * 1000)
        
        # Agregar a historial de conversación
        self._add_to_conversation(session_id, "user", query)
        self._add_to_conversation(session_id, "assistant", prediction.response)
        
        # Cachear respuesta
        self.cache.set(query, prediction.intent, prediction.response, prediction.confidence)
        
        # Detectar si debe escalar
        should_escalate = (
            prediction.confidence < 40 or
            prediction.intent == "unknown" or
            any(word in query.lower() for word in ["fraude", "robo", "estafa", "cobro doble", "hackeo"])
        )
        
        self.metrics.record(MetricEntry(
            timestamp=time.time(),
            query=query,
            intent=prediction.intent,
            source="local_ai",
            confidence=prediction.confidence,
            latency_ms=latency,
            escalated=should_escalate
        ))
        
        logger.info(
            "local_ai_response",
            intent=prediction.intent,
            confidence=prediction.confidence,
            reasoning=prediction.reasoning,
            latency_ms=latency
        )
        
        return LLMResponse(
            content=prediction.response,
            confidence=prediction.confidence,
            source="local_ai",
            model=self.model,
            latency_ms=latency,
            should_escalate=should_escalate,
            escalation_reason="low_confidence" if should_escalate else None
        )
    
    def record_feedback(self, query: str, intent: str, keywords: List[str], response: str, helpful: bool):
        """Record feedback for learning - entrena el modelo local"""
        self.metrics.record_feedback(helpful)
        self.cache.learn(intent, keywords, response, helpful)
        
        # Entrenar modelo local con feedback
        self.local_ai.learn_from_feedback(query, intent, response, helpful)
        
        logger.info(
            "feedback_recorded",
            intent=intent,
            helpful=helpful,
            total_helpful=self.metrics.helpful_count,
            total_unhelpful=self.metrics.unhelpful_count
        )
    
    def get_metrics(self) -> Dict:
        """Get current metrics"""
        stats = self.metrics.get_stats()
        stats["llm_provider"] = self.llm_provider
        stats["model"] = self.model
        stats["cached_responses"] = len(self.cache.cache)
        stats["learned_patterns"] = len(self.cache.learned_patterns)
        
        # Agregar stats del modelo local
        local_stats = self.local_ai.get_stats()
        stats["local_ai"] = local_stats
        
        return stats


# Singleton
_llm_service: Optional[HybridLLMService] = None


def get_llm_service() -> HybridLLMService:
    """Get singleton LLM service"""
    global _llm_service
    if _llm_service is None:
        _llm_service = HybridLLMService()
    return _llm_service
