"""
Supabase Service - Persistencia y aprendizaje automático

Guarda todas las conversaciones, mensajes y feedback en Supabase.
Implementa aprendizaje automático basado en interacciones.
"""

import os
import json
from typing import Optional, Dict, List, Any
from datetime import datetime
from dataclasses import dataclass
from pathlib import Path
import structlog
import httpx

# Cargar variables de entorno desde .env
from dotenv import load_dotenv
env_path = Path(__file__).parent / ".env"
load_dotenv(env_path)

logger = structlog.get_logger()

@dataclass
class ConversationRecord:
    """Registro de conversación"""
    id: str
    user_id: str
    session_id: str
    status: str = "active"


@dataclass
class MessageRecord:
    """Registro de mensaje"""
    conversation_id: str
    role: str
    content: str
    confidence_score: Optional[int] = None
    detected_intent: Optional[str] = None
    intent_category: Optional[str] = None
    clarity_score: Optional[float] = None
    completeness_score: Optional[float] = None
    is_aggressive: bool = False
    is_confused: bool = False
    matched_keywords: Optional[List[str]] = None
    response_time_ms: Optional[int] = None


class SupabaseService:
    """
    Servicio para interactuar con Supabase.
    Guarda conversaciones, mensajes y maneja el aprendizaje automático.
    """
    
    def __init__(self):
        self.supabase_url = os.getenv("RENDLY_AI_SUPABASE_URL", os.getenv("SUPABASE_URL", ""))
        self.supabase_key = os.getenv("RENDLY_AI_SUPABASE_KEY", os.getenv("SUPABASE_SERVICE_KEY", ""))
        
        self.is_configured = bool(self.supabase_url and self.supabase_key)
        
        if not self.is_configured:
            logger.warning("supabase_not_configured", 
                message="Set SUPABASE_URL and SUPABASE_SERVICE_KEY environment variables")
        else:
            logger.info("supabase_configured", url=self.supabase_url[:30] + "...")
        
        self.headers = {
            "apikey": self.supabase_key,
            "Authorization": f"Bearer {self.supabase_key}",
            "Content-Type": "application/json",
            "Prefer": "return=representation"
        }
    
    async def _request(self, method: str, endpoint: str, data: Dict = None) -> Optional[Dict]:
        """Hace una petición a Supabase REST API"""
        if not self.is_configured:
            return None
        
        url = f"{self.supabase_url}/rest/v1/{endpoint}"
        
        try:
            async with httpx.AsyncClient(timeout=30.0) as client:
                if method == "GET":
                    response = await client.get(url, headers=self.headers)
                elif method == "POST":
                    response = await client.post(url, headers=self.headers, json=data)
                elif method == "PATCH":
                    response = await client.patch(url, headers=self.headers, json=data)
                elif method == "DELETE":
                    response = await client.delete(url, headers=self.headers)
                else:
                    return None
                
                if response.status_code in [200, 201]:
                    try:
                        return response.json()
                    except Exception:
                        return {"raw": response.text}
                elif response.status_code == 204:
                    return {"success": True}
                else:
                    logger.error("supabase_error", 
                        status=response.status_code, 
                        body=response.text[:200],
                        endpoint=endpoint)
                    return None
                    
        except Exception as e:
            logger.error("supabase_exception", error=str(e), endpoint=endpoint)
            return None
    
    async def create_conversation(self, user_id: str, session_id: str) -> Optional[str]:
        """Crea una nueva conversación"""
        data = {
            "user_id": user_id,
            "session_id": session_id,
            "status": "active"
        }
        
        result = await self._request("POST", "support_conversations", data)
        
        if result and len(result) > 0:
            conv_id = result[0].get("id")
            logger.info("conversation_created", conversation_id=conv_id)
            return conv_id
        
        return None
    
    async def get_or_create_conversation(self, user_id: str, session_id: str) -> Optional[str]:
        """Obtiene conversación existente o crea una nueva"""
        # Buscar conversación activa
        endpoint = f"support_conversations?user_id=eq.{user_id}&session_id=eq.{session_id}&status=eq.active&limit=1"
        result = await self._request("GET", endpoint)
        
        if result and len(result) > 0:
            return result[0].get("id")
        
        # Crear nueva
        return await self.create_conversation(user_id, session_id)
    
    async def is_conversation_escalated(self, conversation_id: str) -> bool:
        """Verifica si una conversación está escalada a agente humano.
        
        Una conversación se considera escalada si:
        1. Hay una escalación pendiente en ai_escalations, O
        2. Hay al menos un mensaje del agente humano (role='human_support') en la conversación
        """
        # Verificar escalación pendiente
        endpoint = f"ai_escalations?conversation_id=eq.{conversation_id}&status=eq.pending&limit=1"
        escalation_result = await self._request("GET", endpoint)
        if escalation_result and len(escalation_result) > 0:
            logger.info("conversation_escalated_pending", conversation_id=conversation_id)
            return True
        
        # Verificar si hay mensajes del agente humano en la conversación
        messages_endpoint = f"support_messages?conversation_id=eq.{conversation_id}&role=eq.human_support&limit=1"
        human_messages = await self._request("GET", messages_endpoint)
        if human_messages and len(human_messages) > 0:
            logger.info("conversation_has_human_agent", conversation_id=conversation_id)
            return True
        
        return False
    
    async def save_user_message(
        self,
        conversation_id: str,
        content: str,
        analysis: Dict
    ) -> Optional[str]:
        """Guarda mensaje del usuario con análisis"""
        data = {
            "conversation_id": conversation_id,
            "role": "user",
            "content": content,
            "confidence_score": analysis.get("confidence_score"),
            "detected_intent": analysis.get("detected_intent"),
            "intent_category": analysis.get("category"),
            "clarity_score": analysis.get("clarity_score"),
            "completeness_score": analysis.get("completeness_score"),
            "is_aggressive": analysis.get("is_aggressive", False),
            "is_confused": analysis.get("is_confused", False),
            "matched_keywords": analysis.get("matched_keywords", [])
        }
        
        result = await self._request("POST", "support_messages", data)
        
        if result and len(result) > 0:
            msg_id = result[0].get("id")
            logger.info("user_message_saved", message_id=msg_id)
            return msg_id
        
        return None
    
    async def save_ai_response(
        self,
        conversation_id: str,
        content: str,
        response_time_ms: int
    ) -> Optional[str]:
        """Guarda respuesta de IA"""
        data = {
            "conversation_id": conversation_id,
            "role": "ai",
            "content": content,
            "response_time_ms": response_time_ms
        }
        
        result = await self._request("POST", "support_messages", data)
        
        if result and len(result) > 0:
            msg_id = result[0].get("id")
            logger.info("ai_response_saved", message_id=msg_id)
            return msg_id
        
        return None
    
    async def create_escalation(
        self,
        conversation_id: str,
        user_id: str,
        reason: str,
        confidence_score: int,
        detected_intent: str
    ) -> Optional[str]:
        """Crea registro de escalación"""
        data = {
            "conversation_id": conversation_id,
            "user_id": user_id,
            "reason": reason,
            "confidence_score": confidence_score,
            "detected_intent": detected_intent,
            "status": "pending"
        }
        
        result = await self._request("POST", "ai_escalations", data)
        
        if result and len(result) > 0:
            esc_id = result[0].get("id")
            logger.info("escalation_created", escalation_id=esc_id)
            
            # Actualizar estado de conversación
            await self._request(
                "PATCH", 
                f"support_conversations?id=eq.{conversation_id}",
                {"status": "escalated", "escalated_at": datetime.utcnow().isoformat()}
            )
            
            return esc_id
        
        return None
    
    async def save_feedback(
        self,
        message_id: str,
        user_id: str,
        helpful: bool,
        feedback_text: Optional[str] = None
    ) -> bool:
        """Guarda feedback del usuario"""
        data = {
            "message_id": message_id,
            "user_id": user_id,
            "helpful": helpful,
            "feedback_text": feedback_text
        }
        
        result = await self._request("POST", "ai_feedback", data)
        
        if result:
            logger.info("feedback_saved", message_id=message_id, helpful=helpful)
            return True
        
        return False
    
    async def get_learning_data(self, limit: int = 100) -> List[Dict]:
        """
        Obtiene datos para aprendizaje automático.
        Retorna mensajes con feedback positivo para entrenar el modelo.
        """
        if not self.is_configured:
            return []
        
        # Obtener mensajes de usuario con feedback positivo
        endpoint = f"""support_messages?role=eq.user&select=content,detected_intent,matched_keywords,ai_feedback(helpful)&ai_feedback.helpful=eq.true&limit={limit}"""
        
        result = await self._request("GET", endpoint)
        
        if result:
            # Filtrar solo los que tienen feedback positivo
            learning_data = []
            for msg in result:
                if msg.get("ai_feedback") and any(f.get("helpful") for f in msg["ai_feedback"]):
                    learning_data.append({
                        "query": msg.get("content"),
                        "intent": msg.get("detected_intent"),
                        "keywords": msg.get("matched_keywords", [])
                    })
            
            logger.info("learning_data_fetched", count=len(learning_data))
            return learning_data
        
        return []
    
    async def get_conversation_history(
        self, 
        conversation_id: str, 
        limit: int = 20
    ) -> List[Dict]:
        """Obtiene historial de conversación"""
        endpoint = f"support_messages?conversation_id=eq.{conversation_id}&order=created_at.asc&limit={limit}"
        
        result = await self._request("GET", endpoint)
        
        if result:
            return [
                {"role": msg["role"], "content": msg["content"]}
                for msg in result
            ]
        
        return []
    
    async def get_stats(self) -> Dict:
        """Obtiene estadísticas de la IA"""
        if not self.is_configured:
            return {"error": "Supabase not configured"}
        
        # Obtener stats de últimos 30 días
        result = await self._request("GET", "v_ai_stats_summary")
        
        if result and len(result) > 0:
            return result[0]
        
        return {}
    
    async def get_agent_responses_for_learning(self, limit: int = 50) -> List[Dict]:
        """
        Obtiene respuestas de agentes humanos para aprendizaje.
        La IA aprende cómo los agentes resuelven problemas.
        """
        if not self.is_configured:
            return []
        
        # Obtener feedback con respuestas de agentes
        endpoint = f"ai_feedback?feedback_type=eq.agent_response&order=created_at.desc&limit={limit}"
        
        result = await self._request("GET", endpoint)
        
        if result:
            learning_examples = []
            for item in result:
                if item.get("user_message") and item.get("agent_response"):
                    learning_examples.append({
                        "user_query": item["user_message"],
                        "ideal_response": item["agent_response"],
                        "conversation_id": item.get("conversation_id")
                    })
            
            logger.info("agent_responses_fetched", count=len(learning_examples))
            return learning_examples
        
        return []
    
    async def check_for_similar_query(self, user_message: str) -> Optional[str]:
        """
        Busca si existe una respuesta de agente para una consulta similar.
        Usa búsqueda simple por palabras clave.
        """
        if not self.is_configured:
            return None
        
        # Obtener ejemplos de aprendizaje recientes
        examples = await self.get_agent_responses_for_learning(limit=100)
        
        if not examples:
            return None
        
        # Normalizar mensaje del usuario
        user_words = set(user_message.lower().split())
        
        best_match = None
        best_score = 0
        
        for example in examples:
            query_words = set(example["user_query"].lower().split())
            
            # Calcular similitud por Jaccard
            intersection = len(user_words & query_words)
            union = len(user_words | query_words)
            
            if union > 0:
                score = intersection / union
                
                # Si hay buena coincidencia (>40%), usar esta respuesta
                if score > 0.4 and score > best_score:
                    best_score = score
                    best_match = example["ideal_response"]
        
        if best_match:
            logger.info("similar_query_found", score=best_score)
        
        return best_match


# Singleton
_supabase_service: Optional[SupabaseService] = None


def get_supabase_service() -> SupabaseService:
    """Obtiene instancia singleton del servicio Supabase"""
    global _supabase_service
    if _supabase_service is None:
        _supabase_service = SupabaseService()
    return _supabase_service
