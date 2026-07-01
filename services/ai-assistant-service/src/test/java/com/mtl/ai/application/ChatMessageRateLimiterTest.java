package com.mtl.ai.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mtl.ai.exception.AiAssistantException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ChatMessageRateLimiterTest {

  @Test
  void checkAllowed_permitsRequestsWithinLimits() {
    MutableClock clock = new MutableClock(Instant.parse("2026-07-01T10:00:00Z"));
    ChatMessageRateLimiter limiter = new ChatMessageRateLimiter(clock);

    assertThatCode(() -> limiter.checkAllowed("user-1")).doesNotThrowAnyException();
    clock.advance(ChatMessageRateLimiter.MIN_INTERVAL);
    assertThatCode(() -> limiter.checkAllowed("user-1")).doesNotThrowAnyException();
  }

  @Test
  void checkAllowed_rejectsSecondRequestWithinMinInterval() {
    MutableClock clock = new MutableClock(Instant.parse("2026-07-01T10:00:00Z"));
    ChatMessageRateLimiter limiter = new ChatMessageRateLimiter(clock);

    limiter.checkAllowed("user-1");

    assertThatThrownBy(() -> limiter.checkAllowed("user-1"))
        .isInstanceOf(AiAssistantException.class)
        .extracting(ex -> ((AiAssistantException) ex).getStatus())
        .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
  }

  @Test
  void checkAllowed_rejectsMoreThanFortyTurnsInSlidingHour() {
    MutableClock clock = new MutableClock(Instant.parse("2026-07-01T10:00:00Z"));
    ChatMessageRateLimiter limiter = new ChatMessageRateLimiter(clock);

    for (int i = 0; i < ChatMessageRateLimiter.MAX_TURNS_PER_HOUR; i++) {
      limiter.checkAllowed("user-1");
      clock.advance(ChatMessageRateLimiter.MIN_INTERVAL);
    }

    assertThatThrownBy(() -> limiter.checkAllowed("user-1"))
        .isInstanceOf(AiAssistantException.class)
        .extracting(ex -> ((AiAssistantException) ex).getStatus())
        .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
  }

  @Test
  void checkAllowed_dropsTurnsOutsideSlidingWindow() {
    MutableClock clock = new MutableClock(Instant.parse("2026-07-01T10:00:00Z"));
    ChatMessageRateLimiter limiter = new ChatMessageRateLimiter(clock);

    for (int i = 0; i < ChatMessageRateLimiter.MAX_TURNS_PER_HOUR; i++) {
      limiter.checkAllowed("user-1");
      clock.advance(ChatMessageRateLimiter.MIN_INTERVAL);
    }

    clock.advance(ChatMessageRateLimiter.WINDOW.plus(ChatMessageRateLimiter.MIN_INTERVAL));

    assertThatCode(() -> limiter.checkAllowed("user-1")).doesNotThrowAnyException();
  }

  private static final class MutableClock extends Clock {
    private Instant instant;

    private MutableClock(Instant instant) {
      this.instant = instant;
    }

    private void advance(Duration duration) {
      instant = instant.plus(duration);
    }

    @Override
    public ZoneOffset getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(java.time.ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return instant;
    }
  }
}
