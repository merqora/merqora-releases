# 🧠 AI Continuous Learning Pipeline - Deployment Guide

## ✅ Pre-requisitos

- Python 3.10+ instalado
- Acceso a Supabase (credenciales en `.env`)
- Backend deployado en Railway
- Admin panel deployado en Netlify

---

## 📋 Paso 1: Ejecutar SQL en Supabase

1. **Ir a Supabase Dashboard**
   - https://app.supabase.com/project/YOUR_PROJECT_ID/editor

2. **Abrir SQL Editor**
   - Click en "SQL Editor" en el sidebar izquierdo
   - Click en "New query"

3. **Copiar y ejecutar el SQL**
   - Abrir archivo: `SUPABASE_AI_TRAINING_PIPELINE.sql`
   - Copiar TODO el contenido (634 líneas)
   - Pegar en el SQL Editor
   - Click en "Run" (o Ctrl+Enter)

4. **Verificar tablas creadas**
   ```sql
   SELECT table_name FROM information_schema.tables 
   WHERE table_schema = 'public' 
   AND table_name LIKE 'ai_%'
   ORDER BY table_name;
   ```
   
   Deberías ver:
   - `ai_intent_corrections`
   - `ai_metrics_snapshots`
   - `ai_prompt_versions`
   - `ai_training_data`
   - `ai_training_runs`

5. **Verificar RPCs creados**
   ```sql
   SELECT routine_name FROM information_schema.routines
   WHERE routine_schema = 'public'
   AND routine_name LIKE '%training%';
   ```
   
   Deberías ver:
   - `create_metrics_snapshot`
   - `get_ai_training_metrics`
   - `get_training_dataset`
   - `submit_intent_correction`

---

## 📦 Paso 2: Instalar Dependencias Python

### Opción A: Local (para testing)

```powershell
cd c:\Users\Rodrigo\Documents\Rendly\ai-support\python
pip install -r requirements.txt
```

**Nuevas dependencias instaladas:**
- `scikit-learn>=1.4.0` - Machine learning
- `numpy>=1.26.0` - Numerical computing

### Opción B: Railway (production)

Railway instalará automáticamente las nuevas dependencias del `requirements.txt` en el próximo deploy.

**Verificar instalación local:**
```powershell
python -c "import sklearn; import numpy; print('✅ scikit-learn:', sklearn.__version__); print('✅ numpy:', numpy.__version__)"
```

---

## 🚀 Paso 3: Deploy Backend (Railway)

### A. Verificar cambios en Git

```powershell
cd c:\Users\Rodrigo\Documents\Rendly
git status
```

**Archivos modificados:**
- `ai-support/python/orchestrator.py`
- `ai-support/python/main.py`
- `ai-support/python/models.py`
- `ai-support/python/supabase_service.py`
- `ai-support/python/learning_engine.py`
- `ai-support/python/requirements.txt`

**Archivos nuevos:**
- `ai-support/python/training_pipeline.py`
- `ai-support/scripts/train_model.ps1`
- `SUPABASE_AI_TRAINING_PIPELINE.sql`

### B. Commit y push

```powershell
git add .
git commit -m "feat: AI Continuous Learning Pipeline con scikit-learn

- Nuevo sistema de entrenamiento ML real (TF-IDF + LinearSVC)
- 13 endpoints REST para training, corrections, metrics
- Admin dashboard con tabs: Metrics, Review, Errors, Intents
- Auto-retraining cada 100 samples
- Dataset export JSONL para fine-tuning
- Human feedback loop con RPC submit_intent_correction
- Buffer auto-flush cada 20 interacciones
- Métricas live con window de 24h
- Confusion matrix y per-intent metrics
"

git push origin main
```

### C. Railway auto-deploy

Railway detectará el push y re-deployará automáticamente.

**Verificar deploy:**
1. Ir a https://railway.app/project/YOUR_PROJECT_ID
2. Ver logs del servicio `ai-support`
3. Esperar "✅ Deployment successful"

**Verificar nuevos endpoints:**
```powershell
# Test health check
curl https://merqora-releases-production.up.railway.app/health

# Test training metrics endpoint
curl https://merqora-releases-production.up.railway.app/ai/training/metrics?hours=24
```

---

## 🎨 Paso 4: Deploy Admin Panel (Netlify)

### A. Build local (opcional - para testing)

```powershell
cd c:\Users\Rodrigo\Documents\Rendly\admin-web
npm install
npm run build
```

### B. Deploy a Netlify

