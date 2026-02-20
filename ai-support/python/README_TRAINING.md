# 🧠 AI Training Pipeline - Technical Documentation

## Architecture Overview

```
┌─────────────────┐
│  User Message   │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────────┐
│         AIOrchestrator                      │
│  ┌──────────────────────────────────────┐  │
│  │ 1. Security validation (Rust)        │  │
│  │ 2. Cache lookup (LearningEngine)     │  │
│  │ 3. Analysis (C++ scoring)            │  │
│  │ 4. FAQ matching (KnowledgeBase)      │  │
│  │ 5. LLM response (LLMService)         │  │
│  │ 6. Save to Supabase                  │  │
│  └──────────────────────────────────────┘  │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│       TrainingPipeline.record_interaction   │
│  • Adds to _training_buffer (in-memory)     │
│  • Increments _samples_since_last_train     │
│  • Adds to _interaction_log (metrics)       │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
         ┌────────┴────────┐
         │                 │
         ▼                 ▼
┌─────────────────┐  ┌──────────────────┐
│ Buffer >= 20?   │  │ Samples >= 100?  │
│ → Flush to DB   │  │ → Auto-retrain   │
└─────────────────┘  └──────────────────┘
         │                 │
         ▼                 ▼
┌─────────────────┐  ┌──────────────────────────┐
│ ai_training_data│  │ TrainingPipeline.run()   │
│ (Supabase)      │  │ 1. Fetch dataset         │
└─────────────────┘  │ 2. Clean & split         │
                     │ 3. Train TF-IDF + SVC    │
                     │ 4. Evaluate (F1, acc)    │
                     │ 5. Deploy if improved    │
                     │ 6. Save to ai_training_  │
                     │    runs                  │
                     └──────────────────────────┘
```

## Core Components

### 1. DatasetManager
**File:** `training_pipeline.py:146-318`

**Responsibilities:**
- Store training samples in memory
- Export to JSONL/CSV formats
- Clean dataset (remove duplicates, invalid samples)
- Stratified train/val/test split (70/15/15)
- Import external datasets

**Key Methods:**
```python
add_sample(sample: TrainingSample)
clean_dataset() -> int
split_dataset(train=0.7, val=0.15, test=0.15) -> Tuple[List, List, List]
export_jsonl() -> str
export_csv() -> str
get_stats() -> Dict
```

### 2. IntentClassifierTrainer
**File:** `training_pipeline.py:320-581`

**ML Stack:**
- **Vectorizer:** TF-IDF (ngram_range=(1,3), max_features=10000)
- **Classifier:** LinearSVC (C=1.0, class_weight='balanced')
- **Calibration:** CalibratedClassifierCV (for probability estimates)
- **Weighting:** Human-corrected samples weighted 2x

**Key Methods:**
```python
train(train_samples, val_samples) -> EvaluationResult
evaluate(test_samples) -> EvaluationResult
predict(text: str) -> Tuple[intent, confidence, top_intents]
save(path) -> str
load(path) -> bool
```

**Evaluation Metrics:**
- Accuracy (overall correctness)
- Precision/Recall/F1 (macro-averaged)
- Per-intent metrics (precision, recall, F1, support)
- Confusion matrix
- Misclassified samples list

### 3. TrainingPipeline
**File:** `training_pipeline.py:587-1176`

**Orchestrates:**
- Data collection (buffer + Supabase)
- Dataset building and cleaning
- Model training with baseline comparison
- Model deployment (only if improved)
- Metrics tracking and snapshots
- Auto-retraining triggers

**Key Methods:**
```python
record_interaction(**kwargs) -> Dict  # Called by orchestrator
flush_buffer_to_supabase() -> int
fetch_training_dataset(min_confidence, only_reviewed) -> int
run_training(run_name, run_type, ...) -> TrainingRunResult
predict_intent(text) -> Tuple[intent, confidence, top_intents]
should_retrain() -> bool
get_live_metrics(hours=24) -> Dict
get_stats() -> Dict
```

**Configuration:**
- `min_samples_for_training = 20`
- `auto_retrain_threshold = 100`
- `_buffer_max_size = 500`

## Data Models

### TrainingSample
```python
@dataclass
class TrainingSample:
    user_message: str
    intent: str
    response: str
    confidence_score: int
    category: str = ""
    was_escalated: bool = False
    was_corrected: bool = False
    source: str = "live"  # live, imported, synthetic
```

### EvaluationResult
```python
@dataclass
class EvaluationResult:
    accuracy: float
    precision_macro: float
    recall_macro: float
    f1_macro: float
    per_intent_metrics: Dict[str, Dict[str, float]]
    confusion_matrix: Dict[str, Dict[str, int]]
    total_samples: int
    total_intents: int
    misclassified_samples: List[Dict]
```

