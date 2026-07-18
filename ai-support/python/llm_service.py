"""
LLM Service - Sistema de IA híbrido para Vinzay

Combina:
- Groq (Llama 3 70B) como LLM principal
- Modelo local entrenado como fallback
- Caché inteligente y aprendizaje continuo
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
import httpx

logger = structlog.get_logger()

from local_ai_model import get_local_ai, LocalAIModel


@dataclass
class LLMResponse:
    content: str
    confidence: int
    source: str
    model: str
    tokens_used: int = 0
    latency_ms: int = 0
    should_escalate: bool = False
    escalation_reason: Optional[str] = None


@dataclass
class ConversationMessage:
    role: str
    content: str
    timestamp: float = field(default_factory=time.time)


@dataclass
class MetricEntry:
    timestamp: float
    query: str
    intent: str
    source: str
    confidence: int
    latency_ms: int
    helpful: Optional[bool] = None
    escalated: bool = False


class LLMMetrics:
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
        if len(self.metrics) > 1000:
            self.metrics = self.metrics[-1000:]

    def record_feedback(self, helpful: bool):
        if helpful:
            self.helpful_count += 1
        else:
            self.unhelpful_count += 1

    def get_stats(self) -> Dict:
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
    def __init__(self, max_size: int = 500):
        self.cache: Dict[str, Tuple[str, int, float]] = {}
        self.max_size = max_size
        self.learned_patterns: Dict[str, str] = {}

    def _hash_query(self, query: str, intent: str) -> str:
        normalized = query.lower().strip()
        key = f"{intent}:{normalized}"
        return hashlib.md5(key.encode()).hexdigest()[:16]

    def get(self, query: str, intent: str) -> Optional[Tuple[str, int]]:
        h = self._hash_query(query, intent)
        if h in self.cache:
            response, confidence, ts = self.cache[h]
            if time.time() - ts < 3600:
                return response, confidence
            del self.cache[h]
        return None

    def set(self, query: str, intent: str, response: str, confidence: int):
        if len(self.cache) >= self.max_size:
            sorted_cache = sorted(self.cache.items(), key=lambda x: x[1][2])
            for key, _ in sorted_cache[:100]:
                del self.cache[key]
        h = self._hash_query(query, intent)
        self.cache[h] = (response, confidence, time.time())

    def learn(self, intent: str, keywords: List[str], response: str, was_helpful: bool):
        if was_helpful:
            key = f"{intent}:{','.join(sorted(keywords[:5]))}"
            self.learned_patterns[key] = response

    def get_learned(self, intent: str, keywords: List[str]) -> Optional[str]:
        key = f"{intent}:{','.join(sorted(keywords[:5]))}"
        return self.learned_patterns.get(key)


class HybridLLMService:
    SYSTEM_PROMPT = """Eres el asistente de soporte de Vinzay, una aplicacion de compra-venta similar a Wallapop/Mercado Libre.

CONOCIMIENTO DE Vinzay:
- Comision por venta: 10%
- Tiempo de envio estandar: 5-10 dias habiles
- Tiempo de envio express: 2-4 dias habiles
- Reembolso en tarjeta: 5-10 dias habiles
- Ventana de devolucion: 7 dias desde entrega
- Soporte: 24/7 por chat

NAVEGACION EN LA APP:
- Pedidos: Perfil > Historial de pedidos
- Direcciones: Perfil > Direcciones
- Pagos: Perfil > Metodos de pago
- Billetera: Perfil > Billetera
- Configuracion: Perfil > Configuracion
- Seguridad: Perfil > Configuracion > Seguridad
- Publicar: Boton + > Publicacion
- Ventas: Perfil > Mis ventas

REGLAS:
1. Responde en espanol, de forma amigable pero profesional
2. Se conciso pero completo
3. Usa formato con **negritas** para rutas y acciones importantes
4. Incluye emojis relevantes pero no excesivos
5. Si no puedes ayudar, indica que transferiras a un agente humano
6. NUNCA inventes informacion que no conozcas
7. Para problemas complejos (cobros dobles, fraudes, etc.) sugiere escalacion

