"""
Reasoning Engine - Intelligent response generation for Vinzay AI Support

This engine generates contextual responses when FAQ doesn't have an exact match.
It analyzes the user's question and constructs helpful responses based on:
- Detected intent and category
- Extracted keywords and context
- Vinzay's features and policies
"""

from typing import Dict, List, Optional, Tuple
from dataclasses import dataclass
import re


@dataclass
class ReasoningResult:
    """Result from reasoning engine"""
    response: str
    confidence: int  # 0-100
    should_escalate: bool
    escalation_reason: Optional[str] = None
    suggested_actions: List[str] = None
    
    def __post_init__(self):
        if self.suggested_actions is None:
            self.suggested_actions = []


class VinzayKnowledge:
    """Core knowledge about Vinzay platform"""
    
    # Platform policies and facts
    POLICIES = {
        "commission": "10% del precio de venta",
        "refund_time_card": "5-10 días hábiles",
        "refund_time_debit": "3-7 días hábiles",
        "refund_time_wallet": "inmediato",
        "return_window": "7 días desde la entrega",
        "shipping_standard": "5-10 días hábiles",
        "shipping_express": "2-4 días hábiles",
        "support_hours": "24/7 por chat, 9am-6pm por teléfono",
        "verification_required": "para vender productos",
        "min_withdraw": "$50 MXN",
    }
    
    # Navigation paths in the app
    NAVIGATION = {
        "orders": "Perfil → Historial de pedidos",
        "addresses": "Perfil → Direcciones",
        "payment_methods": "Perfil → Métodos de pago",
        "wallet": "Perfil → Billetera",
        "settings": "Perfil → Configuración",
        "security": "Perfil → Configuración → Seguridad",
        "verification": "Perfil → Verificación",
        "sales": "Perfil → Mis ventas",
        "publish": "Botón + → Publicación",
        "help": "Perfil → Centro de ayuda",
        "notifications": "Perfil → Notificaciones",
    }
    
    # Common issues and solutions
    TROUBLESHOOTING = {
        "payment_rejected": [
            "Verificar fondos disponibles",
            "Revisar datos de tarjeta (número, fecha, CVV)",
            "Contactar al banco para autorizar la compra",
            "Probar con otro método de pago",
        ],
        "order_delayed": [
            "Revisar el número de seguimiento en el pedido",
            "Contactar al vendedor por chat",
            "Si pasaron más de 15 días, reportar el problema",
        ],
        "cant_login": [
            "Verificar que el email sea correcto",
            "Usar 'Olvidé mi contraseña' para recuperar acceso",
            "Revisar bandeja de spam por el correo de recuperación",
        ],
        "app_error": [
            "Cerrar y volver a abrir la app",
            "Verificar conexión a internet",
            "Actualizar la app desde la tienda",
            "Limpiar caché de la app",
        ],
    }


