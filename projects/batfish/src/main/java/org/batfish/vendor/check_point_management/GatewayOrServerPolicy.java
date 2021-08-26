package org.batfish.vendor.check_point_management;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Policy information on a gateway or server from the {@code objects} field of the response to the
 * {@code show-gateways-and-servers} command.
 */
public final class GatewayOrServerPolicy implements Serializable {

  public static @Nonnull GatewayOrServerPolicy empty() {
    return EMPTY;
  }

  private static final GatewayOrServerPolicy EMPTY =
      new GatewayOrServerPolicy(false, null, false, null);

  @VisibleForTesting
  GatewayOrServerPolicy(
      boolean accessPolicyInstalled,
      @Nullable String accessPolicyName,
      boolean threatPolicyInstalled,
      @Nullable String threatPolicyName) {
    _accessPolicyInstalled = accessPolicyInstalled;
    _accessPolicyName = accessPolicyName;
    _threatPolicyInstalled = threatPolicyInstalled;
    _threatPolicyName = threatPolicyName;
  }

  @JsonCreator
  private static @Nonnull GatewayOrServerPolicy create(
      @JsonProperty(PROP_ACCESS_POLICY_INSTALLED) @Nullable Boolean accessPolicyInstalled,
      @JsonProperty(PROP_ACCESS_POLICY_NAME) @Nullable String accessPolicyName,
      @JsonProperty(PROP_THREAT_POLICY_INSTALLED) @Nullable Boolean threatPolicyInstalled,
      @JsonProperty(PROP_THREAT_POLICY_NAME) @Nullable String threatPolicyName) {
    return new GatewayOrServerPolicy(
        firstNonNull(accessPolicyInstalled, Boolean.FALSE),
        accessPolicyName,
        firstNonNull(threatPolicyInstalled, Boolean.FALSE),
        threatPolicyName);
  }

  /** Whether to install access and NAT rules on this device. */
  public boolean isAccessPolicyInstalled() {
    return _accessPolicyInstalled;
  }

  /**
   * The source package for access and NAT rules for this device, if {@link
   * #isAccessPolicyInstalled()} is {@code true}.
   */
  @Nullable
  public String getAccessPolicyName() {
    return _accessPolicyName;
  }

  /** Whether to install threat policy on this device. {@code null} if inapplicable. */
  public boolean isThreatPolicyInstalled() {
    return _threatPolicyInstalled;
  }

  /** The name of the installed package, if {@link #isThreatPolicyInstalled()} is {@code true}. */
  @Nullable
  public String getThreatPolicyName() {
    return _threatPolicyName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof GatewayOrServerPolicy)) {
      return false;
    }
    GatewayOrServerPolicy that = (GatewayOrServerPolicy) o;
    return _accessPolicyInstalled == that._accessPolicyInstalled
        && Objects.equals(_accessPolicyName, that._accessPolicyName)
        && _threatPolicyInstalled == that._threatPolicyInstalled
        && Objects.equals(_threatPolicyName, that._threatPolicyName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _accessPolicyInstalled, _accessPolicyName, _threatPolicyInstalled, _threatPolicyName);
  }

  @Override
  public @Nonnull String toString() {
    return toStringHelper(this)
        .omitNullValues()
        .add(PROP_ACCESS_POLICY_INSTALLED, _accessPolicyInstalled)
        .add(PROP_ACCESS_POLICY_NAME, _accessPolicyName)
        .add(PROP_THREAT_POLICY_INSTALLED, _threatPolicyInstalled)
        .add(PROP_THREAT_POLICY_NAME, _threatPolicyName)
        .toString();
  }

  private static final String PROP_ACCESS_POLICY_INSTALLED = "accessPolicyInstalled";
  private static final String PROP_ACCESS_POLICY_NAME = "accessPolicyName";
  private static final String PROP_THREAT_POLICY_INSTALLED = "threatPolicyInstalled";
  private static final String PROP_THREAT_POLICY_NAME = "threatPolicyName";

  private final @Nonnull boolean _accessPolicyInstalled;
  private final @Nullable String _accessPolicyName;
  private final @Nonnull boolean _threatPolicyInstalled;
  private final @Nullable String _threatPolicyName;
}
