package org.batfish.representation.fortios;

import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** FortiOS datamodel component containing firewall policy configuration */
public final class Policy implements Serializable {

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

  @Nonnull
  public Set<Object> getSrcAddr() {
    return _srcAddr;
  }

  @Nonnull
  public Set<Object> getDstAddr() {
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
  @Nonnull private Set<String> _srcIntf;
  @Nonnull private Set<String> _dstIntf;
  @Nonnull private Set<Object> _srcAddr;
  @Nonnull private Set<Object> _dstAddr;
  @Nonnull private Set<Service> _service;
  @Nullable private Status _status;
  @Nullable private String _comments;
  @Nullable private Action _action;
}
