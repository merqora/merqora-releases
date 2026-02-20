# 🎯 Sistema de Feedback y Aprendizaje de IA - IMPLEMENTADO

## 📋 RESUMEN DE CAMBIOS

### ✅ Problema 1: Feedback de app NO visible en admin-web
**SOLUCIONADO** - La página `admin-web/src/pages/Feedback.jsx` ya está configurada correctamente y mostrará todos los registros de `app_feedback`.

### ✅ Problema 2: Botón "Resolver" en Escalations
**IMPLEMENTADO** - Sistema completo de resolución con calificación del usuario y aprendizaje automático.

---

## 🔧 PASOS PARA ACTIVAR EL SISTEMA

### 1️⃣ Ejecutar SQL en Supabase

Ejecuta el siguiente archivo SQL en tu proyecto de Supabase:

```bash
SUPABASE_AI_FEEDBACK_ENHANCED.sql
```

Este archivo:
- Agrega campos de `rating`, `conversation_id`, `user_message`, `agent_response` a `ai_feedback`
- Crea vistas `v_ai_learning_feedback` y `v_feedback_analysis`
- Configura índices para búsquedas rápidas
- Asegura políticas RLS correctas

### 2️⃣ Verificar Políticas RLS en Supabase

Asegúrate de que estas políticas existen en la tabla `app_feedback`:

```sql
-- Para que admin-web pueda leer
CREATE POLICY "Admin can view all feedback"
    ON app_feedback FOR SELECT
    TO authenticated
    USING (true);

-- Para que usuarios puedan crear su feedback
CREATE POLICY "Los usuarios pueden crear su propio feedback"
    ON app_feedback FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);
```

### 3️⃣ Iniciar Admin Web

```bash
cd admin-web
npm install
npm run dev
```

Accede a http://localhost:5173 y verás:
- **Feedback** → Ver todos los comentarios de usuarios desde la app
- **Aprendizaje IA** → Nueva página con análisis completo del aprendizaje

---

## 🎬 FLUJO COMPLETO DEL SISTEMA

### 📱 Cuando un usuario envía feedback desde la app:

**Paso 1:** Usuario va a `Configuración` → `Enviar comentarios` o `Reportar un problema`

**Paso 2:** Llena el formulario y envía

**Paso 3:** Se registra en tabla `app_feedback` de Supabase

**Paso 4:** Admin-web **AHORA SÍ LO VE** en la página `/feedback`

---

### 🆘 Cuando un usuario es escalado a soporte humano:

**Paso 1:** Usuario chatea con IA en `Centro de ayuda`

**Paso 2:** IA detecta que no puede resolver (confidence < 70% o problema complejo)

**Paso 3:** Crea registro en `ai_escalations` con status `pending`

**Paso 4:** Admin ve la escalación en `/escalations`

**Paso 5:** Admin chatea con el usuario en tiempo real

**Paso 6:** Admin pulsa botón **"Resolver"**

**Paso 7:** Sistema ejecuta:
```javascript
// 1. Envía mensaje del sistema al usuario pidiendo calificación
const feedbackMessage = `✅ Tu consulta ha sido resuelta.
¿Cómo calificarías la ayuda? (1-5 estrellas)`

// 2. Marca escalación como resuelta
UPDATE ai_escalations SET status='resolved'

// 3. Marca conversación como resuelta
UPDATE support_conversations SET status='resolved', resolved_by='human'
```

**Paso 8:** Usuario ve el mensaje en su app y envía un número (1-5)

**Paso 9:** App detecta que es una calificación y ejecuta:
```kotlin
AISupportRepository.saveUserRating(
    conversationId = conversationId,
    userId = userId,
    rating = calificacion // 1-5
)
```

**Paso 10:** Se guarda en `ai_feedback`:
```sql
INSERT INTO ai_feedback (
    conversation_id,
    user_id,
    helpful,        -- true si rating >= 4
    rating,         -- 1-5
    feedback_type   -- 'resolution_feedback'
)
```

**Paso 11:** 🧠 **APRENDIZAJE AUTOMÁTICO**
- Si `rating >= 4` (buena calificación):
  - Se marca como `helpful = true`
  - IA aprenderá de esta conversación
  - Aparece en `/ai-learning` con badge "✨ Para aprendizaje"
  
- Si `rating < 4` (mala calificación):
  - Se guarda para análisis
  - Admin puede ver en `/ai-learning` qué salió mal
  - Se filtra del aprendizaje automático

---

## 🤖 CÓMO LA IA APRENDE

### Backend Python (`ai-support/python/`)

El sistema de aprendizaje funciona en 3 niveles:

#### 1. Aprendizaje de Agentes Humanos

En `supabase_service.py`:

