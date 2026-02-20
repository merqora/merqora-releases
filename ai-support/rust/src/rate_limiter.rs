//! Rate limiting implementation

use std::collections::HashMap;
use std::time::{Duration, Instant};

/// Token bucket rate limiter
pub struct RateLimiter {
    buckets: HashMap<String, TokenBucket>,
    window_seconds: u64,
    max_requests: i32,
}

struct TokenBucket {
    tokens: i32,
    last_refill: Instant,
}

impl RateLimiter {
    pub fn new(window_seconds: u64, max_requests: i32) -> Self {
        Self {
            buckets: HashMap::new(),
            window_seconds,
            max_requests,
        }
    }

    /// Check if request is allowed, returns remaining tokens or error
    pub fn check(&mut self, user_id: &str) -> Result<i32, String> {
        let now = Instant::now();
        
        let bucket = self.buckets.entry(user_id.to_string()).or_insert_with(|| {
            TokenBucket {
                tokens: self.max_requests,
                last_refill: now,
            }
        });

        // Refill tokens based on time passed
        let elapsed = now.duration_since(bucket.last_refill);
        if elapsed >= Duration::from_secs(self.window_seconds) {
            bucket.tokens = self.max_requests;
            bucket.last_refill = now;
        }

        if bucket.tokens > 0 {
            bucket.tokens -= 1;
            Ok(bucket.tokens)
        } else {
            let wait_time = Duration::from_secs(self.window_seconds) - elapsed;
            Err(format!("Please wait {} seconds", wait_time.as_secs()))
        }
    }

    /// Get current status (remaining, max)
    pub fn get_status(&self, user_id: &str) -> (i32, i32) {
        match self.buckets.get(user_id) {
            Some(bucket) => (bucket.tokens, self.max_requests),
            None => (self.max_requests, self.max_requests),
        }
    }

    /// Reset rate limit for user
    pub fn reset(&mut self, user_id: &str) {
        self.buckets.remove(user_id);
    }

    /// Clean up expired buckets
    pub fn cleanup(&mut self) {
        let now = Instant::now();
        let window = Duration::from_secs(self.window_seconds * 2);
        
        self.buckets.retain(|_, bucket| {
            now.duration_since(bucket.last_refill) < window
        });
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_rate_limiting() {
        let mut limiter = RateLimiter::new(60, 5);
        
        // Should allow 5 requests
        for i in (0..5).rev() {
            assert_eq!(limiter.check("user1").unwrap(), i);
        }
        
        // Should block 6th request
        assert!(limiter.check("user1").is_err());
    }
}