**Opción 1: Auto-deploy desde Git**
```powershell
git add admin-web/
git commit -m "feat: AI Training Dashboard para continuous learning"
git push origin main
```

Netlify detectará el push y re-deployará.

**Opción 2: Manual deploy con CLI**
```powershell
cd admin-web
netlify deploy --prod
```

### C. Verificar admin panel

1. Ir a https://YOUR_ADMIN_PANEL.netlify.app/admin/training-pipeline
2. Deberías ver el nuevo dashboard con tabs:
   - **Metrics** - KPIs, confidence distribution, intent distribution
   - **Review** - Pending human review (sorted by lowest confidence)
   - **Errors** - Classification errors
   - **Intents** - Per-intent accuracy table
   - **Training** - History of training runs
   - **Test** - Predict intent on test message

---

## 🧪 Paso 5: Primera Corrida de Entrenamiento

### Opción A: Desde PowerShell Script

```powershell
cd c:\Users\Rodrigo\Documents\Rendly\ai-support

# Ejecutar script completo (mensajes + flush + train)
.\scripts\train_model.ps1

# O por pasos:
.\scripts\train_model.ps1 -SkipMessages  # Solo entrenar con datos existentes
.\scripts\train_model.ps1 -OnlyFlush     # Solo flush buffer
.\scripts\train_model.ps1 -OnlyTrain     # Solo entrenar
```

**Output esperado:**
```
═══════════════════════════════════════════════
  STEP 1: Sending training messages (70)
═══════════════════════════════════════════════
[1/70] OK C:85 MATCH | Como puedo comprar un producto?
[2/70] OK C:90 MATCH | Quiero comprar algo pero no se como
...

═══════════════════════════════════════════════
  STEP 2: Flushing training buffer to Supabase
═══════════════════════════════════════════════
Flushed: 70 records

═══════════════════════════════════════════════
  STEP 3: Training ML model (TF-IDF + SVM)
═══════════════════════════════════════════════

═══════════════════════════════════════════════
  TRAINING COMPLETED
═══════════════════════════════════════════════
  Status:        completed
  Accuracy:      92.5%
  F1 Score:      91.3%
  Train:         56
  Test:          14
  Intents:       35
```

### Opción B: Desde Admin Dashboard

1. Ir a `/admin/training-pipeline`
2. Click en botón **"Entrenar Modelo"**
3. Esperar mensaje de confirmación con métricas
4. Ver el run en el tab "Training Runs"

---

## 📊 Paso 6: Verificar Sistema Funcionando

### Test 1: Enviar mensaje real

```powershell
$headers = @{ "Content-Type" = "application/json" }
$body = @{
    user_id = "test_user_123"
    message = "Como compro algo en Rendly?"
    session_id = "test_session_1"
} | ConvertTo-Json

Invoke-WebRequest -Uri "https://merqora-releases-production.up.railway.app/ai/support/message" -Method POST -Headers $headers -Body $body
```

**Esperado:**
- Respuesta con `confidence_score` >= 70
- Registro en `ai_training_data` (buffer)

### Test 2: Verificar buffer flush

```powershell
curl -X POST https://merqora-releases-production.up.railway.app/ai/training/flush
```

**Esperado:**
```json
{"flushed": 1, "remaining_buffer": 0}
```

### Test 3: Revisar datos en Supabase

```sql
SELECT 
    user_message,
    detected_intent,
    confidence_score,
    response_source,
    created_at
FROM ai_training_data
ORDER BY created_at DESC
LIMIT 10;
```

### Test 4: Verificar métricas live

```powershell
curl "https://merqora-releases-production.up.railway.app/ai/training/metrics?hours=24"
```

**Esperado:**
```json
{
  "live_metrics": {
    "total_messages": 70,
    "ai_resolved": 65,
    "escalated": 5,
    "avg_confidence": 85,
    "escalation_rate": 7.1
  },
  "pipeline_stats": {
    "model_trained": true,
    "model_intents": 35,
    "samples_since_last_train": 0
  }
}
```

### Test 5: Probar predicción

```powershell
curl "https://merqora-releases-production.up.railway.app/ai/training/predict?message=quiero%20devolver%20un%20producto"
```

**Esperado:**
```json
{
  "message": "quiero devolver un producto",
  "predicted_intent": "return_process",
  "confidence": 0.89,
  "top_intents": {
    "return_process": 0.89,
    "refund": 0.06,
    "shipping_problem": 0.03
  },
  "model_trained": true
}
```

