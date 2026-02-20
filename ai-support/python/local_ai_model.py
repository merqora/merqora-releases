"""
Local AI Model - Modelo de IA entrenable para Rendly

Sistema de IA local que:
- Aprende de ejemplos de entrenamiento
- Usa TF-IDF + similitud coseno para matching
- Clasificación de intents con Naive Bayes
- Generación de respuestas basada en templates + contexto
- Mejora continua con feedback de usuarios
"""

import json
import math
import re
import pickle
from collections import defaultdict, Counter
from typing import Dict, List, Tuple, Optional, Set
from dataclasses import dataclass, field
from pathlib import Path
import structlog

logger = structlog.get_logger()

# Directorio para guardar el modelo entrenado
MODEL_DIR = Path(__file__).parent / "trained_model"
MODEL_DIR.mkdir(exist_ok=True)


@dataclass
class TrainingExample:
    """Ejemplo de entrenamiento"""
    query: str
    intent: str
    response: str
    keywords: List[str] = field(default_factory=list)
    variations: List[str] = field(default_factory=list)


@dataclass
class PredictionResult:
    """Resultado de predicción"""
    intent: str
    confidence: float
    response: str
    matched_example: Optional[str] = None
    reasoning: str = ""


class TextProcessor:
    """Procesador de texto para normalización y tokenización"""
    
    STOPWORDS_ES = {
        'de', 'la', 'que', 'el', 'en', 'y', 'a', 'los', 'del', 'se', 'las',
        'por', 'un', 'para', 'con', 'no', 'una', 'su', 'al', 'lo', 'como',
        'más', 'pero', 'sus', 'le', 'ya', 'o', 'este', 'sí', 'porque', 'esta',
        'entre', 'cuando', 'muy', 'sin', 'sobre', 'también', 'me', 'hasta',
        'hay', 'donde', 'quien', 'desde', 'todo', 'nos', 'durante', 'todos',
        'uno', 'les', 'ni', 'contra', 'otros', 'ese', 'eso', 'ante', 'ellos',
        'e', 'esto', 'mí', 'antes', 'algunos', 'qué', 'unos', 'yo', 'otro',
        'otras', 'otra', 'él', 'tanto', 'esa', 'estos', 'mucho', 'quienes',
        'nada', 'muchos', 'cual', 'poco', 'ella', 'estar', 'estas', 'algunas',
        'algo', 'nosotros', 'mi', 'mis', 'tú', 'te', 'ti', 'tu', 'tus',
        'es', 'son', 'está', 'están', 'hola', 'buenas', 'buenos', 'gracias',
        'oye', 'hey', 'bro', 'we', 'wey', 'porfa', 'porfavor', 'please',
    }
    
    @staticmethod
    def normalize(text: str) -> str:
        """Normaliza texto"""
        text = text.lower().strip()
        # Reemplazar acentos
        replacements = {
            'á': 'a', 'é': 'e', 'í': 'i', 'ó': 'o', 'ú': 'u',
            'ü': 'u', 'ñ': 'n'
        }
        for old, new in replacements.items():
            text = text.replace(old, new)
        return text
    
    @staticmethod
    def tokenize(text: str, remove_stopwords: bool = True) -> List[str]:
        """Tokeniza texto en palabras"""
        text = TextProcessor.normalize(text)
        # Extraer palabras (alfanuméricas)
        tokens = re.findall(r'\b[a-z0-9]+\b', text)
        
        if remove_stopwords:
            tokens = [t for t in tokens if t not in TextProcessor.STOPWORDS_ES and len(t) > 2]
        
        return tokens
    
    @staticmethod
    def extract_entities(text: str) -> Dict[str, List[str]]:
        """Extrae entidades del texto"""
        entities = {
            'numbers': re.findall(r'\b\d+\b', text),
            'prices': re.findall(r'\$?\d+(?:[.,]\d+)?(?:\s*(?:pesos|mxn|usd|dolares))?', text.lower()),
            'emails': re.findall(r'\b[\w.-]+@[\w.-]+\.\w+\b', text),
            'order_ids': re.findall(r'#?\d{4,}', text),
            'dates': re.findall(r'\d{1,2}[/-]\d{1,2}(?:[/-]\d{2,4})?', text),
            'times': re.findall(r'\b\d+\s*(?:dias?|horas?|minutos?|semanas?|meses?)\b', text.lower()),
        }
        return {k: v for k, v in entities.items() if v}


