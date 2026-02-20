"""
Merqora AI Training Pipeline - Real ML Training System

This module handles:
1. Dataset management (export, import, clean, split)
2. Model training with scikit-learn (TF-IDF + classifiers)
3. Evaluation with precision/recall/F1 per intent
4. Confusion matrix generation
5. Automatic retraining triggers
6. JSONL export for fine-tuning
7. Metrics tracking and snapshots
8. Dynamic prompt updating with real examples
"""

import json
import uuid
import time
import asyncio
from datetime import datetime, timedelta
from pathlib import Path
from typing import Dict, List, Optional, Tuple, Any
from dataclasses import dataclass, field, asdict
from collections import Counter, defaultdict
import structlog
import pickle
import math
import re

logger = structlog.get_logger()

# Directories
BASE_DIR = Path(__file__).parent
MODEL_DIR = BASE_DIR / "trained_model"
DATASET_DIR = BASE_DIR / "datasets"
REPORTS_DIR = BASE_DIR / "training_reports"

for d in [MODEL_DIR, DATASET_DIR, REPORTS_DIR]:
    d.mkdir(exist_ok=True)


# ═══════════════════════════════════════
# DATA MODELS
# ═══════════════════════════════════════

@dataclass
class TrainingSample:
    """Single training sample"""
    id: str
    user_message: str
    intent: str
    response: str
    confidence_score: int = 0
    category: str = ""
    was_corrected: bool = False
    was_escalated: bool = False
    source: str = "live"  # live, correction, synthetic


@dataclass
class EvaluationResult:
    """Result of model evaluation"""
    accuracy: float
    precision_macro: float
    recall_macro: float
    f1_macro: float
    per_intent_metrics: Dict[str, Dict[str, float]]
    confusion_matrix: Dict[str, Dict[str, int]]
    total_samples: int
    total_intents: int
    misclassified_samples: List[Dict]
    timestamp: str = field(default_factory=lambda: datetime.utcnow().isoformat())


@dataclass 
class TrainingRunResult:
    """Result of a training run"""
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
    timestamp: str = field(default_factory=lambda: datetime.utcnow().isoformat())


@dataclass
class MetricsSnapshot:
    """Periodic metrics snapshot"""
    period_type: str
    total_messages: int = 0
    ai_resolved: int = 0
    escalated: int = 0
    intent_accuracy: float = 0.0
    avg_confidence: float = 0.0
    helpful_rate: float = 0.0
    escalation_rate: float = 0.0
    human_corrections: int = 0
    correction_rate: float = 0.0
    top_misclassified: List[Dict] = field(default_factory=list)
    metrics_by_intent: Dict[str, Dict] = field(default_factory=dict)


# ═══════════════════════════════════════
# TEXT PROCESSING (shared with local_ai_model)
# ═══════════════════════════════════════

STOPWORDS_ES = {
    'de', 'la', 'que', 'el', 'en', 'y', 'a', 'los', 'del', 'se', 'las',
    'por', 'un', 'para', 'con', 'no', 'una', 'su', 'al', 'lo', 'como',
    'mas', 'pero', 'sus', 'le', 'ya', 'o', 'este', 'si', 'porque', 'esta',
    'entre', 'cuando', 'muy', 'sin', 'sobre', 'me', 'hasta', 'hay', 'donde',
    'es', 'son', 'mi', 'tu', 'te', 'ti', 'tus', 'mis', 'nos', 'hola',
    'buenas', 'buenos', 'gracias', 'oye', 'hey',
}


def normalize_text(text: str) -> str:
    """Normalize Spanish text"""
    text = text.lower().strip()
    replacements = {'á': 'a', 'é': 'e', 'í': 'i', 'ó': 'o', 'ú': 'u', 'ü': 'u', 'ñ': 'n'}
    for old, new in replacements.items():
        text = text.replace(old, new)
    return text


def tokenize(text: str, remove_stopwords: bool = True) -> List[str]:
    """Tokenize text into words"""
    text = normalize_text(text)
    tokens = re.findall(r'\b[a-z0-9]+\b', text)
    if remove_stopwords:
        tokens = [t for t in tokens if t not in STOPWORDS_ES and len(t) > 2]
    return tokens


# ═══════════════════════════════════════
# DATASET MANAGER
# ═══════════════════════════════════════

