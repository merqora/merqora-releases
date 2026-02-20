#include "scoring_engine.hpp"
#include <mutex>

namespace rendly {
namespace ai {

static std::unique_ptr<ScoringEngine> g_instance;
static std::once_flag g_init_flag;

ScoringEngine::ScoringEngine() 
    : text_analyzer_(std::make_unique<TextAnalyzer>())
    , intent_matcher_(std::make_unique<IntentMatcher>())
    , confidence_calculator_(std::make_unique<ConfidenceCalculator>())
{}

ScoringEngine::~ScoringEngine() = default;

ScoringEngine& ScoringEngine::instance() {
    std::call_once(g_init_flag, []() {
        g_instance = std::make_unique<ScoringEngine>();
        g_instance->initialize();
    });
    return *g_instance;
}

void ScoringEngine::initialize() {
    if (initialized_) return;
    
    // Intent patterns are loaded in IntentMatcher constructor
    initialized_ = true;
}

void ScoringEngine::load_intent_patterns(const std::vector<IntentPattern>& patterns) {
    intent_matcher_->load_patterns(patterns);
}

AnalysisResult ScoringEngine::analyze(const std::string& user_message) const {
    AnalysisResult result;
    
    // Normalize text
    std::string normalized = text_analyzer_->normalize_text(user_message);
    
    // Extract keywords
    result.matched_keywords = text_analyzer_->extract_keywords(normalized);
    
    // Calculate text quality scores
    result.clarity_score = text_analyzer_->calculate_clarity(user_message);
    result.completeness_score = text_analyzer_->calculate_completeness(user_message);
    
    // Detect negative flags
    result.is_aggressive = text_analyzer_->detect_aggression(user_message);
    result.is_confused = text_analyzer_->detect_confusion(user_message);
    result.is_spam = text_analyzer_->detect_spam(user_message);
    
    bool has_negative_flags = result.is_aggressive || result.is_spam;
    
    // Match intent
    auto [intent, intent_score] = intent_matcher_->match_intent(
        normalized, 
        result.matched_keywords
    );
    result.detected_intent = intent;
    
    // Calculate final confidence
    result.confidence_score = confidence_calculator_->calculate(
        intent_score,
        result.clarity_score,
        result.completeness_score,
        has_negative_flags
    );
    
    // Determine recommendation
    if (result.is_spam) {
        result.recommendation = "block";
        result.confidence_score = 0;
    } else if (result.is_aggressive || result.confidence_score < ESCALATION_THRESHOLD) {
        result.recommendation = "escalate";
    } else {
        result.recommendation = "respond";
    }
    
    return result;
}

int ScoringEngine::calculate_confidence_score(const std::string& user_message) const {
    return analyze(user_message).confidence_score;
}

} // namespace ai
} // namespace rendly
