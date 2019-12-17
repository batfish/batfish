package org.batfish.representation.palo_alto;

import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;

/** A {@link PolicyRuleMatch} that matches on a set of "from peer" names. */
public final class PolicyRuleMatchFromPeerSet implements PolicyRuleMatch {

  private final @Nonnull Set<String> _fromPeers;

  public PolicyRuleMatchFromPeerSet(Set<String> fromPeers) {
    _fromPeers = fromPeers;
  }

  @Override
  public <T> T accept(PolicyRuleMatchVisitor<T> visitor) {
    return visitor.visitPolicyRuleMatchFromPeerSet(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PolicyRuleMatchFromPeerSet)) {
      return false;
    }
    PolicyRuleMatchFromPeerSet that = (PolicyRuleMatchFromPeerSet) o;
    return _fromPeers.equals(that._fromPeers);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_fromPeers);
  }

  public @Nonnull Set<String> getFromPeers() {
    return _fromPeers;
  }
}
