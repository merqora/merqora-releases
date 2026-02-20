"""Knowledge base for Rendly AI Support - FAQ and responses"""

from typing import Dict, List, Optional, Tuple
from rapidfuzz import fuzz, process
from dataclasses import dataclass
import json


@dataclass
class ActionButton:
    """Button for navigation/actions in responses"""
    id: str
    label: str
    action: str  # "navigate", "open_url", "call_function"
    target: str  # Screen route or URL
    icon: str = ""  # Icon name


@dataclass
class FAQEntry:
    id: str
    intent: str
    category: str
    questions: List[str]  # Variations of the question
    answer: str
    keywords: List[str]
    confidence_boost: float = 0.0  # Extra confidence for exact matches
    action_buttons: List[ActionButton] = None  # Navigation buttons
    
    def __post_init__(self):
        if self.action_buttons is None:
            self.action_buttons = []


# Rendly FAQ Knowledge Base
FAQ_DATABASE: List[FAQEntry] = [
    # === COMPRAS ===
    FAQEntry(
        id="purchase_track",
        intent="purchase_status",
        category="compras",
        questions=[
            "¿Dónde está mi pedido?",
            "¿Cuándo llega mi compra?",
            "Quiero rastrear mi pedido",
            "Estado de mi orden",
            "¿Cuánto tarda en llegar?",
        ],
        answer="""Para rastrear tu pedido:

1. Ve a tu **Perfil** → **Historial de pedidos**
2. Selecciona el pedido que quieres rastrear
3. Verás el estado actual y el número de seguimiento

Los estados posibles son:
• **Confirmado**: El vendedor recibió tu orden
• **Preparando**: Se está empaquetando
• **Enviado**: Ya está en camino
• **En tránsito**: Con la paquetería
• **Entregado**: ¡Ya llegó!""",
        keywords=["pedido", "rastrear", "tracking", "seguimiento", "llega", "estado", "orden"],
        confidence_boost=0.1,
        action_buttons=[
            ActionButton("btn_orders", "📦 Ver mis pedidos", "navigate", "profile/orders", "package"),
            ActionButton("btn_contact_seller", "💬 Contactar vendedor", "navigate", "chat/seller", "message-circle"),
        ],
    ),
    FAQEntry(
        id="purchase_cancel",
        intent="purchase_cancel",
        category="compras",
        questions=[
            "¿Cómo cancelo mi pedido?",
            "Quiero cancelar mi compra",
            "¿Puedo cancelar un pedido?",
            "Cancelar orden",
        ],
        answer="""Para cancelar un pedido:

1. Ve a **Perfil** → **Historial de pedidos**
2. Selecciona el pedido
3. Toca **"Cancelar pedido"** (solo disponible si no ha sido enviado)

**Importante:**
• Solo puedes cancelar antes de que el vendedor envíe
• Si ya fue enviado, deberás solicitar una devolución
• El reembolso tarda 3-5 días hábiles en reflejarse""",
        keywords=["cancelar", "cancelo", "anular", "deshacer", "pedido"],
        confidence_boost=0.1,
        action_buttons=[
            ActionButton("btn_orders", "📦 Ver mis pedidos", "navigate", "profile/orders", "package"),
            ActionButton("btn_refund_info", "💰 Info reembolsos", "navigate", "help/refunds", "dollar-sign"),
        ],
    ),
    FAQEntry(
        id="purchase_problem",
        intent="purchase_problem",
        category="compras",
        questions=[
            "Mi pedido llegó dañado",
            "No recibí lo que pedí",
            "El producto está roto",
            "Me enviaron algo diferente",
            "Problema con mi pedido",
        ],
        answer="""Lamento que hayas tenido este problema. Para reportarlo:

1. Ve a **Perfil** → **Historial de pedidos** → selecciona el pedido
2. Toca **"Reportar problema"**
3. Selecciona el tipo de problema:
   • Producto dañado
   • Producto diferente
   • No llegó
   • Otro
4. Adjunta fotos como evidencia
5. Describe el problema

**Tienes 7 días** desde la entrega para reportar. Rendly protege tus compras.""",
        keywords=["dañado", "roto", "diferente", "problema", "incorrecto", "mal"],
        action_buttons=[
            ActionButton("btn_orders", "📦 Ver mis pedidos", "navigate", "profile/orders", "package"),
            ActionButton("btn_report", "⚠️ Reportar problema", "navigate", "order/report", "alert-triangle"),
        ],
    ),
    
    # === PAGOS ===
    FAQEntry(
        id="payment_methods",
        intent="payment_methods",
        category="pagos",
        questions=[
            "¿Qué métodos de pago aceptan?",
            "¿Puedo pagar con tarjeta?",
            "Formas de pago",
            "¿Aceptan PayPal?",
        ],
        answer="""En Rendly aceptamos:

💳 **Tarjetas de crédito/débito**
• Visa, Mastercard, American Express

🏦 **Transferencia bancaria**
• SPEI (México)
• Transferencia directa

💰 **Billetera Rendly**
• Saldo disponible de ventas
• Recargas

📱 **Otros**
• PayPal
• Mercado Pago (en algunos países)

Para agregar un método: **Perfil** → **Métodos de pago** → **Agregar**""",
        keywords=["pago", "tarjeta", "metodo", "pagar", "visa", "mastercard", "paypal"],
        confidence_boost=0.1,
        action_buttons=[
            ActionButton("btn_payment", "💳 Métodos de pago", "navigate", "profile/payment-methods", "credit-card"),
            ActionButton("btn_wallet", "👛 Mi billetera", "navigate", "profile/wallet", "wallet"),
        ],
    ),
    FAQEntry(
        id="payment_refund",
        intent="refund",
        category="pagos",
        questions=[
            "¿Cuándo llega mi reembolso?",
            "No me han devuelto el dinero",
            "¿Cómo funciona el reembolso?",
            "Quiero mi dinero de vuelta",
        ],
        answer="""Los reembolsos en Rendly:

⏱️ **Tiempos de procesamiento:**
• Billetera Rendly: Inmediato
• Tarjeta de crédito: 5-10 días hábiles
• Tarjeta de débito: 3-7 días hábiles
• PayPal: 3-5 días hábiles

📍 **Ver estado del reembolso:**
1. **Perfil** → **Historial de pedidos**
2. Selecciona el pedido reembolsado
3. Verás "Reembolso en proceso" o "Reembolsado"

Si han pasado más de 10 días hábiles, contacta a tu banco.""",
        keywords=["reembolso", "devolucion", "dinero", "devolver", "regreso"],
        action_buttons=[
            ActionButton("btn_orders", "📦 Ver mis pedidos", "navigate", "profile/orders", "package"),
            ActionButton("btn_human", "👤 Hablar con agente", "call_function", "escalate_to_human", "user"),
        ],
    ),
    FAQEntry(
        id="payment_failed",
        intent="payment_problem",
        category="pagos",
        questions=[
            "Mi pago fue rechazado",
            "No pude pagar",
            "Error al pagar",
            "La tarjeta no pasa",
        ],
        answer="""Si tu pago fue rechazado, puede ser por:

1. **Fondos insuficientes** - Verifica tu saldo
2. **Datos incorrectos** - Revisa número, fecha y CVV
3. **Límite excedido** - Contacta a tu banco
4. **Tarjeta bloqueada** - Tu banco puede haberla bloqueado por seguridad
5. **Problemas de red** - Intenta de nuevo en unos minutos

💡 **Soluciones:**
• Prueba con otro método de pago
• Verifica que la dirección de facturación coincida
• Contacta a tu banco para autorizar la compra
• Usa la Billetera Rendly como alternativa

Si el problema persiste, intenta con otro método de pago.""",
        keywords=["rechazado", "error", "fallo", "pago", "tarjeta", "problema"],
        action_buttons=[
            ActionButton("btn_payment", "💳 Métodos de pago", "navigate", "profile/payment-methods", "credit-card"),
            ActionButton("btn_retry", "🔄 Reintentar compra", "navigate", "cart", "shopping-cart"),
        ],
    ),
    
    # === CUENTA ===
    FAQEntry(
        id="account_password",
        intent="account_access",
        category="cuenta",
        questions=[
            "Olvidé mi contraseña",
            "No puedo entrar a mi cuenta",
            "Recuperar contraseña",
            "Cambiar password",
        ],
        answer="""Para recuperar tu contraseña:

1. En la pantalla de inicio de sesión, toca **"¿Olvidaste tu contraseña?"**
2. Ingresa tu email registrado
3. Revisa tu bandeja de entrada (y spam)
4. Sigue el enlace para crear una nueva contraseña

**Para cambiar tu contraseña actual:**
1. **Perfil** → **Configuración** → **Seguridad**
2. Toca **"Cambiar contraseña"**
3. Ingresa tu contraseña actual y la nueva

💡 Usa una contraseña de al menos 8 caracteres con letras, números y símbolos.""",
        keywords=["contraseña", "password", "olvide", "recuperar", "acceso", "entrar"],
        confidence_boost=0.15,
        action_buttons=[
            ActionButton("btn_reset", "🔑 Recuperar contraseña", "navigate", "auth/reset-password", "key"),
            ActionButton("btn_security", "🔒 Configurar seguridad", "navigate", "profile/security", "shield"),
        ],
    ),
    FAQEntry(
        id="account_delete",
        intent="account_delete",
        category="cuenta",
        questions=[
            "Quiero eliminar mi cuenta",
            "¿Cómo borro mi cuenta?",
            "Cerrar cuenta Rendly",
            "Desactivar cuenta",
        ],
        answer="""Para eliminar tu cuenta de Rendly:

1. **Perfil** → **Configuración** → **Cuenta**
2. Desplázate hasta **"Eliminar cuenta"**
3. Lee la información importante
4. Confirma con tu contraseña

⚠️ **Antes de eliminar, considera:**
• Tus datos se borrarán permanentemente
• Pedidos pendientes deben completarse primero
• Saldo en billetera debe retirarse
• No podrás recuperar tu nombre de usuario

💡 Si solo quieres un descanso, considera **desactivar temporalmente** en lugar de eliminar.""",
        keywords=["eliminar", "borrar", "cerrar", "desactivar", "cuenta"],
        confidence_boost=0.1,
        action_buttons=[
            ActionButton("btn_account", "👤 Configuración de cuenta", "navigate", "profile/settings/account", "user-cog"),
            ActionButton("btn_human", "👤 Hablar con agente", "call_function", "escalate_to_human", "user"),
        ],
    ),
    
    # === ENVÍOS ===
    FAQEntry(
        id="shipping_address",
        intent="shipping_info",
        category="envios",
        questions=[
            "¿Cómo cambio mi dirección de envío?",
            "Agregar dirección",
            "Modificar dirección",
            "Dirección incorrecta",
        ],
        answer="""Para gestionar tus direcciones:

**Agregar nueva dirección:**
1. **Perfil** → **Direcciones**
2. Toca **"+ Agregar dirección"**
3. Completa los datos y guarda

**Cambiar dirección de un pedido:**
• Solo es posible si el pedido aún no fue enviado
• Ve al pedido y toca **"Cambiar dirección"**
• O contacta al vendedor directamente

**Editar dirección existente:**
1. **Perfil** → **Direcciones**
2. Selecciona la dirección
3. Toca **"Editar"**

💡 Puedes marcar una dirección como **predeterminada** para futuras compras.""",
        keywords=["direccion", "envio", "domicilio", "agregar", "cambiar"],
        action_buttons=[
            ActionButton("btn_addresses", "📍 Mis direcciones", "navigate", "profile/addresses", "map-pin"),
            ActionButton("btn_orders", "📦 Ver mis pedidos", "navigate", "profile/orders", "package"),
        ],
    ),
    FAQEntry(
        id="shipping_time",
        intent="shipping_info",
        category="envios",
        questions=[
            "¿Cuánto tarda el envío?",
            "Tiempo de entrega",
            "¿Cuántos días tarda en llegar?",
        ],
        answer="""Los tiempos de envío en Rendly varían:

📦 **Envío estándar:** 5-10 días hábiles
🚀 **Envío express:** 2-4 días hábiles
🏃 **Envío mismo día:** Disponible en algunas ciudades

El tiempo depende de:
• Ubicación del vendedor
• Tu ciudad de destino
• Método de envío elegido
• Disponibilidad del producto

💡 En la página del producto verás el tiempo estimado para tu ubicación.

Cada vendedor indica sus tiempos de preparación (1-3 días generalmente) antes del envío.""",
        keywords=["tiempo", "tarda", "llegar", "dias", "entrega", "envio"],
        action_buttons=[
            ActionButton("btn_orders", "📦 Ver mis pedidos", "navigate", "profile/orders", "package"),
        ],
    ),
    
    # === VENTAS ===
    FAQEntry(
        id="sell_how",
        intent="sell_how",
        category="ventas",
        questions=[
            "¿Cómo vendo en Rendly?",
            "Quiero vender productos",
            "¿Cómo publico algo?",
            "Empezar a vender",
            "¿Cómo publico un producto?",
            "Como publico un producto",
            "Cómo publicar producto",
            "Como vender",
            "Quiero publicar",
            "Como subo un producto",
            "Publicar algo",
            "Vender algo",
            "Como pongo a la venta",
            "Subir producto",
        ],
        answer="""Para vender en Rendly:

1. **Verifica tu cuenta** (Perfil → Verificación)
2. Toca el botón **"+"** en la barra inferior
3. Selecciona **"Publicación"**
4. Sube fotos de calidad de tu producto
5. Completa:
   • Título descriptivo
   • Precio
   • Categoría
   • Descripción detallada
   • Opciones de envío
6. Toca **"Publicar"**

💡 **Tips para vender más:**
• Usa buena iluminación en las fotos
• Describe medidas y condición
• Responde rápido a los interesados
• Precio competitivo

¡Tu producto estará visible inmediatamente!""",
        keywords=["vender", "publicar", "venta", "producto", "anuncio"],
        confidence_boost=0.1,
        action_buttons=[
            ActionButton("btn_publish", "➕ Publicar producto", "navigate", "publish", "plus-circle"),
            ActionButton("btn_verify", "✅ Verificar cuenta", "navigate", "profile/verification", "check-circle"),
            ActionButton("btn_my_sales", "🏪 Mis ventas", "navigate", "profile/sales", "store"),
        ],
    ),
    FAQEntry(
        id="sell_commission",
        intent="sell_payment",
        category="ventas",
        questions=[
            "¿Cuánto cobra Rendly de comisión?",
            "¿Cuál es la comisión por venta?",
            "¿Qué porcentaje se llevan?",
        ],
        answer="""Comisiones en Rendly:

💰 **Comisión por venta:** 10% del precio final

Esto incluye:
• Procesamiento de pago
• Protección al comprador
• Soporte al vendedor
• Infraestructura de la plataforma

**Ejemplo:**
• Vendes a $100
• Comisión: $10
• Recibes: $90

📌 **No hay costos por:**
• Publicar productos
• Tener cuenta de vendedor
• Recibir mensajes

El cobro se hace automáticamente al completarse la venta.""",
        keywords=["comision", "porcentaje", "cobra", "costo", "tarifa"],
        action_buttons=[
            ActionButton("btn_wallet", "👛 Mi billetera", "navigate", "profile/wallet", "wallet"),
            ActionButton("btn_my_sales", "🏪 Mis ventas", "navigate", "profile/sales", "store"),
        ],
    ),
    
    # === SEGURIDAD ===
    FAQEntry(
        id="security_2fa",
        intent="security_verify",
        category="seguridad",
        questions=[
            "¿Cómo activo la verificación en dos pasos?",
            "Activar 2FA",
            "Doble autenticación",
            "Proteger mi cuenta",
        ],
        answer="""Para activar la verificación en dos pasos (2FA):

1. **Perfil** → **Configuración** → **Seguridad**
2. Toca **"Verificación en dos pasos"**
3. Elige tu método:
   • 📱 SMS (código por mensaje)
   • 📧 Email (código por correo)
   • 🔐 App autenticadora (Google Authenticator, etc.)
4. Sigue las instrucciones para configurar

**Beneficios:**
• Mayor seguridad para tu cuenta
• Protección contra accesos no autorizados
• Alertas de inicio de sesión sospechoso

⚠️ Guarda tus códigos de respaldo en un lugar seguro.""",
        keywords=["2fa", "verificacion", "dos pasos", "autenticacion", "seguridad"],
        confidence_boost=0.1,
        action_buttons=[
            ActionButton("btn_security", "🔒 Configurar seguridad", "navigate", "profile/security", "shield"),
        ],
    ),
    FAQEntry(
        id="security_report",
        intent="security_report",
        category="seguridad",
        questions=[
            "Quiero reportar a un usuario",
            "Me quieren estafar",
            "Usuario sospechoso",
            "Cuenta falsa",
            "Fraude",
        ],
        answer="""Para reportar un usuario o contenido sospechoso:

**Reportar usuario:**
1. Ve al perfil del usuario
2. Toca los **tres puntos (⋮)** arriba
3. Selecciona **"Reportar"**
4. Elige el motivo:
   • Fraude/Estafa
   • Contenido inapropiado
   • Acoso
   • Suplantación de identidad
   • Otro
5. Agrega detalles y evidencia

**Reportar producto:**
1. En la página del producto
2. Toca **"Reportar"**
3. Selecciona el motivo

⚠️ **Si ya fuiste estafado:**
• No borres la conversación
• Guarda capturas de pantalla
• Reporta inmediatamente
• Contacta a soporte con los detalles

Investigamos cada reporte en menos de 24 horas.""",
        keywords=["reportar", "estafa", "fraude", "sospechoso", "falso", "denuncia"],
        action_buttons=[
            ActionButton("btn_report", "⚠️ Reportar usuario", "navigate", "report/user", "alert-triangle"),
            ActionButton("btn_human", "👤 Hablar con agente", "call_function", "escalate_to_human", "user"),
        ],
    ),
    
    # === APP ===
    FAQEntry(
        id="app_crash",
        intent="app_bug",
        category="app",
        questions=[
            "La app se cierra sola",
            "Rendly no funciona",
            "Error en la app",
            "La app está muy lenta",
            "No carga nada",
        ],
        answer="""Si tienes problemas con la app:

**Soluciones rápidas:**
1. **Cierra y vuelve a abrir** la app
2. **Verifica tu conexión** a internet
3. **Actualiza la app** en la tienda de aplicaciones
4. **Reinicia tu dispositivo**
5. **Limpia la caché:**
   • Android: Ajustes → Apps → Rendly → Almacenamiento → Borrar caché
   • iOS: Elimina y reinstala la app

**Si el problema persiste:**
• Envíanos una captura del error
• Cuéntanos qué estabas haciendo cuando falló
• Menciona tu modelo de teléfono y versión de Android/iOS

Trabajamos constantemente para mejorar la app.""",
        keywords=["error", "bug", "falla", "crash", "lento", "no funciona", "cierra"],
        action_buttons=[
            ActionButton("btn_update", "🔄 Actualizar app", "open_url", "https://play.google.com/store/apps/details?id=com.rendly.app", "download"),
            ActionButton("btn_human", "👤 Reportar bug", "call_function", "escalate_to_human", "bug"),
        ],
    ),
    
    # === VERIFICACIÓN ===
    FAQEntry(
        id="verification_how",
        intent="account_verify",
        category="cuenta",
        questions=[
            "¿Cómo verifico mi cuenta?",
            "Quiero verificar mi cuenta",
            "¿Cómo me verifico?",
            "Verificar identidad",
            "¿Qué necesito para verificarme?",
            "Proceso de verificación",
        ],
        answer="""Para verificar tu cuenta en Rendly:

1. Ve a **Perfil** → **Verificación**
2. Sube tu **identificación oficial** (INE, pasaporte, licencia)
3. Toma una **selfie** para confirmar tu identidad
4. Espera la revisión (generalmente 24-48 horas)

**Beneficios de verificarte:**
• ✅ Badge de verificado en tu perfil
• 🏪 Puedes vender productos
• 🤝 Mayor confianza de compradores
• 💰 Acceso a retiros de dinero

⚠️ La verificación es **obligatoria para vender** en Rendly.""",
        keywords=["verificar", "verificacion", "identidad", "ine", "pasaporte", "badge"],
        confidence_boost=0.1,
        action_buttons=[
            ActionButton("btn_verify", "✅ Verificar cuenta", "navigate", "profile/verification", "check-circle"),
        ],
    ),
    
    # === BILLETERA ===
    FAQEntry(
        id="wallet_balance",
        intent="wallet_info",
        category="pagos",
        questions=[
            "¿Cómo retiro mi dinero?",
            "Retirar fondos",
            "¿Cómo cobro mis ventas?",
            "Quiero sacar mi dinero",
            "¿Dónde veo mi saldo?",
            "Mi billetera",
            "¿Cuándo me pagan?",
        ],
        answer="""Para gestionar tu **Billetera Rendly**:

📍 Ve a **Perfil** → **Billetera**

**Ver saldo:**
• Tu saldo disponible aparece en la parte superior
• Incluye ganancias de ventas completadas

**Retirar fondos:**
1. Toca **"Retirar fondos"**
2. Ingresa el monto (mínimo $50 MXN)
3. Selecciona tu cuenta bancaria
4. Confirma la transferencia
5. El dinero llega en **1-3 días hábiles**

💡 El saldo se libera cuando el comprador confirma la recepción del producto.""",
        keywords=["billetera", "saldo", "retirar", "cobrar", "dinero", "fondos", "pagan"],
        confidence_boost=0.1,
        action_buttons=[
            ActionButton("btn_wallet", "👛 Mi billetera", "navigate", "profile/wallet", "wallet"),
            ActionButton("btn_bank", "🏦 Agregar cuenta bancaria", "navigate", "profile/bank-accounts", "building"),
        ],
    ),
    
    # === HANDSHAKE / COMPRA PRESENCIAL ===
    FAQEntry(
        id="handshake_info",
        intent="handshake_info",
        category="compras",
        questions=[
            "¿Qué es el handshake?",
            "¿Cómo funciona la compra presencial?",
            "Comprar en persona",
            "¿Cómo hago un handshake?",
            "Entrega en persona",
            "Quedar con el vendedor",
        ],
        answer="""El **Handshake** es nuestro sistema de compra presencial seguro:

**¿Cómo funciona?**
1. Acuerda con el vendedor/comprador por chat
2. Uno de los dos inicia el **Handshake** desde el chat (botón +)
3. Se propone un punto de encuentro y precio
4. El otro acepta la propuesta
5. Ambos se dirigen al punto de encuentro
6. Al llegar, ambos **confirman la entrega** en la app
7. ¡Transacción completada! ✅

**Seguridad:**
• 🗺️ Mapa en tiempo real para ver la ubicación de ambos
• 📍 Detección automática de llegada (50m)
• ✅ Ambas partes deben confirmar
• 🔒 Sistema de disputas si hay problemas
• 📱 Funciona **offline** con QR si no hay internet""",
        keywords=["handshake", "presencial", "persona", "encuentro", "quedar", "entrega"],
        action_buttons=[
            ActionButton("btn_chat", "💬 Ir al chat", "navigate", "chat", "message-circle"),
        ],
    ),
    
    # === NOTIFICACIONES ===
    FAQEntry(
        id="notifications_settings",
        intent="notification_settings",
        category="cuenta",
        questions=[
            "¿Cómo desactivo las notificaciones?",
            "No quiero recibir notificaciones",
            "Configurar notificaciones",
            "Muchas notificaciones",
            "Silenciar notificaciones",
        ],
        answer="""Para configurar tus notificaciones:

1. Ve a **Perfil** → **Configuración** → **Notificaciones**
2. Activa o desactiva por categoría:
   • 💬 **Mensajes** - Nuevos chats y respuestas
   • 🛒 **Compras** - Estados de pedidos
   • ❤️ **Interacciones** - Likes, comentarios, seguidores
   • 🏪 **Ventas** - Nuevas ventas y consultas
   • 📢 **Promociones** - Ofertas y novedades

💡 También puedes **silenciar chats individuales** desde el chat específico.""",
        keywords=["notificaciones", "silenciar", "desactivar", "alertas", "avisos"],
        action_buttons=[
            ActionButton("btn_notif", "🔔 Configurar notificaciones", "navigate", "profile/settings/notifications", "bell"),
        ],
    ),
    
    # === DEVOLUCIONES ===
    FAQEntry(
        id="returns_how",
        intent="return_process",
        category="compras",
        questions=[
            "¿Cómo devuelvo un producto?",
            "Quiero hacer una devolución",
            "¿Puedo devolver algo?",
            "Proceso de devolución",
            "Devolver compra",
        ],
        answer="""Para devolver un producto en Rendly:

1. Ve a **Perfil** → **Historial de pedidos**
2. Selecciona el pedido
3. Toca **"Solicitar devolución"**
4. Selecciona el motivo:
   • Producto diferente al anunciado
   • Producto dañado
   • No es lo que esperaba
   • Otro motivo
5. Adjunta **fotos del producto**
6. Describe el problema

**Importante:**
• ⏰ Tienes **7 días** desde la entrega para solicitar devolución
• 📦 Deberás enviar el producto de vuelta al vendedor
• 💰 El reembolso se procesa al confirmar la recepción del vendedor
• 🔒 Rendly protege tu compra durante todo el proceso""",
        keywords=["devolver", "devolucion", "regresar", "retornar", "producto"],
        confidence_boost=0.1,
        action_buttons=[
            ActionButton("btn_orders", "📦 Ver mis pedidos", "navigate", "profile/orders", "package"),
            ActionButton("btn_human", "👤 Hablar con agente", "call_function", "escalate_to_human", "user"),
        ],
    ),
    
    # === STORIES / HISTORIAS ===
    FAQEntry(
        id="stories_how",
        intent="stories_info",
        category="app",
        questions=[
            "¿Cómo subo una historia?",
            "¿Cómo funcionan las historias?",
            "Publicar historia",
            "Stories en Rendly",
            "¿Cuánto duran las historias?",
        ],
        answer="""Las **Historias** en Rendly te permiten compartir momentos con tus seguidores:

**Publicar una historia:**
1. Toca el botón **"+"** en la sección de historias (arriba del feed)
2. Toma una foto/video o elige de tu galería
3. Agrega texto, stickers o efectos
4. Toca **"Publicar"**

**Características:**
• ⏰ Duran **24 horas**
• 👁️ Puedes ver quién las vio
• ❤️ Los seguidores pueden reaccionar y responder
• 🔒 Puedes **ocultar** historias a usuarios específicos
• ↗️ Se pueden reenviar/compartir

💡 Las historias son ideales para mostrar productos nuevos o promociones.""",
        keywords=["historia", "story", "stories", "publicar", "subir"],
        action_buttons=[
            ActionButton("btn_stories", "📸 Ver historias", "navigate", "home/stories", "camera"),
        ],
    ),
    
    # === RENDS ===
    FAQEntry(
        id="rends_info",
        intent="rends_info",
        category="app",
        questions=[
            "¿Qué son los Rends?",
            "¿Cómo subo un Rend?",
            "Publicar un Rend",
            "¿Cómo funcionan los Rends?",
            "Videos cortos",
        ],
        answer="""Los **Rends** son videos cortos en Rendly (similar a Reels/TikTok):

**Crear un Rend:**
1. Toca el botón **"+"** en la barra inferior
2. Selecciona **"Rend"**
3. Graba un video (hasta 60 segundos) o sube uno
4. Edita: agrega música, texto, filtros
5. Escribe una descripción y hashtags
6. Toca **"Publicar"**

**Funcionalidades:**
• ❤️ Likes y comentarios
• 💾 Guardar favoritos
• ↗️ Compartir
• 🏷️ Etiquetar productos (¡vende directo desde el video!)

💡 Los Rends con productos etiquetados tienen **3x más engagement**.""",
        keywords=["rend", "rends", "video", "reels", "corto", "grabar"],
        action_buttons=[
            ActionButton("btn_rends", "🎬 Ver Rends", "navigate", "rends", "play-circle"),
            ActionButton("btn_create", "➕ Crear Rend", "navigate", "create/rend", "plus-circle"),
        ],
    ),
    
    # === PRIVACIDAD ===
    FAQEntry(
        id="privacy_settings",
        intent="privacy_info",
        category="cuenta",
        questions=[
            "¿Cómo hago mi perfil privado?",
            "Configurar privacidad",
            "No quiero que vean mi perfil",
            "Ocultar actividad",
            "¿Quién ve mi información?",
            "Perfil privado",
        ],
        answer="""Para configurar tu privacidad en Rendly:

1. Ve a **Perfil** → **Configuración** → **Privacidad**

**Opciones disponibles:**
• 👤 **Visibilidad del perfil** - Público, solo seguidores, o privado
• 🟢 **Estado en línea** - Mostrar/ocultar cuándo estás conectado
• 👁️ **Última conexión** - Mostrar/ocultar tu última vez en línea
• 🏷️ **Etiquetado** - Permitir/bloquear que te etiqueten
• @ **Menciones** - Permitir/bloquear menciones
• ❤️ **Likes** - Mostrar/ocultar tus likes
• 🛒 **Actividad de compras** - Mostrar/ocultar
• 📖 **Historias** - Ocultar de usuarios específicos

💡 Un perfil **privado** solo permite que tus seguidores vean tu contenido.""",
        keywords=["privacidad", "privado", "ocultar", "visibilidad", "configurar"],
        action_buttons=[
            ActionButton("btn_privacy", "🔒 Configurar privacidad", "navigate", "profile/settings/privacy", "eye-off"),
        ],
    ),
    
    # === PUBLICACIONES / PRODUCTOS ===
    FAQEntry(
        id="product_edit",
        intent="product_manage",
        category="ventas",
        questions=[
            "¿Cómo edito mi publicación?",
            "Cambiar precio de producto",
            "Modificar mi anuncio",
            "Actualizar publicación",
            "Borrar publicación",
        ],
        answer="""Para gestionar tus publicaciones:

**Editar publicación:**
1. Ve a tu **Perfil** → tus publicaciones
2. Toca la publicación que quieres editar
3. Toca los **tres puntos (⋮)** → **"Editar"**
4. Modifica lo que necesites (precio, fotos, descripción)
5. Guarda los cambios

**Eliminar publicación:**
1. Ve a la publicación
2. Toca **⋮** → **"Eliminar"**
3. Confirma la eliminación

⚠️ No puedes eliminar publicaciones con **pedidos activos**.""",
        keywords=["editar", "modificar", "publicacion", "producto", "anuncio", "precio", "borrar"],
        action_buttons=[
            ActionButton("btn_profile", "👤 Mi perfil", "navigate", "profile", "user"),
            ActionButton("btn_sales", "🏪 Mis ventas", "navigate", "profile/sales", "store"),
        ],
    ),
    
    # === CHAT ===
    FAQEntry(
        id="chat_features",
        intent="chat_info",
        category="app",
        questions=[
            "¿Cómo funciona el chat?",
            "¿Puedo hacer llamadas?",
            "¿Cómo contacto al vendedor?",
            "Enviar mensaje al vendedor",
            "¿Hay videollamadas?",
        ],
        answer="""El **Chat de Rendly** tiene muchas funciones:

**Mensajes:**
• 💬 Texto con formato
• 📷 Fotos y videos
• @ Menciones de usuarios
• 🔗 Compartir productos

**Llamadas:**
• 📞 Llamadas de voz (VoIP)
• Calidad HD con cancelación de ruido

**Funciones especiales:**
• 🤝 Iniciar **Handshake** (compra presencial)
• 📌 Fijar mensajes importantes
• 🏷️ Etiquetar conversaciones
• 🔍 Buscar en el chat
• ✅ Indicador de lectura

**Contactar vendedor:**
1. Ve a la publicación del producto
2. Toca **"Consultar"** o **"Enviar mensaje"**""",
        keywords=["chat", "mensaje", "llamada", "contactar", "vendedor", "comunicar"],
        action_buttons=[
            ActionButton("btn_chats", "💬 Mis chats", "navigate", "chat", "message-circle"),
        ],
    ),
    
    # === SEGUIDORES ===
    FAQEntry(
        id="followers_info",
        intent="social_info",
        category="cuenta",
        questions=[
            "¿Cómo consigo más seguidores?",
            "¿Cómo sigo a alguien?",
            "Ver mis seguidores",
            "Bloquear seguidor",
            "¿Para qué sirven los seguidores?",
        ],
        answer="""Los **seguidores** en Rendly son importantes para vender más:

**Seguir a alguien:**
• Ve a su perfil y toca **"Seguir"**

**Ver tus seguidores:**
• **Perfil** → toca el número de **seguidores**

**Beneficios de tener seguidores:**
• 📣 Tus publicaciones aparecen en su feed
• 📖 Ven tus historias y Rends
• 🔔 Reciben notificaciones de tus nuevos productos
• ⭐ Mayor visibilidad en búsquedas

**Tips para crecer:**
• 📸 Publica contenido de calidad regularmente
• 🎬 Crea Rends atractivos
• 💬 Responde rápido a consultas
• 🏷️ Usa hashtags relevantes""",
        keywords=["seguidores", "seguir", "followers", "bloquear", "social"],
        action_buttons=[
            ActionButton("btn_profile", "👤 Mi perfil", "navigate", "profile", "user"),
        ],
    ),
    
    # === LIKES Y GUARDADOS ===
    FAQEntry(
        id="likes_saves",
        intent="interaction_info",
        category="app",
        questions=[
            "¿Dónde veo mis guardados?",
            "¿Cómo guardo un producto?",
            "Mis favoritos",
            "Productos que me gustan",
            "¿Cómo doy like?",
        ],
        answer="""Para gestionar tus **likes y guardados**:

**Dar like:** Toca el ❤️ en cualquier publicación o Rend

**Guardar producto:** Toca el 🔖 (bookmark) en la publicación

**Ver guardados:**
• **Perfil** → **Guardados** (ícono de bookmark)
• Están organizados por categoría

**Ver likes:**
• **Perfil** → **Likes** (ícono de corazón)

💡 Los productos guardados te **notifican** si bajan de precio o tienen oferta.""",
        keywords=["guardados", "favoritos", "like", "guardar", "bookmark", "corazon"],
        action_buttons=[
            ActionButton("btn_saved", "🔖 Mis guardados", "navigate", "profile/saved", "bookmark"),
        ],
    ),
    
    # === RESEÑAS ===
    FAQEntry(
        id="reviews_info",
        intent="review_info",
        category="compras",
        questions=[
            "¿Cómo dejo una reseña?",
            "Calificar al vendedor",
            "¿Dónde pongo mi opinión?",
            "Dejar comentario de compra",
            "Calificar producto",
        ],
        answer="""Para dejar una **reseña** después de tu compra:

1. Ve a **Perfil** → **Historial de pedidos**
2. Selecciona el pedido completado
3. Toca **"Dejar reseña"**
4. Califica con ⭐ (1-5 estrellas)
5. Escribe tu opinión
6. Opcionalmente adjunta fotos
7. Publica

**Tu reseña ayuda a:**
• 🌟 Otros compradores a decidir
• 📊 El vendedor a mejorar
• 🏆 Construir la reputación de la comunidad

⚠️ Las reseñas son **permanentes** y solo se pueden dejar en pedidos completados.""",
        keywords=["reseña", "calificar", "opinion", "estrellas", "review", "comentario"],
        action_buttons=[
            ActionButton("btn_orders", "📦 Ver mis pedidos", "navigate", "profile/orders", "package"),
        ],
    ),
    
    # === OFERTAS ===
    FAQEntry(
        id="offers_info",
        intent="offer_info",
        category="compras",
        questions=[
            "¿Cómo hago una oferta?",
            "¿Puedo negociar el precio?",
            "Hacer contraoferta",
            "¿El precio es fijo?",
            "Enviar oferta al vendedor",
        ],
        answer="""El sistema de **ofertas** en Rendly te permite negociar:

**Hacer una oferta:**
1. Ve a la publicación del producto
2. Toca **"Hacer oferta"**
3. Ingresa el precio que propones
4. Opcionalmente agrega un mensaje
5. Envía la oferta

**El vendedor puede:**
• ✅ **Aceptar** - Se procede con la compra
• 🔄 **Contraoferta** - Propone otro precio
• ❌ **Rechazar** - La oferta se cancela

💡 **Tips:**
• Las ofertas demasiado bajas pueden ser ignoradas
• Sé respetuoso en las negociaciones
• Puedes tener múltiples ofertas activas""",
        keywords=["oferta", "negociar", "precio", "contraoferta", "descuento"],
        action_buttons=[
            ActionButton("btn_offers", "🏷️ Mis ofertas", "navigate", "profile/offers", "tag"),
        ],
    ),
    
    # === REPUTACIÓN ===
    FAQEntry(
        id="reputation_info",
        intent="reputation_info",
        category="cuenta",
        questions=[
            "¿Cómo funciona la reputación?",
            "¿Qué es el puntaje de reputación?",
            "Mejorar mi reputación",
            "¿Por qué importa la reputación?",
        ],
        answer="""La **reputación** en Rendly refleja tu confiabilidad:

**Se compone de:**
• ⭐ **Calificación promedio** de reseñas
• 📦 **Pedidos completados** exitosamente
• ⏱️ **Tiempo de respuesta** en chat
• 🚚 **Velocidad de envío**
• 🔄 **Tasa de cancelación** (menor = mejor)
• ✅ **Cuenta verificada** (bonus)

**Niveles:**
• 🥉 Bronce - Inicio
• 🥈 Plata - Buen historial
• 🥇 Oro - Excelente reputación
• 💎 Diamante - Top vendedor

💡 **Tips para mejorar:**
• Responde mensajes rápido
• Envía pedidos a tiempo
• Mantén buenas calificaciones
• Resuelve problemas amigablemente""",
        keywords=["reputacion", "puntaje", "calificacion", "confianza", "nivel"],
        action_buttons=[
            ActionButton("btn_profile", "👤 Ver mi reputación", "navigate", "profile", "user"),
        ],
    ),
    
    # === ZONAS / UBICACIÓN ===
    FAQEntry(
        id="zones_info",
        intent="zone_info",
        category="app",
        questions=[
            "¿Qué son las zonas?",
            "¿Cómo cambio mi zona?",
            "Ver productos cerca de mí",
            "Productos en mi ciudad",
            "Filtrar por ubicación",
        ],
        answer="""Las **Zonas** te permiten ver productos cerca de ti:

**Configurar tu zona:**
1. La app detecta tu ubicación automáticamente
2. También puedes configurarla manualmente en **Perfil** → **Direcciones**

**Beneficios:**
• 📍 Ver productos cerca de tu zona
• 🤝 Facilita compras presenciales (Handshake)
• 🚚 Envíos más rápidos y baratos
• 👥 Conectar con vendedores locales

💡 Usa el filtro de **"Cerca de mí"** en la búsqueda para ver solo productos en tu zona.""",
        keywords=["zona", "ubicacion", "cerca", "ciudad", "local", "region"],
        action_buttons=[
            ActionButton("btn_explore", "🔍 Explorar cerca", "navigate", "explore/nearby", "map-pin"),
        ],
    ),
    
    # === IDIOMA ===
    FAQEntry(
        id="language_settings",
        intent="language_info",
        category="cuenta",
        questions=[
            "¿Cómo cambio el idioma?",
            "Cambiar a inglés",
            "¿La app está en inglés?",
            "Configurar idioma",
        ],
        answer="""Para cambiar el idioma de Rendly:

1. Ve a **Perfil** → **Configuración** → **Idioma**
2. Selecciona tu idioma preferido:
   • 🇪🇸 **Español**
   • 🇺🇸 **English**
3. La app se actualizará automáticamente

💡 El idioma se guarda en tu cuenta, así que se mantiene en cualquier dispositivo.""",
        keywords=["idioma", "lenguaje", "ingles", "español", "cambiar", "language"],
        action_buttons=[
            ActionButton("btn_lang", "🌐 Cambiar idioma", "navigate", "profile/settings/language", "globe"),
        ],
    ),
    
    # === LIVE STREAMS ===
    FAQEntry(
        id="livestream_info",
        intent="livestream_info",
        category="app",
        questions=[
            "¿Cómo hago un en vivo?",
            "¿Hay transmisiones en vivo?",
            "Live stream",
            "¿Puedo vender en vivo?",
        ],
        answer="""Las **transmisiones en vivo** en Rendly:

**Iniciar un Live:**
1. Toca el botón **"+"** en la barra inferior
2. Selecciona **"En vivo"**
3. Agrega un título descriptivo
4. Toca **"Iniciar transmisión"**

**Durante el Live:**
• 💬 Chat en tiempo real con viewers
• 🏷️ Muestra y etiqueta productos
• 🛒 Los viewers pueden comprar directo
• ❤️ Reacciones en tiempo real
• 👥 Ver cantidad de espectadores

💡 Los Lives son excelentes para mostrar productos en detalle y responder preguntas al instante.""",
        keywords=["vivo", "live", "transmision", "stream", "directo"],
        action_buttons=[
            ActionButton("btn_live", "📺 Ir a Lives", "navigate", "live", "video"),
        ],
    ),
    
    # === PROBLEMAS CON VENDEDOR ===
    FAQEntry(
        id="seller_issue",
        intent="seller_problem",
        category="compras",
        questions=[
            "El vendedor no responde",
            "No me contesta el vendedor",
            "Problema con un vendedor",
            "El vendedor no envía mi pedido",
            "Vendedor desapareció",
        ],
        answer="""Si tienes problemas con un vendedor:

**Si no responde mensajes:**
• ⏰ Dale un tiempo razonable (24-48 horas)
• 📩 Envía un recordatorio amable
• Si pasan más de 48h sin respuesta, reporta

**Si no envía tu pedido:**
1. Ve a **Perfil** → **Historial de pedidos**
2. Si pasaron más de 3 días sin envío, toca **"Reportar problema"**
3. Selecciona **"El vendedor no envía"**
4. Rendly intervendrá para resolver

**Si sospechas fraude:**
• 🚨 Reporta inmediatamente al usuario
• 📸 Guarda capturas de pantalla
• 💬 No borres la conversación

⚠️ Rendly protege tus compras. Si el vendedor no cumple, recibirás reembolso completo.""",
        keywords=["vendedor", "responde", "envia", "problema", "contactar"],
        confidence_boost=0.1,
        action_buttons=[
            ActionButton("btn_orders", "📦 Ver mis pedidos", "navigate", "profile/orders", "package"),
            ActionButton("btn_human", "👤 Hablar con agente", "call_function", "escalate_to_human", "user"),
        ],
    ),
    
    # === HIGHLIGHTS ===
    FAQEntry(
        id="highlights_info",
        intent="highlights_info",
        category="app",
        questions=[
            "¿Qué son los highlights?",
            "¿Cómo creo un highlight?",
            "Guardar historias en destacados",
            "Historias destacadas",
        ],
        answer="""Los **Highlights** (Destacados) te permiten guardar historias permanentemente:

**Crear un Highlight:**
1. Ve a tu **Perfil**
2. Toca **"+ Nuevo"** en la sección de highlights
3. Selecciona historias pasadas que quieras incluir
4. Dale un **nombre** y elige una **portada**
5. Toca **"Crear"**

**Usos populares:**
• 🏷️ **Catálogo** - Muestra tus productos
• ⭐ **Reseñas** - Comparte opiniones de clientes
• 📦 **Envíos** - Muestra tu proceso de empaque
• ❓ **FAQ** - Responde preguntas frecuentes
• 🎉 **Ofertas** - Promociones activas""",
        keywords=["highlights", "destacados", "historias", "guardar", "permanente"],
        action_buttons=[
            ActionButton("btn_profile", "👤 Mi perfil", "navigate", "profile", "user"),
        ],
    ),
    
    # === AGENTE HUMANO ===
    FAQEntry(
        id="human_agent",
        intent="escalation_request",
        category="general",
        questions=[
            "Quiero hablar con una persona",
            "¿Puedo hablar con un humano?",
            "Necesito un agente",
            "Hablar con soporte humano",
            "No me sirve el bot",
            "Quiero hablar con un agente",
            "Transferir a humano",
        ],
        answer="""Entiendo que prefieres hablar con una persona. Voy a transferirte con un agente de soporte humano.

Un momento por favor, alguien del equipo de Rendly se comunicará contigo en breve. 🙏

Mientras esperas:
• 📋 Ten a mano los detalles de tu consulta
• 📸 Prepara capturas de pantalla si aplica
• 📦 Si es sobre un pedido, ten el número listo""",
        keywords=["humano", "persona", "agente", "bot", "transferir", "real"],
        confidence_boost=0.2,
        action_buttons=[
            ActionButton("btn_escalate", "👤 Conectar con agente", "call_function", "escalate_to_human", "user"),
        ],
    ),
    
    # === GRACIAS / DESPEDIDA ===
    FAQEntry(
        id="farewell",
        intent="farewell",
        category="general",
        questions=[
            "Gracias",
            "Muchas gracias",
            "Eso es todo",
            "Ya no necesito nada",
            "Adiós",
            "Hasta luego",
            "Bye",
            "Listo, gracias",
        ],
        answer="""¡Me alegra haber podido ayudarte! 😊

Si necesitas algo más en el futuro, no dudes en escribirme. Estoy disponible **24/7**.

💡 **¿Sabías que?** Puedes calificar esta conversación para ayudarnos a mejorar.

¡Que tengas un excelente día! 👋""",
        keywords=["gracias", "adios", "bye", "luego", "listo", "todo"],
        confidence_boost=0.15,
    ),

    # === GENERAL ===
    FAQEntry(
        id="greeting",
        intent="greeting",
        category="general",
        questions=[
            "Hola",
            "Buenos días",
            "Buenas tardes",
            "Buenas noches",
            "Hey",
        ],
        answer="""¡Hola! 👋 Bienvenido al soporte de Rendly.

Soy el asistente virtual y estoy aquí para ayudarte. Puedo responder preguntas sobre:

• 🛒 **Compras** - Pedidos, rastreo, cancelaciones, devoluciones
• 💰 **Pagos** - Métodos, reembolsos, billetera
• 👤 **Cuenta** - Contraseña, verificación, privacidad
• 📦 **Envíos** - Direcciones, tiempos, problemas
• 🏪 **Ventas** - Publicar, comisiones, cobros
• 🔒 **Seguridad** - 2FA, reportes, protección
• 📱 **App** - Historias, Rends, chat, Lives

¿En qué puedo ayudarte hoy?""",
        keywords=["hola", "buenos", "buenas", "hey", "saludos"],
        confidence_boost=0.2,
        action_buttons=[
            ActionButton("btn_orders", "📦 Mis pedidos", "navigate", "profile/orders", "package"),
            ActionButton("btn_sell", "🏪 Vender producto", "navigate", "publish", "plus-circle"),
            ActionButton("btn_help", "❓ Ver preguntas frecuentes", "navigate", "help/faq", "help-circle"),
        ],
    ),
]