class ReasoningEngine:
    """
    Intelligent reasoning engine that generates contextual responses.
    Uses pattern matching, template generation, and Vinzay knowledge.
    """
    
    def __init__(self):
        self.knowledge = VinzayKnowledge()
        self._build_response_templates()
    
    def _build_response_templates(self):
        """Build dynamic response templates for each category"""
        self.templates = {
            # PURCHASES
            "purchase_status": self._template_order_status,
            "purchase_cancel": self._template_order_cancel,
            "purchase_problem": self._template_order_problem,
            
            # PAYMENTS
            "payment_methods": self._template_payment_methods,
            "payment_problem": self._template_payment_problem,
            "refund": self._template_refund,
            
            # ACCOUNT
            "account_access": self._template_account_access,
            "account_settings": self._template_account_settings,
            "account_delete": self._template_account_delete,
            
            # SHIPPING
            "shipping_info": self._template_shipping_info,
            "shipping_problem": self._template_shipping_problem,
            
            # SALES
            "sell_how": self._template_sell_how,
            "sell_payment": self._template_sell_payment,
            
            # SECURITY
            "security_report": self._template_security_report,
            "security_verify": self._template_security_verify,
            
            # APP
            "app_bug": self._template_app_bug,
            
            # GENERAL
            "greeting": self._template_greeting,
            "unknown": self._template_unknown,
        }
    
    def reason(
        self, 
        message: str, 
        detected_intent: str,
        keywords: List[str],
        confidence_score: int
    ) -> ReasoningResult:
        """
        Main reasoning method - generates intelligent response.
        """
        # Extract context from message
        context = self._extract_context(message, keywords)
        
        # Get the appropriate template handler
        template_handler = self.templates.get(
            detected_intent, 
            self._template_unknown
        )
        
        # Generate response using template
        response, response_confidence = template_handler(message, context)
        
        # Combine confidence scores
        final_confidence = (confidence_score + response_confidence) // 2
        
        # Determine if we should escalate
        should_escalate = self._should_escalate(
            message, context, final_confidence, detected_intent
        )
        
        escalation_reason = None
        if should_escalate:
            escalation_reason = self._get_escalation_reason(context, final_confidence)
        
        return ReasoningResult(
            response=response,
            confidence=final_confidence,
            should_escalate=should_escalate,
            escalation_reason=escalation_reason,
            suggested_actions=context.get("actions", [])
        )
    
    def _extract_context(self, message: str, keywords: List[str]) -> Dict:
        """Extract contextual information from the message"""
        context = {
            "has_order_number": False,
            "order_number": None,
            "has_amount": False,
            "amount": None,
            "has_timeframe": False,
            "timeframe": None,
            "is_urgent": False,
            "is_specific": False,
            "actions": [],
        }
        
        msg_lower = message.lower()
        
        # Extract order number (various formats)
        order_patterns = [
            r'(?:pedido|orden|order)\s*#?\s*(\d{4,})',
            r'#(\d{4,})',
            r'numero\s*(\d{4,})',
        ]
        for pattern in order_patterns:
            match = re.search(pattern, msg_lower)
            if match:
                context["has_order_number"] = True
                context["order_number"] = match.group(1)
                context["is_specific"] = True
                break
        
        # Extract amount
        amount_match = re.search(r'\$?\s*(\d+(?:\.\d{2})?)\s*(?:pesos|mxn|dolares)?', msg_lower)
        if amount_match:
            context["has_amount"] = True
            context["amount"] = amount_match.group(1)
            context["is_specific"] = True
        
        # Extract timeframe
        time_patterns = {
            r'(\d+)\s*dias?': lambda m: f"{m.group(1)} días",
            r'(\d+)\s*semanas?': lambda m: f"{m.group(1)} semanas",
            r'(\d+)\s*horas?': lambda m: f"{m.group(1)} horas",
            r'hace\s+(\d+)': lambda m: f"hace {m.group(1)}",
            r'desde\s+(?:el\s+)?(\d{1,2}(?:/\d{1,2})?)': lambda m: f"desde {m.group(1)}",
        }
        for pattern, formatter in time_patterns.items():
            match = re.search(pattern, msg_lower)
            if match:
                context["has_timeframe"] = True
                context["timeframe"] = formatter(match)
                break
        
        # Detect urgency
        urgent_words = ["urgente", "rapido", "ya", "ahora", "inmediato", "emergencia"]
        context["is_urgent"] = any(w in msg_lower for w in urgent_words)
        
        # Check specificity
        if len(keywords) >= 4 or context["has_order_number"] or context["has_amount"]:
            context["is_specific"] = True
        
        return context
    
    def _should_escalate(
        self, 
        message: str, 
        context: Dict, 
        confidence: int,
        intent: str
    ) -> bool:
        """Determine if we should escalate to human"""
        
        # Very low confidence - escalate
        if confidence < 30:
            return True
        
        # Complex issues that need human
        complex_patterns = [
            r'reembolso.*(no|nunca).*(llego|recib)',
            r'cobr.*(doble|dos veces)',
            r'hackea|robar|acceso no autorizado',
            r'demanda|abogado|legal',
        ]
        msg_lower = message.lower()
        for pattern in complex_patterns:
            if re.search(pattern, msg_lower):
                return True
        
        # Unknown intent with specific details - human can help better
        if intent == "unknown" and context["is_specific"]:
            return True
        
        return False
    
    def _get_escalation_reason(self, context: Dict, confidence: int) -> str:
        """Get reason for escalation"""
        if confidence < 30:
            return "low_confidence"
        if context.get("is_urgent"):
            return "urgent_request"
        return "complex_issue"
    
    # ==================== TEMPLATE HANDLERS ====================
    
    def _template_order_status(self, message: str, context: Dict) -> Tuple[str, int]:
        """Generate response for order status inquiries"""
        nav = self.knowledge.NAVIGATION
        
        if context["has_order_number"]:
            response = f"""Para ver el estado de tu pedido #{context["order_number"]}:

1. Ve a **{nav["orders"]}**
2. Busca el pedido con ese número
3. Ahí verás el estado actual y número de seguimiento

**Estados posibles:**
• **Confirmado** - El vendedor recibió tu orden
• **Preparando** - Se está empaquetando
• **Enviado** - Ya está en camino con la paquetería
• **En tránsito** - En proceso de entrega
• **Entregado** - ¡Ya llegó!"""
            
            if context["has_timeframe"]:
                response += f"""

⏰ Mencionas que han pasado {context["timeframe"]}. El envío estándar tarda {self.knowledge.POLICIES["shipping_standard"]}. Si ya pasó ese tiempo y no ha llegado, puedes:
• Contactar al vendedor desde el chat del pedido
• Reportar el problema tocando "Reportar problema" en el pedido"""
            
            return response, 85
        
        else:
            response = f"""Para rastrear cualquier pedido:

1. Ve a **{nav["orders"]}**
2. Selecciona el pedido que quieres revisar
3. Verás el estado actual, número de seguimiento y fecha estimada

💡 **Tip:** Puedes contactar al vendedor directamente desde la pantalla del pedido si tienes alguna duda.

¿Tienes el número de pedido? Si me lo compartes puedo darte información más específica."""
            
            return response, 75

    def _template_order_cancel(self, message: str, context: Dict) -> Tuple[str, int]:
        """Generate response for order cancellation"""
        nav = self.knowledge.NAVIGATION
        
        response = f"""Para cancelar un pedido:

1. Ve a **{nav["orders"]}**
2. Selecciona el pedido
3. Toca **"Cancelar pedido"**

⚠️ **Importante:**
• Solo puedes cancelar **antes** de que el vendedor envíe
• Si ya fue enviado, deberás solicitar una **devolución** cuando llegue
• El reembolso tarda {self.knowledge.POLICIES["refund_time_card"]} en tarjeta de crédito

Si el botón de cancelar no aparece, significa que el pedido ya está en camino. En ese caso, espera a recibirlo y solicita la devolución."""
        
        if context["has_order_number"]:
            response = f"Para tu pedido #{context['order_number']}:\n\n" + response
        
        return response, 80

    def _template_order_problem(self, message: str, context: Dict) -> Tuple[str, int]:
        """Generate response for order problems"""
        nav = self.knowledge.NAVIGATION
        msg_lower = message.lower()
        
        # Detect specific problem type
        problem_type = "general"
        if any(w in msg_lower for w in ["dañado", "roto", "golpeado"]):
            problem_type = "damaged"
        elif any(w in msg_lower for w in ["diferente", "incorrecto", "equivocado"]):
            problem_type = "wrong_item"
        elif any(w in msg_lower for w in ["no llego", "no llegó", "perdido", "no recibí"]):
            problem_type = "not_received"
        
        base_response = f"""Lamento que hayas tenido este problema. Vamos a solucionarlo:

1. Ve a **{nav["orders"]}** y selecciona el pedido
2. Toca **"Reportar problema"**
3. Selecciona el tipo de problema
4. **Adjunta fotos como evidencia** (esto es importante)
5. Describe lo que pasó

"""
        
        if problem_type == "damaged":
            base_response += """📸 **Para productos dañados:**
Toma fotos del empaque y del producto mostrando el daño. Esto ayuda a procesar tu reclamo más rápido."""
        elif problem_type == "wrong_item":
            base_response += """📸 **Para producto incorrecto:**
Toma foto de lo que recibiste y compáralo con lo que muestra la publicación original."""
        elif problem_type == "not_received":
            base_response += f"""⏰ **Si no llegó tu pedido:**
• Primero verifica el estado del envío en tu pedido
• Contacta al vendedor para confirmar la dirección
• Si pasaron más de {self.knowledge.POLICIES["shipping_standard"]}, reporta el problema"""
        
        base_response += f"""

💡 Tienes **{self.knowledge.POLICIES["return_window"]}** para reportar. Vinzay protege todas tus compras."""
        
        return base_response, 82

    def _template_payment_methods(self, message: str, context: Dict) -> Tuple[str, int]:
        """Generate response for payment methods"""
        nav = self.knowledge.NAVIGATION
        msg_lower = message.lower()
        
        if "agregar" in msg_lower or "añadir" in msg_lower or "nueva" in msg_lower:
            response = f"""Para agregar un nuevo método de pago:

1. Ve a **{nav["payment_methods"]}**
2. Toca **"+ Agregar método de pago"**
3. Selecciona el tipo:
   • 💳 Tarjeta de crédito/débito
   • 🏦 Cuenta bancaria
   • 📱 PayPal
4. Ingresa los datos y guarda

🔒 Tus datos de pago están protegidos con encriptación de grado bancario."""
        else:
            response = f"""En Vinzay aceptamos varios métodos de pago:

💳 **Tarjetas:** Visa, Mastercard, American Express
🏦 **Transferencia:** SPEI y transferencia directa  
💰 **Billetera Vinzay:** Usa tu saldo de ventas
📱 **Otros:** PayPal, Mercado Pago

Para gestionar tus métodos de pago:
**{nav["payment_methods"]}**

¿Necesitas agregar una tarjeta o tienes algún problema con un pago?"""
        
        return response, 80

    def _template_payment_problem(self, message: str, context: Dict) -> Tuple[str, int]:
        """Generate response for payment problems"""
        nav = self.knowledge.NAVIGATION
        msg_lower = message.lower()
        
        # Check for specific payment issue
        if "rechaz" in msg_lower or "no pasa" in msg_lower or "declina" in msg_lower:
            troubleshooting = self.knowledge.TROUBLESHOOTING["payment_rejected"]
            response = f"""Si tu pago fue rechazado, puede ser por varias razones. Prueba lo siguiente:

"""
            for i, step in enumerate(troubleshooting, 1):
                response += f"{i}. {step}\n"
            
            response += f"""
💡 **Alternativa rápida:** Usa la **Billetera Vinzay** si tienes saldo disponible.

Si el problema persiste después de verificar todo, es posible que tu banco esté bloqueando la transacción por seguridad. Llama a tu banco para autorizar el pago."""
        
        elif "doble" in msg_lower or "dos veces" in msg_lower:
            response = """⚠️ Entiendo que te preocupa un posible cobro doble. Esto es lo que debes saber:

1. **Verifica en tu banco** - A veces aparecen "autorizaciones" temporales que desaparecen en 24-48 horas
2. **Revisa tu historial de pedidos** - Confirma si hay pedidos duplicados
3. Si efectivamente hay un cobro doble, reporta el problema desde el pedido

Los cargos duplicados por error se reembolsan automáticamente en 3-5 días hábiles una vez confirmados."""
        
        else:
            response = f"""Para resolver problemas de pago:

1. **Pago rechazado:** Verifica fondos, datos de tarjeta, y contacta a tu banco
2. **Error al pagar:** Cierra la app, verifica tu conexión e intenta de nuevo
3. **Cobro sin confirmación:** Revisa tu email por la confirmación del pedido

Gestiona tus métodos de pago en:
**{nav["payment_methods"]}**

¿Cuál es el problema específico que tienes con tu pago?"""
        
        return response, 78

    def _template_refund(self, message: str, context: Dict) -> Tuple[str, int]:
        """Generate response for refund inquiries"""
        policies = self.knowledge.POLICIES
        nav = self.knowledge.NAVIGATION
        
        response = f"""Información sobre reembolsos en Vinzay:

⏱️ **Tiempos de procesamiento:**
• Billetera Vinzay: {policies["refund_time_wallet"]}
• Tarjeta de crédito: {policies["refund_time_card"]}
• Tarjeta de débito: {policies["refund_time_debit"]}

📍 **Ver estado del reembolso:**
1. Ve a **{nav["orders"]}**
2. Selecciona el pedido
3. Verás "Reembolso en proceso" o "Reembolsado"

"""
        if context["has_timeframe"]:
            response += f"""⏰ Mencionas que han pasado {context["timeframe"]}. Si ya pasaron más de 10 días hábiles y no ves el reembolso:
1. Verifica que el status sea "Reembolsado" en la app
2. Contacta a tu banco con el número de referencia del reembolso
3. Si el banco no tiene registro, podemos abrir una investigación"""
        else:
            response += """💡 **Tip:** El tiempo empieza a contar desde que el vendedor confirma la devolución, no desde que enviaste el producto de vuelta."""
        
        return response, 80

    def _template_account_access(self, message: str, context: Dict) -> Tuple[str, int]:
        """Generate response for account access issues"""
        msg_lower = message.lower()
        
        if "olvide" in msg_lower or "recuperar" in msg_lower or "no recuerdo" in msg_lower:
            response = """Para recuperar tu contraseña:

1. En la pantalla de inicio de sesión, toca **"¿Olvidaste tu contraseña?"**
2. Ingresa tu email registrado
3. Revisa tu bandeja de entrada (y carpeta de spam)
4. Sigue el enlace del correo para crear una nueva contraseña

📧 El correo llega en menos de 5 minutos. Si no lo ves:
• Verifica que el email sea el correcto
• Revisa la carpeta de spam/no deseado
• Intenta de nuevo después de 10 minutos"""
        
        elif "cambiar" in msg_lower or "nueva contraseña" in msg_lower:
            nav = self.knowledge.NAVIGATION
            response = f"""Para cambiar tu contraseña actual:

1. Ve a **{nav["security"]}**
2. Toca **"Cambiar contraseña"**
3. Ingresa tu contraseña actual
4. Crea la nueva contraseña
5. Confirma y guarda

🔒 **Tip de seguridad:** Usa al menos 8 caracteres con letras, números y símbolos."""
        
        else:
            response = """¿Tienes problemas para acceder a tu cuenta?

**Si olvidaste tu contraseña:**
• Usa "¿Olvidaste tu contraseña?" en el login
• Te enviaremos un correo para recuperarla

**Si no puedes entrar por otro motivo:**
• Verifica que el email sea correcto
• Revisa si tienes verificación en dos pasos activada
• Cierra sesión en otros dispositivos si es necesario

¿Cuál es el problema específico que tienes para acceder?"""
        
        return response, 82

    def _template_account_settings(self, message: str, context: Dict) -> Tuple[str, int]:
        """Generate response for account settings"""
        nav = self.knowledge.NAVIGATION
        msg_lower = message.lower()
        
        if "foto" in msg_lower or "imagen" in msg_lower or "avatar" in msg_lower:
            response = f"""Para cambiar tu foto de perfil:

1. Ve a tu **Perfil** (ícono de persona abajo)
2. Toca tu foto actual o el ícono de cámara
3. Elige una foto de tu galería o toma una nueva
4. Ajusta el encuadre y confirma

💡 Recomendamos usar una foto clara de tu rostro para generar confianza con compradores y vendedores."""
        
        elif "nombre" in msg_lower or "usuario" in msg_lower:
            response = f"""Para cambiar tu nombre o username:

1. Ve a **{nav["settings"]}** → **Cuenta**
2. Toca **"Editar perfil"**
3. Modifica tu nombre o nombre de usuario
4. Guarda los cambios

⚠️ El nombre de usuario solo se puede cambiar cada 30 días."""
        
        else:
            response = f"""Puedes modificar tu perfil y configuración aquí:

**{nav["settings"]}**

Opciones disponibles:
• 👤 **Editar perfil** - Nombre, foto, bio
• 🔔 **Notificaciones** - Qué alertas recibes
• 🔒 **Seguridad** - Contraseña, 2FA
• 📍 **Direcciones** - Para envíos
• 💳 **Pagos** - Métodos de pago

¿Qué configuración específica quieres cambiar?"""
        
        return response, 78

    def _template_account_delete(self, message: str, context: Dict) -> Tuple[str, int]:
        """Generate response for account deletion"""
        nav = self.knowledge.NAVIGATION
        
        response = f"""Para eliminar tu cuenta de Vinzay:

1. Ve a **{nav["settings"]}** → **Cuenta**
2. Desplázate hasta **"Eliminar cuenta"**
3. Lee la información importante
4. Confirma con tu contraseña

⚠️ **Antes de eliminar, considera:**
• Todos tus datos se borrarán **permanentemente**
• Pedidos pendientes deben completarse primero
• Debes retirar cualquier saldo de tu billetera
• Tu nombre de usuario no podrá recuperarse

💡 **Alternativa:** Si solo quieres un descanso, puedes **desactivar temporalmente** tu cuenta en lugar de eliminarla. Así podrás volver cuando quieras."""
        
        return response, 75

    def _template_shipping_info(self, message: str, context: Dict) -> Tuple[str, int]:
        """Generate response for shipping information"""
        nav = self.knowledge.NAVIGATION
        policies = self.knowledge.POLICIES
        msg_lower = message.lower()
        
        if "direccion" in msg_lower or "domicilio" in msg_lower:
            response = f"""Para gestionar tus direcciones de envío:

**Agregar nueva dirección:**
1. Ve a **{nav["addresses"]}**
2. Toca **"+ Agregar dirección"**
3. Completa los datos y guarda

**Cambiar dirección de un pedido:**
• Solo es posible si aún **no fue enviado**
• Ve al pedido y toca "Cambiar dirección"
• O contacta al vendedor directamente

💡 Puedes marcar una dirección como **predeterminada** para futuras compras."""
        
        elif "tiempo" in msg_lower or "tarda" in msg_lower or "dias" in msg_lower:
            response = f"""Tiempos de envío en Vinzay:

📦 **Envío estándar:** {policies["shipping_standard"]}
🚀 **Envío express:** {policies["shipping_express"]}
🏃 **Mismo día:** Disponible en ciudades principales

El tiempo depende de:
• Ubicación del vendedor y tu ciudad
• Método de envío elegido
• Tiempo de preparación del vendedor (1-3 días)

💡 En cada producto verás el tiempo estimado para tu ubicación específica."""
        
        else:
            response = f"""Información de envíos en Vinzay:

📦 **Tiempos:** {policies["shipping_standard"]} (estándar) / {policies["shipping_express"]} (express)
📍 **Direcciones:** Gestiona en **{nav["addresses"]}**
🔍 **Seguimiento:** Ve el tracking en **{nav["orders"]}**

¿Necesitas ayuda con algo específico sobre envíos?"""
        
        return response, 80

    def _template_shipping_problem(self, message: str, context: Dict) -> Tuple[str, int]:
        """Generate response for shipping problems"""
        nav = self.knowledge.NAVIGATION
        policies = self.knowledge.POLICIES
        msg_lower = message.lower()
        
        if "no llego" in msg_lower or "perdido" in msg_lower or "no llega" in msg_lower:
            response = f"""Si tu paquete no ha llegado:

1. **Verifica el estado** en **{nav["orders"]}** → selecciona el pedido
2. **Revisa el tracking** - A veces hay actualizaciones de la paquetería
3. **Contacta al vendedor** - Puede tener información adicional

⏰ El envío estándar tarda {policies["shipping_standard"]}."""
            
            if context["has_timeframe"]:
                response += f"""

Mencionas que han pasado {context["timeframe"]}. Si ya pasó el tiempo estimado:
• Toca **"Reportar problema"** en el pedido
• Selecciona "No recibí mi pedido"
• Iniciaremos una investigación con la paquetería"""
            else:
                response += """

Si ya pasó el tiempo estimado de entrega, puedes reportar el problema directamente desde el pedido."""
        
        elif "demora" in msg_lower or "retras" in msg_lower or "tarda" in msg_lower:
            response = f"""Entiendo la frustración por la demora. Esto puede deberse a:

• **Alta demanda** - Fechas especiales o promociones
• **Ubicación** - Ciudades remotas tardan más
• **Paquetería** - Retrasos externos a Vinzay

**Qué puedes hacer:**
1. Revisa el tracking del pedido para ver su ubicación actual
2. Contacta al vendedor por cualquier información adicional
3. Si ya pasaron {policies["shipping_standard"]}, reporta el problema

💡 El vendedor puede darte un mejor estimado basado en su experiencia."""
        
        else:
            response = f"""Para problemas con tu envío:

1. Ve a **{nav["orders"]}** y selecciona el pedido
2. Revisa el estado y número de seguimiento
3. Si hay problema, toca **"Reportar problema"**

Problemas comunes:
• 📦 No llegó - Reportar después de {policies["shipping_standard"]}
• 🔍 Sin tracking - Contactar al vendedor
• 📍 Dirección incorrecta - Cambiar antes del envío

¿Cuál es el problema específico con tu envío?"""
        
        return response, 78

    def _template_sell_how(self, message: str, context: Dict) -> Tuple[str, int]:
        """Generate response for selling inquiries"""
        nav = self.knowledge.NAVIGATION
        policies = self.knowledge.POLICIES
        
        response = f"""Para vender en Vinzay, sigue estos pasos:

**1. Verifica tu cuenta** (si no lo has hecho)
   → **{nav["verification"]}**

**2. Publica tu producto**
   → Toca el botón **"+"** en la barra inferior
   → Selecciona **"Publicación"**

**3. Completa la información:**
   • 📸 Sube fotos de calidad (mínimo 3)
   • ✏️ Título descriptivo
   • 💰 Precio competitivo
   • 📝 Descripción detallada
   • 📦 Opciones de envío

**4. ¡Publica!** Tu producto estará visible inmediatamente.

💡 **Tips para vender más rápido:**
• Usa buena iluminación en las fotos
• Describe medidas, condición y detalles
• Responde rápido a los interesados
• Precio justo = ventas rápidas

📊 La comisión por venta es del {policies["commission"]}."""
        
        return response, 88

    def _template_sell_payment(self, message: str, context: Dict) -> Tuple[str, int]:
        """Generate response for seller payment inquiries"""
        nav = self.knowledge.NAVIGATION
        policies = self.knowledge.POLICIES
        msg_lower = message.lower()
        
        if "cobrar" in msg_lower or "retir" in msg_lower or "dinero" in msg_lower:
            response = f"""Para recibir el dinero de tus ventas:

1. El pago llega a tu **Billetera Vinzay** cuando el comprador confirma recepción
2. Ve a **{nav["wallet"]}**
3. Toca **"Retirar fondos"**
4. Selecciona tu método de retiro (cuenta bancaria)
5. El dinero llega en 1-3 días hábiles

💰 **Mínimo para retirar:** {policies["min_withdraw"]}
📊 **Comisión de Vinzay:** {policies["commission"]} (se descuenta automáticamente)"""
        
        elif "comision" in msg_lower or "porcentaje" in msg_lower:
            response = f"""Comisiones de venta en Vinzay:

💰 **Comisión por venta:** {policies["commission"]}

Esto incluye:
• Procesamiento de pago seguro
• Protección al comprador y vendedor
• Soporte al cliente
• Infraestructura de la plataforma

**Ejemplo:**
• Vendes a $100
• Comisión: $10
• Recibes: $90

✅ **Sin costo por:** publicar, tener cuenta, recibir mensajes."""
        
        else:
            response = f"""Información para vendedores:

💰 **Comisión:** {policies["commission"]} por venta
🏦 **Retiros:** A tu cuenta bancaria desde **{nav["wallet"]}**
📊 **Mínimo retiro:** {policies["min_withdraw"]}
⏱️ **Tiempo de pago:** 1-3 días hábiles

Ve tus ventas en: **{nav["sales"]}**

¿Tienes alguna pregunta específica sobre cobros o comisiones?"""
        
        return response, 82

    def _template_security_report(self, message: str, context: Dict) -> Tuple[str, int]:
        """Generate response for security reports"""
        msg_lower = message.lower()
        
        if "estafa" in msg_lower or "fraude" in msg_lower:
            response = """⚠️ Lamento que hayas tenido esta experiencia. Para reportar un fraude o estafa:

**Pasos inmediatos:**
1. **NO borres la conversación** - Es evidencia importante
2. **Toma capturas de pantalla** de todo
3. **Reporta al usuario:**
   - Ve a su perfil → ⋮ (tres puntos) → "Reportar"
   - Selecciona "Fraude/Estafa"
   - Adjunta las capturas

**Si ya pagaste:**
• Reporta el problema desde el pedido
• Contacta a tu banco para disputar el cargo
• Guarda toda la evidencia

🔍 Investigamos cada reporte en menos de 24 horas y tomamos acción inmediata contra usuarios fraudulentos."""
        
        else:
            response = """Para reportar un usuario o contenido sospechoso:

**Reportar usuario:**
1. Ve al perfil del usuario
2. Toca los **tres puntos (⋮)** arriba
3. Selecciona **"Reportar"**
4. Elige el motivo
5. Agrega detalles y evidencia

**Reportar producto:**
1. En la página del producto
2. Toca **"Reportar"**
3. Selecciona el motivo

🔍 Investigamos cada reporte en menos de 24 horas.

¿Qué tipo de problema quieres reportar?"""
        
        return response, 80

    def _template_security_verify(self, message: str, context: Dict) -> Tuple[str, int]:
        """Generate response for verification and 2FA"""
        nav = self.knowledge.NAVIGATION
        msg_lower = message.lower()
        
        if "2fa" in msg_lower or "dos pasos" in msg_lower or "autenticacion" in msg_lower:
            response = f"""Para activar la verificación en dos pasos (2FA):

1. Ve a **{nav["security"]}**
2. Toca **"Verificación en dos pasos"**
3. Elige tu método:
   • 📱 SMS - Código por mensaje
   • 📧 Email - Código por correo
   • 🔐 App autenticadora - Google Authenticator, etc.
4. Sigue las instrucciones para configurar

🔒 **Beneficios:**
• Mayor seguridad para tu cuenta
• Protección contra accesos no autorizados
• Alertas de inicio de sesión sospechoso

⚠️ Guarda tus **códigos de respaldo** en un lugar seguro."""
        
        elif "verific" in msg_lower and ("cuenta" in msg_lower or "vendedor" in msg_lower):
            response = f"""Para verificar tu cuenta de vendedor:

1. Ve a **{nav["verification"]}**
2. Completa los pasos:
   • 📸 Foto de tu identificación oficial
   • 🤳 Selfie para verificar que eres tú
   • 📱 Verificar número de teléfono
3. Espera la verificación (24-48 horas)

✅ **Beneficios de verificarte:**
• Insignia de cuenta verificada
• Mayor confianza de compradores
• Límites de venta más altos
• Acceso a funciones premium"""
        
        else:
            response = f"""Opciones de seguridad en Vinzay:

🔐 **Verificación en dos pasos (2FA):**
Actívala en **{nav["security"]}**

✅ **Verificación de cuenta:**
Verifica tu identidad en **{nav["verification"]}**

🔒 **Cambiar contraseña:**
**{nav["security"]}** → Cambiar contraseña

¿Qué opción de seguridad te interesa configurar?"""
        
        return response, 82

    def _template_app_bug(self, message: str, context: Dict) -> Tuple[str, int]:
        """Generate response for app issues"""
        msg_lower = message.lower()
        troubleshooting = self.knowledge.TROUBLESHOOTING["app_error"]
        
        response = """Si tienes problemas con la app, prueba estos pasos:

"""
        for i, step in enumerate(troubleshooting, 1):
            response += f"{i}. {step}\n"
        
        response += """
📱 **Para limpiar caché:**
• Android: Ajustes → Apps → Vinzay → Almacenamiento → Borrar caché
• iOS: Elimina y reinstala la app

"""
        
        if "lento" in msg_lower or "lenta" in msg_lower:
            response += """⚡ **Para mejorar el rendimiento:**
• Cierra otras apps en segundo plano
• Verifica tu conexión a internet
• Libera espacio en tu dispositivo"""
        elif "crash" in msg_lower or "cierra" in msg_lower:
            response += """💥 **Si la app se cierra sola:**
• Actualiza a la última versión
• Reinicia tu dispositivo
• Si persiste, reinstala la app"""
        else:
            response += """Si el problema persiste después de estos pasos, cuéntame:
• ¿Qué estabas haciendo cuando falló?
• ¿Qué mensaje de error apareció?
• ¿Qué modelo de teléfono tienes?"""
        
        return response, 75

    def _template_greeting(self, message: str, context: Dict) -> Tuple[str, int]:
        """Generate response for greetings"""
        response = """¡Hola! 👋 Soy el asistente virtual de Vinzay.

Estoy aquí para ayudarte con:

• 🛒 **Compras** - Pedidos, rastreo, cancelaciones
• 💰 **Pagos** - Métodos, reembolsos, problemas
• 👤 **Cuenta** - Contraseña, configuración, verificación
• 📦 **Envíos** - Direcciones, tiempos, seguimiento
• 🏪 **Ventas** - Publicar productos, comisiones, cobros
• 🔒 **Seguridad** - 2FA, reportes, protección

¿En qué puedo ayudarte hoy?"""
        
        return response, 95

    def _template_unknown(self, message: str, context: Dict) -> Tuple[str, int]:
        """Generate response for unknown intents - tries to be helpful"""
        msg_lower = message.lower()
        
        # Try to extract what they might want
        possible_topics = []
        
        topic_keywords = {
            "pedidos o compras": ["compra", "pedido", "orden", "compre"],
            "pagos o reembolsos": ["pago", "cobro", "reembolso", "dinero", "tarjeta"],
            "tu cuenta": ["cuenta", "perfil", "contraseña", "acceso"],
            "envíos": ["envio", "direccion", "llegar", "paquete"],
            "vender productos": ["vender", "venta", "publicar", "producto"],
        }
        
        for topic, keywords in topic_keywords.items():
            if any(kw in msg_lower for kw in keywords):
                possible_topics.append(topic)
        
        if possible_topics:
            topics_str = ", ".join(possible_topics[:2])
            response = f"""Entiendo que tu consulta es sobre {topics_str}.

Para poder ayudarte mejor, ¿podrías darme más detalles? Por ejemplo:

• ¿Tienes un número de pedido o referencia?
• ¿Cuál es el problema específico que estás experimentando?
• ¿Qué intentaste hacer y qué sucedió?

Cuanta más información me des, mejor podré asistirte. 🙂"""
            return response, 55
        
        # Truly unknown - ask for clarification
        response = """No estoy seguro de entender completamente tu consulta, pero quiero ayudarte.

Puedo asistirte con temas como:
• 🛒 Compras y pedidos
• 💰 Pagos y reembolsos
• 👤 Tu cuenta y configuración
• 📦 Envíos y direcciones
• 🏪 Vender productos
• 🔒 Seguridad

¿Podrías reformular tu pregunta o decirme con cuál de estos temas necesitas ayuda?"""
        
        return response, 40


# Singleton
_reasoning_engine: Optional[ReasoningEngine] = None


def get_reasoning_engine() -> ReasoningEngine:
    """Get singleton reasoning engine instance"""
    global _reasoning_engine
    if _reasoning_engine is None:
        _reasoning_engine = ReasoningEngine()
    return _reasoning_engine
