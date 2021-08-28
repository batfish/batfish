package org.batfish.vendor.check_point_management;

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

  private static final GatewayOrServerPolicy EMPTY = new GatewayOrServerPolicy(null, null);

  @VisibleForTesting
  public GatewayOrServerPolicy(
      @Nullable String accessPolicyName, @Nullable String threatPolicyName) {
    _accessPolicyName = accessPolicyName;
    _threatPolicyName = threatPolicyName;
  }

  @JsonCreator
  private static @Nonnull GatewayOrServerPolicy create(
      @JsonProperty(PROP_ACCESS_POLICY_INSTALLED) @Nullable Boolean accessPolicyInstalled,
      @JsonProperty(PROP_ACCESS_POLICY_NAME) @Nullable String accessPolicyName,
      @JsonProperty(PROP_THREAT_POLICY_INSTALLED) @Nullable Boolean threatPolicyInstalled,
      @JsonProperty(PROP_THREAT_POLICY_NAME) @Nullable String threatPolicyName) {
    return new GatewayOrServerPolicy(
        Boolean.TRUE.equals(accessPolicyInstalled) ? accessPolicyName : null,
        Boolean.TRUE.equals(threatPolicyInstalled) ? threatPolicyName : null);
  }

  /**
   * The source package for access and NAT rules for this device, if {@code access-policy-installed}
   * is {@code true}.
   */
  @Nullable
  public String getAccessPolicyName() {
    return _accessPolicyName;
  }

  /** The name of the installed package, if {@code threat-policy-installed} is {@code true}. */
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
    return Objects.equals(_accessPolicyName, that._accessPolicyName)
        && Objects.equals(_threatPolicyName, that._threatPolicyName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_accessPolicyName, _threatPolicyName);
  }

  @Override
  public @Nonnull String toString() {
    return toStringHelper(this)
        .omitNullValues()
        .add(PROP_ACCESS_POLICY_NAME, _accessPolicyName)
        .add(PROP_THREAT_POLICY_NAME, _threatPolicyName)
        .toString();
  }

  private static final String PROP_ACCESS_POLICY_INSTALLED = "accessPolicyInstalled";
  private static final String PROP_ACCESS_POLICY_NAME = "accessPolicyName";
  private static final String PROP_THREAT_POLICY_INSTALLED = "threatPolicyInstalled";
  private static final String PROP_THREAT_POLICY_NAME = "threatPolicyName";

  private final @Nullable String _accessPolicyName;
  private final @Nullable String _threatPolicyName;
}
