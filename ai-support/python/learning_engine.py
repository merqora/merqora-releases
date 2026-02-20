"""
Merqora AI Learning Engine

This module handles:
1. Response caching - fast responses for similar questions
2. Feedback learning - improve based on user feedback
3. Pattern recognition - learn new question patterns
4. Integration with TrainingPipeline for real ML training
"""

import json
import hashlib
from typing import Dict, List, Optional, Tuple
from datetime import datetime, timedelta
from dataclasses import dataclass, field, asdict
from pathlib import Path
import structlog

logger = structlog.get_logger()


@dataclass
class CachedResponse:
    """A cached successful response"""
    question_hash: str
    question_text: str
    response_text: str
    intent: str
    category: str
    confidence_score: int
    helpful_count: int = 0
    unhelpful_count: int = 0
    use_count: int = 0
    created_at: str = field(default_factory=lambda: datetime.utcnow().isoformat())
    last_used: str = field(default_factory=lambda: datetime.utcnow().isoformat())
    action_buttons: List[dict] = field(default_factory=list)


@dataclass
class LearnedPattern:
    """A pattern learned from user interactions"""
    pattern_id: str
    keywords: List[str]
    mapped_intent: str
    source_questions: List[str]
    success_rate: float = 0.0
    usage_count: int = 0


