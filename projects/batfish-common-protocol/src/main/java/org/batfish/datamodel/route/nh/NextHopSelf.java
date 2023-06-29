package org.batfish.datamodel.route.nh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the fact that the next-hop IP of a BGP route is set to the local address of the BGP
 * session by a particular route policy. Objects of this class can be used in results produced by
 * the {@link org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion}
 * question.
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
