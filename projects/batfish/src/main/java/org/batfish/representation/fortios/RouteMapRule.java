package org.batfish.representation.fortios;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** FortiOS datamodel component containing route-map rule configuration */
public class RouteMapRule implements Serializable {

  public enum Action {
    DENY,
    PERMIT,
  }

  public static final Action DEFAULT_ACTION = Action.PERMIT;

  public @Nonnull String getNumber() {
    return _number;
  }

  public @Nullable Action getAction() {
    return _action;
  }

  public @Nonnull Action getActionEffective() {
    return firstNonNull(_action, DEFAULT_ACTION);
  }

  /** Name of the access-list or prefix-list this rule matches on */
  public @Nullable String getMatchIpAddress() {
    return _matchIpAddress;
  }

  public void setAction(Action action) {
    _action = action;
  }

  public void setMatchIpAddress(String matchIpAddress) {
    _matchIpAddress = matchIpAddress;
  }

  public RouteMapRule(String number) {
    _number = number;
  }

  @Nonnull private final String _number;
  @Nullable private Action _action;
  @Nullable private String _matchIpAddress;
}
