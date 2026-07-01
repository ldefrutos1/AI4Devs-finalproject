package com.mtl.ai.application;

import com.mtl.ai.exception.AiAssistantException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Rate limit en memoria para {@code POST /api/ai/chat/messages} (HU-010).
 *
 * <p>Límites MVP: máx. 40 turnos/hora por {@code subject_oidc} (ventana deslizante) y mínimo 2 s
 * entre peticiones consecutivas del mismo usuario. Solo aplica al chat; no afecta a enriquecimiento
 * de especie (**HU-016**).
 *
 * <p>En despliegues con varias réplicas del servicio, el límite efectivo por usuario puede ser algo
 * mayor porque cada instancia mantiene su propio contador.
 */
@Component
public class ChatMessageRateLimiter {

  static final int MAX_TURNS_PER_HOUR = 40;
  static final Duration MIN_INTERVAL = Duration.ofSeconds(2);
  static final Duration WINDOW = Duration.ofHours(1);

  private final Clock clock;
  private final ConcurrentHashMap<String, UserRateState> states = new ConcurrentHashMap<>();

  public ChatMessageRateLimiter() {
    this(Clock.systemUTC());
  }

  ChatMessageRateLimiter(Clock clock) {
    this.clock = clock;
  }

  /** Limpia contadores en memoria; solo para tests de integración. */
  public void resetForTests() {
    states.clear();
  }

  public void checkAllowed(String subjectOidc) {
    if (subjectOidc == null || subjectOidc.isBlank()) {
      throw new AiAssistantException(
          HttpStatus.UNAUTHORIZED,
          "No autenticado",
          "Se requiere un usuario autenticado para usar el chat.");
    }
    UserRateState state = states.computeIfAbsent(subjectOidc, ignored -> new UserRateState());
    synchronized (state) {
      Instant now = clock.instant();
      pruneOlderThan(state, now.minus(WINDOW));
      if (state.lastRequestAt != null
          && Duration.between(state.lastRequestAt, now).compareTo(MIN_INTERVAL) < 0) {
        throw tooManyRequests();
      }
      if (state.requestTimes.size() >= MAX_TURNS_PER_HOUR) {
        throw tooManyRequests();
      }
      state.requestTimes.addLast(now);
      state.lastRequestAt = now;
    }
  }

  private static void pruneOlderThan(UserRateState state, Instant cutoff) {
    Deque<Instant> times = state.requestTimes;
    while (!times.isEmpty() && times.peekFirst().isBefore(cutoff)) {
      times.removeFirst();
    }
  }

  private static AiAssistantException tooManyRequests() {
    return new AiAssistantException(
        HttpStatus.TOO_MANY_REQUESTS,
        ChatRateLimitMessages.TITLE_TOO_MANY_REQUESTS,
        ChatRateLimitMessages.DETAIL_TOO_MANY_REQUESTS);
  }

  static final class UserRateState {
    private final Deque<Instant> requestTimes = new ArrayDeque<>();
    private Instant lastRequestAt;
  }
}
