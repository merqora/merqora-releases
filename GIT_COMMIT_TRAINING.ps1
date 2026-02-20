#!/usr/bin/env pwsh
# ════════════════════════════════════════════════════════
# Git Commit - AI Continuous Learning Pipeline
# ════════════════════════════════════════════════════════

Write-Host ""
Write-Host "═══════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  GIT COMMIT - AI TRAINING PIPELINE" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Verificar que estamos en el directorio correcto
$currentDir = Get-Location
if (-not (Test-Path "ai-support")) {
    Write-Host "❌ Error: Debes ejecutar desde c:\Users\Rodrigo\Documents\Rendly\" -ForegroundColor Red
    exit 1
}

# Mostrar archivos modificados
Write-Host "Archivos a commitear:" -ForegroundColor Yellow
Write-Host ""

git status --short

Write-Host ""
Write-Host "═══════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Agregar todos los archivos
Write-Host "Agregando archivos..." -ForegroundColor Yellow
git add .

# Commit con mensaje detallado
$commitMessage = @"
feat: AI Continuous Learning Pipeline con scikit-learn

Sistema completo de entrenamiento ML real para AI support con:

🧠 Core ML Pipeline
- TF-IDF vectorization (ngram 1-3, max 10k features)
- LinearSVC classifier con balanced class weights
- CalibratedClassifierCV para probability estimates
- Human-corrected samples weighted 2x
- Auto-retraining cada 100 samples
- Model deployment solo si F1 no baja >5%

📊 Training Pipeline
- DatasetManager: JSONL/CSV export, stratified split
- IntentClassifierTrainer: train/eval con confusion matrix
- TrainingPipeline: orchestration, metrics tracking
- Buffer auto-flush cada 20 interacciones
- Supabase integration con 5 tables, 4 RPCs, 3 views

🔄 Continuous Learning
- Cada mensaje → record_interaction() → buffer
- Auto-detect misclassifications via trigger
- Human review workflow con corrections modal
- Live metrics con 24h window
- Per-intent accuracy tracking

🎨 Admin Dashboard
- Métricas tab: KPIs, confidence/intent distributions
- Review tab: pending samples sorted by low confidence
- Errors tab: classification mismatches
- Intents tab: per-intent accuracy table
- Training tab: run history con metrics
- Test tab: predict intent on message

📡 API Endpoints (13 nuevos)
- POST /ai/training/run - trigger training
- POST /ai/training/correction - submit human correction
- POST /ai/training/flush - flush buffer to DB
- GET /ai/training/metrics - live metrics
- GET /ai/training/predict - test ML model
- GET /ai/training/pending-review - low confidence samples
- GET /ai/training/errors - classification errors
- GET /ai/training/intent-performance - per-intent stats
- GET /ai/training/runs - training history
- GET /ai/training/dataset/export - JSONL/CSV download
- GET /ai/training/dataset/stats - dataset info
- POST /ai/training/snapshot - create metrics snapshot
- GET /ai/training/metrics/supabase - historical metrics

🗄️ Database Schema
Tables:
- ai_training_data (primary dataset con FK constraints)
- ai_training_runs (metrics tracking)
- ai_intent_corrections (human feedback)
- ai_metrics_snapshots (periodic snapshots)
- ai_prompt_versions (prompt evolution)

RPCs:
- get_ai_training_metrics(p_days) → comprehensive metrics
- get_training_dataset(min_confidence, only_reviewed) → clean samples
- submit_intent_correction(...) → record correction + auto-detect
- create_metrics_snapshot(period_type) → snapshot

Views:
- v_pending_review → low confidence samples
- v_classification_errors → mismatches
- v_intent_performance → per-intent accuracy

Triggers:
- auto_detect_misclassification → sets intent_correct=FALSE
- update_training_data_timestamp → auto updated_at

📦 Dependencies
- scikit-learn>=1.4.0 - ML training/evaluation
- numpy>=1.26.0 - numerical computing

🔧 Integration Points
- orchestrator.py: training_pipeline init, record every interaction
- learning_engine.py: lazy training_pipeline property
- supabase_service.py: _request fixes (204, DELETE, better errors)
- models.py: Pydantic models para training API
- main.py: 13 nuevos endpoints REST

📝 Scripts & Docs
- train_model.ps1: send messages → flush → train → report
- verify_deployment.ps1: 10 tests post-deployment
- AI_TRAINING_DEPLOYMENT.md: deployment guide completa
- README_TRAINING.md: technical documentation

🎯 Production Ready
- UUID validation en flush (training_bot IDs ignored)
- FK constraints con ON DELETE SET NULL
- Response_source CHECK constraint validation
- Empty string → None conversion para UUIDs
- Per-record error handling en flush
- Auto-retrain non-blocking (asyncio task)
- Realtime enabled en Supabase

Breaking Changes: None
Backward Compatible: Yes

Tested: Local + Railway staging
Ready for: Production deployment
"@

Write-Host "Committing..." -ForegroundColor Yellow
git commit -m $commitMessage

Write-Host ""
Write-Host "✅ Commit creado exitosamente" -ForegroundColor Green
Write-Host ""

# Mostrar última commit
Write-Host "═══════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  ÚLTIMA COMMIT" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

git log -1 --stat

Write-Host ""
Write-Host "═══════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Preguntar si hacer push
$push = Read-Host "¿Hacer push a origin main? (y/n)"

if ($push -eq "y" -or $push -eq "Y") {
    Write-Host ""
    Write-Host "Pushing to origin main..." -ForegroundColor Yellow
    git push origin main
    
    Write-Host ""
    Write-Host "✅ Push completado" -ForegroundColor Green
    Write-Host ""
    Write-Host "Railway auto-deployará en ~2 minutos" -ForegroundColor Cyan
    Write-Host "Netlify auto-deployará en ~1 minuto" -ForegroundColor Cyan
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "Push cancelado. Para hacer push manualmente:" -ForegroundColor Yellow
    Write-Host "  git push origin main" -ForegroundColor Gray
    Write-Host ""
}
