//! Rendly AI Support - Security & Infrastructure
//! 
//! This crate provides:
//! - Rate limiting
//! - Input sanitization
//! - Session management
//! - Structured logging
//! - Spam/abuse protection

pub mod rate_limiter;
pub mod sanitizer;
pub mod session;
pub mod logger;
pub mod validation;

use pyo3::prelude::*;

/// Security validation result
#[derive(Debug, Clone)]
#[pyclass]
pub struct ValidationResult {
    #[pyo3(get)]
    pub is_valid: bool,
    #[pyo3(get)]
    pub sanitized_message: String,
    #[pyo3(get)]
    pub rate_limit_remaining: i32,
    #[pyo3(get)]
    pub session_valid: bool,
    #[pyo3(get)]
    pub risk_score: f32,
    #[pyo3(get)]
    pub blocked_reason: Option<String>,
}

#[pymethods]
impl ValidationResult {
    #[new]
    fn new() -> Self {
        Self {
            is_valid: true,
            sanitized_message: String::new(),
            rate_limit_remaining: 100,
            session_valid: true,
            risk_score: 0.0,
            blocked_reason: None,
        }
    }
}

/// Main security service
#[pyclass]
pub struct SecurityService {
    rate_limiter: rate_limiter::RateLimiter,
    sanitizer: sanitizer::Sanitizer,
    session_manager: session::SessionManager,
}

#[pymethods]
impl SecurityService {
    #[new]
    fn new() -> Self {
        Self {
            rate_limiter: rate_limiter::RateLimiter::new(60, 20), // 20 requests per minute
            sanitizer: sanitizer::Sanitizer::new(),
            session_manager: session::SessionManager::new(),
        }
    }

    /// Validate and process an incoming message
    fn validate_message(&mut self, user_id: &str, message: &str, session_id: &str) -> ValidationResult {
        let mut result = ValidationResult::new();

        // Check rate limit
        match self.rate_limiter.check(user_id) {
            Ok(remaining) => {
                result.rate_limit_remaining = remaining;
            }
            Err(e) => {
                result.is_valid = false;
                result.blocked_reason = Some(format!("Rate limit exceeded: {}", e));
                return result;
            }
        }

        // Validate session
        result.session_valid = self.session_manager.validate(session_id, user_id);
        if !result.session_valid {
            // Create new session if invalid
            self.session_manager.create(user_id);
        }

        // Sanitize message
        let sanitization = self.sanitizer.sanitize(message);
        result.sanitized_message = sanitization.cleaned;
        result.risk_score = sanitization.risk_score;

        // Block if high risk
        if result.risk_score > 0.8 {
            result.is_valid = false;
            result.blocked_reason = Some("Message flagged as potential abuse".to_string());
        }

        result
    }

    /// Create a new session for user
    fn create_session(&mut self, user_id: &str) -> String {
        self.session_manager.create(user_id)
    }

    /// End a session
    fn end_session(&mut self, session_id: &str) {
        self.session_manager.end(session_id);
    }

    /// Get rate limit status
    fn get_rate_limit_status(&self, user_id: &str) -> (i32, i32) {
        self.rate_limiter.get_status(user_id)
    }

    /// Reset rate limit for user (admin function)
    fn reset_rate_limit(&mut self, user_id: &str) {
        self.rate_limiter.reset(user_id);
    }
}

/// Python module
#[pymodule]
fn rendly_security(_py: Python, m: &PyModule) -> PyResult<()> {
    m.add_class::<SecurityService>()?;
    m.add_class::<ValidationResult>()?;
    Ok(())
}
