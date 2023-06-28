package org.batfish.datamodel.route.nh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import javax.annotation.Nonnull;

/**
 * Represents the fact that the next-hop IP of a BGP route is set to the address of the BGP peer by
 * a particular route policy. Objects of this class can be used in results produced by the {@link
 * org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion} question.
 */
public final class NextHopBgpPeerAddress implements NextHopResult {

  @JsonValue
  @JsonCreator
  public static @Nonnull NextHopBgpPeerAddress instance() {
    return INSTANCE;
  }

  @Override
  public String toString() {
    return NextHopBgpPeerAddress.class.getName();
  }

  private static final NextHopBgpPeerAddress INSTANCE = new NextHopBgpPeerAddress();

  private NextHopBgpPeerAddress() {}
}