class LearningEngine:
    """
    Adaptive learning system that improves responses over time.
    
    Features:
    - Response caching for fast retrieval
    - Feedback-based learning
    - New pattern discovery
    - Confidence boosting for successful responses
    - Integration with TrainingPipeline for real ML training
    """
    
    def __init__(self, cache_file: str = "response_cache.json"):
        self.cache_file = Path(__file__).parent / cache_file
        self.patterns_file = Path(__file__).parent / "learned_patterns.json"
        
        self.response_cache: Dict[str, CachedResponse] = {}
        self.learned_patterns: Dict[str, LearnedPattern] = {}
        
        # Training pipeline reference (set lazily)
        self._training_pipeline = None
        
        # Load existing cache
        self._load_cache()
        self._load_patterns()
        
        # Settings
        self.cache_threshold = 50
        self.similarity_threshold = 80
        self.max_cache_size = 2000
    
    @property
    def training_pipeline(self):
        """Lazy access to training pipeline to avoid circular imports"""
        if self._training_pipeline is None:
            try:
                from training_pipeline import get_training_pipeline
                self._training_pipeline = get_training_pipeline()
            except Exception as e:
                logger.warn("training_pipeline_not_available", error=str(e))
        return self._training_pipeline
        
    def _load_cache(self):
        """Load cached responses from file"""
        try:
            if self.cache_file.exists():
                with open(self.cache_file, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                    for key, value in data.items():
                        self.response_cache[key] = CachedResponse(**value)
                logger.info("Loaded response cache", count=len(self.response_cache))
        except Exception as e:
            logger.warning("Could not load cache", error=str(e))
            
    def _save_cache(self):
        """Save cached responses to file"""
        try:
            data = {k: asdict(v) for k, v in self.response_cache.items()}
            with open(self.cache_file, 'w', encoding='utf-8') as f:
                json.dump(data, f, ensure_ascii=False, indent=2)
        except Exception as e:
            logger.warning("Could not save cache", error=str(e))
            
    def _load_patterns(self):
        """Load learned patterns from file"""
        try:
            if self.patterns_file.exists():
                with open(self.patterns_file, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                    for key, value in data.items():
                        self.learned_patterns[key] = LearnedPattern(**value)
                logger.info("Loaded patterns", count=len(self.learned_patterns))
        except Exception as e:
            logger.warning("Could not load patterns", error=str(e))
            
    def _save_patterns(self):
        """Save learned patterns to file"""
        try:
            data = {k: asdict(v) for k, v in self.learned_patterns.items()}
            with open(self.patterns_file, 'w', encoding='utf-8') as f:
                json.dump(data, f, ensure_ascii=False, indent=2)
        except Exception as e:
            logger.warning("Could not save patterns", error=str(e))
    
    def _hash_question(self, question: str) -> str:
        """Create a hash for a question (normalized)"""
        normalized = question.lower().strip()
        stop_words = {'el', 'la', 'los', 'las', 'un', 'una', 'de', 'del', 'en', 'con', 'por', 'para', 'a', 'y', 'o', 'que', 'es', 'mi', 'tu', 'su'}
        words = [w for w in normalized.split() if w not in stop_words]
        normalized = ' '.join(sorted(words))
        return hashlib.md5(normalized.encode()).hexdigest()[:16]
    
    def find_cached_response(self, question: str) -> Optional[CachedResponse]:
        """
        Find a cached response for a similar question.
        Uses exact hash match first, then fuzzy matching.
        """
        question_hash = self._hash_question(question)
        
        # Exact match
        if question_hash in self.response_cache:
            cached = self.response_cache[question_hash]
            if cached.helpful_count >= cached.unhelpful_count:
                cached.use_count += 1
                cached.last_used = datetime.utcnow().isoformat()
                self._save_cache()
                logger.info("Cache hit (exact)", intent=cached.intent)
                return cached
        
        # Fuzzy match
        from rapidfuzz import fuzz
        best_match = None
        best_score = 0
        
        for hash_key, cached in self.response_cache.items():
            score = fuzz.ratio(question.lower(), cached.question_text.lower())
            if score > best_score and score >= self.similarity_threshold:
                if cached.helpful_count >= cached.unhelpful_count:
                    best_score = score
                    best_match = cached
        
        if best_match:
            best_match.use_count += 1
            best_match.last_used = datetime.utcnow().isoformat()
            self._save_cache()
            logger.info("Cache hit (fuzzy)", intent=best_match.intent, score=best_score)
            return best_match
            
        return None
    
    def cache_response(
        self,
        question: str,
        response: str,
        intent: str,
        category: str,
        confidence: int,
        action_buttons: List[dict] = None
    ):
        """Cache a successful response for future use"""
        if confidence < self.cache_threshold:
            return
            
        question_hash = self._hash_question(question)
        
        if question_hash in self.response_cache:
            existing = self.response_cache[question_hash]
            if existing.helpful_count > 0:
                return
        
        self.response_cache[question_hash] = CachedResponse(
            question_hash=question_hash,
            question_text=question,
            response_text=response,
            intent=intent,
            category=category,
            confidence_score=confidence,
            action_buttons=action_buttons or []
        )
        
        if len(self.response_cache) > self.max_cache_size:
            self._prune_cache()
            
        self._save_cache()
        logger.info("Cached response", intent=intent, confidence=confidence)
    
    def record_feedback(self, message_id: str, question: str, helpful: bool):
        """Record user feedback to improve future responses"""
        question_hash = self._hash_question(question)
        
        if question_hash in self.response_cache:
            cached = self.response_cache[question_hash]
            if helpful:
                cached.helpful_count += 1
            else:
                cached.unhelpful_count += 1
            self._save_cache()
            logger.info(
                "Recorded feedback",
                helpful=helpful,
                intent=cached.intent,
                helpful_count=cached.helpful_count,
                unhelpful_count=cached.unhelpful_count
            )
    
    def learn_new_pattern(self, question: str, intent: str, keywords: List[str]):
        """Learn a new question pattern"""
        pattern_id = f"learned_{len(self.learned_patterns)}"
        
        words = question.lower().split()
        existing_keywords = set(keywords)
        new_keywords = [w for w in words if len(w) > 3 and w not in existing_keywords]
        
        if new_keywords:
            all_keywords = list(existing_keywords) + new_keywords[:3]
            self.learned_patterns[pattern_id] = LearnedPattern(
                pattern_id=pattern_id,
                keywords=all_keywords,
                mapped_intent=intent,
                source_questions=[question]
            )
            self._save_patterns()
            logger.info("Learned new pattern", pattern_id=pattern_id, keywords=all_keywords)
    
    def get_confidence_boost(self, intent: str) -> float:
        """Get confidence boost based on past success"""
        successful_responses = [
            c for c in self.response_cache.values()
            if c.intent == intent and c.helpful_count > c.unhelpful_count
        ]
        
        if not successful_responses:
            return 0.0
        
        total_helpful = sum(c.helpful_count for c in successful_responses)
        total_unhelpful = sum(c.unhelpful_count for c in successful_responses)
        
        if total_helpful + total_unhelpful == 0:
            return 0.0
            
        success_rate = total_helpful / (total_helpful + total_unhelpful)
        return min(0.15, success_rate * 0.15)
    
    def _prune_cache(self):
        """Remove old/unused cache entries"""
        sorted_cache = sorted(
            self.response_cache.items(),
            key=lambda x: x[1].last_used
        )
        keep_count = int(self.max_cache_size * 0.8)
        self.response_cache = dict(sorted_cache[-keep_count:])
        logger.info("Pruned cache", remaining=len(self.response_cache))
    
    def get_stats(self) -> dict:
        """Get learning statistics including training pipeline stats"""
        total_cached = len(self.response_cache)
        total_patterns = len(self.learned_patterns)
        
        base_stats = {
            "cached_responses": total_cached,
            "learned_patterns": total_patterns,
            "avg_helpful_rate": 0,
            "total_uses": 0,
        }
        
        if total_cached > 0:
            total_helpful = sum(c.helpful_count for c in self.response_cache.values())
            total_unhelpful = sum(c.unhelpful_count for c in self.response_cache.values())
            total_uses = sum(c.use_count for c in self.response_cache.values())
            
            helpful_rate = 0
            if total_helpful + total_unhelpful > 0:
                helpful_rate = total_helpful / (total_helpful + total_unhelpful) * 100
            
            base_stats["avg_helpful_rate"] = round(helpful_rate, 1)
            base_stats["total_uses"] = total_uses
        
        # Add training pipeline stats
        if self.training_pipeline:
            base_stats["training_pipeline"] = self.training_pipeline.get_stats()
        
        return base_stats


# Singleton instance
_learning_engine: Optional[LearningEngine] = None


def get_learning_engine() -> LearningEngine:
    """Get the singleton learning engine instance"""
    global _learning_engine
    if _learning_engine is None:
        _learning_engine = LearningEngine()
    return _learning_engine
