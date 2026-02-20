#include "scoring_engine.hpp"
#include <algorithm>
#include <cctype>
#include <regex>
#include <sstream>
#include <cmath>

namespace rendly {
namespace ai {

TextAnalyzer::TextAnalyzer() {
    // Spanish aggressive patterns
    aggressive_patterns_ = {
        "mierda", "carajo", "estafa", "robo", "ladrones", "basura",
        "inutil", "incompetentes", "porqueria", "asco", "fraude",
        "demanda", "denuncia", "abogado", "legal", "estafadores"
    };
    
    // Confusion indicators
    confusion_indicators_ = {
        "no entiendo", "no se", "como hago", "ayuda", "confundido",
        "perdido", "que significa", "no funciona", "problema",
        "error", "bug", "falla", "???", "help", "socorro"
    };
    
    // Spam patterns
    spam_patterns_ = {
        "http://", "https://", "www.", ".com/", "gratis", "dinero facil",
        "bitcoin", "crypto", "invertir", "gana dinero", "click aqui",
        "oferta", "promocion especial", "urgente!!!"
    };
}

TextAnalyzer::~TextAnalyzer() = default;

std::string TextAnalyzer::to_lowercase(const std::string& text) const {
    std::string result = text;
    std::transform(result.begin(), result.end(), result.begin(),
        [](unsigned char c) { return std::tolower(c); });
    return result;
}

std::string TextAnalyzer::remove_accents(const std::string& text) const {
    std::string result = text;
    // Simple accent removal for Spanish
    const std::vector<std::pair<std::string, std::string>> replacements = {
        {"á", "a"}, {"é", "e"}, {"í", "i"}, {"ó", "o"}, {"ú", "u"},
        {"ñ", "n"}, {"ü", "u"}, {"Á", "A"}, {"É", "E"}, {"Í", "I"},
        {"Ó", "O"}, {"Ú", "U"}, {"Ñ", "N"}, {"Ü", "U"}
    };
    
    for (const auto& [from, to] : replacements) {
        size_t pos = 0;
        while ((pos = result.find(from, pos)) != std::string::npos) {
            result.replace(pos, from.length(), to);
            pos += to.length();
        }
    }
    return result;
}

std::string TextAnalyzer::normalize_text(const std::string& text) const {
    std::string normalized = to_lowercase(text);
    normalized = remove_accents(normalized);
    
    // Remove extra whitespace
    std::regex whitespace_regex("\\s+");
    normalized = std::regex_replace(normalized, whitespace_regex, " ");
    
    // Trim
    size_t start = normalized.find_first_not_of(" ");
    size_t end = normalized.find_last_not_of(" ");
    if (start != std::string::npos && end != std::string::npos) {
        normalized = normalized.substr(start, end - start + 1);
    }
    
    return normalized;
}

std::vector<std::string> TextAnalyzer::tokenize(const std::string& text) const {
    std::vector<std::string> tokens;
    std::istringstream stream(text);
    std::string token;
    
    while (stream >> token) {
        // Remove punctuation
        token.erase(std::remove_if(token.begin(), token.end(),
            [](char c) { return std::ispunct(c); }), token.end());
        
        if (!token.empty() && token.length() > 2) {
            tokens.push_back(token);
        }
    }
    
    return tokens;
}

float TextAnalyzer::calculate_clarity(const std::string& text) const {
    std::string normalized = normalize_text(text);
    
    if (normalized.empty()) return 0.0f;
    
    float clarity = 1.0f;
    
    // Penalize very short messages
    if (normalized.length() < 10) {
        clarity -= 0.3f;
    }
    
    // Penalize excessive punctuation
    int punct_count = std::count_if(text.begin(), text.end(),
        [](char c) { return std::ispunct(c); });
    float punct_ratio = static_cast<float>(punct_count) / text.length();
    if (punct_ratio > 0.2f) {
        clarity -= 0.2f;
    }
    
    // Penalize ALL CAPS
    int upper_count = std::count_if(text.begin(), text.end(),
        [](char c) { return std::isupper(c); });
    float upper_ratio = static_cast<float>(upper_count) / text.length();
    if (upper_ratio > 0.5f && text.length() > 5) {
        clarity -= 0.15f;
    }
    
    // Check for confusion indicators
    for (const auto& indicator : confusion_indicators_) {
        if (normalized.find(indicator) != std::string::npos) {
            clarity -= 0.1f;
            break;
        }
    }
    
    return std::max(0.0f, std::min(1.0f, clarity));
}

float TextAnalyzer::calculate_completeness(const std::string& text) const {
    std::string normalized = normalize_text(text);
    auto tokens = tokenize(normalized);
    
    if (tokens.empty()) return 0.0f;
    
    float completeness = 0.0f;
    
    // More tokens = potentially more complete
    if (tokens.size() >= 3) completeness += 0.3f;
    if (tokens.size() >= 5) completeness += 0.2f;
    if (tokens.size() >= 8) completeness += 0.2f;
    
    // Contains question structure
    if (normalized.find("?") != std::string::npos ||
        normalized.find("como") != std::string::npos ||
        normalized.find("donde") != std::string::npos ||
        normalized.find("cuando") != std::string::npos ||
        normalized.find("por que") != std::string::npos ||
        normalized.find("cual") != std::string::npos) {
        completeness += 0.15f;
    }
    
    // Contains context keywords
    std::vector<std::string> context_words = {
        "pedido", "compra", "venta", "cuenta", "pago", "envio",
        "producto", "app", "usuario", "contrasena", "direccion"
    };
    
    for (const auto& word : context_words) {
        if (normalized.find(word) != std::string::npos) {
            completeness += 0.15f;
            break;
        }
    }
    
    return std::min(1.0f, completeness);
}

bool TextAnalyzer::detect_aggression(const std::string& text) const {
    std::string normalized = normalize_text(text);
    
    for (const auto& pattern : aggressive_patterns_) {
        if (normalized.find(pattern) != std::string::npos) {
            return true;
        }
    }
    
    // Check for excessive caps (shouting)
    int upper_count = std::count_if(text.begin(), text.end(),
        [](char c) { return std::isupper(c); });
    float upper_ratio = static_cast<float>(upper_count) / text.length();
    
    if (upper_ratio > 0.7f && text.length() > 10) {
        return true;
    }
    
    return false;
}

bool TextAnalyzer::detect_confusion(const std::string& text) const {
    std::string normalized = normalize_text(text);
    
    int confusion_count = 0;
    for (const auto& indicator : confusion_indicators_) {
        if (normalized.find(indicator) != std::string::npos) {
            confusion_count++;
        }
    }
    
    // Multiple question marks
    if (std::count(text.begin(), text.end(), '?') >= 3) {
        confusion_count++;
    }
    
    return confusion_count >= 2;
}

bool TextAnalyzer::detect_spam(const std::string& text) const {
    std::string normalized = normalize_text(text);
    
    for (const auto& pattern : spam_patterns_) {
        if (normalized.find(pattern) != std::string::npos) {
            return true;
        }
    }
    
    // Repetitive characters
    std::regex repetitive("(.)\\1{4,}");
    if (std::regex_search(text, repetitive)) {
        return true;
    }
    
    return false;
}

std::vector<std::string> TextAnalyzer::extract_keywords(const std::string& text) const {
    std::string normalized = normalize_text(text);
    auto tokens = tokenize(normalized);
    
    // Filter stopwords (Spanish)
    std::vector<std::string> stopwords = {
        "el", "la", "los", "las", "un", "una", "unos", "unas",
        "de", "del", "al", "a", "en", "con", "por", "para",
        "que", "y", "o", "pero", "si", "no", "se", "su", "sus",
        "este", "esta", "estos", "estas", "ese", "esa", "mi", "tu"
    };
    
    std::vector<std::string> keywords;
    for (const auto& token : tokens) {
        if (std::find(stopwords.begin(), stopwords.end(), token) == stopwords.end()) {
            keywords.push_back(token);
        }
    }
    
    return keywords;
}

} // namespace ai
} // namespace rendly