```python
async def check_for_similar_query(user_message: str) -> Optional[str]:
    """
    Busca respuestas de agentes humanos para consultas similares.
    Si encuentra coincidencia >40%, usa la respuesta del agente.
    """
    # 1. Obtiene ejemplos de ai_feedback donde:
    #    - feedback_type = 'agent_response'
    #    - Tiene user_message y agent_response
    
    # 2. Compara con consulta actual usando similitud Jaccard
    
    # 3. Si score > 40%, retorna la respuesta del agente
    #    IA responde como lo haría el humano
```

En `orchestrator.py` (línea 320-359):

```python
# Antes de procesar con IA, busca respuestas aprendidas
learned_response = await supabase.check_for_similar_query(message)

if learned_response:
    # IA usa la respuesta del agente humano
    return learned_response  # Confidence: 95%
```

#### 2. Cache Inteligente

En `learning_engine.py`:

```python
# Cuando se responde una consulta con éxito:
learning_engine.cache_response(
    question=user_message,
    response=faq_answer,
    intent=intent,
    confidence=confidence
)

# Próxima vez que alguien pregunte algo similar:
cached = learning_engine.find_cached_response(message)
if cached:
    return cached.response  # Instantáneo
```

#### 3. Auto-entrenamiento (línea 683-691)

```python
# Después de cada respuesta exitosa (no escalada, confidence > 60):
if not escalated and confidence >= 60:
    local_ai.learn_from_feedback(
        query=user_message,
        intent=detected_intent,
        response=ai_response,
        was_helpful=True
    )
```

---

## 📊 PÁGINA DE APRENDIZAJE IA (`/ai-learning`)

### Estadísticas en Tiempo Real

```javascript
- Total Feedback: Todas las calificaciones recibidas
- Positivos: Calificaciones con helpful=true
- Negativos: Calificaciones con helpful=false
- Calificación Promedio: De 1 a 5 estrellas
- Aprendidos: Feedback con rating >= 4 usado para entrenar IA
```

### Filtros Disponibles

- **Por tipo:** Todos / Positivos / Negativos
- **Por estrellas:** 1⭐, 2⭐⭐, 3⭐⭐⭐, 4⭐⭐⭐⭐, 5⭐⭐⭐⭐⭐
- **Búsqueda:** Por texto, usuario, intent detectado

### Información de cada Feedback

```javascript
{
  usuario: "nombre_usuario",
  rating: 5,  // Estrellas
  feedback_text: "Comentario del usuario",
  detected_intent: "Intent que detectó la IA",
  conversation_status: "resolved",
  resolved_by: "human" | "ai",
  message_count: 12,  // Mensajes en la conversación
  
  // Si rating >= 4:
  badge: "✨ Para aprendizaje"
}
```

---

## 🗄️ ESTRUCTURA DE BASE DE DATOS

### Tabla: `app_feedback`
```sql
- id, user_id, category, title, description
- rating (1-5)
- status (pending, reviewing, planned, implemented, rejected)
- device_info (JSONB con info del dispositivo)
```

### Tabla: `ai_feedback` (MEJORADA)
```sql
- id, message_id, user_id, conversation_id
- helpful (boolean)
- rating (1-5 estrellas) -- NUEVO
- feedback_type (message_feedback, resolution_feedback, agent_response) -- NUEVO
- user_message (TEXT) -- NUEVO - Para aprendizaje
- agent_response (TEXT) -- NUEVO - Respuesta ideal
- feedback_text (Comentario opcional del usuario)
```

### Vistas Creadas

**`v_ai_learning_feedback`** - Solo feedback positivo para entrenar IA
```sql
SELECT * FROM ai_feedback 
WHERE helpful = true AND rating >= 4
```

**`v_feedback_analysis`** - Análisis completo con stats
```sql
- Feedback + info de usuario + conversación
- Cuenta mensajes, muestra intent detectado
- Estado de conversación y quién resolvió
```

---

## 🎨 INTERFAZ EN ADMIN-WEB

### Página: Feedback (`/feedback`)
- Lista de todos los comentarios desde la app
- Filtros por estado (pending, reviewing, planned, etc.)
- Detalles de cada feedback con info del dispositivo
- Cambiar estado y prioridad
- Responder internamente

### Página: Aprendizaje IA (`/ai-learning`)
- Dashboard con métricas de aprendizaje
- Lista de todo el feedback de calificaciones
- Filtros por rating y tipo
- Indicador visual de qué feedback usa la IA
- Detalle completo de cada interacción

### Página: Escalaciones (`/escalations`)
- Lista de conversaciones escaladas
- Botón "Resolver" que:
  - Envía mensaje pidiendo calificación
  - Marca como resuelta
  - Activa flujo de feedback automático

---

## 🔄 FLUJO DE DATOS COMPLETO

