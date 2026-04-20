package com.khaleo.flashcard.config.activitylog;

import java.time.Duration;

/**
 * Defines retry and dead-letter policy for asynchronous activity log publishing.
 */
public interface ActivityLogPublishPolicy {

    int maxAttempts();

    Duration retryBackoff();

    default boolean shouldRetry(int attempt, Throwable cause) {
        return attempt < maxAttempts();
    }

    default boolean shouldDeadLetter(int attempt, Throwable cause) {
        return attempt >= maxAttempts();
    }

    static ActivityLogPublishPolicy defaultPolicy() {
        return new DefaultActivityLogPublishPolicy(3, Duration.ofSeconds(2));
    }

    final class DefaultActivityLogPublishPolicy implements ActivityLogPublishPolicy {

        private final int maxAttempts;
        private final Duration retryBackoff;

        public DefaultActivityLogPublishPolicy(int maxAttempts, Duration retryBackoff) {
            this.maxAttempts = maxAttempts;
            this.retryBackoff = retryBackoff;
        }

        @Override
        public int maxAttempts() {
            return maxAttempts;
        }

        @Override
        public Duration retryBackoff() {
            return retryBackoff;
        }
    }
}
