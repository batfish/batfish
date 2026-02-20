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
    ACCEPT,
    DENY,
    IPSEC,
  }

  public @Nonnull String getNumber() {
    return _number;
  }

  public @Nullable String getName() {
    return _name;
  }

  @VisibleForTesting
  public @Nullable Action getAction() {
    return _action;
  }

  public @Nonnull Action getActionEffective() {
    return _action == null ? DEFAULT_ACTION : _action;
  }

  public @Nullable String getComments() {
    return _comments;
  }

  @VisibleForTesting
  public @Nullable Status getStatus() {
    return _status;
  }

  public @Nonnull Status getStatusEffective() {
    return _status == null ? DEFAULT_STATUS : _status;
  }

  public @Nonnull Set<String> getSrcIntf() {
    return _srcIntf;
  }

  /**
   * Names of zones referenced in srcintf field in this policy. Should be derived from {@link
   * this#getSrcIntfZoneUUIDs} when finishing building the VS model.
   */
  public @Nullable Set<String> getSrcIntfZones() {
    return _srcIntfZones;
  }

  /** Set of Batfish-internal UUIDs associated with srcintf zone references. */
  public @Nonnull Set<BatfishUUID> getSrcIntfZoneUUIDs() {
    return _srcIntfZoneUuids;
  }

  public @Nonnull Set<String> getDstIntf() {
    return _dstIntf;
  }

  /**
   * Names of zones referenced in dstintf field in this policy. Should be derived from {@link
   * this#getDstIntfZoneUUIDs} when finishing building the VS model.
   */
  public @Nullable Set<String> getDstIntfZones() {
    return _dstIntfZones;
  }

  /** Set of Batfish-internal UUIDs associated with dstintf zone references. */
  public @Nonnull Set<BatfishUUID> getDstIntfZoneUUIDs() {
    return _dstIntfZoneUuids;
  }

  public @Nullable Set<String> getSrcAddr() {
    return _srcAddr;
  }

  /** Set of Batfish-internal UUIDs associated with srcaddr references. */
  public @Nonnull Set<BatfishUUID> getSrcAddrUUIDs() {
    return _srcAddrUuids;
  }

  public @Nullable Set<String> getDstAddr() {
    return _dstAddr;
  }

  /** Set of Batfish-internal UUIDs associated with dstaddr references. */
  public @Nonnull Set<BatfishUUID> getDstAddrUUIDs() {
    return _dstAddrUuids;
  }

  public @Nullable Set<String> getService() {
    return _service;
  }

  public @Nullable Boolean getNat() {
    return _nat;
  }

  public @Nullable Boolean getIppool() {
    return _ippool;
  }

  public @Nonnull Set<String> getPoolnames() {
    return _poolnames;
  }

  /** Set of Batfish-internal UUIDs associated with poolname references. */
  public @Nonnull Set<BatfishUUID> getPoolnameUUIDs() {
    return _poolnameUuids;
  }

  public void setNat(@Nullable Boolean nat) {
    _nat = nat;
  }

  public void setIppool(@Nullable Boolean ippool) {
    _ippool = ippool;
  }

  public void setPoolnames(Set<String> poolnames) {
    _poolnames = ImmutableSet.copyOf(poolnames);
  }

  public void setDstAddr(Set<String> dstAddr) {
    _dstAddr = ImmutableSet.copyOf(dstAddr);
  }

  /** Set of Batfish-internal UUIDs associated with service references. */
  public @Nonnull Set<BatfishUUID> getServiceUUIDs() {
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

  public void setNumber(String number) {
    _number = number;
  }

  public void setService(Set<String> service) {
    _service = ImmutableSet.copyOf(service);
  }

  public void setSrcIntfZones(Set<String> srcintfZones) {
    _srcIntfZones = ImmutableSet.copyOf(srcintfZones);
  }

  public void setDstIntfZones(Set<String> dstintfZones) {
    _dstIntfZones = ImmutableSet.copyOf(dstintfZones);
  }

  public void setSrcAddr(Set<String> srcAddr) {
    _srcAddr = ImmutableSet.copyOf(srcAddr);
  }

  public void setStatus(Status status) {
    _status = status;
  }

  public Policy(String number) {
    _number = number;
    _srcIntf = new HashSet<>();
    _dstIntf = new HashSet<>();
    _srcAddr = new HashSet<>();
    _dstAddr = new HashSet<>();
    _service = new HashSet<>();

    _srcIntfZoneUuids = new HashSet<>();
    _dstIntfZoneUuids = new HashSet<>();
    _srcAddrUuids = new HashSet<>();
    _dstAddrUuids = new HashSet<>();
    _serviceUuids = new HashSet<>();
    _poolnames = new HashSet<>();
    _poolnameUuids = new HashSet<>();
  }

  private @Nonnull String _number;
  private @Nullable String _name;
  private final @Nonnull Set<String> _srcIntf;
  private @Nullable Set<String> _srcIntfZones;
  private final @Nonnull Set<BatfishUUID> _srcIntfZoneUuids;
  private final @Nonnull Set<String> _dstIntf;
  private @Nullable Set<String> _dstIntfZones;
  private final @Nonnull Set<BatfishUUID> _dstIntfZoneUuids;
  private @Nullable Set<String> _srcAddr;
  private final @Nonnull Set<BatfishUUID> _srcAddrUuids;
  private @Nullable Set<String> _dstAddr;
  private final @Nonnull Set<BatfishUUID> _dstAddrUuids;
  private @Nullable Set<String> _service;
  private final @Nonnull Set<BatfishUUID> _serviceUuids;
  private @Nullable Status _status;
  private @Nullable String _comments;
  private @Nullable Action _action;
  private @Nullable Boolean _nat;
  private @Nullable Boolean _ippool;
  private @Nonnull Set<String> _poolnames;
  private final @Nonnull Set<BatfishUUID> _poolnameUuids;
}
