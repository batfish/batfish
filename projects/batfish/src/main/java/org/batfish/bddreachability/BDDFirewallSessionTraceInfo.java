package org.batfish.bddreachability;

import com.google.common.base.MoreObjects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.datamodel.flow.SessionAction;

/** BDD version of {@link org.batfish.datamodel.flow.FirewallSessionTraceInfo}. */
@ParametersAreNonnullByDefault
final class BDDFirewallSessionTraceInfo {
  private final @Nonnull String _hostname;
  private final @Nonnull Set<String> _incomingInterfaces;
  private final @Nullable SessionAction _action;
  private final @Nonnull BDD _sessionFlows;
  private final @Nonnull Transition _transformation;

  BDDFirewallSessionTraceInfo(
      String hostname,
      Set<String> incomingInterfaces,
      SessionAction action,
      BDD sessionFlows,
      Transition transformation) {
    _hostname = hostname;
    _incomingInterfaces = incomingInterfaces;
    _action = action;
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

  /** The action to take on return traffic. */
  @Nonnull
  public SessionAction getAction() {
    return _action;
  }

  @Nonnull
  public BDD getSessionFlows() {
    return _sessionFlows;
  }

  @Nonnull
  public Transition getTransformation() {
    return _transformation;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(BDDFirewallSessionTraceInfo.class)
        .omitNullValues()
        .add("hostname", _hostname)
        .add("incomingInterfaces", _incomingInterfaces)
        .add("action", _action)
        // sessionFlows deliberately omitted since it's not readable
        .add("transformation", _transformation)
        .toString();
  }
}
