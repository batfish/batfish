package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
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
import org.batfish.datamodel.flow.PreNatFibLookup;
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
    FORWARD_OUT_IFACE,
    /** Do a FIB lookup on the untransformed return flow to determine egress interface */
    PRE_NAT_FIB_LOOKUP,
    /** Do a FIB lookup on the transformed return flow to determine egress interface */
    POST_NAT_FIB_LOOKUP;

    /**
     * Converts this {@link Action} to the corresponding {@link SessionAction}. Parameters are only
     * necessary when creating a {@link ForwardOutInterface} action. Never returns {@link
     * org.batfish.datamodel.flow.Accept}; caller is expected to determine whether a return flow
     * should be accepted.
     */
    public SessionAction toSessionAction(
        String originalFlowIngressIface, @Nullable NodeInterfacePair nextHop) {
      return switch (this) {
        case FORWARD_OUT_IFACE -> new ForwardOutInterface(originalFlowIngressIface, nextHop);
        case PRE_NAT_FIB_LOOKUP -> PreNatFibLookup.INSTANCE;
        case POST_NAT_FIB_LOOKUP -> PostNatFibLookup.INSTANCE;
      };
    }
  }

  private static final String PROP_ACTION = "action";
  private static final String PROP_FIB_LOOKUP = "fibLookup"; // for JSON backwards compatibility
  private static final String PROP_SESSION_INTERFACES = "sessionInterfaces";
  private static final String PROP_SOURCES = "sources";
  private static final String PROP_INCOMING_ACL_NAME = "incomingAclName";
  private static final String PROP_OUTGOING_ACL_NAME = "outgoingAclName";

  private final Action _action;
  private final SortedSet<String> _sessionInterfaces;
  private final @Nullable Set<String> _sources;
  private final @Nullable String _incomingAclName;
  private final @Nullable String _outgoingAclName;

  /**
   * Creates session info that sets up a session for any outgoing traffic.
   *
   * @see #FirewallSessionInterfaceInfo(Action, Iterable, Iterable, String, String)
   */
  public FirewallSessionInterfaceInfo(
      Action action,
      Iterable<String> sessionInterfaces,
      @Nullable String incomingAclName,
      @Nullable String outgoingAclName) {
    this(action, sessionInterfaces, null, incomingAclName, outgoingAclName);
  }

  /**
   * @param action {@link Action} to take on a matching return flow
   * @param sessionInterfaces This session can be set up when a flow exits these interfaces
   * @param sources This session can be set up by flows from these sources, which may include both
   *     interface names and {@link
   *     org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists#SOURCE_ORIGINATING_FROM_DEVICE}
   * @param incomingAclName Session flows entering an interface with this session info will be
   *     filtered through this ACL
   * @param outgoingAclName Session flows exiting an interface with this session info will be
   *     filtered through this ACL
   */
  public FirewallSessionInterfaceInfo(
      Action action,
      Iterable<String> sessionInterfaces,
      @Nullable Iterable<String> sources,
      @Nullable String incomingAclName,
      @Nullable String outgoingAclName) {
    // A FirewallSessionInterfaceInfo with no interfaces wouldn't create or match any sessions.
    // In this case the interface should just have null FirewallSessionInterfaceInfo.
    checkArgument(
        sessionInterfaces.iterator().hasNext(),
        "Cannot create FirewallSessionInterfaceInfo with zero session interfaces.");
    _sessionInterfaces = ImmutableSortedSet.copyOf(sessionInterfaces);
    _sources = sources == null ? null : ImmutableSet.copyOf(sources);
    _incomingAclName = incomingAclName;
    _outgoingAclName = outgoingAclName;
    _action = action;
  }

  @JsonCreator
  private static FirewallSessionInterfaceInfo jsonCreator(
      @JsonProperty(PROP_ACTION) @Nullable Action action,
      @JsonProperty(PROP_FIB_LOOKUP) boolean fibLookup,
      @JsonProperty(PROP_SESSION_INTERFACES) @Nullable Set<String> sessionInterfaces,
      @JsonProperty(PROP_SOURCES) @Nullable Set<String> sources,
      @JsonProperty(PROP_INCOMING_ACL_NAME) @Nullable String incomingAclName,
      @JsonProperty(PROP_OUTGOING_ACL_NAME) @Nullable String outgoingAclName) {
    checkNotNull(sessionInterfaces, PROP_SESSION_INTERFACES + " cannot be null");
    Action backwardsCompatibleAction =
        action != null ? action : fibLookup ? Action.POST_NAT_FIB_LOOKUP : Action.FORWARD_OUT_IFACE;

    return new FirewallSessionInterfaceInfo(
        backwardsCompatibleAction, sessionInterfaces, sources, incomingAclName, outgoingAclName);
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
        && Objects.equals(_sources, that._sources)
        && Objects.equals(_incomingAclName, that._incomingAclName)
        && Objects.equals(_outgoingAclName, that._outgoingAclName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _action.ordinal(), _sessionInterfaces, _sources, _incomingAclName, _outgoingAclName);
  }

  /** What {@link Action} should be taken for return traffic that matches a session */
  @JsonProperty(PROP_ACTION)
  public Action getAction() {
    return _action;
  }

  /** The set of interfaces through which return flows can enter. */
  @JsonProperty(PROP_SESSION_INTERFACES)
  public Set<String> getSessionInterfaces() {
    return _sessionInterfaces;
  }

  /**
   * Session can only be set up by flows that came from these sources, which may include both names
   * of ingress interfaces and {@link
   * org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists#SOURCE_ORIGINATING_FROM_DEVICE}. If
   * {@code null}, the session can be set up by flows from any source.
   */
  @JsonProperty(PROP_SOURCES)
  public @Nullable Set<String> getSources() {
    return _sources;
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

  /**
   * Convenience method: whether session should be set up for a flow from the given {@code src}
   *
   * @param src Name of flow's ingress interface, or {@link
   *     org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists#SOURCE_ORIGINATING_FROM_DEVICE}
   *     if the flow originates from the device.
   */
  public boolean canSetUpSessionForFlowFrom(String src) {
    return _sources == null || _sources.contains(src);
  }
}
