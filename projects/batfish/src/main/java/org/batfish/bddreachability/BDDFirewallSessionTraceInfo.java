package org.batfish.bddreachability;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** BDD version of {@link org.batfish.datamodel.flow.FirewallSessionTraceInfo}. */
final class BDDFirewallSessionTraceInfo {
  private final @Nonnull String _hostname;
  private final @Nonnull Set<String> _incomingInterfaces;
  private final @Nullable NodeInterfacePair _nextHop;
  private final @Nullable String _outgoingInterface;
  private final @Nonnull BDD _sessionFlows;
  private final @Nullable Transition _transformation;

  BDDFirewallSessionTraceInfo(
      @Nonnull String hostname,
      @Nonnull Set<String> incomingInterfaces,
      @Nullable NodeInterfacePair nextHop,
      @Nullable String outgoingInterface,
      @Nonnull BDD sessionFlows,
      @Nullable Transition transformation) {
    _hostname = hostname;
    _incomingInterfaces = incomingInterfaces;
    _nextHop = nextHop;
    _outgoingInterface = outgoingInterface;
    _sessionFlows = sessionFlows;
    _transformation = transformation;
  }

  @Nonnull
  public String getHostname() {
    return _hostname;
  }

  @Nonnull
  public Set<String> getIncomingInterfaces() {
    return _incomingInterfaces;
  }

  @Nullable
  public NodeInterfacePair getNextHop() {
    return _nextHop;
  }

  @Nullable
  public String getOutgoingInterface() {
    return _outgoingInterface;
  }

  @Nonnull
  public BDD getSessionFlows() {
    return _sessionFlows;
  }

  @Nullable
  public Transition getTransformation() {
    return _transformation;
  }
}
