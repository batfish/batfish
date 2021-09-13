package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** NAT settings attached to automatic-NAT-compatible objects, like {@link Network}. */
public class NatSettings implements Serializable {

  public boolean getAutoRule() {
    return _autoRule;
  }

  public @Nullable String getHideBehind() {
    return _hideBehind;
  }

  public @Nullable String getInstallOn() {
    return _installOn;
  }

  public @Nullable Ip getIpv4Address() {
    return _ipv4Address;
  }

  public @Nullable String getMethod() {
    return _method;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof NatSettings)) {
      return false;
    }
    NatSettings that = (NatSettings) o;
    return _autoRule == that._autoRule
        && Objects.equals(_hideBehind, that._hideBehind)
        && Objects.equals(_installOn, that._installOn)
        && Objects.equals(_ipv4Address, that._ipv4Address)
        && Objects.equals(_method, that._method);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_autoRule, _hideBehind, _installOn, _ipv4Address, _method);
  }

  @JsonCreator
  private static @Nonnull NatSettings create(
      @JsonProperty(PROP_AUTO_RULE) @Nullable Boolean autoRule,
      @JsonProperty(PROP_HIDE_BEHIND) @Nullable String hideBehind,
      @JsonProperty(PROP_INSTALL_ON) @Nullable String installOn,
      @JsonProperty(PROP_IPV4_ADDRESS) @Nullable Ip ipv4Address,
      @JsonProperty(PROP_METHOD) @Nullable String method) {
    checkArgument(autoRule != null, "Missing %s", PROP_AUTO_RULE);
    return new NatSettings(autoRule, hideBehind, installOn, ipv4Address, method);
  }

  @VisibleForTesting
  public NatSettings(
      boolean autoRule,
      @Nullable String hideBehind,
      @Nullable String installOn,
      @Nullable Ip ipv4Address,
      @Nullable String method) {
    _autoRule = autoRule;
    _hideBehind = hideBehind;
    _installOn = installOn;
    _ipv4Address = ipv4Address;
    _method = method;
  }

  private static final String PROP_AUTO_RULE = "auto-rule";
  private static final String PROP_HIDE_BEHIND = "hide-behind";
  private static final String PROP_INSTALL_ON = "install-on";
  private static final String PROP_IPV4_ADDRESS = "ipv4-address";
  private static final String PROP_METHOD = "method";
  private final boolean _autoRule;
  private final @Nullable String _hideBehind;
  private final @Nullable String _installOn;
  private final @Nullable Ip _ipv4Address;
  private final @Nullable String _method;
}
