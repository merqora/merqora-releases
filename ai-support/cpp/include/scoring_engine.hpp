#pragma once

#include <string>
#include <vector>
#include <unordered_map>
#include <memory>

namespace rendly {
namespace ai {

struct AnalysisResult {
    int confidence_score;          // 0-100
    std::string detected_intent;   // intent category
    float clarity_score;           // 0.0-1.0
    float completeness_score;      // 0.0-1.0
    bool is_aggressive;
    bool is_confused;
    bool is_spam;
    std::vector<std::string> matched_keywords;
    std::string recommendation;    // "respond" or "escalate"
};

struct IntentPattern {
    std::string intent_id;
    std::string category;
    std::vector<std::string> keywords;
    std::vector<std::string> patterns;
    float base_confidence;
};

class TextAnalyzer {
public:
    TextAnalyzer();
    ~TextAnalyzer();

    float calculate_clarity(const std::string& text) const;
    float calculate_completeness(const std::string& text) const;
    bool detect_aggression(const std::string& text) const;
    bool detect_confusion(const std::string& text) const;
    bool detect_spam(const std::string& text) const;
    std::vector<std::string> extract_keywords(const std::string& text) const;
    std::string normalize_text(const std::string& text) const;

private:
    std::vector<std::string> aggressive_patterns_;
    std::vector<std::string> confusion_indicators_;
    std::vector<std::string> spam_patterns_;
    
    std::vector<std::string> tokenize(const std::string& text) const;
    std::string to_lowercase(const std::string& text) const;
    std::string remove_accents(const std::string& text) const;
};

class IntentMatcher {
public:
    IntentMatcher();
    ~IntentMatcher();

    void load_patterns(const std::vector<IntentPattern>& patterns);
    std::pair<std::string, float> match_intent(
        const std::string& text,
        const std::vector<std::string>& keywords
    ) const;
    
    const std::vector<IntentPattern>& get_patterns() const { return patterns_; }

private:
    std::vector<IntentPattern> patterns_;
    
    float calculate_pattern_match(
        const std::string& text,
        const IntentPattern& pattern,
        const std::vector<std::string>& keywords
    ) const;
};

class ConfidenceCalculator {
public:
    ConfidenceCalculator();
    ~ConfidenceCalculator();

    int calculate(
        float intent_match_score,
        float clarity_score,
        float completeness_score,
        bool has_negative_flags
    ) const;

    void set_weights(
        float intent_weight,
        float clarity_weight,
        float completeness_weight
    );

private:
    float intent_weight_ = 0.5f;
    float clarity_weight_ = 0.25f;
    float completeness_weight_ = 0.25f;
    float negative_penalty_ = 0.3f;
};

class ScoringEngine {
public:
    ScoringEngine();
    ~ScoringEngine();

    void initialize();
    void load_intent_patterns(const std::vector<IntentPattern>& patterns);
    
    AnalysisResult analyze(const std::string& user_message) const;
    int calculate_confidence_score(const std::string& user_message) const;
    
    static ScoringEngine& instance();

private:
    std::unique_ptr<TextAnalyzer> text_analyzer_;
    std::unique_ptr<IntentMatcher> intent_matcher_;
    std::unique_ptr<ConfidenceCalculator> confidence_calculator_;
    bool initialized_ = false;
    
    static constexpr int ESCALATION_THRESHOLD = 70;
};

} // namespace ai
} // namespace rendly
