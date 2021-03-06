package org.batfish.representation.fortios;

import com.google.common.annotations.VisibleForTesting;
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
  public Set<Interface> getSrcIntf() {
    return _srcIntf;
  }

  @Nonnull
  public Set<Interface> getDstIntf() {
    return _dstIntf;
  }

  @Nonnull
  public Set<Address> getSrcAddr() {
    return _srcAddr;
  }

  @Nonnull
  public Set<Address> getDstAddr() {
    return _dstAddr;
  }

  @Nonnull
  public Set<Service> getService() {
    return _service;
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
  }

  @Nonnull private String _number;
  @Nullable private String _name;
  @Nonnull private final Set<Interface> _srcIntf;
  @Nonnull private final Set<Interface> _dstIntf;
  @Nonnull private final Set<Address> _srcAddr;
  @Nonnull private final Set<Address> _dstAddr;
  @Nonnull private final Set<Service> _service;
  @Nullable private Status _status;
  @Nullable private String _comments;
  @Nullable private Action _action;
}