```
Usuario escribe mensaje
        ↓
Rust valida seguridad
        ↓
Python busca respuesta aprendida ← ai_feedback (agent_response)
        ↓
Si no encuentra → C++ analiza (confidence_score)
        ↓
Si confidence >= 70 → IA responde
Si confidence < 70 → Escala a humano
        ↓
Agente humano resuelve
        ↓
Agente pulsa "Resolver"
        ↓
Usuario califica (1-5 ⭐)
        ↓
Rating >= 4 → Guarda en ai_feedback para aprendizaje
Rating < 4 → Guarda para análisis (NO entrena)
        ↓
Python backend usa ai_feedback en próximas consultas
        ↓
IA mejora automáticamente ✨
```

---

## 📝 CHECKLIST DE VERIFICACIÓN

### En Supabase:
- [ ] Ejecutar `SUPABASE_AI_FEEDBACK_ENHANCED.sql`
- [ ] Verificar que `app_feedback` tiene políticas RLS correctas
- [ ] Verificar que `ai_feedback` tiene nuevas columnas
- [ ] Verificar que vistas `v_ai_learning_feedback` y `v_feedback_analysis` existen

### En Admin-Web:
- [ ] `npm install` ejecutado
- [ ] Página `/feedback` carga y muestra datos
- [ ] Página `/ai-learning` carga y muestra stats
- [ ] Realtime funciona (nuevos feedbacks aparecen automáticamente)

### En App Android:
- [ ] `HelpCenterScreen.kt` maneja calificaciones (1-5)
- [ ] `AISupportRepository.kt` tiene `saveUserRating()`
- [ ] Chat de soporte muestra mensajes del sistema

### En Backend IA:
- [ ] `ai-support/python/.env` configurado con Supabase credentials
- [ ] `supabase_service.py` tiene `check_for_similar_query()`
- [ ] `orchestrator.py` usa aprendizaje antes de procesar

---

## 🚀 TESTING

### Test 1: Feedback desde App
1. Abre la app → Configuración → Enviar comentarios
2. Llena formulario y envía
3. Ve a admin-web `/feedback`
4. **Debe aparecer** el comentario en la lista

### Test 2: Escalación y Resolución
1. Abre app → Centro de ayuda
2. Escribe consulta compleja que escale
3. En admin-web `/escalations` → Abre chat
4. Responde al usuario
5. Pulsa "Resolver"
6. En app, debe aparecer mensaje pidiendo calificación
7. Envía número (1-5)
8. En admin-web `/ai-learning` → **Debe aparecer** el feedback

### Test 3: Aprendizaje de IA
1. Crea escalación con consulta específica
2. Agente responde y resuelve
3. Usuario califica 5 estrellas
4. Próximo usuario pregunta algo similar
5. IA debe usar la respuesta del agente (confidence: 95%)

---

## 🎯 RESULTADO FINAL

### Para Usuarios:
- ✅ Pueden enviar feedback desde la app
- ✅ Pueden calificar el soporte recibido
- ✅ Reciben mejores respuestas de IA cada vez

### Para Admins:
- ✅ Ven todos los comentarios en `/feedback`
- ✅ Ven escalaciones en tiempo real en `/escalations`
- ✅ Monitorean aprendizaje de IA en `/ai-learning`
- ✅ Saben exactamente qué aprende la IA y qué no

### Para la IA:
- ✅ Aprende de agentes humanos automáticamente
- ✅ Solo usa feedback positivo (≥4 ⭐) para entrenar
- ✅ Responde cada vez mejor sin intervención manual
- ✅ Reduce escalaciones con el tiempo

---

## 📞 SOPORTE

Si algo no funciona:

1. **Feedback no aparece en web:**
   - Verifica políticas RLS en Supabase
   - Revisa console del navegador (F12)
   - Verifica que `SUPABASE_URL` y `SUPABASE_ANON_KEY` están en `admin-web/.env`

2. **Calificaciones no se guardan:**
   - Verifica que ejecutaste `SUPABASE_AI_FEEDBACK_ENHANCED.sql`
   - Revisa logs de Android: `adb logcat | grep AISupportRepository`
   - Verifica que tabla `ai_feedback` tiene columna `rating`

3. **IA no aprende:**
   - Verifica que backend Python está corriendo
   - Verifica `.env` en `ai-support/python/` tiene Supabase credentials
   - Revisa logs: `cd ai-support/python && uvicorn main:app --reload`

---

## 🎉 ¡SISTEMA COMPLETO Y PROFESIONAL!

Todo está implementado para que la IA mejore continuamente basándose en el feedback real de usuarios y las resoluciones de agentes humanos. Es un sistema de aprendizaje automático verdadero que se optimiza solo. 🚀
