//! Input sanitization and risk detection

use lazy_static::lazy_static;
use regex::Regex;

lazy_static! {
    // Patterns for dangerous content
    static ref URL_PATTERN: Regex = Regex::new(r"https?://[^\s]+").unwrap();
    static ref EMAIL_PATTERN: Regex = Regex::new(r"[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}").unwrap();
    static ref PHONE_PATTERN: Regex = Regex::new(r"\+?\d{10,15}").unwrap();
    static ref HTML_PATTERN: Regex = Regex::new(r"<[^>]+>").unwrap();
    static ref SQL_PATTERN: Regex = Regex::new(r"(?i)(select|insert|update|delete|drop|union|exec)\s").unwrap();
    static ref SCRIPT_PATTERN: Regex = Regex::new(r"(?i)<script|javascript:|on\w+\s*=").unwrap();
    
    // Spam patterns
    static ref SPAM_PATTERNS: Vec<Regex> = vec![
        Regex::new(r"(?i)gratis|free money|click here|act now").unwrap(),
        Regex::new(r"(.)\1{5,}").unwrap(), // Repeated characters
        Regex::new(r"(?i)bitcoin|crypto|invest|earn money").unwrap(),
    ];
    
    // Aggressive patterns (Spanish)
    static ref AGGRESSIVE_PATTERNS: Vec<Regex> = vec![
        Regex::new(r"(?i)mierda|carajo|estafa|ladrones|fraude").unwrap(),
        Regex::new(r"(?i)demanda|denuncia|abogado").unwrap(),
    ];
}

pub struct SanitizationResult {
    pub cleaned: String,
    pub risk_score: f32,
    pub flags: Vec<String>,
}

pub struct Sanitizer {
    max_length: usize,
}

impl Sanitizer {
    pub fn new() -> Self {
        Self { max_length: 2000 }
    }

    pub fn sanitize(&self, input: &str) -> SanitizationResult {
        let mut cleaned = input.to_string();
        let mut risk_score: f32 = 0.0;
        let mut flags = Vec::new();

        // Truncate if too long
        if cleaned.len() > self.max_length {
            cleaned.truncate(self.max_length);
            flags.push("truncated".to_string());
            risk_score += 0.1;
        }

        // Remove HTML tags
        if HTML_PATTERN.is_match(&cleaned) {
            cleaned = HTML_PATTERN.replace_all(&cleaned, "").to_string();
            flags.push("html_removed".to_string());
            risk_score += 0.2;
        }

        // Detect script injection
        if SCRIPT_PATTERN.is_match(&cleaned) {
            flags.push("script_detected".to_string());
            risk_score += 0.5;
        }

        // Detect SQL injection attempts
        if SQL_PATTERN.is_match(&cleaned) {
            flags.push("sql_detected".to_string());
            risk_score += 0.4;
        }

        // Mask URLs (potential phishing)
        if URL_PATTERN.is_match(&cleaned) {
            cleaned = URL_PATTERN.replace_all(&cleaned, "[URL REMOVED]").to_string();
            flags.push("url_masked".to_string());
            risk_score += 0.15;
        }

        // Mask emails (privacy)
        if EMAIL_PATTERN.is_match(&cleaned) {
            cleaned = EMAIL_PATTERN.replace_all(&cleaned, "[EMAIL]").to_string();
            flags.push("email_masked".to_string());
        }

        // Check for spam patterns
        for pattern in SPAM_PATTERNS.iter() {
            if pattern.is_match(&cleaned) {
                flags.push("spam_detected".to_string());
                risk_score += 0.3;
                break;
            }
        }

        // Check for aggressive content
        for pattern in AGGRESSIVE_PATTERNS.iter() {
            if pattern.is_match(&cleaned) {
                flags.push("aggressive_content".to_string());
                risk_score += 0.2;
                break;
            }
        }

        // Normalize whitespace
        cleaned = cleaned.split_whitespace().collect::<Vec<_>>().join(" ");

        // Trim
        cleaned = cleaned.trim().to_string();

        // Cap risk score at 1.0
        risk_score = risk_score.min(1.0);

        SanitizationResult {
            cleaned,
            risk_score,
            flags,
        }
    }
}

impl Default for Sanitizer {
    fn default() -> Self {
        Self::new()
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_html_removal() {
        let sanitizer = Sanitizer::new();
        let result = sanitizer.sanitize("<script>alert('xss')</script>Hello");
        assert!(!result.cleaned.contains("<script>"));
        assert!(result.flags.contains(&"html_removed".to_string()));
    }

    #[test]
    fn test_url_masking() {
        let sanitizer = Sanitizer::new();
        let result = sanitizer.sanitize("Check out https://malicious.com please");
        assert!(result.cleaned.contains("[URL REMOVED]"));
    }
}
