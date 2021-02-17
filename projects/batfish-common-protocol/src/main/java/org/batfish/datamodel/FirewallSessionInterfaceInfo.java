package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.ForwardOutInterface;
import org.batfish.datamodel.flow.PostNatFibLookup;
import org.batfish.datamodel.flow.SessionAction;

/**
 * Data that determines how to create sessions for flows that routed out interfaces. In particular,
 * sessions are needed for bidirectional traceroute and reachability, specifically for return flows.
 */
@ParametersAreNonnullByDefault
public final class FirewallSessionInterfaceInfo implements Serializable {
  /** Possible actions for traffic matching a session on this interface */
  public enum Action {
    /** Forward traffic directly out the interface where the original flow entered */
    NO_FIB_LOOKUP,
    /** Do a FIB lookup on the transformed return flow to determine egress interface */
    POST_NAT_FIB_LOOKUP;

    /**
     * Converts this {@link Action} to the corresponding {@link SessionAction}. Parameters are only
     * necessary when creating a {@link ForwardOutInterface} action.
     */
    public SessionAction toSessionAction(
        String originalFlowIngressIface, @Nullable NodeInterfacePair nextHop) {
      switch (this) {
        case NO_FIB_LOOKUP:
          return new ForwardOutInterface(originalFlowIngressIface, nextHop);
        case POST_NAT_FIB_LOOKUP:
          return PostNatFibLookup.INSTANCE;
        default:
          throw new UnsupportedOperationException("Unknown session action " + this);
      }
    }
  }

  private static final String PROP_ACTION = "action";
  private static final String PROP_FIB_LOOKUP = "fibLookup"; // for JSON backwards compatibility
  private static final String PROP_SESSION_INTERFACES = "sessionInterfaces";
  private static final String PROP_INCOMING_ACL_NAME = "incomingAclName";
  private static final String PROP_OUTGOING_ACL_NAME = "outgoingAclName";

  private final Action _action;
  private final SortedSet<String> _sessionInterfaces;
  private final @Nullable String _incomingAclName;
  private final @Nullable String _outgoingAclName;

  public FirewallSessionInterfaceInfo(
      Action action,
      Iterable<String> sessionInterfaces,
      @Nullable String incomingAclName,
      @Nullable String outgoingAclName) {
    // A FirewallSessionInterfaceInfo with no interfaces wouldn't create or match any sessions.
    // In this case the interface should just have null FirewallSessionInterfaceInfo.
    checkArgument(
        sessionInterfaces.iterator().hasNext(),
        "Cannot create FirewallSessionInterfaceInfo with zero session interfaces.");
    _sessionInterfaces = ImmutableSortedSet.copyOf(sessionInterfaces);
    _incomingAclName = incomingAclName;
    _outgoingAclName = outgoingAclName;
    _action = action;
  }

  @JsonCreator
  private static FirewallSessionInterfaceInfo jsonCreator(
      @JsonProperty(PROP_ACTION) @Nullable Action action,
      @JsonProperty(PROP_FIB_LOOKUP) boolean fibLookup,
      @JsonProperty(PROP_SESSION_INTERFACES) @Nullable Set<String> sessionInterfaces,
      @JsonProperty(PROP_INCOMING_ACL_NAME) @Nullable String incomingAclName,
      @JsonProperty(PROP_OUTGOING_ACL_NAME) @Nullable String outgoingAclName) {
    checkNotNull(sessionInterfaces, PROP_SESSION_INTERFACES + " cannot be null");
    if (action == null) {
      // for backwards compatibility
      action = fibLookup ? Action.POST_NAT_FIB_LOOKUP : Action.NO_FIB_LOOKUP;
    }
    return new FirewallSessionInterfaceInfo(
        action, sessionInterfaces, incomingAclName, outgoingAclName);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FirewallSessionInterfaceInfo)) {
      return false;
    }
    FirewallSessionInterfaceInfo that = (FirewallSessionInterfaceInfo) o;
    return _action == that._action
        && Objects.equals(_sessionInterfaces, that._sessionInterfaces)
        && Objects.equals(_incomingAclName, that._incomingAclName)
        && Objects.equals(_outgoingAclName, that._outgoingAclName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_action.ordinal(), _sessionInterfaces, _incomingAclName, _outgoingAclName);
  }

  /** What {@link Action} should be taken for return traffic that matches a session */
  @JsonProperty(PROP_FIB_LOOKUP)
  public Action getAction() {
    return _action;
  }

  /** The set of interfaces through which return flows can enter. */
  @JsonProperty(PROP_SESSION_INTERFACES)
  public Set<String> getSessionInterfaces() {
    return _sessionInterfaces;
  }

  /** The name of the incoming ACL for sessions that enter this interface. */
  @JsonProperty(PROP_INCOMING_ACL_NAME)
  public @Nullable String getIncomingAclName() {
    return _incomingAclName;
  }

  /** The name of the outgoing ACL for sessions that exit this interface. */
  @JsonProperty(PROP_OUTGOING_ACL_NAME)
  public @Nullable String getOutgoingAclName() {
    return _outgoingAclName;
  }
}