class DatasetManager:
    """Manages training datasets: export, import, split, clean"""
    
    def __init__(self):
        self.samples: List[TrainingSample] = []
    
    def add_sample(self, sample: TrainingSample):
        """Add a training sample"""
        self.samples.append(sample)
    
    def add_samples_from_supabase(self, rows: List[Dict]):
        """Convert Supabase rows to training samples"""
        for row in rows:
            intent = row.get("intent") or row.get("correct_intent") or row.get("detected_intent")
            if not intent or intent == "unknown":
                continue
            
            sample = TrainingSample(
                id=str(row.get("id", uuid.uuid4())),
                user_message=row.get("user_message", ""),
                intent=intent,
                response=row.get("response") or row.get("correct_response") or row.get("ai_response") or "",
                confidence_score=row.get("confidence_score", 0) or 0,
                category=row.get("category", "") or "",
                was_corrected=row.get("was_corrected", False) or False,
                was_escalated=row.get("was_escalated", False) or False,
                source="correction" if row.get("was_corrected") else "live"
            )
            
            if sample.user_message.strip():
                self.samples.append(sample)
    
    def clean_dataset(self) -> int:
        """Remove duplicates and empty samples, returns count removed"""
        original_count = len(self.samples)
        
        # Remove empty messages
        self.samples = [s for s in self.samples if s.user_message.strip() and s.intent.strip()]
        
        # Remove exact duplicates (by normalized message + intent)
        seen = set()
        unique = []
        for s in self.samples:
            key = (normalize_text(s.user_message), s.intent)
            if key not in seen:
                seen.add(key)
                unique.append(s)
        self.samples = unique
        
        removed = original_count - len(self.samples)
        logger.info("dataset_cleaned", original=original_count, removed=removed, remaining=len(self.samples))
        return removed
    
    def split_dataset(self, train_ratio: float = 0.8, val_ratio: float = 0.1) -> Tuple[List[TrainingSample], List[TrainingSample], List[TrainingSample]]:
        """Split into train/validation/test sets with stratification"""
        import random
        random.seed(42)
        
        # Group by intent for stratified split
        by_intent: Dict[str, List[TrainingSample]] = defaultdict(list)
        for s in self.samples:
            by_intent[s.intent].append(s)
        
        train, val, test = [], [], []
        
        for intent, samples in by_intent.items():
            random.shuffle(samples)
            n = len(samples)
            n_train = max(1, int(n * train_ratio))
            n_val = max(0, int(n * val_ratio))
            
            train.extend(samples[:n_train])
            val.extend(samples[n_train:n_train + n_val])
            test.extend(samples[n_train + n_val:])
        
        random.shuffle(train)
        random.shuffle(val)
        random.shuffle(test)
        
        logger.info("dataset_split", train=len(train), val=len(val), test=len(test))
        return train, val, test
    
    def export_jsonl(self, filepath: Optional[str] = None) -> str:
        """Export dataset as JSONL for fine-tuning"""
        if not filepath:
            timestamp = datetime.utcnow().strftime("%Y%m%d_%H%M%S")
            filepath = str(DATASET_DIR / f"training_data_{timestamp}.jsonl")
        
        with open(filepath, 'w', encoding='utf-8') as f:
            for sample in self.samples:
                record = {
                    "messages": [
                        {"role": "user", "content": sample.user_message},
                        {"role": "assistant", "content": sample.response}
                    ],
                    "intent": sample.intent,
                    "category": sample.category,
                    "confidence": sample.confidence_score,
                    "was_corrected": sample.was_corrected,
                }
                f.write(json.dumps(record, ensure_ascii=False) + "\n")
        
        logger.info("dataset_exported_jsonl", path=filepath, samples=len(self.samples))
        return filepath
    
    def export_csv(self, filepath: Optional[str] = None) -> str:
        """Export dataset as CSV"""
        if not filepath:
            timestamp = datetime.utcnow().strftime("%Y%m%d_%H%M%S")
            filepath = str(DATASET_DIR / f"training_data_{timestamp}.csv")
        
        import csv
        with open(filepath, 'w', encoding='utf-8', newline='') as f:
            writer = csv.writer(f)
            writer.writerow(["user_message", "intent", "response", "category", "confidence", "was_corrected", "was_escalated"])
            for s in self.samples:
                writer.writerow([s.user_message, s.intent, s.response, s.category, s.confidence_score, s.was_corrected, s.was_escalated])
        
        logger.info("dataset_exported_csv", path=filepath, samples=len(self.samples))
        return filepath
    
    def import_jsonl(self, filepath: str) -> int:
        """Import dataset from JSONL"""
        count = 0
        with open(filepath, 'r', encoding='utf-8') as f:
            for line in f:
                record = json.loads(line.strip())
                messages = record.get("messages", [])
                user_msg = next((m["content"] for m in messages if m["role"] == "user"), "")
                assistant_msg = next((m["content"] for m in messages if m["role"] == "assistant"), "")
                
                sample = TrainingSample(
                    id=str(uuid.uuid4()),
                    user_message=user_msg,
                    intent=record.get("intent", "unknown"),
                    response=assistant_msg,
                    category=record.get("category", ""),
                    confidence_score=record.get("confidence", 0),
                    was_corrected=record.get("was_corrected", False),
                    source="import"
                )
                if sample.user_message.strip() and sample.intent != "unknown":
                    self.samples.append(sample)
                    count += 1
        
        logger.info("dataset_imported_jsonl", path=filepath, imported=count)
        return count
    
    def get_stats(self) -> Dict:
        """Get dataset statistics"""
        intent_counts = Counter(s.intent for s in self.samples)
        category_counts = Counter(s.category for s in self.samples if s.category)
        
        return {
            "total_samples": len(self.samples),
            "unique_intents": len(intent_counts),
            "intent_distribution": dict(intent_counts.most_common(30)),
            "category_distribution": dict(category_counts.most_common(10)),
            "corrected_samples": sum(1 for s in self.samples if s.was_corrected),
            "escalated_samples": sum(1 for s in self.samples if s.was_escalated),
            "avg_confidence": round(sum(s.confidence_score for s in self.samples) / max(len(self.samples), 1), 1),
            "min_samples_per_intent": min(intent_counts.values()) if intent_counts else 0,
            "max_samples_per_intent": max(intent_counts.values()) if intent_counts else 0,
        }


