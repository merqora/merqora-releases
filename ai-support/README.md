# Mercora AI Support System

Sistema de IA de soporte interno para Rendly con arquitectura multi-lenguaje.

## Arquitectura

```
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚                      Android App                             â”‚
â”‚                   (SupportChatScreen)                        â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
                      â”‚ HTTP/REST
                      â–¼
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚                    Python (FastAPI)                          â”‚
â”‚                    Orquestador IA                            â”‚
â”‚  - Recibe mensajes                                           â”‚
â”‚  - Clasifica intenciÃ³n                                       â”‚
â”‚  - Consulta FAQ/conocimiento                                 â”‚
â”‚  - Decide: responder o escalar                               â”‚
â””â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
       â”‚                                  â”‚
       â–¼                                  â–¼
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”            â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚   C++ (pybind11) â”‚            â”‚      Rust (Service)         â”‚
â”‚   Motor Scoring  â”‚            â”‚  - Rate limiting            â”‚
â”‚  - confidence    â”‚            â”‚  - SanitizaciÃ³n             â”‚
â”‚  - intent match  â”‚            â”‚  - Sesiones                 â”‚
â”‚  - text analysis â”‚            â”‚  - Logging                  â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜            â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
                      â”‚
                      â–¼
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚                       Supabase                               â”‚
â”‚  - support_conversations                                     â”‚
â”‚  - support_messages                                          â”‚
â”‚  - ai_feedback                                               â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
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

## LÃ³gica de DecisiÃ³n

```
Usuario escribe mensaje
        â”‚
        â–¼
   Rust valida (rate limit, sanitizaciÃ³n)
        â”‚
        â–¼
   Python clasifica intenciÃ³n
        â”‚
        â–¼
   C++ calcula confidence_score
        â”‚
        â–¼
â”Œâ”€â”€â”€â”€â”€â”€â”€â”´â”€â”€â”€â”€â”€â”€â”€â”
â”‚ score >= 70?  â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”˜
    YES â”‚ NO
        â”‚
   â”Œâ”€â”€â”€â”€â”´â”€â”€â”€â”€â”
   â”‚         â”‚
   â–¼         â–¼
IA responde  Escalar a humano
```

## InstalaciÃ³n

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

## EjecuciÃ³n

```bash
cd python
uvicorn main:app --host 0.0.0.0 --port 8000
```

## API Endpoints

- `POST /ai/support/message` - Procesar mensaje de usuario
- `POST /ai/support/feedback` - Feedback sobre respuesta
- `GET /ai/support/conversation/{id}` - Obtener conversaciÃ³n
- `POST /ai/support/escalate` - Escalar a humano