class TFIDFVectorizer:
    """Vectorizador TF-IDF simple"""
    
    def __init__(self):
        self.vocabulary: Dict[str, int] = {}
        self.idf: Dict[str, float] = {}
        self.documents: List[List[str]] = []
    
    def fit(self, documents: List[str]):
        """Entrena el vectorizador con documentos"""
        self.documents = [TextProcessor.tokenize(doc) for doc in documents]
        
        # Construir vocabulario
        all_tokens = set()
        for tokens in self.documents:
            all_tokens.update(tokens)
        
        self.vocabulary = {token: idx for idx, token in enumerate(sorted(all_tokens))}
        
        # Calcular IDF
        n_docs = len(self.documents)
        doc_freq = Counter()
        
        for tokens in self.documents:
            unique_tokens = set(tokens)
            doc_freq.update(unique_tokens)
        
        self.idf = {
            token: math.log((n_docs + 1) / (freq + 1)) + 1
            for token, freq in doc_freq.items()
        }
    
    def transform(self, text: str) -> Dict[str, float]:
        """Transforma texto a vector TF-IDF"""
        tokens = TextProcessor.tokenize(text)
        tf = Counter(tokens)
        total = len(tokens) if tokens else 1
        
        vector = {}
        for token, count in tf.items():
            if token in self.vocabulary:
                tfidf = (count / total) * self.idf.get(token, 1.0)
                vector[token] = tfidf
        
        return vector
    
    def cosine_similarity(self, vec1: Dict[str, float], vec2: Dict[str, float]) -> float:
        """Calcula similitud coseno entre dos vectores"""
        if not vec1 or not vec2:
            return 0.0
        
        # Producto punto
        common_keys = set(vec1.keys()) & set(vec2.keys())
        dot_product = sum(vec1[k] * vec2[k] for k in common_keys)
        
        # Magnitudes
        mag1 = math.sqrt(sum(v ** 2 for v in vec1.values()))
        mag2 = math.sqrt(sum(v ** 2 for v in vec2.values()))
        
        if mag1 == 0 or mag2 == 0:
            return 0.0
        
        return dot_product / (mag1 * mag2)


class NaiveBayesClassifier:
    """Clasificador Naive Bayes para intents"""
    
    def __init__(self):
        self.class_counts: Counter = Counter()
        self.feature_counts: Dict[str, Counter] = defaultdict(Counter)
        self.vocabulary: Set[str] = set()
        self.total_docs: int = 0
    
    def fit(self, documents: List[str], labels: List[str]):
        """Entrena el clasificador"""
        self.total_docs = len(documents)
        
        for doc, label in zip(documents, labels):
            tokens = TextProcessor.tokenize(doc)
            self.class_counts[label] += 1
            self.vocabulary.update(tokens)
            
            for token in tokens:
                self.feature_counts[label][token] += 1
    
    def predict(self, text: str) -> Tuple[str, float]:
        """Predice la clase más probable"""
        tokens = TextProcessor.tokenize(text)
        
        if not tokens or not self.class_counts:
            return "unknown", 0.0
        
        scores = {}
        vocab_size = len(self.vocabulary) + 1
        
        for label in self.class_counts:
            # Log prior
            log_prob = math.log(self.class_counts[label] / self.total_docs)
            
            # Log likelihood con Laplace smoothing
            total_words = sum(self.feature_counts[label].values())
            
            for token in tokens:
                count = self.feature_counts[label].get(token, 0)
                log_prob += math.log((count + 1) / (total_words + vocab_size))
            
            scores[label] = log_prob
        
        # Normalizar a probabilidades
        max_score = max(scores.values())
        exp_scores = {k: math.exp(v - max_score) for k, v in scores.items()}
        total = sum(exp_scores.values())
        
        probs = {k: v / total for k, v in exp_scores.items()}
        best_label = max(probs, key=probs.get)
        
        return best_label, probs[best_label]


