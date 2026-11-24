package org.batfish.common.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class BackoffTest {
  @Test
  void testAttemptLimited() {
    int attemptLimit = 5;
    Backoff backoff = Backoff.builder().withAttemptLimit(attemptLimit).build();
    int i = 1;
    while (backoff.hasNext()) {
      ++i;
      backoff.nextBackoff();
    }
    assertThat(i, equalTo(attemptLimit));
  }

  @Test
  void testTimeLimited() {
    Duration cumulativeLimit = Duration.ofMinutes(1);
    Backoff backoff = Backoff.builder().withCumulativeBackoffLimit(cumulativeLimit).build();
    Duration totalSoFar = Duration.ZERO;

    while (backoff.hasNext()) {
      assertThat(totalSoFar, lessThan(cumulativeLimit));
      totalSoFar = totalSoFar.plus(backoff.nextBackoff());
    }
    assertThat(totalSoFar, greaterThanOrEqualTo(cumulativeLimit));
  }

  @Test
  void testExpansion() {
    Backoff backoff = Backoff.builder().withExpansion(2.0).withAttemptLimit(5).build();
    assertTrue(backoff.hasNext());
    Duration lastBackoff = backoff.nextBackoff();
    assertTrue(backoff.hasNext());

    while (backoff.hasNext()) {
      Duration nextBackoff = backoff.nextBackoff();
      assertThat(nextBackoff, equalTo(lastBackoff.plus(lastBackoff)));
      lastBackoff = nextBackoff;
    }
  }

  @Test
  void testInitialBackoff() {
    Backoff backoff = Backoff.builder().withInitialBackoff(Duration.ofSeconds(17)).build();
    assertTrue(backoff.hasNext());
    assertThat(backoff.nextBackoff(), equalTo(Duration.ofSeconds(17)));
  }

  @Test
  void testMaximumBackoffAndSeveralSteps() {
    Backoff backoff =
        Backoff.builder()
            .withInitialBackoff(Duration.ofSeconds(1))
            .withMaximumBackoff(Duration.ofSeconds(11))
            .withExpansion(2.0)
            .withAttemptLimit(8)
            .build();
    int[] expected = {1, 2, 4, 8, 11, 11, 11};
    for (int seconds : expected) {
      assertTrue(backoff.hasNext());
      assertThat(backoff.nextBackoff(), equalTo(Duration.ofSeconds(seconds)));
    }
    assertFalse(backoff.hasNext());
  }
}