RESPONDE SOLO con la respuesta al usuario, sin explicaciones adicionales."""

    GROQ_MODELS = {
        "primary": "llama-3.3-70b-versatile",
        "fallback": "llama-3.1-8b-instant",
    }

    def __init__(self):
        self.metrics = LLMMetrics()
        self.cache = ResponseCache()
        self.conversations: Dict[str, List[ConversationMessage]] = {}

        self.groq_key = os.getenv("VINZAY_AI_GROQ_KEY", os.getenv("GROQ_API_KEY", ""))
        self.groq_enabled = bool(self.groq_key)

        self.local_ai = get_local_ai()

        if self.groq_enabled:
            self.llm_provider = "groq"
            self.model = self.GROQ_MODELS["primary"]
            self.http_client = httpx.AsyncClient(timeout=30.0)
            logger.info("groq_initialized", model=self.model)
        else:
            self.llm_provider = "local_trained"
            self.model = "VINZAY_AI_v1"
            logger.info("groq_not_configured", message="Set VINZAY_AI_GROQ_KEY or GROQ_API_KEY env var")

        logger.info(
            "ai_initialized",
            provider=self.llm_provider,
            examples=self.local_ai.get_stats()['total_examples'],
            intents=self.local_ai.get_stats()['unique_intents']
        )

    def _get_conversation_history(self, session_id: str, limit: int = 5) -> List[Dict]:
        if session_id not in self.conversations:
            return []
        messages = self.conversations[session_id][-limit:]
        return [{"role": m.role, "content": m.content} for m in messages]

    def _add_to_conversation(self, session_id: str, role: str, content: str):
        if session_id not in self.conversations:
            self.conversations[session_id] = []
        self.conversations[session_id].append(ConversationMessage(role=role, content=content))
        if len(self.conversations[session_id]) > 20:
            self.conversations[session_id] = self.conversations[session_id][-20:]

    async def _call_groq(self, messages: List[Dict], model: str = None) -> Tuple[Optional[str], int]:
        model = model or self.GROQ_MODELS["primary"]
        try:
            groq_messages = [m for m in messages if m["role"] != "system"]
            system_content = next((m["content"] for m in messages if m["role"] == "system"), self.SYSTEM_PROMPT)

            response = await self.http_client.post(
                "https://api.groq.com/openai/v1/chat/completions",
                headers={
                    "Authorization": f"Bearer {self.groq_key}",
                    "Content-Type": "application/json",
                },
                json={
                    "model": model,
                    "messages": [{"role": "system", "content": system_content}, *groq_messages],
                    "max_tokens": 600,
                    "temperature": 0.7,
                },
            )

            if response.status_code == 200:
                data = response.json()
                content = data["choices"][0]["message"]["content"]
                tokens = data.get("usage", {}).get("total_tokens", 0)
                return content, tokens
            else:
                logger.error("groq_error", status=response.status_code, body=response.text[:200])
                return None, 0

        except Exception as e:
            logger.error("groq_exception", error=str(e))
            return None, 0

    async def generate_response(
        self,
        query: str,
        intent: str,
        keywords: List[str],
        session_id: str,
        local_response: str,
        local_confidence: int,
    ) -> LLMResponse:
        start_time = time.time()

        cached = self.cache.get(query, intent)
        if cached:
            response, confidence = cached
            latency = int((time.time() - start_time) * 1000)
            self.metrics.record(MetricEntry(
                timestamp=time.time(), query=query, intent=intent,
                source="cached", confidence=confidence, latency_ms=latency,
            ))
            return LLMResponse(
                content=response, confidence=confidence,
                source="cached", model="cache", latency_ms=latency,
            )

        self._add_to_conversation(session_id, "user", query)

        groq_success = False
        if self.groq_enabled:
            history = self._get_conversation_history(session_id, limit=5)
            user_msg = {"role": "user", "content": query}
            if history:
                groq_messages = history + [user_msg]
            else:
                groq_messages = [user_msg]

            content, tokens = await self._call_groq(groq_messages)
            if content:
                latency = int((time.time() - start_time) * 1000)
                self._add_to_conversation(session_id, "assistant", content)
                self.cache.set(query, intent, content, 90)
                self.metrics.record(MetricEntry(
                    timestamp=time.time(), query=query, intent=intent,
                    source="llm", confidence=90, latency_ms=latency,
                ))
                self.metrics.llm_calls += 1

                should_escalate = any(
                    word in query.lower() for word in ["fraude", "robo", "estafa", "cobro doble", "hackeo"]
                )
                groq_success = True

                return LLMResponse(
                    content=content, confidence=90, source="groq",
                    model=self.GROQ_MODELS["primary"], tokens_used=tokens,
                    latency_ms=latency, should_escalate=should_escalate,
                    escalation_reason="high_severity_topic" if should_escalate else None,
                )

        prediction = self.local_ai.predict(query)
        latency = int((time.time() - start_time) * 1000)
        self._add_to_conversation(session_id, "assistant", prediction.response)
        self.cache.set(query, prediction.intent, prediction.response, prediction.confidence)

        should_escalate = (
            prediction.confidence < 40
            or prediction.intent == "unknown"
            or any(word in query.lower() for word in ["fraude", "robo", "estafa", "cobro doble", "hackeo"])
        )

        self.metrics.record(MetricEntry(
            timestamp=time.time(), query=query, intent=prediction.intent,
            source="local_ai", confidence=prediction.confidence,
            latency_ms=latency, escalated=should_escalate,
        ))

        logger.info(
            "local_ai_response",
            intent=prediction.intent, confidence=prediction.confidence,
            reasoning=prediction.reasoning, latency_ms=latency,
        )

        return LLMResponse(
            content=prediction.response, confidence=prediction.confidence,
            source="local_ai", model=self.model, latency_ms=latency,
            should_escalate=should_escalate,
            escalation_reason="low_confidence" if should_escalate else None,
        )

    def record_feedback(self, query: str, intent: str, keywords: List[str], response: str, helpful: bool):
        self.metrics.record_feedback(helpful)
        self.cache.learn(intent, keywords, response, helpful)
        self.local_ai.learn_from_feedback(query, intent, response, helpful)
        logger.info(
            "feedback_recorded", intent=intent, helpful=helpful,
            total_helpful=self.metrics.helpful_count,
            total_unhelpful=self.metrics.unhelpful_count,
        )

    def get_metrics(self) -> Dict:
        stats = self.metrics.get_stats()
        stats["llm_provider"] = self.llm_provider
        stats["model"] = self.model
        stats["cached_responses"] = len(self.cache.cache)
        stats["learned_patterns"] = len(self.cache.learned_patterns)
        stats["groq_enabled"] = self.groq_enabled
        local_stats = self.local_ai.get_stats()
        stats["local_ai"] = local_stats
        return stats


_llm_service: Optional[HybridLLMService] = None


def get_llm_service() -> HybridLLMService:
    global _llm_service
    if _llm_service is None:
        _llm_service = HybridLLMService()
    return _llm_service
