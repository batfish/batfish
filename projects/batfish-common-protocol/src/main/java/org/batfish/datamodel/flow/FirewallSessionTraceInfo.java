package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.transformation.Transformation;

/**
 * Data about a firewall session created by a specific {@link Trace trace}. Currently assumes that
 * the outgoing interface and nextHop are predetermined by the session. If this is not the case
 * (i.e. we need to consult the FIB), we need to be careful about whether to apply the
 * transformation before or after the transformation. Leaving that as a TODO for now.
 */
@ParametersAreNonnullByDefault
public final class FirewallSessionTraceInfo {
  private final @Nonnull String _hostname;
  private final @Nonnull Set<String> _incomingInterfaces;
  private final @Nullable NodeInterfacePair _nextHop;
  private final @Nullable String _outgoingInterface;
  private final @Nonnull AclLineMatchExpr _sessionFlows;
  private final @Nullable Transformation _transformation;

  public FirewallSessionTraceInfo(
      @Nonnull String hostname,
      @Nullable String outgoingInterface,
      @Nullable NodeInterfacePair nextHop,
      @Nonnull Set<String> incomingInterfaces,
      @Nonnull AclLineMatchExpr sessionFlows,
      @Nullable Transformation transformation) {
    checkArgument(
        !(outgoingInterface == null && nextHop != null),
        "Cannot have a nextHop without an outgoingInterface");
    _hostname = hostname;
    _outgoingInterface = outgoingInterface;
    _nextHop = nextHop;
    _incomingInterfaces = incomingInterfaces;
    _sessionFlows = sessionFlows;
    _transformation = transformation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FirewallSessionTraceInfo)) {
      return false;
    }
    FirewallSessionTraceInfo that = (FirewallSessionTraceInfo) o;
    return Objects.equals(_hostname, that._hostname)
        && Objects.equals(_incomingInterfaces, that._incomingInterfaces)
        && Objects.equals(_nextHop, that._nextHop)
        && Objects.equals(_outgoingInterface, that._outgoingInterface)
        && Objects.equals(_sessionFlows, that._sessionFlows)
        && Objects.equals(_transformation, that._transformation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _hostname,
        _incomingInterfaces,
        _nextHop,
        _outgoingInterface,
        _sessionFlows,
        _transformation);
  }

  /** The hostname where the session exists. */
  public @Nonnull String getHostname() {
    return _hostname;
  }

  /** The interfaces through which the flow is allowed to enter. */
  public @Nonnull Set<String> getIncomingInterfaces() {
    return _incomingInterfaces;
  }

  /**
   * The next hop and ingress interface for session traffic. If null, and outgoing interface is not
   * null, then the traffic should be delivered to the attached subnet of the outgoing interface, or
   * else should exit the network.
   */
  public @Nullable NodeInterfacePair getNextHop() {
    return _nextHop;
  }

  /**
   * The interface out which session traffic is to be forwarded. If null, traffic should be
   * delivered to the device itself.
   */
  public @Nullable String getOutgoingInterface() {
    return _outgoingInterface;
  }

  /**
   * The allowed ingress flows for the session. Not allowed to contain {@link
   * org.batfish.datamodel.acl.PermittedByAcl} or {@link org.batfish.datamodel.IpSpaceReference}.
   */
  public @Nonnull AclLineMatchExpr getSessionFlows() {
    return _sessionFlows;
  }

  /** The (optional) transformation for session traffic. */
  @Nullable
  public Transformation getTransformation() {
    return _transformation;
  }
}