### TrainingRunResult
```python
@dataclass
class TrainingRunResult:
    run_id: str
    status: str  # completed, failed
    training_samples: int
    validation_samples: int
    test_samples: int
    total_intents: int
    baseline_metrics: Optional[EvaluationResult]
    final_metrics: Optional[EvaluationResult]
    improvement: Dict[str, float]
    model_path: str
    dataset_path: str
    report_path: str
    duration_seconds: float
```

## Database Schema

### ai_training_data
Primary table for storing all training samples.

**Key Columns:**
- `user_message TEXT NOT NULL` - Original user query
- `detected_intent TEXT` - AI's predicted intent
- `correct_intent TEXT` - Human-corrected intent (if reviewed)
- `ai_response TEXT` - AI's response
- `correct_response TEXT` - Human-corrected response
- `confidence_score INTEGER` - 0-100
- `corrected_by_human BOOLEAN` - Has been reviewed
- `intent_correct BOOLEAN` - NULL=not reviewed, TRUE=correct, FALSE=error
- `escalated BOOLEAN` - Was escalated to human
- `response_source TEXT` - faq, llm, cache, etc.
- `dataset_status TEXT` - pending, reviewed, approved, rejected, used_in_training

**Foreign Keys:**
- `conversation_id → support_conversations(id)`
- `message_id → support_messages(id)`
- `user_id → usuarios(user_id)`
- `corrected_by → usuarios(user_id)`

### ai_training_runs
Tracks each training run with metrics.

**Key Columns:**
- `run_name TEXT` - e.g., "training_20260220_070000"
- `run_type TEXT` - full, incremental, intent_only
- `status TEXT` - completed, failed, running
- `training_samples INTEGER`
- `validation_samples INTEGER`
- `test_samples INTEGER`
- `total_intents INTEGER`
- `intent_accuracy REAL` - 0.0 to 1.0
- `intent_precision REAL`
- `intent_recall REAL`
- `intent_f1_score REAL`
- `baseline_intent_accuracy REAL` - Previous model's accuracy
- `model_config JSONB` - TF-IDF + SVC params
- `model_file_path TEXT`
- `dataset_file_path TEXT`
- `report_file_path TEXT`

### ai_intent_corrections
Tracks each human correction submitted.

**Columns:**
- `training_data_id UUID` → ai_training_data(id)
- `original_intent TEXT`
- `corrected_intent TEXT`
- `was_mismatch BOOLEAN` - Did intent change?
- `notes TEXT`
- `corrected_by UUID` → usuarios(user_id)

## RPC Functions

### get_ai_training_metrics(p_days)
Returns comprehensive metrics for the dashboard.

**Returns:**
```json
{
  "total_messages": 150,
  "ai_resolved": 135,
  "escalated": 15,
  "escalation_rate": 10.0,
  "avg_confidence": 82.5,
  "intent_accuracy": 89.3,
  "correction_rate": 5.2,
  "top_intents": {"purchase_status": 25, ...},
  "top_misclassified": [...]
}
```

### get_training_dataset(p_min_confidence, p_only_reviewed, p_limit)
Fetches clean training samples for ML training.

**Filters:**
- Confidence >= `p_min_confidence`
- Only human-reviewed if `p_only_reviewed = TRUE`
- Excludes NULL intents
- Orders by `created_at DESC`

**Returns:** Array of training samples with all fields.

### submit_intent_correction(...)
Records a human correction and auto-detects misclassifications.

**Parameters:**
- `p_training_data_id UUID`
- `p_correct_intent TEXT`
- `p_correct_response TEXT`
- `p_should_escalate BOOLEAN`
- `p_notes TEXT`
- `p_corrector_id UUID`

**Side Effects:**
- Updates `ai_training_data` row
- Inserts into `ai_intent_corrections`
- Trigger sets `intent_correct = FALSE` if mismatch detected

### create_metrics_snapshot(p_period_type)
Creates a periodic snapshot for historical tracking.

**Period Types:** `hourly`, `daily`, `weekly`, `monthly`

**Stores:** All current metrics frozen in time.

## API Endpoints

### Training Operations

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/ai/training/run` | POST | Trigger training run |
| `/ai/training/flush` | POST | Flush buffer to DB |
| `/ai/training/predict` | GET | Test intent prediction |

### Data & Corrections

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/ai/training/correction` | POST | Submit human correction |
| `/ai/training/pending-review` | GET | Get samples needing review |
| `/ai/training/errors` | GET | Get classification errors |
| `/ai/training/dataset/export` | GET | Export JSONL/CSV |
| `/ai/training/dataset/stats` | GET | Dataset statistics |

### Metrics & Monitoring

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/ai/training/metrics` | GET | Live metrics (in-memory) |
| `/ai/training/metrics/supabase` | GET | Historical metrics (DB) |
| `/ai/training/runs` | GET | Training run history |
| `/ai/training/intent-performance` | GET | Per-intent accuracy |
| `/ai/training/snapshot` | POST | Create metrics snapshot |

## Training Pipeline Flow

### Phase 1: Data Collection (Real-time)
```
User Message → Orchestrator → Response
                     ↓
         record_interaction()
                     ↓
           _training_buffer[]
                     ↓
          (if buffer >= 20)
                     ↓
        flush_buffer_to_supabase()
                     ↓
           ai_training_data
