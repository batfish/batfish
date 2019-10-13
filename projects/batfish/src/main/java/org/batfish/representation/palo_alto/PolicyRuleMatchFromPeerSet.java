package org.batfish.representation.palo_alto;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;

/** A {@link PolicyRuleMatch} that matches on a set from peer names. */
public final class PolicyRuleMatchFromPeerSet implements PolicyRuleMatch {

  private final @Nonnull Set<String> _fromPeers;

  public PolicyRuleMatchFromPeerSet() {
    _fromPeers = new HashSet<>();
  }

  @Override
  public <T> T accept(PolicyRuleMatchVisitor<T> visitor) {
    return visitor.visitPolicyRuleMatchFromPeerSet(this);
  }

  public @Nonnull Set<String> getFromPeers() {
    return _fromPeers;
  }
}
