"""
Scoring Service - Bridge to C++ scoring engine

This module provides a Python fallback when the C++ extension is not available,
and handles all scoring/analysis operations.
"""

from typing import List, Optional
from dataclasses import dataclass
import re
from functools import lru_cache

# Try to import C++ extension, fall back to Python implementation
try:
    import scoring_engine as cpp_scoring
    CPP_AVAILABLE = True
except ImportError:
    CPP_AVAILABLE = False


@dataclass
class AnalysisResult:
    """Result of analyzing a user message"""
    confidence_score: int  # 0-100
    detected_intent: str
    clarity_score: float  # 0.0-1.0
    completeness_score: float  # 0.0-1.0
    is_aggressive: bool
    is_confused: bool
    is_spam: bool
    matched_keywords: List[str]
    recommendation: str  # "respond", "escalate", "block"


class PythonScoringEngine:
    """Pure Python fallback for scoring when C++ is unavailable"""
    
    # Spanish patterns
    AGGRESSIVE_WORDS = {
        "mierda", "carajo", "estafa", "robo", "ladrones", "basura",
        "inutil", "incompetentes", "porqueria", "asco", "fraude",
        "demanda", "denuncia", "abogado", "estafadores"
    }
    
    CONFUSION_INDICATORS = {
        "no entiendo", "no se", "como hago", "ayuda", "confundido",
        "perdido", "que significa", "no funciona", "problema", "???"
    }
    
    SPAM_PATTERNS = [
        r"https?://", r"www\.", r"gratis", r"dinero facil",
        r"bitcoin", r"crypto", r"invertir", r"click aqui",
        r"(.)\1{4,}"  # Repeated characters
    ]
    
    STOPWORDS = {
        "el", "la", "los", "las", "un", "una", "unos", "unas",
        "de", "del", "al", "a", "en", "con", "por", "para",
        "que", "y", "o", "pero", "si", "no", "se", "su", "sus",
        "este", "esta", "estos", "estas", "ese", "esa", "mi", "tu"
    }
    
    INTENT_PATTERNS = {
        "purchase_status": ["pedido", "compra", "orden", "estado", "tracking", "llega", "entrega"],
        "purchase_cancel": ["cancelar", "devolver", "reembolso", "anular"],
        "purchase_problem": ["problema", "error", "dañado", "roto", "incorrecto", "no llego"],
        "payment_methods": ["pago", "tarjeta", "metodo", "agregar"],
        "payment_problem": ["rechazado", "fallo", "cobro", "doble"],
        "refund": ["reembolso", "devolucion", "dinero", "regreso"],
        "account_access": ["contraseña", "password", "acceso", "entrar", "login", "recuperar"],
        "account_settings": ["perfil", "configuracion", "datos", "nombre", "foto", "email"],
        "account_delete": ["eliminar", "borrar", "cerrar", "desactivar", "cuenta"],
        "account_verify": ["verificar", "verificacion", "identidad", "ine", "pasaporte", "badge"],
        "shipping_info": ["envio", "direccion", "domicilio", "entregar"],
        "shipping_problem": ["no llego", "perdido", "demora", "retraso"],
        "sell_how": ["vender", "publicar", "producto", "anuncio"],
        "sell_payment": ["cobrar", "comision", "retiro", "venta"],
        "product_manage": ["editar", "modificar", "publicacion", "anuncio", "borrar"],
        "security_report": ["reportar", "fraude", "estafa", "sospechoso"],
        "security_verify": ["verificar", "2fa", "autenticacion"],
        "app_bug": ["error", "bug", "falla", "crash", "lento", "no funciona"],
        "wallet_info": ["billetera", "saldo", "retirar", "fondos", "cobrar"],
        "handshake_info": ["handshake", "presencial", "persona", "encuentro", "quedar"],
        "notification_settings": ["notificaciones", "silenciar", "alertas", "avisos"],
        "return_process": ["devolver", "devolucion", "regresar", "retornar"],
        "stories_info": ["historia", "story", "stories"],
        "rends_info": ["rend", "rends", "video", "reels", "corto"],
        "privacy_info": ["privacidad", "privado", "ocultar", "visibilidad"],
        "chat_info": ["chat", "mensaje", "llamada", "contactar"],
        "social_info": ["seguidores", "seguir", "followers"],
        "interaction_info": ["guardados", "favoritos", "like", "guardar"],
        "review_info": ["reseña", "calificar", "opinion", "estrellas"],
        "offer_info": ["oferta", "negociar", "contraoferta", "descuento"],
        "reputation_info": ["reputacion", "puntaje", "confianza", "nivel"],
        "zone_info": ["zona", "ubicacion", "cerca", "ciudad"],
        "language_info": ["idioma", "lenguaje", "ingles", "español"],
        "livestream_info": ["vivo", "live", "transmision", "stream"],
        "seller_problem": ["vendedor", "responde", "envia"],
        "highlights_info": ["highlights", "destacados"],
        "escalation_request": ["humano", "persona", "agente", "bot", "transferir"],
        "farewell": ["gracias", "adios", "bye", "luego"],
        "greeting": ["hola", "buenos", "buenas", "hey", "saludos"],
    }
    
    def normalize_text(self, text: str) -> str:
        """Normalize text for analysis"""
        # Lowercase
        normalized = text.lower()
        
        # Remove accents (simple)
        replacements = {
            'á': 'a', 'é': 'e', 'í': 'i', 'ó': 'o', 'ú': 'u',
            'ñ': 'n', 'ü': 'u'
        }
        for old, new in replacements.items():
            normalized = normalized.replace(old, new)
        
        # Normalize whitespace
        normalized = ' '.join(normalized.split())
        
        return normalized.strip()
    
    def extract_keywords(self, text: str) -> List[str]:
        """Extract meaningful keywords from text"""
        normalized = self.normalize_text(text)
        words = re.findall(r'\b\w+\b', normalized)
        
        keywords = [
            w for w in words 
            if len(w) > 2 and w not in self.STOPWORDS
        ]
        
        return keywords
    
    def calculate_clarity(self, text: str) -> float:
        """Calculate clarity score (0.0-1.0)"""
        if not text.strip():
            return 0.0
        
        clarity = 1.0
        
        # Penalize very short messages
        if len(text) < 10:
            clarity -= 0.3
        
        # Penalize excessive punctuation
        punct_count = sum(1 for c in text if c in '!?.,;:')
        if len(text) > 0 and punct_count / len(text) > 0.2:
            clarity -= 0.2
        
        # Penalize ALL CAPS
        if len(text) > 5:
            upper_ratio = sum(1 for c in text if c.isupper()) / len(text)
            if upper_ratio > 0.5:
                clarity -= 0.15
        
        return max(0.0, min(1.0, clarity))
    
    def calculate_completeness(self, text: str) -> float:
        """Calculate completeness score (0.0-1.0)"""
        normalized = self.normalize_text(text)
        keywords = self.extract_keywords(text)
        
        if not keywords:
            return 0.0
        
        completeness = 0.0
        
        # More words = more complete
        if len(keywords) >= 3:
            completeness += 0.3
        if len(keywords) >= 5:
            completeness += 0.2
        if len(keywords) >= 8:
            completeness += 0.2
        
        # Contains question structure
        question_words = ["como", "donde", "cuando", "por que", "cual", "que"]
        if any(qw in normalized for qw in question_words) or "?" in text:
            completeness += 0.15
        
        # Contains context keywords
        context_words = ["pedido", "compra", "venta", "cuenta", "pago", "envio", "producto"]
        if any(cw in normalized for cw in context_words):
            completeness += 0.15
        
        return min(1.0, completeness)
    
    def detect_aggression(self, text: str) -> bool:
        """Detect aggressive content"""
        normalized = self.normalize_text(text)
        
        # Check aggressive words
        for word in self.AGGRESSIVE_WORDS:
            if word in normalized:
                return True
        
        # Check for excessive caps (shouting)
        if len(text) > 10:
            upper_ratio = sum(1 for c in text if c.isupper()) / len(text)
            if upper_ratio > 0.7:
                return True
        
        return False
    
    def detect_confusion(self, text: str) -> bool:
        """Detect confused user"""
        normalized = self.normalize_text(text)
        
        confusion_count = sum(
            1 for indicator in self.CONFUSION_INDICATORS
            if indicator in normalized
        )
        
        # Multiple question marks
        if text.count('?') >= 3:
            confusion_count += 1
        
        return confusion_count >= 2
    
    def detect_spam(self, text: str) -> bool:
        """Detect spam content"""
        for pattern in self.SPAM_PATTERNS:
            if re.search(pattern, text, re.IGNORECASE):
                return True
        return False
    
    def match_intent(self, text: str, keywords: List[str]) -> tuple:
        """Match user intent, returns (intent_id, score)"""
        normalized = self.normalize_text(text)
        best_intent = "unknown"
        best_score = 0.0
        
        for intent, intent_keywords in self.INTENT_PATTERNS.items():
            matches = sum(
                1 for kw in intent_keywords
                if kw in normalized or any(kw in uk for uk in keywords)
            )
            
            score = matches / len(intent_keywords) if intent_keywords else 0
            
            if score > best_score:
                best_score = score
                best_intent = intent
        
        return best_intent, best_score
    
    def analyze(self, message: str) -> AnalysisResult:
        """Analyze a user message"""
        keywords = self.extract_keywords(message)
        clarity = self.calculate_clarity(message)
        completeness = self.calculate_completeness(message)
        is_aggressive = self.detect_aggression(message)
        is_confused = self.detect_confusion(message)
        is_spam = self.detect_spam(message)
        
        intent, intent_score = self.match_intent(message, keywords)
        
        # Calculate confidence
        has_negative = is_aggressive or is_spam
        
        base_score = (
            intent_score * 0.5 +
            clarity * 0.25 +
            completeness * 0.25
        )
        
        if has_negative:
            base_score *= 0.7
        
        if intent_score > 0.8:
            base_score = min(1.0, base_score * 1.1)
        
        confidence = int(round(base_score * 100))
        confidence = max(0, min(100, confidence))
        
        # Determine recommendation
        if is_spam:
            recommendation = "block"
            confidence = 0
        elif is_aggressive or confidence < 70:
            recommendation = "escalate"
        else:
            recommendation = "respond"
        
        return AnalysisResult(
            confidence_score=confidence,
            detected_intent=intent,
            clarity_score=clarity,
            completeness_score=completeness,
            is_aggressive=is_aggressive,
            is_confused=is_confused,
            is_spam=is_spam,
            matched_keywords=keywords,
            recommendation=recommendation
        )