# ═══════════════════════════════════════
# MODEL TRAINER (scikit-learn based)
# ═══════════════════════════════════════

class IntentClassifierTrainer:
    """
    Trains an intent classification model using scikit-learn.
    
    Architecture:
    - TF-IDF vectorization (with n-grams)
    - LinearSVC or SGDClassifier for intent classification
    - Calibrated probabilities for confidence scores
    """
    
    def __init__(self):
        self.vectorizer = None
        self.classifier = None
        self.label_encoder = None
        self.is_trained = False
        self.intent_labels: List[str] = []
        self.training_timestamp: Optional[str] = None
    
    def train(self, train_samples: List[TrainingSample], val_samples: List[TrainingSample] = None) -> EvaluationResult:
        """Train the intent classifier"""
        from sklearn.feature_extraction.text import TfidfVectorizer
        from sklearn.svm import LinearSVC
        from sklearn.calibration import CalibratedClassifierCV
        from sklearn.preprocessing import LabelEncoder
        from sklearn.metrics import classification_report, confusion_matrix, accuracy_score
        
        if len(train_samples) < 5:
            raise ValueError(f"Need at least 5 training samples, got {len(train_samples)}")
        
        # Prepare data
        train_texts = [normalize_text(s.user_message) for s in train_samples]
        train_labels = [s.intent for s in train_samples]
        
        # Give more weight to human-corrected samples
        sample_weights = []
        for s in train_samples:
            weight = 1.0
            if s.was_corrected:
                weight = 2.0  # Corrected samples are 2x more valuable
            if s.confidence_score >= 80:
                weight *= 1.2  # High confidence = slightly more weight
            sample_weights.append(weight)
        
        # Label encoding
        self.label_encoder = LabelEncoder()
        train_y = self.label_encoder.fit_transform(train_labels)
        self.intent_labels = list(self.label_encoder.classes_)
        
        # TF-IDF with Spanish-optimized params
        self.vectorizer = TfidfVectorizer(
            analyzer='word',
            ngram_range=(1, 3),  # Unigrams, bigrams, trigrams
            max_features=10000,
            min_df=1,
            max_df=0.95,
            sublinear_tf=True,
            strip_accents='unicode',
        )
        
        train_X = self.vectorizer.fit_transform(train_texts)
        
        # Train LinearSVC with calibration for probability estimates
        base_clf = LinearSVC(
            C=1.0,
            class_weight='balanced',  # Handle imbalanced intents
            max_iter=5000,
            random_state=42
        )
        
        # Use CalibratedClassifierCV for probability estimates
        n_classes = len(set(train_labels))
        if n_classes > 1 and len(train_samples) >= 10:
            cv_folds = min(5, min(Counter(train_labels).values()))
            cv_folds = max(2, cv_folds)
            try:
                self.classifier = CalibratedClassifierCV(base_clf, cv=cv_folds, method='sigmoid')
                self.classifier.fit(train_X, train_y, sample_weight=sample_weights)
            except Exception:
                # Fallback: train without calibration
                base_clf.fit(train_X, train_y, sample_weight=sample_weights)
                self.classifier = CalibratedClassifierCV(base_clf, cv='prefit', method='sigmoid')
                try:
                    self.classifier.fit(train_X, train_y)
                except Exception:
                    self.classifier = base_clf
                    self.classifier.fit(train_X, train_y, sample_weight=sample_weights)
        else:
            base_clf.fit(train_X, train_y, sample_weight=sample_weights)
            self.classifier = base_clf
        
        self.is_trained = True
        self.training_timestamp = datetime.utcnow().isoformat()
        
        # Evaluate on validation set
        eval_samples = val_samples if val_samples else train_samples
        eval_result = self.evaluate(eval_samples)
        
        logger.info(
            "model_trained",
            samples=len(train_samples),
            intents=n_classes,
            accuracy=eval_result.accuracy,
            f1=eval_result.f1_macro
        )
        
        return eval_result
    
    def evaluate(self, test_samples: List[TrainingSample]) -> EvaluationResult:
        """Evaluate the model on test samples"""
        from sklearn.metrics import accuracy_score, precision_recall_fscore_support, confusion_matrix
        
        if not self.is_trained:
            raise ValueError("Model not trained yet")
        
        texts = [normalize_text(s.user_message) for s in test_samples]
        true_labels = [s.intent for s in test_samples]
        
        X = self.vectorizer.transform(texts)
        pred_y = self.classifier.predict(X)
        pred_labels = self.label_encoder.inverse_transform(pred_y)
        
        # Overall metrics
        accuracy = accuracy_score(true_labels, pred_labels)
        
        # Filter to only labels present in both true and pred
        unique_labels = sorted(set(true_labels) | set(pred_labels))
        precision, recall, f1, support = precision_recall_fscore_support(
            true_labels, pred_labels, labels=unique_labels, average=None, zero_division=0
        )
        
        precision_macro = float(sum(precision) / max(len(precision), 1))
        recall_macro = float(sum(recall) / max(len(recall), 1))
        f1_macro = float(sum(f1) / max(len(f1), 1))
        
        # Per-intent metrics
        per_intent = {}
        for i, label in enumerate(unique_labels):
            per_intent[label] = {
                "precision": round(float(precision[i]), 3),
                "recall": round(float(recall[i]), 3),
                "f1": round(float(f1[i]), 3),
                "support": int(support[i]) if i < len(support) else 0,
            }
        
        # Confusion matrix
        cm = confusion_matrix(true_labels, pred_labels, labels=unique_labels)
        confusion = {}
        for i, actual in enumerate(unique_labels):
            confusion[actual] = {}
            for j, predicted in enumerate(unique_labels):
                if cm[i][j] > 0:
                    confusion[actual][predicted] = int(cm[i][j])
        
        # Misclassified samples
        misclassified = []
        for sample, pred in zip(test_samples, pred_labels):
            if sample.intent != pred:
                misclassified.append({
                    "message": sample.user_message[:100],
                    "true_intent": sample.intent,
                    "predicted_intent": pred,
                    "confidence": sample.confidence_score,
                })
        
        return EvaluationResult(
            accuracy=round(float(accuracy), 4),
            precision_macro=round(precision_macro, 4),
            recall_macro=round(recall_macro, 4),
            f1_macro=round(f1_macro, 4),
            per_intent_metrics=per_intent,
            confusion_matrix=confusion,
            total_samples=len(test_samples),
            total_intents=len(unique_labels),
            misclassified_samples=misclassified[:50],
        )
    
    def predict(self, text: str) -> Tuple[str, float, Dict[str, float]]:
        """Predict intent with confidence scores"""
        if not self.is_trained:
            return "unknown", 0.0, {}
        
        X = self.vectorizer.transform([normalize_text(text)])
        
        # Get predicted label
        pred_y = self.classifier.predict(X)[0]
        pred_label = self.label_encoder.inverse_transform([pred_y])[0]
        
        # Get probability estimates if available
        top_intents = {}
        confidence = 0.5
        
        if hasattr(self.classifier, 'predict_proba'):
            try:
                proba = self.classifier.predict_proba(X)[0]
                # Get top 5 intents by probability
                top_indices = proba.argsort()[-5:][::-1]
                for idx in top_indices:
                    label = self.label_encoder.inverse_transform([idx])[0]
                    top_intents[label] = round(float(proba[idx]), 4)
                confidence = float(proba[pred_y])
            except Exception:
                confidence = 0.7  # Default if probabilities unavailable
        elif hasattr(self.classifier, 'decision_function'):
            try:
                decision = self.classifier.decision_function(X)[0]
                if hasattr(decision, '__len__'):
                    # Multi-class: sigmoid of max decision value
                    confidence = 1.0 / (1.0 + math.exp(-max(decision)))
                else:
                    confidence = 1.0 / (1.0 + math.exp(-float(decision)))
            except Exception:
                confidence = 0.5
        
        return pred_label, round(confidence, 4), top_intents
    
    def save(self, path: Optional[str] = None) -> str:
        """Save trained model"""
        if not path:
            timestamp = datetime.utcnow().strftime("%Y%m%d_%H%M%S")
            path = str(MODEL_DIR / f"intent_classifier_{timestamp}.pkl")
        
        model_data = {
            'vectorizer': self.vectorizer,
            'classifier': self.classifier,
            'label_encoder': self.label_encoder,
            'intent_labels': self.intent_labels,
            'training_timestamp': self.training_timestamp,
            'is_trained': self.is_trained,
        }
        
        with open(path, 'wb') as f:
            pickle.dump(model_data, f)
        
        # Also save as "latest"
        latest_path = str(MODEL_DIR / "intent_classifier_latest.pkl")
        with open(latest_path, 'wb') as f:
            pickle.dump(model_data, f)
        
        logger.info("model_saved", path=path)
        return path
    
    def load(self, path: Optional[str] = None) -> bool:
        """Load trained model"""
        if not path:
            path = str(MODEL_DIR / "intent_classifier_latest.pkl")
        
        if not Path(path).exists():
            return False
        
        try:
            with open(path, 'rb') as f:
                model_data = pickle.load(f)
            
            self.vectorizer = model_data['vectorizer']
            self.classifier = model_data['classifier']
            self.label_encoder = model_data['label_encoder']
            self.intent_labels = model_data['intent_labels']
            self.training_timestamp = model_data.get('training_timestamp')
            self.is_trained = model_data.get('is_trained', True)
            
            logger.info("model_loaded", path=path, intents=len(self.intent_labels))
            return True
        except Exception as e:
            logger.error("model_load_error", error=str(e))
            return False


