//! Input validation utilities

use regex::Regex;
use lazy_static::lazy_static;

lazy_static! {
    static ref UUID_PATTERN: Regex = Regex::new(
        r"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
    ).unwrap();
}

/// Validate user ID format (UUID)
pub fn validate_user_id(user_id: &str) -> bool {
    UUID_PATTERN.is_match(user_id)
}

/// Validate session ID format (UUID)
pub fn validate_session_id(session_id: &str) -> bool {
    UUID_PATTERN.is_match(session_id)
}

/// Validate message is not empty and within limits
pub fn validate_message(message: &str) -> Result<(), String> {
    let trimmed = message.trim();
    
    if trimmed.is_empty() {
        return Err("Message cannot be empty".to_string());
    }
    
    if trimmed.len() < 2 {
        return Err("Message too short".to_string());
    }
    
    if trimmed.len() > 5000 {
        return Err("Message too long (max 5000 characters)".to_string());
    }
    
    Ok(())
}

/// Check if message appears to be automated/bot
pub fn detect_bot_behavior(messages: &[&str], timestamps: &[i64]) -> bool {
    if messages.len() < 3 {
        return false;
    }
    
    // Check for identical messages
    let unique: std::collections::HashSet<_> = messages.iter().collect();
    if unique.len() == 1 && messages.len() >= 3 {
        return true;
    }
    
    // Check for too rapid messages (less than 500ms apart)
    for window in timestamps.windows(2) {
        if window[1] - window[0] < 500 {
            return true;
        }
    }
    
    false
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_uuid_validation() {
        assert!(validate_user_id("550e8400-e29b-41d4-a716-446655440000"));
        assert!(!validate_user_id("not-a-uuid"));
    }

    #[test]
    fn test_message_validation() {
        assert!(validate_message("Hello").is_ok());
        assert!(validate_message("").is_err());
        assert!(validate_message("a").is_err());
    }
}