# Singleton instances
_python_engine: Optional[PythonScoringEngine] = None


def get_scoring_engine():
    """Get the scoring engine (C++ if available, else Python)"""
    global _python_engine
    
    if CPP_AVAILABLE:
        return cpp_scoring.ScoringEngine.instance()
    
    if _python_engine is None:
        _python_engine = PythonScoringEngine()
    
    return _python_engine


def analyze_message(message: str) -> AnalysisResult:
    """Analyze a message and return results"""
    engine = get_scoring_engine()
    
    if CPP_AVAILABLE:
        # Convert C++ result to Python dataclass
        cpp_result = engine.analyze(message)
        return AnalysisResult(
            confidence_score=cpp_result.confidence_score,
            detected_intent=cpp_result.detected_intent,
            clarity_score=cpp_result.clarity_score,
            completeness_score=cpp_result.completeness_score,
            is_aggressive=cpp_result.is_aggressive,
            is_confused=cpp_result.is_confused,
            is_spam=cpp_result.is_spam,
            matched_keywords=list(cpp_result.matched_keywords),
            recommendation=cpp_result.recommendation
        )
    
    return engine.analyze(message)


def get_confidence_score(message: str) -> int:
    """Quick confidence score lookup"""
    return analyze_message(message).confidence_score


@lru_cache(maxsize=1000)
def cached_analyze(message: str) -> AnalysisResult:
    """Cached analysis for repeated messages"""
    return analyze_message(message)
