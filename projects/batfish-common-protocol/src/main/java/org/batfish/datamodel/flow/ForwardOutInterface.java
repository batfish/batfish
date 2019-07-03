package org.batfish.datamodel.flow;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.visitors.SessionActionVisitor;

/**
 * A {@link SessionAction} whereby a return flow is forwarded out a specified interface to a
 * specified next hop with neither FIB resolution nor ARP lookup.
 */
@ParametersAreNonnullByDefault
public final class ForwardOutInterface implements SessionAction {

  private final @Nullable NodeInterfacePair _nextHop;
  private final @Nonnull String _outgoingInterface;

  public ForwardOutInterface(String outgoingInterface, NodeInterfacePair nextHop) {
    _outgoingInterface = outgoingInterface;
    _nextHop = nextHop;
  }

  @Override
  public <T> T accept(SessionActionVisitor<T> visitor) {
    return visitor.visitForwardOutInterface(this);
  }

  /**
   * The next hop and ingress interface for session traffic. If null, then the traffic should be
   * delivered to the attached subnet of the outgoing interface, or else should exit the network.
   */
  public @Nullable NodeInterfacePair getNextHop() {
    return _nextHop;
  }

  /** The interface out which return traffic should be sent. */
  public @Nonnull String getOutgoingInterface() {
    return _outgoingInterface;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ForwardOutInterface)) {
      return false;
    }
    ForwardOutInterface rhs = (ForwardOutInterface) o;
    return _outgoingInterface.equals(rhs._outgoingInterface)
        && Objects.equals(_nextHop, rhs._nextHop);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_outgoingInterface, _nextHop);
  }
}