class ResponseGenerator:
    """Generador de respuestas basado en templates y contexto"""
    
    def __init__(self):
        self.response_templates: Dict[str, List[str]] = {}
        self.context_modifiers: Dict[str, str] = {}
    
    def add_template(self, intent: str, response: str):
        """Agrega template de respuesta"""
        if intent not in self.response_templates:
            self.response_templates[intent] = []
        self.response_templates[intent].append(response)
    
    def generate(self, intent: str, context: Dict) -> str:
        """Genera respuesta basada en intent y contexto"""
        templates = self.response_templates.get(intent, [])
        
        if not templates:
            return self._generate_fallback(intent, context)
        
        # Seleccionar mejor template basado en contexto
        best_template = templates[0]
        
        # Personalizar con contexto
        response = self._apply_context(best_template, context)
        
        return response
    
    def _apply_context(self, template: str, context: Dict) -> str:
        """Aplica contexto al template"""
        response = template
        
        # Reemplazar placeholders
        entities = context.get('entities', {})
        
        if entities.get('order_ids'):
            order_id = entities['order_ids'][0]
            response = response.replace('{pedido}', f'#{order_id}')
            response = response.replace('{order_id}', f'#{order_id}')
        
        if entities.get('times'):
            time_ref = entities['times'][0]
            response += f"\n\n⏰ Mencionas {time_ref}. "
        
        if entities.get('prices'):
            price = entities['prices'][0]
            response += f"\n\n💰 Con respecto al monto de {price}: "
        
        return response
    
    def _generate_fallback(self, intent: str, context: Dict) -> str:
        """Genera respuesta fallback"""
        return f"""Entiendo que necesitas ayuda con {intent.replace('_', ' ')}.

Puedo asistirte con:
• 🛒 Compras y pedidos
• 💰 Pagos y métodos de pago
• 📦 Envíos y direcciones
• 👤 Tu cuenta
• 🏪 Vender productos

¿Podrías darme más detalles sobre lo que necesitas?"""


