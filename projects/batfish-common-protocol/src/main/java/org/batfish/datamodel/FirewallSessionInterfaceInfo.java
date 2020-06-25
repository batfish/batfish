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

/**
 * Data that determines how to create sessions for flows that routed out interfaces. In particular,
 * sessions are needed for bidirectional traceroute and reachability, specifically for return flows.
 */
@ParametersAreNonnullByDefault
public final class FirewallSessionInterfaceInfo implements Serializable {

  private static final String PROP_FIB_LOOKUP = "fibLookup";
  private static final String PROP_SESSION_INTERFACES = "sessionInterfaces";
  private static final String PROP_INCOMING_ACL_NAME = "incomingAclName";
  private static final String PROP_OUTGOING_ACL_NAME = "outgoingAclName";

  private final boolean _fibLookup;
  private final SortedSet<String> _sessionInterfaces;
  private final @Nullable String _incomingAclName;
  private final @Nullable String _outgoingAclName;

  public FirewallSessionInterfaceInfo(
      boolean fibLookup,
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
    _fibLookup = fibLookup;
  }

  @JsonCreator
  private static FirewallSessionInterfaceInfo jsonCreator(
      @JsonProperty(PROP_FIB_LOOKUP) boolean fibLookup,
      @JsonProperty(PROP_SESSION_INTERFACES) @Nullable Set<String> sessionInterfaces,
      @JsonProperty(PROP_INCOMING_ACL_NAME) @Nullable String incomingAclName,
      @JsonProperty(PROP_OUTGOING_ACL_NAME) @Nullable String outgoingAclName) {
    checkNotNull(sessionInterfaces, PROP_SESSION_INTERFACES + " cannot be null");
    return new FirewallSessionInterfaceInfo(
        fibLookup, sessionInterfaces, incomingAclName, outgoingAclName);
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
    return _fibLookup == that._fibLookup
        && Objects.equals(_sessionInterfaces, that._sessionInterfaces)
        && Objects.equals(_incomingAclName, that._incomingAclName)
        && Objects.equals(_outgoingAclName, that._outgoingAclName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_fibLookup, _sessionInterfaces, _incomingAclName, _outgoingAclName);
  }

  /** Whether session return traffic should be forwarded via a FIB lookup */
  @JsonProperty(PROP_FIB_LOOKUP)
  public boolean getFibLookup() {
    return _fibLookup;
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
