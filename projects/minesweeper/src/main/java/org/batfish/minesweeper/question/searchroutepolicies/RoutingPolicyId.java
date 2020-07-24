package org.batfish.minesweeper.question.searchroutepolicies;

import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Global identifier of a routing policy (node name, policy name). */
@ParametersAreNonnullByDefault
final class RoutingPolicyId implements Comparable<RoutingPolicyId> {
  private static final Comparator<RoutingPolicyId> COMPARATOR =
      Comparator.comparing(RoutingPolicyId::getNode).thenComparing(RoutingPolicyId::getPolicy);

  private final @Nonnull String _node;
  private final @Nonnull String _policy;

  RoutingPolicyId(String node, String policy) {
    _node = node;
    _policy = policy;
  }

  @Override
  public int compareTo(RoutingPolicyId o) {
    return COMPARATOR.compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RoutingPolicyId)) {
      return false;
    }
    RoutingPolicyId that = (RoutingPolicyId) o;
    return Objects.equals(_node, that._node) && Objects.equals(_policy, that._policy);
  }

  @Nonnull
  public String getNode() {
    return _node;
  }

  @Nonnull
  public String getPolicy() {
    return _policy;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_node, _policy);
  }
}
