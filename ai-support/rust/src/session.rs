//! Session management

use std::collections::HashMap;
use std::time::{Duration, Instant};
use uuid::Uuid;

const SESSION_TIMEOUT_MINUTES: u64 = 30;

pub struct Session {
    pub id: String,
    pub user_id: String,
    pub created_at: Instant,
    pub last_activity: Instant,
    pub message_count: u32,
}

pub struct SessionManager {
    sessions: HashMap<String, Session>,
    user_sessions: HashMap<String, String>, // user_id -> session_id
}

impl SessionManager {
    pub fn new() -> Self {
        Self {
            sessions: HashMap::new(),
            user_sessions: HashMap::new(),
        }
    }

    /// Create a new session for a user
    pub fn create(&mut self, user_id: &str) -> String {
        // End any existing session
        if let Some(old_session_id) = self.user_sessions.get(user_id) {
            self.sessions.remove(old_session_id);
        }

        let session_id = Uuid::new_v4().to_string();
        let now = Instant::now();
        
        let session = Session {
            id: session_id.clone(),
            user_id: user_id.to_string(),
            created_at: now,
            last_activity: now,
            message_count: 0,
        };

        self.sessions.insert(session_id.clone(), session);
        self.user_sessions.insert(user_id.to_string(), session_id.clone());

        session_id
    }

    /// Validate a session
    pub fn validate(&mut self, session_id: &str, user_id: &str) -> bool {
        if let Some(session) = self.sessions.get_mut(session_id) {
            // Check if session belongs to user
            if session.user_id != user_id {
                return false;
            }

            // Check if session expired
            let elapsed = Instant::now().duration_since(session.last_activity);
            if elapsed > Duration::from_secs(SESSION_TIMEOUT_MINUTES * 60) {
                self.sessions.remove(session_id);
                self.user_sessions.remove(user_id);
                return false;
            }

            // Update activity
            session.last_activity = Instant::now();
            session.message_count += 1;
            true
        } else {
            false
        }
    }

    /// End a session
    pub fn end(&mut self, session_id: &str) {
        if let Some(session) = self.sessions.remove(session_id) {
            self.user_sessions.remove(&session.user_id);
        }
    }

    /// Get session info
    pub fn get(&self, session_id: &str) -> Option<&Session> {
        self.sessions.get(session_id)
    }

    /// Get session by user ID
    pub fn get_by_user(&self, user_id: &str) -> Option<&Session> {
        self.user_sessions
            .get(user_id)
            .and_then(|sid| self.sessions.get(sid))
    }

    /// Clean up expired sessions
    pub fn cleanup(&mut self) {
        let timeout = Duration::from_secs(SESSION_TIMEOUT_MINUTES * 60);
        let now = Instant::now();

        let expired: Vec<String> = self
            .sessions
            .iter()
            .filter(|(_, s)| now.duration_since(s.last_activity) > timeout)
            .map(|(id, _)| id.clone())
            .collect();

        for session_id in expired {
            if let Some(session) = self.sessions.remove(&session_id) {
                self.user_sessions.remove(&session.user_id);
            }
        }
    }
}

impl Default for SessionManager {
    fn default() -> Self {
        Self::new()
    }
}