class KnowledgeBase:
    """FAQ matching and response retrieval"""
    
    def __init__(self):
        self.faq_entries = FAQ_DATABASE
        self._build_question_index()
    
    def _build_question_index(self):
        """Build index for fast question matching"""
        self.question_to_entry: Dict[str, FAQEntry] = {}
        self.all_questions: List[str] = []
        
        for entry in self.faq_entries:
            for q in entry.questions:
                normalized = q.lower()
                self.question_to_entry[normalized] = entry
                self.all_questions.append(normalized)
    
    def find_best_match(
        self, 
        query: str, 
        detected_intent: str,
        keywords: List[str]
    ) -> Tuple[Optional[FAQEntry], float]:
        """Find best matching FAQ entry"""
        
        query_lower = query.lower()
        
        # 1. Try exact question match
        if query_lower in self.question_to_entry:
            entry = self.question_to_entry[query_lower]
            return entry, 0.95 + entry.confidence_boost
        
        # 2. Fuzzy match on questions
        result = process.extractOne(
            query_lower,
            self.all_questions,
            scorer=fuzz.token_sort_ratio
        )
        
        if result and result[1] >= 70:
            matched_question = result[0]
            score = result[1] / 100.0
            entry = self.question_to_entry[matched_question]
            return entry, min(1.0, score + entry.confidence_boost)
        
        # 3. Match by intent
        for entry in self.faq_entries:
            if entry.intent == detected_intent:
                # Check keyword overlap
                keyword_matches = sum(
                    1 for kw in keywords 
                    if any(kw in ek for ek in entry.keywords)
                )
                if keyword_matches >= 2:
                    return entry, 0.7
        
        # 4. Match by keywords alone
        best_match = None
        best_score = 0.0
        
        for entry in self.faq_entries:
            keyword_score = sum(
                1 for kw in keywords 
                if any(kw in ek or ek in kw for ek in entry.keywords)
            ) / max(len(entry.keywords), 1)
            
            if keyword_score > best_score:
                best_score = keyword_score
                best_match = entry
        
        if best_match and best_score >= 0.4:
            return best_match, best_score * 0.6  # Lower confidence for keyword-only
        
        return None, 0.0
    
    def get_response(self, entry_id: str) -> Optional[str]:
        """Get response by entry ID"""
        for entry in self.faq_entries:
            if entry.id == entry_id:
                return entry.answer
        return None
    
    def get_entries_by_category(self, category: str) -> List[FAQEntry]:
        """Get all entries in a category"""
        return [e for e in self.faq_entries if e.category == category]


# Singleton instance
_knowledge_base: Optional[KnowledgeBase] = None


def get_knowledge_base() -> KnowledgeBase:
    global _knowledge_base
    if _knowledge_base is None:
        _knowledge_base = KnowledgeBase()
    return _knowledge_base
