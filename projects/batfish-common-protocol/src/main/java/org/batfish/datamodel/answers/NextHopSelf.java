package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is used by the symbolic route-policy analysis questions, {@link
 * org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion} and {@link
 * org.batfish.minesweeper.question.compareroutepolicies.CompareRoutePoliciesQuestion}, to represent
 * the fact that a route's next-hop was set to {@link
 * org.batfish.datamodel.routing_policy.expr.SelfNextHop}.
 */
public final class NextHopSelf implements NextHopResult {

  @JsonValue
  @JsonCreator
  public static @Nonnull NextHopSelf instance() {
    return INSTANCE;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return obj instanceof NextHopSelf;
  }

  @Override
  public int hashCode() {
    return 569605681; // randomly generated
  }

  @Override
  public String toString() {
    return NextHopSelf.class.getName();
  }

  private static final NextHopSelf INSTANCE = new NextHopSelf();

  private NextHopSelf() {}
}
