package org.batfish.representation.fortios;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** FortiOS datamodel component containing firewall policy configuration */
public final class Policy implements Serializable {
  public static final String ALL_ADDRESSES = "all";
  public static final String ALL_SERVICE = "ALL";
  public static final String ANY_INTERFACE = "any";

  public static final Action DEFAULT_ACTION = Action.DENY;
  public static final Status DEFAULT_STATUS = Status.ENABLE;

  public enum Status {
    ENABLE,
    DISABLE,
  }

  public enum Action {
    ALLOW,
    DENY,
    IPSEC,
  }

  @Nonnull
  public String getNumber() {
    return _number;
  }

  @Nullable
  public String getName() {
    return _name;
  }

  @VisibleForTesting
  @Nullable
  public Action getAction() {
    return _action;
  }

  @Nonnull
  public Action getActionEffective() {
    return _action == null ? DEFAULT_ACTION : _action;
  }

  @Nullable
  public String getComments() {
    return _comments;
  }

  @VisibleForTesting
  @Nullable
  public Status getStatus() {
    return _status;
  }

  @Nonnull
  public Status getStatusEffective() {
    return _status == null ? DEFAULT_STATUS : _status;
  }

  @Nonnull
  public Set<String> getSrcIntf() {
    return _srcIntf;
  }

  @Nonnull
  public Set<String> getDstIntf() {
    return _dstIntf;
  }

  public @Nullable Set<String> getSrcAddr() {
    return _srcAddr;
  }

  /** Set of Batfish-internal UUIDs associated with srcaddr references. */
  @Nonnull
  public Set<BatfishUUID> getSrcAddrUUIDs() {
    return _srcAddrUuids;
  }

  public @Nullable Set<String> getDstAddr() {
    return _dstAddr;
  }

  /** Set of Batfish-internal UUIDs associated with dstaddr references. */
  @Nonnull
  public Set<BatfishUUID> getDstAddrUUIDs() {
    return _dstAddrUuids;
  }

  public @Nullable Set<String> getService() {
    return _service;
  }

  public void setDstAddr(Set<String> dstAddr) {
    _dstAddr = ImmutableSet.copyOf(dstAddr);
  }

  /** Returns message indicating why this policy can't be committed in the CLI, or null if it can */
  public @Nullable String getInvalidReason() {
    // _valid indicates whether any invalid lines have gone into this policy that would cause the
    // CLI to pop out of its edit block
    if (!_valid) {
      return "name is invalid"; // currently, only invalid name can cause valid to be false
    } else if (_srcIntf.isEmpty()) {
      return "srcintf must be set";
    } else if (_dstIntf.isEmpty()) {
      return "dstintf must be set";
    } else if (_srcAddrUuids.isEmpty()) {
      return "srcaddr must be set";
    } else if (_dstAddrUuids.isEmpty()) {
      return "dstaddr must be set";
    } else if (_serviceUuids.isEmpty()) {
      return "service must be set";
    }
    // TODO "schedule" must be set to commit policy, but we don't parse it. Should we?
    return null;
  }

  /** Set of Batfish-internal UUIDs associated with service references. */
  @Nonnull
  public Set<BatfishUUID> getServiceUUIDs() {
    return _serviceUuids;
  }

  public void setAction(Action action) {
    _action = action;
  }

  public void setComments(String comments) {
    _comments = comments;
  }

  public void setName(String name) {
    _name = name;
  }

  public void setService(Set<String> service) {
    _service = ImmutableSet.copyOf(service);
  }

  public void setSrcAddr(Set<String> srcAddr) {
    _srcAddr = ImmutableSet.copyOf(srcAddr);
  }

  public void setStatus(Status status) {
    _status = status;
  }

  public void setValid(boolean valid) {
    _valid = valid;
  }

  public Policy(String number) {
    _number = number;
    _srcIntf = new HashSet<>();
    _dstIntf = new HashSet<>();
    _srcAddr = new HashSet<>();
    _dstAddr = new HashSet<>();
    _service = new HashSet<>();

    _srcAddrUuids = new HashSet<>();
    _dstAddrUuids = new HashSet<>();
    _serviceUuids = new HashSet<>();
  }

  @Nonnull private String _number;
  @Nullable private String _name;
  @Nonnull private final Set<String> _srcIntf;
  @Nonnull private final Set<String> _dstIntf;
  @Nullable private Set<String> _srcAddr;
  @Nonnull private final Set<BatfishUUID> _srcAddrUuids;
  @Nullable private Set<String> _dstAddr;
  @Nonnull private final Set<BatfishUUID> _dstAddrUuids;
  @Nullable private Set<String> _service;
  @Nonnull private final Set<BatfishUUID> _serviceUuids;
  @Nullable private Status _status;
  @Nullable private String _comments;
  @Nullable private Action _action;

  // Whether this policy has invalid lines that would prevent it from being committed in CLI
  private transient boolean _valid;
}
