# Rendly AI Support System

Sistema de IA de soporte interno para Rendly con arquitectura multi-lenguaje.

## Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                      Android App                             │
│                   (SupportChatScreen)                        │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTP/REST
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    Python (FastAPI)                          │
│                    Orquestador IA                            │
│  - Recibe mensajes                                           │
│  - Clasifica intención                                       │
│  - Consulta FAQ/conocimiento                                 │
│  - Decide: responder o escalar                               │
└──────┬──────────────────────────────────┬───────────────────┘
       │                                  │
       ▼                                  ▼
┌──────────────────┐            ┌─────────────────────────────┐
│   C++ (pybind11) │            │      Rust (Service)         │
│   Motor Scoring  │            │  - Rate limiting            │
│  - confidence    │            │  - Sanitización             │
│  - intent match  │            │  - Sesiones                 │
│  - text analysis │            │  - Logging                  │
└──────────────────┘            └─────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                       Supabase                               │
│  - support_conversations                                     │
│  - support_messages                                          │
│  - ai_feedback                                               │
└─────────────────────────────────────────────────────────────┘
```

## Componentes

### 1. Python (`/python`)
- FastAPI server
- Intent classification
- FAQ matching
- Response generation
- Orchestration logic

### 2. C++ (`/cpp`)
- Confidence scoring engine
- Text analysis
- Pattern matching
- Compiled as Python extension (pybind11)

### 3. Rust (`/rust`)
- Security service
- Rate limiting
- Input sanitization
- Session management
- Structured logging

## Lógica de Decisión

```
Usuario escribe mensaje
        │
        ▼
   Rust valida (rate limit, sanitización)
        │
        ▼
   Python clasifica intención
        │
        ▼
   C++ calcula confidence_score
        │
        ▼
┌───────┴───────┐
│ score >= 70?  │
└───────┬───────┘
    YES │ NO
        │
   ┌────┴────┐
   │         │
   ▼         ▼
IA responde  Escalar a humano
```

## Instalación

```bash
# Python
cd python
pip install -r requirements.txt

# C++ (build)
cd cpp
mkdir build && cd build
cmake ..
make

# Rust
cd rust
cargo build --release
```

## Ejecución

```bash
cd python
uvicorn main:app --host 0.0.0.0 --port 8000
```

## API Endpoints

- `POST /ai/support/message` - Procesar mensaje de usuario
- `POST /ai/support/feedback` - Feedback sobre respuesta
- `GET /ai/support/conversation/{id}` - Obtener conversación
- `POST /ai/support/escalate` - Escalar a humano
