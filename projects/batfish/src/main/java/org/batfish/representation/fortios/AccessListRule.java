package org.batfish.representation.fortios;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

/** FortiOS datamodel component containing access-list rule configuration */
public class AccessListRule implements Serializable {
  public enum Action {
    DENY,
    PERMIT,
  }

  public static final Action DEFAULT_ACTION = Action.PERMIT;
  public static final boolean DEFAULT_EXACT_MATCH = false;

  public @Nonnull String getNumber() {
    return _number;
  }

  public @Nullable Action getAction() {
    return _action;
  }

  public @Nonnull Action getActionEffective() {
    return firstNonNull(_action, DEFAULT_ACTION);
  }

  public @Nullable Boolean getExactMatch() {
    return _exactMatch;
  }

  public boolean getExactMatchEffective() {
    return firstNonNull(_exactMatch, DEFAULT_EXACT_MATCH);
  }

  public @Nullable Prefix getPrefix() {
    return _prefix;
  }

  public @Nullable IpWildcard getWildcard() {
    return _wildcard;
  }

  public void setAction(Action action) {
    _action = action;
  }

  public void setExactMatch(boolean exactMatch) {
    _exactMatch = exactMatch;
  }

  public void setPrefix(Prefix prefix) {
    _wildcard = null;
    _prefix = prefix;
  }

  public void setWildcard(IpWildcard wildcard) {
    _prefix = null;
    _wildcard = wildcard;
  }

  public AccessListRule(String number) {
    _number = number;
  }

  private final @Nonnull String _number;
  private @Nullable Action _action;
  private @Nullable Prefix _prefix;
  private @Nullable IpWildcard _wildcard;
  private @Nullable Boolean _exactMatch;
}