class LocalAIModel:
    """
    Modelo de IA local entrenable para Rendly
    
    Características:
    - Clasificación de intents con Naive Bayes
    - Matching de consultas con TF-IDF + similitud coseno
    - Generación de respuestas con templates + contexto
    - Entrenamiento incremental
    - Persistencia del modelo entrenado
    """
    
    def __init__(self):
        self.vectorizer = TFIDFVectorizer()
        self.classifier = NaiveBayesClassifier()
        self.generator = ResponseGenerator()
        self.training_examples: List[TrainingExample] = []
        self.example_vectors: List[Tuple[str, Dict[str, float], str]] = []
        self.is_trained = False
        
        # Cargar modelo si existe
        self._load_model()
        
        # Si no hay modelo, cargar datos iniciales
        if not self.is_trained:
            self._load_initial_training_data()
    
    def _load_initial_training_data(self):
        """Carga datos de entrenamiento iniciales"""
        initial_data = [
            # Publicar/Vender
            TrainingExample(
                query="como publico un producto",
                intent="sell_publish",
                response="""Para publicar un producto en Rendly:

1. Toca el botón **"+"** en la barra inferior
2. Selecciona **"Publicación"**
3. Sube fotos de calidad de tu producto
4. Completa los detalles:
   • Título descriptivo
   • Precio
   • Categoría
   • Descripción
5. Toca **"Publicar"**

💡 **Tips:** Usa buena iluminación y describe el estado del producto.

¡Tu publicación estará visible de inmediato!""",
                keywords=["publicar", "vender", "producto", "subir"],
                variations=[
                    "como vendo algo",
                    "quiero publicar",
                    "como subo un producto",
                    "como pongo algo a la venta",
                    "quiero vender",
                ]
            ),
            
            # Rastrear pedido
            TrainingExample(
                query="donde esta mi pedido",
                intent="order_track",
                response="""Para ver el estado de tu pedido:

1. Ve a **Perfil → Historial de pedidos**
2. Selecciona el pedido que quieres revisar
3. Verás el estado actual y número de seguimiento

**Estados posibles:**
• **Confirmado** - El vendedor recibió tu orden
• **Preparando** - Se está empaquetando
• **Enviado** - Ya está en camino
• **En tránsito** - En proceso de entrega
• **Entregado** - ¡Ya llegó!

Si tienes el número de seguimiento, puedes rastrearlo en la web de la paquetería.""",
                keywords=["pedido", "rastrear", "seguimiento", "llegar", "estado"],
                variations=[
                    "rastrear mi pedido",
                    "cuando llega mi pedido",
                    "estado de mi compra",
                    "mi paquete no llega",
                ]
            ),
            
            # Pagos
            TrainingExample(
                query="como agrego tarjeta",
                intent="payment_add",
                response="""Para agregar un método de pago:

1. Ve a **Perfil → Métodos de pago**
2. Toca **"Agregar método"**
3. Elige el tipo:
   • 💳 Tarjeta de crédito/débito
   • 🏦 Cuenta bancaria
   • 📱 OXXO Pay
4. Completa los datos
5. Confirma con tu código de seguridad

🔒 Tus datos están protegidos con encriptación bancaria.""",
                keywords=["tarjeta", "pago", "agregar", "metodo"],
                variations=[
                    "agregar forma de pago",
                    "como pago",
                    "metodos de pago",
                    "agregar mi tarjeta",
                ]
            ),
            
            # Reembolso
            TrainingExample(
                query="quiero un reembolso",
                intent="payment_refund",
                response="""Para solicitar un reembolso:

1. Ve a **Perfil → Historial de pedidos**
2. Selecciona el pedido
3. Toca **"Reportar problema"**
4. Elige **"Solicitar reembolso"**
5. Describe el motivo
6. Adjunta fotos si aplica

**Tiempos de reembolso:**
• Tarjeta: 5-10 días hábiles
• OXXO: 3-5 días hábiles
• Billetera Rendly: Inmediato

⚠️ Tienes 7 días desde la entrega para solicitar devolución.""",
                keywords=["reembolso", "devolucion", "dinero", "regresar"],
                variations=[
                    "como pido reembolso",
                    "devolver producto",
                    "me devuelven mi dinero",
                    "cancelar y reembolsar",
                ]
            ),
            
            # Cuenta - Contraseña
            TrainingExample(
                query="olvide mi contraseña",
                intent="account_password",
                response="""Para recuperar tu contraseña:

1. En la pantalla de login, toca **"¿Olvidaste tu contraseña?"**
2. Ingresa tu email registrado
3. Revisa tu bandeja de entrada (y spam)
4. Sigue el enlace para crear nueva contraseña

**Para cambiar tu contraseña actual:**
**Perfil → Configuración → Seguridad → Cambiar contraseña**

🔐 Recomendamos usar una contraseña única con letras, números y símbolos.""",
                keywords=["contraseña", "password", "olvide", "recuperar", "acceso"],
                variations=[
                    "no puedo entrar",
                    "cambiar contraseña",
                    "recuperar cuenta",
                    "no recuerdo mi clave",
                ]
            ),
            
            # Perfil
            TrainingExample(
                query="como cambio mi foto de perfil",
                intent="account_profile",
                response="""Para cambiar tu foto de perfil:

1. Ve a tu **Perfil** (ícono de persona abajo)
2. Toca tu foto actual o el ícono de cámara
3. Elige una foto de tu galería o toma una nueva
4. Ajusta el encuadre
5. Confirma

📸 **Recomendaciones:**
• Usa una foto clara de tu rostro
• Buena iluminación
• Genera confianza con compradores y vendedores""",
                keywords=["foto", "perfil", "cambiar", "imagen", "avatar"],
                variations=[
                    "actualizar foto",
                    "poner otra foto",
                    "cambiar mi imagen",
                ]
            ),
            
            # Envíos
            TrainingExample(
                query="como agrego direccion",
                intent="shipping_address",
                response="""Para agregar una dirección de envío:

1. Ve a **Perfil → Direcciones**
2. Toca **"Agregar dirección"**
3. Completa:
   • Nombre del destinatario
   • Calle y número
   • Colonia
   • Ciudad y estado
   • Código postal
   • Teléfono de contacto
4. Guarda

📍 Puedes tener múltiples direcciones y elegir la predeterminada.""",
                keywords=["direccion", "envio", "agregar", "domicilio"],
                variations=[
                    "cambiar direccion",
                    "agregar domicilio",
                    "donde me envian",
                ]
            ),
            
            # Seguridad
            TrainingExample(
                query="como activo verificacion",
                intent="security_2fa",
                response="""Para activar la verificación en dos pasos (2FA):

1. Ve a **Perfil → Configuración → Seguridad**
2. Toca **"Verificación en dos pasos"**
3. Elige el método:
   • 📱 SMS
   • 📧 Email
   • 🔐 App autenticadora
4. Sigue los pasos de configuración

🛡️ **Beneficios:**
• Mayor seguridad en tu cuenta
• Protección contra accesos no autorizados
• Notificaciones de inicio de sesión""",
                keywords=["verificacion", "2fa", "seguridad", "autenticacion"],
                variations=[
                    "activar 2fa",
                    "proteger cuenta",
                    "verificacion dos pasos",
                ]
            ),
            
            # Comisiones
            TrainingExample(
                query="cuanto cobran de comision",
                intent="sell_commission",
                response="""**Comisiones de Rendly:**

• **Comisión por venta:** 10% del precio de venta
• **Sin costo de publicación:** Publicar es gratis
• **Sin mensualidades:** Solo pagas cuando vendes

**Ejemplo:**
Si vendes algo en $1,000, Rendly cobra $100 de comisión y tú recibes $900.

💡 La comisión cubre:
• Protección al comprador
• Sistema de pagos seguro
• Soporte 24/7""",
                keywords=["comision", "cobran", "porcentaje", "costo"],
                variations=[
                    "cuanto cuesta vender",
                    "que porcentaje cobran",
                    "es gratis publicar",
                ]
            ),
            
            # Problema con pedido
            TrainingExample(
                query="mi pedido no llego",
                intent="order_problem",
                response="""Lamento que tu pedido no haya llegado. Vamos a solucionarlo:

1. Verifica el estado en **Perfil → Historial de pedidos**
2. Revisa el número de seguimiento si está disponible
3. Contacta al vendedor desde el chat del pedido

**Si ya pasó la fecha estimada:**
1. Toca el pedido
2. Selecciona **"Reportar problema"**
3. Elige **"No recibí mi pedido"**

⏰ El envío estándar tarda 5-10 días hábiles.
Si pasaron más de 15 días, abriremos una disputa automática.""",
                keywords=["pedido", "llego", "problema", "no", "recibir"],
                variations=[
                    "no me llego",
                    "mi paquete perdido",
                    "no recibi nada",
                ]
            ),
            
            # Verificación
            TrainingExample(
                query="como verifico mi cuenta",
                intent="account_verify",
                response="""Para verificar tu cuenta en Rendly:

1. Ve a **Perfil → Verificación**
2. Sube tu identificación oficial (INE, pasaporte)
3. Toma una selfie para confirmar identidad
4. Espera la revisión (24-48 horas)

✅ La verificación te permite vender productos y genera confianza.""",
                keywords=["verificar", "verificacion", "identidad", "cuenta"],
                variations=[
                    "quiero verificarme",
                    "como me verifico",
                    "verificar identidad",
                ]
            ),
            
            # Billetera
            TrainingExample(
                query="como retiro mi dinero",
                intent="wallet_withdraw",
                response="""Para retirar dinero de tu Billetera Rendly:

1. Ve a **Perfil → Billetera**
2. Toca **"Retirar fondos"**
3. Ingresa el monto (mínimo $50 MXN)
4. Selecciona tu cuenta bancaria
5. El dinero llega en 1-3 días hábiles

💰 Tu saldo se libera cuando el comprador confirma la recepción.""",
                keywords=["retirar", "dinero", "billetera", "cobrar", "saldo"],
                variations=[
                    "como cobro mis ventas",
                    "donde veo mi saldo",
                    "cuando me pagan",
                    "sacar mi dinero",
                ]
            ),
            
            # Handshake
            TrainingExample(
                query="como funciona el handshake",
                intent="handshake_info",
                response="""El Handshake es el sistema de compra presencial de Rendly:

1. Acuerda con el comprador/vendedor por chat
2. Inicia el Handshake desde el chat (botón +)
3. Propón un punto de encuentro y precio
4. Ambos se dirigen al punto
5. Confirman la entrega en la app

🔒 Incluye mapa en tiempo real y sistema de disputas.""",
                keywords=["handshake", "presencial", "persona", "encuentro"],
                variations=[
                    "compra en persona",
                    "quedar con vendedor",
                    "entrega presencial",
                ]
            ),
            
            # Historias
            TrainingExample(
                query="como subo una historia",
                intent="stories_publish",
                response="""Para publicar una historia:

1. Toca el **"+"** en la sección de historias (arriba del feed)
2. Toma una foto/video o elige de galería
3. Agrega texto, stickers o efectos
4. Toca **"Publicar"**

⏰ Las historias duran 24 horas. Puedes guardarlas como Highlights.""",
                keywords=["historia", "story", "publicar", "subir"],
                variations=[
                    "publicar historia",
                    "como funcionan las historias",
                    "cuanto duran las historias",
                ]
            ),
            
            # Rends
            TrainingExample(
                query="como subo un rend",
                intent="rends_publish",
                response="""Para crear un Rend (video corto):

1. Toca el botón **"+"** en la barra inferior
2. Selecciona **"Rend"**
3. Graba hasta 60 segundos o sube un video
4. Edita: música, texto, filtros
5. Agrega descripción y hashtags
6. Toca **"Publicar"**

🏷️ Puedes etiquetar productos para vender directo desde el video.""",
                keywords=["rend", "video", "corto", "grabar", "reels"],
                variations=[
                    "que son los rends",
                    "publicar un rend",
                    "videos cortos",
                ]
            ),
            
            # Privacidad
            TrainingExample(
                query="como hago mi perfil privado",
                intent="privacy_settings",
                response="""Para hacer tu perfil privado:

1. Ve a **Perfil → Configuración → Privacidad**
2. En **"Visibilidad del perfil"** selecciona **"Privado"**

También puedes configurar:
• Estado en línea
• Última conexión
• Etiquetado y menciones
• Visibilidad de likes

🔒 Un perfil privado solo permite que tus seguidores vean tu contenido.""",
                keywords=["privacidad", "privado", "ocultar", "perfil"],
                variations=[
                    "configurar privacidad",
                    "ocultar actividad",
                    "quien ve mi perfil",
                ]
            ),
            
            # Ofertas
            TrainingExample(
                query="como hago una oferta",
                intent="offer_make",
                response="""Para hacer una oferta en Rendly:

1. Ve a la publicación del producto
2. Toca **"Hacer oferta"**
3. Ingresa el precio que propones
4. Opcionalmente agrega un mensaje
5. Envía la oferta

El vendedor puede aceptar, hacer contraoferta o rechazar.""",
                keywords=["oferta", "negociar", "precio", "proponer"],
                variations=[
                    "puedo negociar el precio",
                    "hacer contraoferta",
                    "el precio es fijo",
                ]
            ),
            
            # Devolución
            TrainingExample(
                query="como devuelvo un producto",
                intent="return_process",
                response="""Para devolver un producto:

1. Ve a **Perfil → Historial de pedidos**
2. Selecciona el pedido
3. Toca **"Solicitar devolución"**
4. Selecciona el motivo
5. Adjunta fotos del producto
6. Describe el problema

⏰ Tienes 7 días desde la entrega. El reembolso se procesa al confirmar.""",
                keywords=["devolver", "devolucion", "regresar", "producto"],
                variations=[
                    "quiero hacer una devolucion",
                    "puedo devolver algo",
                    "proceso de devolucion",
                ]
            ),
            
            # Despedida
            TrainingExample(
                query="gracias por la ayuda",
                intent="farewell",
                response="""¡Me alegra haber podido ayudarte! 😊

Si necesitas algo más, no dudes en escribirme. Estoy disponible 24/7.

¡Que tengas un excelente día! 👋""",
                keywords=["gracias", "adios", "bye", "luego"],
                variations=[
                    "muchas gracias",
                    "eso es todo",
                    "adios",
                    "hasta luego",
                    "listo gracias",
                ]
            ),
            
            # Agente humano
            TrainingExample(
                query="quiero hablar con una persona",
                intent="escalation_request",
                response="""Entiendo que prefieres hablar con una persona. Voy a transferirte con un agente de soporte humano.

Un momento por favor, alguien del equipo de Rendly se comunicará contigo en breve. 🙏""",
                keywords=["humano", "persona", "agente", "transferir"],
                variations=[
                    "hablar con un humano",
                    "necesito un agente",
                    "no me sirve el bot",
                    "quiero hablar con un agente",
                ]
            ),
        ]
        
        for example in initial_data:
            self.add_training_example(example)
        
        self.train()
        logger.info("initial_training_complete", examples=len(initial_data))
    
    def add_training_example(self, example: TrainingExample):
        """Agrega ejemplo de entrenamiento"""
        self.training_examples.append(example)
        
        # Agregar variaciones como ejemplos adicionales
        for variation in example.variations:
            var_example = TrainingExample(
                query=variation,
                intent=example.intent,
                response=example.response,
                keywords=example.keywords
            )
            self.training_examples.append(var_example)
        
        # Agregar template de respuesta
        self.generator.add_template(example.intent, example.response)
    
    def train(self):
        """Entrena el modelo con los ejemplos actuales"""
        if not self.training_examples:
            logger.warning("no_training_examples")
            return
        
        # Preparar datos
        documents = [ex.query for ex in self.training_examples]
        labels = [ex.intent for ex in self.training_examples]
        
        # Entrenar TF-IDF
        self.vectorizer.fit(documents)
        
        # Entrenar clasificador
        self.classifier.fit(documents, labels)
        
        # Pre-calcular vectores de ejemplos
        self.example_vectors = []
        for ex in self.training_examples:
            vec = self.vectorizer.transform(ex.query)
            self.example_vectors.append((ex.query, vec, ex.intent))
        
        self.is_trained = True
        self._save_model()
        
        logger.info(
            "model_trained",
            examples=len(self.training_examples),
            intents=len(set(labels))
        )
    
    def predict(self, query: str) -> PredictionResult:
        """Predice intent y genera respuesta"""
        if not self.is_trained:
            return PredictionResult(
                intent="unknown",
                confidence=0.0,
                response="El modelo aún no ha sido entrenado.",
                reasoning="model_not_trained"
            )
        
        # Extraer entidades
        entities = TextProcessor.extract_entities(query)
        context = {'entities': entities, 'original_query': query}
        
        # 1. Buscar ejemplo más similar con TF-IDF
        query_vec = self.vectorizer.transform(query)
        best_similarity = 0.0
        best_match = None
        
        for ex_query, ex_vec, ex_intent in self.example_vectors:
            similarity = self.vectorizer.cosine_similarity(query_vec, ex_vec)
            if similarity > best_similarity:
                best_similarity = similarity
                best_match = (ex_query, ex_intent)
        
        # 2. Clasificar con Naive Bayes
        nb_intent, nb_confidence = self.classifier.predict(query)
        
        # 3. Combinar resultados
        if best_similarity > 0.6:
            # Alta similitud con ejemplo existente
            intent = best_match[1]
            confidence = min(95, int(best_similarity * 100))
            reasoning = f"similar_to: {best_match[0]}"
        elif best_similarity > 0.4 and nb_confidence > 0.5:
            # Similitud media + clasificador concuerda
            if best_match[1] == nb_intent:
                intent = nb_intent
                confidence = int((best_similarity * 50) + (nb_confidence * 50))
            else:
                intent = nb_intent if nb_confidence > best_similarity else best_match[1]
                confidence = int(max(best_similarity, nb_confidence) * 80)
            reasoning = f"combined: sim={best_similarity:.2f}, nb={nb_confidence:.2f}"
        elif nb_confidence > 0.6:
            # Clasificador seguro
            intent = nb_intent
            confidence = int(nb_confidence * 85)
            reasoning = f"classifier: {nb_confidence:.2f}"
        else:
            # Baja confianza
            intent = nb_intent if nb_confidence > best_similarity else (best_match[1] if best_match else "unknown")
            confidence = int(max(best_similarity, nb_confidence) * 60)
            reasoning = "low_confidence"
        
        # 4. Generar respuesta
        response = self.generator.generate(intent, context)
        
        return PredictionResult(
            intent=intent,
            confidence=confidence,
            response=response,
            matched_example=best_match[0] if best_match else None,
            reasoning=reasoning
        )
    
    def learn_from_feedback(self, query: str, intent: str, response: str, was_helpful: bool):
        """Aprende de feedback del usuario - aprende de TODA interacción"""
        if intent and intent != "unknown":
            # Agregar como nuevo ejemplo (helpful o no - el modelo aprende de todo)
            new_example = TrainingExample(
                query=query,
                intent=intent,
                response=response,
                keywords=TextProcessor.tokenize(query)[:5]
            )
            self.add_training_example(new_example)
            
            # Re-entrenar cada 3 nuevos ejemplos (más agresivo para aprender rápido)
            if len(self.training_examples) % 3 == 0:
                self.train()
            
            logger.info(
                "learned_from_interaction",
                query=query[:50],
                intent=intent,
                was_helpful=was_helpful,
                total_examples=len(self.training_examples)
            )
    
    def _save_model(self):
        """Guarda el modelo entrenado"""
        try:
            model_data = {
                'examples': [(e.query, e.intent, e.response, e.keywords) for e in self.training_examples],
                'vectorizer_vocab': self.vectorizer.vocabulary,
                'vectorizer_idf': self.vectorizer.idf,
                'classifier_classes': dict(self.classifier.class_counts),
                'classifier_features': {k: dict(v) for k, v in self.classifier.feature_counts.items()},
                'classifier_vocab': list(self.classifier.vocabulary),
            }
            
            with open(MODEL_DIR / "model.pkl", "wb") as f:
                pickle.dump(model_data, f)
            
            logger.info("model_saved")
        except Exception as e:
            logger.error("model_save_error", error=str(e))
    
    def _load_model(self):
        """Carga modelo entrenado si existe"""
        model_path = MODEL_DIR / "model.pkl"
        if not model_path.exists():
            return
        
        try:
            with open(model_path, "rb") as f:
                model_data = pickle.load(f)
            
            # Restaurar ejemplos
            for query, intent, response, keywords in model_data['examples']:
                self.training_examples.append(TrainingExample(
                    query=query, intent=intent, response=response, keywords=keywords
                ))
                self.generator.add_template(intent, response)
            
            # Restaurar vectorizador
            self.vectorizer.vocabulary = model_data['vectorizer_vocab']
            self.vectorizer.idf = model_data['vectorizer_idf']
            
            # Restaurar clasificador
            self.classifier.class_counts = Counter(model_data['classifier_classes'])
            self.classifier.feature_counts = defaultdict(Counter, {
                k: Counter(v) for k, v in model_data['classifier_features'].items()
            })
            self.classifier.vocabulary = set(model_data['classifier_vocab'])
            self.classifier.total_docs = len(self.training_examples)
            
            # Pre-calcular vectores
            for ex in self.training_examples:
                vec = self.vectorizer.transform(ex.query)
                self.example_vectors.append((ex.query, vec, ex.intent))
            
            self.is_trained = True
            logger.info("model_loaded", examples=len(self.training_examples))
            
        except Exception as e:
            logger.error("model_load_error", error=str(e))
    
    def get_stats(self) -> Dict:
        """Obtiene estadísticas del modelo"""
        intents = set(e.intent for e in self.training_examples)
        return {
            "total_examples": len(self.training_examples),
            "unique_intents": len(intents),
            "intents": list(intents),
            "is_trained": self.is_trained,
            "vocabulary_size": len(self.vectorizer.vocabulary),
        }


# Singleton
_local_ai: Optional[LocalAIModel] = None


def get_local_ai() -> LocalAIModel:
    """Obtiene instancia singleton del modelo local"""
    global _local_ai
    if _local_ai is None:
        _local_ai = LocalAIModel()
    return _local_ai
