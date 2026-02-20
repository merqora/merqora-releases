# Arreglos Pendientes - Sistema de Feedback

## 🔴 En Progreso

El sistema es muy complejo para hacer todos los cambios en una sola sesión. He separado el trabajo:

### ✅ Completado hasta ahora:

1. **Mensaje del sistema corto** - Cambiado a `__RATING_REQUEST__` en Escalations.jsx
2. **Banner de conversación cerrada** - Agregado en HelpCenterScreen.kt
3. **Variables de estado** - isConversationClosed, selectedRating, ratingComment agregadas

### 🟡 En desarrollo (Requiere composable completo):

1. **RatingInlineInterface composable** - Necesito crear este composable con:
   - 5 estrellas seleccionables (actualmente el RatingDialog tiene un bug mostrando 4)
   - TextField para comentario opcional
   - Botón de envío
   - Diseño profesional inline (NO flotante)

2. **Eliminar chat de Supabase** - El código ya está en Escalations.jsx pero necesito verificar que funcione

3. **Llenar columnas faltantes**:
   - `ai_feedback`: user_message, agent_response, message_id
   - `app_feedback`: user_name, user_email
   - Necesito modificar FeedbackRepository.kt y AISupportRepository.kt

## 📋 Próximos pasos:

Dado que el archivo HelpCenterScreen.kt tiene casi 1900 líneas, voy a:

1. Crear RatingInlineInterface composable separado
2. Modificar SupportMessageBubble para usar este composable con variables de estado compartidas
3. Arreglar repositorios para llenar todas las columnas
4. Verificar eliminación de Supabase

## 🔧 Código necesario:

El RatingInlineInterface necesita:
- Acceso a selectedRating, ratingComment (estado del padre)
- onRatingChange, onCommentChange callbacks
- onSubmit callback
- Diseño con Row de 5 estrellas + TextField + Button

**Nota**: Debido a la complejidad, sugiero al usuario que compile lo que tenemos hasta ahora y luego continuemos con los composables restantes en pasos más pequeños.
