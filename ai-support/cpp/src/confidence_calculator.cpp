#include "scoring_engine.hpp"
#include <algorithm>
#include <cmath>

namespace rendly {
namespace ai {

ConfidenceCalculator::ConfidenceCalculator() = default;
ConfidenceCalculator::~ConfidenceCalculator() = default;

void ConfidenceCalculator::set_weights(
    float intent_weight,
    float clarity_weight,
    float completeness_weight
) {
    // Normalize weights
    float total = intent_weight + clarity_weight + completeness_weight;
    intent_weight_ = intent_weight / total;
    clarity_weight_ = clarity_weight / total;
    completeness_weight_ = completeness_weight / total;
}

int ConfidenceCalculator::calculate(
    float intent_match_score,
    float clarity_score,
    float completeness_score,
    bool has_negative_flags
) const {
    // Weighted combination
    float base_score = 
        (intent_match_score * intent_weight_) +
        (clarity_score * clarity_weight_) +
        (completeness_score * completeness_weight_);
    
    // Apply penalty for negative flags (aggression, spam, etc.)
    if (has_negative_flags) {
        base_score *= (1.0f - negative_penalty_);
    }
    
    // Boost for high intent match
    if (intent_match_score > 0.8f) {
        base_score = std::min(1.0f, base_score * 1.1f);
    }
    
    // Convert to 0-100 scale
    int confidence = static_cast<int>(std::round(base_score * 100.0f));
    
    // Clamp
    return std::max(0, std::min(100, confidence));
}

} // namespace ai
} // namespace rendly
