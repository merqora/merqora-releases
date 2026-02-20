# 🚀 Merqora Releases - Production App Repository

APK releases y código fuente completo para Merqora/Rendly app.

## Stack Tecnológico Enterprise

### **Core**
- **Kotlin** - Lenguaje moderno y eficiente
- **Jetpack Compose** - UI declarativa de Google
- **C++/JNI** - Operaciones críticas de rendimiento
- **Hilt** - Inyección de dependencias

### **Backend & AI**
- **FastAPI (Python)** - API de soporte con IA
- **Supabase** - Base de datos PostgreSQL + Realtime
- **scikit-learn** - ML training pipeline para intent classification
- **C++/Rust** - Scoring engine y rate limiting

### **Arquitectura**
- **MVVM** - Model-View-ViewModel
- **Coroutines + Flow** - Programación asíncrona
- **StateFlow** - Estado reactivo
- **Room** - Base de datos local

### **Rendimiento**
- **Coil** - Carga de imágenes ultra-optimizada
- **LazyColumn** - Scroll optimizado con reciclaje
- **C++ Engine** - Procesamiento nativo para operaciones críticas
- **Memory Cache** - 25% de RAM para imágenes
- **Disk Cache** - 512 MB para persistencia

### **Features Implementadas**
✅ Feed infinito optimizado (estilo Instagram)
✅ Animaciones nativas fluidas (60 FPS garantizado)
✅ Sistema de compras online con Mercado Pago
✅ Chat en tiempo real con Supabase Realtime
✅ Llamadas VoIP con WebRTC
✅ AI Support con continuous learning
✅ Stories con highlights
✅ Sistema de verificación de usuarios
✅ Dark theme (Midnight Luxe)

## Compilar y Ejecutar

```bash
# 1. Abrir en Android Studio
cd Rendly

# 2. Sincronizar Gradle
./gradlew build

# 3. Ejecutar en dispositivo/emulador
./gradlew installDebug

# O desde Android Studio: Run > Run 'app'
```

## AI Support System

El sistema de IA incluye:
- **Training Pipeline** con scikit-learn (TF-IDF + LinearSVC)
- **Auto-retraining** cada 100 samples
- **Human feedback loop** con admin dashboard
- **13 API endpoints** para training y métricas

Ver documentación completa en `ai-support/python/README_TRAINING.md`

## Deployment

- **Backend**: Railway - https://merqora-releases-production.up.railway.app
- **Admin Panel**: Netlify
- **APK Releases**: GitHub Releases

Ver guía completa en `AI_TRAINING_DEPLOYMENT.md`

**¡App nativa ultra-rápida con IA real!** 🚀