# ═══════════════════════════════════════
# TRAINING PIPELINE
# ═══════════════════════════════════════

class TrainingPipeline:
    """
    Complete training pipeline that orchestrates:
    1. Data fetching from Supabase
    2. Dataset preparation and cleaning
    3. Model training with train/val/test split
    4. Evaluation and metrics
    5. Model deployment (swap active model)
    6. Report generation
    7. Metrics snapshots
    """
    
    def __init__(self, supabase_service=None):
        self.supabase = supabase_service
        self.dataset_manager = DatasetManager()
        self.trainer = IntentClassifierTrainer()
        self.active_model = IntentClassifierTrainer()
        
        # Try to load existing model
        self.active_model.load()
        
        # In-memory training data buffer (for real-time collection)
        self._training_buffer: List[Dict] = []
        self._buffer_max_size = 500
        
        # Retraining config
        self.min_samples_for_training = 20
        self.auto_retrain_threshold = 100  # Retrain every N new samples
        self._samples_since_last_train = 0
        
        # Metrics tracking
        self._interaction_log: List[Dict] = []
        self._metrics_window: List[Dict] = []
    
    # ─── Data Collection ───
    
    def record_interaction(
        self,
        user_message: str,
        detected_intent: str,
        ai_response: str,
        confidence_score: int,
        category: str = "",
        escalated: bool = False,
        escalation_reason: str = "",
        response_source: str = "unknown",
        response_time_ms: int = 0,
        conversation_id: str = "",
        message_id: str = "",
        user_id: str = "",
        session_id: str = "",
        matched_keywords: List[str] = None,
        clarity_score: float = 0.0,
        completeness_score: float = 0.0,
    ) -> Dict:
        """Record an interaction for training. Called after every AI response."""
        record = {
            "id": str(uuid.uuid4()),
            "user_message": user_message,
            "detected_intent": detected_intent,
            "ai_response": ai_response,
            "confidence_score": confidence_score,
            "category": category,
            "escalated": escalated,
            "escalation_reason": escalation_reason,
            "response_source": response_source,
            "response_time_ms": response_time_ms,
            "conversation_id": conversation_id,
            "message_id": message_id,
            "user_id": user_id,
            "session_id": session_id,
            "matched_keywords": matched_keywords or [],
            "clarity_score": clarity_score,
            "completeness_score": completeness_score,
            "timestamp": datetime.utcnow().isoformat(),
        }
        
        self._training_buffer.append(record)
        self._interaction_log.append(record)
        self._samples_since_last_train += 1
        
        # Keep interaction log bounded
        if len(self._interaction_log) > 10000:
            self._interaction_log = self._interaction_log[-5000:]
        
        # Prune buffer if too large
        if len(self._training_buffer) > self._buffer_max_size:
            self._training_buffer = self._training_buffer[-self._buffer_max_size:]
        
        return record
    
    async def flush_buffer_to_supabase(self) -> int:
        """Flush training buffer to Supabase ai_training_data table"""
        if not self.supabase or not self.supabase.is_configured:
            return 0
        
        if not self._training_buffer:
            return 0
        
        saved = 0
        batch = self._training_buffer.copy()
        self._training_buffer.clear()
        
        for record in batch:
            try:
                # Helper: convert empty/invalid strings to None for UUID FK fields
                def _uuid_or_none(val):
                    if not val or (isinstance(val, str) and not val.strip()):
                        return None
                    # Validate UUID format
                    try:
                        uuid.UUID(str(val))
                        return str(val)
                    except (ValueError, AttributeError):
                        return None
                
                # Validate response_source against allowed CHECK values
                valid_sources = {'faq', 'llm', 'local_model', 'cache', 'agent_learning', 'reasoning', 'escalation'}
                raw_source = record.get("response_source", "")
                response_source = raw_source if raw_source in valid_sources else None
                
                data = {
                    "user_message": record["user_message"],
                    "detected_intent": record["detected_intent"],
                    "ai_response": record["ai_response"],
                    "confidence_score": record["confidence_score"],
                    "category": record.get("category") or None,
                    "escalated": record.get("escalated", False),
                    "escalation_reason": record.get("escalation_reason") or None,
                    "response_source": response_source,
                    "response_time_ms": record.get("response_time_ms", 0),
                    "session_id": record.get("session_id") or None,
                    "matched_keywords": record.get("matched_keywords", []),
                    "clarity_score": record.get("clarity_score", 0),
                    "completeness_score": record.get("completeness_score", 0),
                    "dataset_status": "pending",
                }
                
                # Only include FK UUID fields if they have real values
                conv_id = _uuid_or_none(record.get("conversation_id"))
                msg_id = _uuid_or_none(record.get("message_id"))
                user_id = _uuid_or_none(record.get("user_id"))
                
                if conv_id:
                    data["conversation_id"] = conv_id
                if msg_id:
                    data["message_id"] = msg_id
                if user_id:
                    data["user_id"] = user_id
                
                result = await self.supabase._request("POST", "ai_training_data", data)
                if result:
                    saved += 1
            except Exception as e:
                logger.warn("flush_record_error", error=str(e))
        
        logger.info("buffer_flushed", saved=saved, total=len(batch))
        return saved
    
    # ─── Dataset Building ───
    
    async def fetch_training_dataset(self, min_confidence: int = 0, only_reviewed: bool = False, limit: int = 10000) -> int:
        """Fetch training data from Supabase"""
        if not self.supabase or not self.supabase.is_configured:
            logger.warn("supabase_not_configured_for_fetch")
            return 0
        
        try:
            # Use RPC function
            result = await self.supabase._request(
                "POST",
                "rpc/get_training_dataset",
                {
                    "p_min_confidence": min_confidence,
                    "p_only_reviewed": only_reviewed,
                    "p_limit": limit
                }
            )
            
            if result:
                self.dataset_manager.add_samples_from_supabase(result)
                logger.info("dataset_fetched", samples=len(result))
                return len(result)
        except Exception as e:
            logger.error("dataset_fetch_error", error=str(e))
        
        return 0
    
    def build_dataset_from_buffer(self) -> int:
        """Build dataset from in-memory buffer (no Supabase needed)"""
        count = 0
        for record in self._interaction_log:
            intent = record.get("detected_intent")
            if not intent or intent == "unknown":
                continue
            
            sample = TrainingSample(
                id=record.get("id", str(uuid.uuid4())),
                user_message=record["user_message"],
                intent=intent,
                response=record.get("ai_response", ""),
                confidence_score=record.get("confidence_score", 0),
                category=record.get("category", ""),
                was_escalated=record.get("escalated", False),
                source="live"
            )
            self.dataset_manager.add_sample(sample)
            count += 1
        
        logger.info("dataset_from_buffer", count=count)
        return count
    
    # ─── Training ───
    
    async def run_training(
        self,
        run_name: str = None,
        run_type: str = "full",
        min_confidence: int = 0,
        only_reviewed: bool = False,
        triggered_by: str = None,
    ) -> TrainingRunResult:
        """
        Run a full training pipeline:
        1. Fetch data
        2. Clean & split
        3. Train model
        4. Evaluate
        5. Compare with baseline
        6. Save if improved
        7. Generate report
        """
        start_time = time.time()
        run_id = str(uuid.uuid4())
        
        if not run_name:
            run_name = f"training_{datetime.utcnow().strftime('%Y%m%d_%H%M%S')}"
        
        logger.info("training_run_started", run_id=run_id, run_name=run_name, run_type=run_type)
        
        # 1. Fetch data from Supabase
        self.dataset_manager = DatasetManager()  # Fresh dataset
        fetched = await self.fetch_training_dataset(
            min_confidence=min_confidence,
            only_reviewed=only_reviewed
        )
        
        # Also add from buffer if not enough
        if len(self.dataset_manager.samples) < self.min_samples_for_training:
            self.build_dataset_from_buffer()
        
        if len(self.dataset_manager.samples) < self.min_samples_for_training:
            return TrainingRunResult(
                run_id=run_id,
                status="failed",
                training_samples=0,
                validation_samples=0,
                test_samples=0,
                total_intents=0,
                baseline_metrics=None,
                final_metrics=None,
                improvement={},
                model_path="",
                dataset_path="",
                report_path="",
                duration_seconds=time.time() - start_time,
            )
        
        # 2. Clean dataset
        self.dataset_manager.clean_dataset()
        
        # 3. Split dataset
        train_set, val_set, test_set = self.dataset_manager.split_dataset()
        
        # 4. Get baseline metrics (current model)
        baseline_metrics = None
        if self.active_model.is_trained and test_set:
            try:
                baseline_metrics = self.active_model.evaluate(test_set)
            except Exception as e:
                logger.warn("baseline_eval_error", error=str(e))
        
        # 5. Train new model
        new_trainer = IntentClassifierTrainer()
        try:
            eval_result = new_trainer.train(train_set, val_set if val_set else None)
        except Exception as e:
            logger.error("training_failed", error=str(e))
            return TrainingRunResult(
                run_id=run_id,
                status="failed",
                training_samples=len(train_set),
                validation_samples=len(val_set),
                test_samples=len(test_set),
                total_intents=len(set(s.intent for s in train_set)),
                baseline_metrics=baseline_metrics,
                final_metrics=None,
                improvement={},
                model_path="",
                dataset_path="",
                report_path="",
                duration_seconds=time.time() - start_time,
            )
        
        # 6. Evaluate on test set
        final_metrics = None
        if test_set:
            try:
                final_metrics = new_trainer.evaluate(test_set)
            except Exception:
                final_metrics = eval_result
        else:
            final_metrics = eval_result
        
        # 7. Calculate improvement
        improvement = {}
        if baseline_metrics and final_metrics:
            improvement = {
                "accuracy": round(final_metrics.accuracy - baseline_metrics.accuracy, 4),
                "f1": round(final_metrics.f1_macro - baseline_metrics.f1_macro, 4),
                "precision": round(final_metrics.precision_macro - baseline_metrics.precision_macro, 4),
                "recall": round(final_metrics.recall_macro - baseline_metrics.recall_macro, 4),
            }
        
        # 8. Save model (deploy if improved or first training)
        should_deploy = True
        if baseline_metrics and final_metrics:
            # Only deploy if not significantly worse
            should_deploy = final_metrics.f1_macro >= (baseline_metrics.f1_macro - 0.05)
        
        model_path = ""
        if should_deploy:
            model_path = new_trainer.save()
            self.active_model = new_trainer
            self._samples_since_last_train = 0
            logger.info("new_model_deployed", accuracy=final_metrics.accuracy if final_metrics else 0)
        else:
            logger.warn("model_not_deployed", reason="performance_regression")
        
        # 9. Export dataset
        dataset_path = self.dataset_manager.export_jsonl()
        
        # 10. Generate report
        report_path = self._generate_report(run_id, run_name, final_metrics, baseline_metrics, improvement, train_set, val_set, test_set)
        
        # 11. Save run info to Supabase
        duration = time.time() - start_time
        
        if self.supabase and self.supabase.is_configured:
            try:
                await self.supabase._request("POST", "ai_training_runs", {
                    "id": run_id,
                    "run_name": run_name,
                    "run_type": run_type,
                    "status": "completed",
                    "training_samples": len(train_set),
                    "validation_samples": len(val_set),
                    "test_samples": len(test_set),
                    "total_intents": len(set(s.intent for s in train_set)),
                    "baseline_intent_accuracy": baseline_metrics.accuracy if baseline_metrics else None,
                    "baseline_escalation_rate": None,
                    "baseline_avg_confidence": None,
                    "intent_accuracy": final_metrics.accuracy if final_metrics else None,
                    "intent_precision": final_metrics.precision_macro if final_metrics else None,
                    "intent_recall": final_metrics.recall_macro if final_metrics else None,
                    "intent_f1_score": final_metrics.f1_macro if final_metrics else None,
                    "per_intent_metrics": final_metrics.per_intent_metrics if final_metrics else None,
                    "confusion_matrix": final_metrics.confusion_matrix if final_metrics else None,
                    "model_config": {
                        "type": "TF-IDF + LinearSVC",
                        "ngram_range": [1, 3],
                        "max_features": 10000,
                        "class_weight": "balanced",
                    },
                    "model_file_path": model_path,
                    "dataset_file_path": dataset_path,
                    "report_file_path": report_path,
                    "started_at": datetime.utcnow().isoformat(),
                    "completed_at": datetime.utcnow().isoformat(),
                    "triggered_by": triggered_by,
                })
            except Exception as e:
                logger.warn("save_training_run_error", error=str(e))
        
        result = TrainingRunResult(
            run_id=run_id,
            status="completed",
            training_samples=len(train_set),
            validation_samples=len(val_set),
            test_samples=len(test_set),
            total_intents=len(set(s.intent for s in train_set)),
            baseline_metrics=baseline_metrics,
            final_metrics=final_metrics,
            improvement=improvement,
            model_path=model_path,
            dataset_path=dataset_path,
            report_path=report_path,
            duration_seconds=round(duration, 2),
        )
        
        logger.info(
            "training_run_completed",
            run_id=run_id,
            accuracy=final_metrics.accuracy if final_metrics else 0,
            f1=final_metrics.f1_macro if final_metrics else 0,
            duration_s=round(duration, 2)
        )
        
        return result
    
    def _generate_report(
        self, run_id: str, run_name: str,
        final_metrics: Optional[EvaluationResult],
        baseline_metrics: Optional[EvaluationResult],
        improvement: Dict,
        train_set: List, val_set: List, test_set: List
    ) -> str:
        """Generate a human-readable training report"""
        report_path = str(REPORTS_DIR / f"report_{run_name}.json")
        
        report = {
            "run_id": run_id,
            "run_name": run_name,
            "timestamp": datetime.utcnow().isoformat(),
            "dataset": {
                "train": len(train_set),
                "validation": len(val_set),
                "test": len(test_set),
                "total": len(train_set) + len(val_set) + len(test_set),
            },
            "baseline": asdict(baseline_metrics) if baseline_metrics else None,
            "final": asdict(final_metrics) if final_metrics else None,
            "improvement": improvement,
        }
        
        with open(report_path, 'w', encoding='utf-8') as f:
            json.dump(report, f, ensure_ascii=False, indent=2)
        
        return report_path
    
    # ─── Prediction (using active model) ───
    
    def predict_intent(self, text: str) -> Tuple[str, float, Dict[str, float]]:
        """Predict intent using the active trained model"""
        if not self.active_model.is_trained:
            return "unknown", 0.0, {}
        return self.active_model.predict(text)
    
    # ─── Metrics ───
    
    def get_live_metrics(self, hours: int = 24) -> Dict:
        """Get live metrics from in-memory interaction log"""
        cutoff = datetime.utcnow() - timedelta(hours=hours)
        
        recent = [
            r for r in self._interaction_log
            if datetime.fromisoformat(r.get("timestamp", "2000-01-01")) > cutoff
        ]
        
        if not recent:
            return {
                "period_hours": hours,
                "total_messages": 0,
                "ai_resolved": 0,
                "escalated": 0,
                "avg_confidence": 0,
                "escalation_rate": 0,
                "intent_distribution": {},
                "confidence_distribution": {},
            }
        
        total = len(recent)
        escalated = sum(1 for r in recent if r.get("escalated"))
        confidences = [r.get("confidence_score", 0) for r in recent]
        intents = Counter(r.get("detected_intent", "unknown") for r in recent)
        
        # Confidence distribution buckets
        conf_buckets = {"0-20": 0, "21-40": 0, "41-60": 0, "61-80": 0, "81-100": 0}
        for c in confidences:
            if c <= 20: conf_buckets["0-20"] += 1
            elif c <= 40: conf_buckets["21-40"] += 1
            elif c <= 60: conf_buckets["41-60"] += 1
            elif c <= 80: conf_buckets["61-80"] += 1
            else: conf_buckets["81-100"] += 1
        
        return {
            "period_hours": hours,
            "total_messages": total,
            "ai_resolved": total - escalated,
            "escalated": escalated,
            "avg_confidence": round(sum(confidences) / max(total, 1), 1),
            "escalation_rate": round(escalated / max(total, 1) * 100, 1),
            "intent_distribution": dict(intents.most_common(20)),
            "confidence_distribution": conf_buckets,
            "samples_since_last_train": self._samples_since_last_train,
            "model_trained": self.active_model.is_trained,
            "model_intents": len(self.active_model.intent_labels) if self.active_model.is_trained else 0,
            "training_buffer_size": len(self._training_buffer),
        }
    
    async def create_metrics_snapshot(self, period_type: str = "daily") -> Optional[str]:
        """Create a metrics snapshot in Supabase"""
        if not self.supabase or not self.supabase.is_configured:
            return None
        
        try:
            result = await self.supabase._request(
                "POST", "rpc/create_metrics_snapshot",
                {"p_period_type": period_type}
            )
            if result:
                logger.info("metrics_snapshot_created", period=period_type)
                return str(result)
        except Exception as e:
            logger.warn("metrics_snapshot_error", error=str(e))
        
        return None
    
    # ─── Auto-retraining ───
    
    def should_retrain(self) -> bool:
        """Check if auto-retraining should be triggered"""
        return self._samples_since_last_train >= self.auto_retrain_threshold
    
    # ─── Dynamic Prompt Updates ───
    
    def get_few_shot_examples(self, n_per_intent: int = 2) -> List[Dict]:
        """
        Get high-quality examples from training data for few-shot prompting.
        Prioritizes human-corrected examples.
        """
        examples_by_intent: Dict[str, List[Dict]] = defaultdict(list)
        
        for sample in self.dataset_manager.samples:
            if sample.was_corrected or sample.confidence_score >= 80:
                examples_by_intent[sample.intent].append({
                    "user": sample.user_message,
                    "intent": sample.intent,
                    "response": sample.response[:300],
                })
        
        # Select top N per intent
        selected = []
        for intent, examples in examples_by_intent.items():
            # Prioritize corrected examples
            corrected = [e for e in examples if any(
                s.was_corrected for s in self.dataset_manager.samples 
                if s.user_message == e["user"]
            )]
            remaining = [e for e in examples if e not in corrected]
            
            chosen = corrected[:n_per_intent]
            if len(chosen) < n_per_intent:
                chosen.extend(remaining[:n_per_intent - len(chosen)])
            
            selected.extend(chosen)
        
        return selected
    
    def get_stats(self) -> Dict:
        """Get comprehensive pipeline stats"""
        return {
            "model_trained": self.active_model.is_trained,
            "model_intents": len(self.active_model.intent_labels) if self.active_model.is_trained else 0,
            "model_timestamp": self.active_model.training_timestamp,
            "training_buffer_size": len(self._training_buffer),
            "interaction_log_size": len(self._interaction_log),
            "samples_since_last_train": self._samples_since_last_train,
            "auto_retrain_threshold": self.auto_retrain_threshold,
            "should_retrain": self.should_retrain(),
            "dataset_stats": self.dataset_manager.get_stats() if self.dataset_manager.samples else {},
        }


# ═══════════════════════════════════════
# SINGLETON
# ═══════════════════════════════════════

_pipeline: Optional[TrainingPipeline] = None


def get_training_pipeline(supabase_service=None) -> TrainingPipeline:
    """Get singleton training pipeline"""
    global _pipeline
    if _pipeline is None:
        _pipeline = TrainingPipeline(supabase_service)
    elif supabase_service and not _pipeline.supabase:
        _pipeline.supabase = supabase_service
    return _pipeline