---

## 🔄 Flujo Continuo de Aprendizaje

### 1. Usuario interactúa
- Mensaje → Orchestrator → Respuesta
- **Auto-guardado** en `training_pipeline._training_buffer`

### 2. Buffer auto-flush
- Cada **20 registros** → flush a `ai_training_data`
- O manual: `POST /ai/training/flush`

### 3. Revisión humana (Admin Dashboard)
- Tab **"Review"** → Ver low-confidence messages
- Click "✏️ Corregir" → Modal con intent selector
- Submit → RPC `submit_intent_correction`
- **Trigger auto-detecta** si hay mismatch

### 4. Auto-retraining
- Cada **100 samples** nuevos → trigger automático
- O manual: Click "Entrenar Modelo" en dashboard
- **Solo se deploya** si F1 no baja >5%

### 5. Monitoreo
- **Metrics tab** → Live metrics 24h
- **Intents tab** → Per-intent accuracy
- **Errors tab** → Misclassifications

---

## 📁 Archivos del Modelo

Los modelos entrenados se guardan en:
```
ai-support/python/trained_model/
├── intent_classifier_latest.pkl          # Modelo activo
├── intent_classifier_YYYYMMDD_HHMMSS.pkl # Backups
└── reports/
    └── training_YYYYMMDD_HHMMSS.txt      # Reporte de cada run
```

Los datasets exportados:
```
ai-support/python/datasets/
├── training_dataset_latest.jsonl
└── training_dataset_YYYYMMDD_HHMMSS.csv
```

---

## 🐛 Troubleshooting

### Error: "Supabase not configured"
**Solución:** Verificar variables de entorno:
```powershell
echo $env:RENDLY_AI_SUPABASE_URL
echo $env:RENDLY_AI_SUPABASE_KEY
```

### Error: "Model not trained yet"
**Solución:** Ejecutar primer entrenamiento:
```powershell
.\scripts\train_model.ps1
```

### Error: "Training failed - not enough samples"
**Solución:** Necesitas mínimo **20 samples**. Enviar más mensajes de prueba.

### Buffer no se flushea
**Verificar:**
```powershell
curl https://merqora-releases-production.up.railway.app/ai/training/dataset/stats
```

**Manual flush:**
```powershell
curl -X POST https://merqora-releases-production.up.railway.app/ai/training/flush
```

### Admin dashboard no carga
**Verificar:**
1. Netlify deploy success
2. API base URL correcto en `AITrainingDashboard.jsx:7`
3. CORS habilitado en FastAPI

---

## 📈 Métricas Clave a Monitorear

| Métrica | Target | Crítico si |
|---------|--------|------------|
| Intent Accuracy | >85% | <70% |
| Escalation Rate | <15% | >30% |
| Avg Confidence | >75% | <60% |
| F1 Score | >0.85 | <0.70 |
| Training Samples | >100 | <20 |
| Misclassifications | <5% | >15% |

---

## ✅ Checklist Final

- [ ] SQL ejecutado en Supabase
- [ ] Tablas y RPCs creados correctamente
- [ ] `requirements.txt` actualizado con scikit-learn
- [ ] Backend re-deployado en Railway
- [ ] Admin panel re-deployado en Netlify
- [ ] Primer entrenamiento ejecutado exitosamente
- [ ] Modelo guardado en `trained_model/`
- [ ] Métricas live funcionando
- [ ] Admin dashboard accesible
- [ ] Buffer flush automático funciona
- [ ] Human corrections funcionan
- [ ] Auto-retraining configurado

---

## 🎯 Próximos Pasos (Post-deployment)

1. **Cargar datos históricos**
   - Migrar `ai_feedback` existente → `ai_training_data`
   - Script de migración si es necesario

2. **Entrenar con datos reales**
   - Dejar sistema activo 1-2 semanas
   - Acumular 500+ samples
   - Re-entrenar con datos de producción

3. **Fine-tuning pipeline**
   - Ajustar `auto_retrain_threshold` (default: 100)
   - Ajustar `min_samples_for_training` (default: 20)
   - Modificar TF-IDF params si accuracy baja

4. **Human review workflow**
   - Asignar reviewers
   - Establecer SLA para correcciones
   - Revisar low-confidence daily

5. **Monitoreo continuo**
   - Dashboard diario
   - Alertas si accuracy < 70%
   - Weekly training runs

---

**🚀 Sistema listo para producción con aprendizaje continuo real.**
