package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
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
@JsonTypeName("ForwardOutInterface")
@ParametersAreNonnullByDefault
public final class ForwardOutInterface implements SessionAction {

  private static final String PROP_NEXT_HOP = "nextHop";
  private static final String PROP_OUTGOING_INTERFACE = "outgoingInterface";

  private final @Nullable NodeInterfacePair _nextHop;
  private final @Nonnull String _outgoingInterface;

  public ForwardOutInterface(String outgoingInterface, @Nullable NodeInterfacePair nextHop) {
    _outgoingInterface = outgoingInterface;
    _nextHop = nextHop;
  }

  @JsonCreator
  private static ForwardOutInterface jsonCreator(
      @JsonProperty(PROP_NEXT_HOP) @Nullable NodeInterfacePair nextHop,
      @JsonProperty(PROP_OUTGOING_INTERFACE) @Nullable String outgoingInterface) {
    checkArgument(
        outgoingInterface != null, "ForwardOutInterface missing %s", PROP_OUTGOING_INTERFACE);
    return new ForwardOutInterface(outgoingInterface, nextHop);
  }

  @Override
  public <T> T accept(SessionActionVisitor<T> visitor) {
    return visitor.visitForwardOutInterface(this);
  }

  /**
   * The next hop and ingress interface for session traffic. If null, then the traffic should be
   * delivered to the attached subnet of the outgoing interface, or else should exit the network.
   */
  @JsonProperty(PROP_NEXT_HOP)
  public @Nullable NodeInterfacePair getNextHop() {
    return _nextHop;
  }

  /** The interface out which return traffic should be sent. */
  @JsonProperty(PROP_OUTGOING_INTERFACE)
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