```

### Phase 2: Human Review (Manual)
```
Admin Dashboard → Review Tab
                     ↓
   (Human reviews low-confidence samples)
                     ↓
     Submit Correction Form
                     ↓
   POST /ai/training/correction
                     ↓
    submit_intent_correction RPC
                     ↓
   Updates ai_training_data
                     ↓
   Trigger auto-detects mismatch
```

### Phase 3: Auto-Retraining (Automatic)
```
_samples_since_last_train >= 100
                     ↓
        should_retrain() = TRUE
                     ↓
     asyncio.create_task(_auto_retrain)
                     ↓
          run_training()
                     ↓
    ┌─────────────────────────┐
    │ 1. Fetch dataset        │
    │ 2. Clean duplicates     │
    │ 3. Split 70/15/15       │
    │ 4. Train TF-IDF + SVC   │
    │ 5. Evaluate baseline    │
    │ 6. Evaluate new model   │
    │ 7. Compare F1 scores    │
    │ 8. Deploy if improved   │
    └─────────────────────────┘
                     ↓
          Model saved to disk
                     ↓
     Record in ai_training_runs
```

## Model Deployment Strategy

**Deployment Rules:**
1. **First training:** Always deploy (no baseline)
2. **Subsequent trainings:** Only deploy if:
   - `new_f1 >= baseline_f1 - 0.05` (max 5% regression allowed)
3. **Failed training:** Keep previous model active

**Rollback:**
- Previous models saved as `intent_classifier_YYYYMMDD_HHMMSS.pkl`
- Manual rollback: Copy backup to `intent_classifier_latest.pkl`

## Monitoring & Alerting

### Critical Metrics
- **Intent Accuracy < 70%** → Immediate review needed
- **Escalation Rate > 30%** → Model not confident enough
- **F1 Score < 0.70** → Retrain with more data
- **Misclassifications > 15%** → Review intents/prompts

### Daily Checks
- Review pending corrections (low confidence samples)
- Check training run status
- Monitor buffer flush (should auto-flush)
- Verify samples_since_last_train incrementing

### Weekly Tasks
- Manual training run (even if not auto-triggered)
- Export dataset backup
- Review per-intent performance
- Add new intents if needed

## Performance Optimization

### Memory Management
- `_training_buffer` max 500 samples
- `_interaction_log` pruned at 10k → 5k
- In-memory metrics window: last 24h only

### Database Performance
- Indices on: `detected_intent`, `correct_intent`, `dataset_status`, `corrected_by_human`
- Partitioning strategy (future): Archive old training_data

### Training Performance
- TF-IDF `max_features=10000` (balance accuracy vs speed)
- Stratified split ensures balanced classes
- Class weights handle imbalanced intents
- CV folds auto-adjusted for small classes

## Extending the System

### Adding New Intents
1. Add to knowledge base FAQ entries
2. Add training examples to PowerShell script
3. Run training to learn new intent
4. Monitor accuracy for new intent

### Custom Retraining Schedule
Modify in `orchestrator.py`:
```python
self.training_pipeline.auto_retrain_threshold = 200  # Default: 100
```

### Adjusting Model Params
Modify in `training_pipeline.py:IntentClassifierTrainer.train()`:
```python
TfidfVectorizer(
    max_features=20000,  # More features
    ngram_range=(1, 4),  # Longer ngrams
)

LinearSVC(
    C=0.5,  # Stronger regularization
)
```

### External Dataset Integration
```python
pipeline = get_training_pipeline()
pipeline.dataset_manager.import_jsonl("path/to/custom_dataset.jsonl")
await pipeline.run_training()
```

## Troubleshooting

### Issue: Training fails with "Not enough samples"
**Cause:** Less than 20 samples in dataset  
**Fix:** Send more test messages or lower `min_samples_for_training`

### Issue: Model accuracy drops after training
**Cause:** New training data has different distribution  
**Fix:** Review new samples for quality, check for duplicates

### Issue: Buffer not flushing automatically
**Cause:** Exception in flush or buffer < 20  
**Fix:** Check logs, manual flush: `POST /ai/training/flush`

### Issue: RPC "function does not exist"
**Cause:** SQL not executed in Supabase  
**Fix:** Run `SUPABASE_AI_TRAINING_PIPELINE.sql` in SQL editor

### Issue: High escalation rate
**Cause:** Model not confident (low data or poor training)  
**Fix:** Add more training samples, review FAQ coverage

---

**Built with:** Python 3.10+, scikit-learn, FastAPI, Supabase, PostgreSQL
