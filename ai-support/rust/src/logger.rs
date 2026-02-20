//! Structured logging for AI support system

use serde::Serialize;
use chrono::{DateTime, Utc};

#[derive(Debug, Serialize)]
pub struct SupportLogEntry {
    pub timestamp: DateTime<Utc>,
    pub level: LogLevel,
    pub event_type: EventType,
    pub user_id: Option<String>,
    pub session_id: Option<String>,
    pub message: String,
    pub metadata: Option<serde_json::Value>,
}

#[derive(Debug, Serialize, Clone, Copy)]
#[serde(rename_all = "lowercase")]
pub enum LogLevel {
    Debug,
    Info,
    Warn,
    Error,
}

#[derive(Debug, Serialize, Clone)]
#[serde(rename_all = "snake_case")]
pub enum EventType {
    MessageReceived,
    MessageProcessed,
    IntentDetected,
    ResponseGenerated,
    Escalated,
    RateLimited,
    SessionCreated,
    SessionEnded,
    Error,
    SecurityAlert,
}

pub struct Logger;

impl Logger {
    pub fn log(entry: SupportLogEntry) {
        let json = serde_json::to_string(&entry).unwrap_or_else(|_| "{}".to_string());
        
        match entry.level {
            LogLevel::Debug => tracing::debug!("{}", json),
            LogLevel::Info => tracing::info!("{}", json),
            LogLevel::Warn => tracing::warn!("{}", json),
            LogLevel::Error => tracing::error!("{}", json),
        }
    }

    pub fn message_received(user_id: &str, session_id: &str, message_preview: &str) {
        Self::log(SupportLogEntry {
            timestamp: Utc::now(),
            level: LogLevel::Info,
            event_type: EventType::MessageReceived,
            user_id: Some(user_id.to_string()),
            session_id: Some(session_id.to_string()),
            message: format!("Message received: {}...", &message_preview[..message_preview.len().min(50)]),
            metadata: None,
        });
    }

    pub fn intent_detected(user_id: &str, intent: &str, confidence: i32) {
        Self::log(SupportLogEntry {
            timestamp: Utc::now(),
            level: LogLevel::Info,
            event_type: EventType::IntentDetected,
            user_id: Some(user_id.to_string()),
            session_id: None,
            message: format!("Intent: {} (confidence: {})", intent, confidence),
            metadata: Some(serde_json::json!({
                "intent": intent,
                "confidence": confidence
            })),
        });
    }

    pub fn escalated(user_id: &str, reason: &str) {
        Self::log(SupportLogEntry {
            timestamp: Utc::now(),
            level: LogLevel::Warn,
            event_type: EventType::Escalated,
            user_id: Some(user_id.to_string()),
            session_id: None,
            message: format!("Escalated to human: {}", reason),
            metadata: None,
        });
    }

    pub fn rate_limited(user_id: &str) {
        Self::log(SupportLogEntry {
            timestamp: Utc::now(),
            level: LogLevel::Warn,
            event_type: EventType::RateLimited,
            user_id: Some(user_id.to_string()),
            session_id: None,
            message: "Rate limit exceeded".to_string(),
            metadata: None,
        });
    }

    pub fn security_alert(user_id: &str, alert_type: &str, details: &str) {
        Self::log(SupportLogEntry {
            timestamp: Utc::now(),
            level: LogLevel::Warn,
            event_type: EventType::SecurityAlert,
            user_id: Some(user_id.to_string()),
            session_id: None,
            message: format!("Security alert: {} - {}", alert_type, details),
            metadata: Some(serde_json::json!({
                "alert_type": alert_type,
                "details": details
            })),
        });
    }

    pub fn error(message: &str, error: Option<&str>) {
        Self::log(SupportLogEntry {
            timestamp: Utc::now(),
            level: LogLevel::Error,
            event_type: EventType::Error,
            user_id: None,
            session_id: None,
            message: message.to_string(),
            metadata: error.map(|e| serde_json::json!({"error": e})),
        });
    }
}
