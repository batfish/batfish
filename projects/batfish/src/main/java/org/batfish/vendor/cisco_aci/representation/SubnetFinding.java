package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a finding from subnet overlap analysis in ACI bridge domains.
 *
 * <p>Subnet findings can include:
 *
 * <ul>
 *   <li>Overlapping subnets within the same VRF (CRITICAL - will cause routing issues)
 *   <li>Overlapping subnets across different VRFs (INFO - may be intentional)
 *   <li>Duplicate subnets in different bridge domains (HIGH - configuration review needed)
 *   <li>Bridge domains with no subnets configured (LOW - may be intentional)
 *   <li>Invalid subnet formats (MEDIUM - configuration error)
 * </ul>
 */
public class SubnetFinding implements Serializable {

  private static final String PROP_SEVERITY = "severity";
  private static final String PROP_CATEGORY = "category";
  private static final String PROP_BRIDGE_DOMAIN_1 = "bridgeDomain1";
  private static final String PROP_VRF_1 = "vrf1";
  private static final String PROP_SUBNET_1 = "subnet1";
  private static final String PROP_BRIDGE_DOMAIN_2 = "bridgeDomain2";
  private static final String PROP_VRF_2 = "vrf2";
  private static final String PROP_SUBNET_2 = "subnet2";
  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_RECOMMENDATION = "recommendation";

  /** Severity levels for subnet findings. */
  public enum Severity {
    /** Critical issues that will cause network problems */
    CRITICAL,
    /** High priority issues that should be reviewed */
    HIGH,
    /** Medium priority issues */
    MEDIUM,
    /** Low priority informational findings */
    LOW,
    /** Informational findings that may be intentional */
    INFO
  }

  /** Categories of subnet findings. */
  public enum Category {
    /** Overlapping subnets within the same VRF */
    OVERLAP_SAME_VRF,
    /** Overlapping subnets across different VRFs */
    OVERLAP_DIFF_VRF,
    /** Identical subnets in different bridge domains */
    DUPLICATE,
    /** Bridge domain with no subnets configured */
    NO_SUBNET,
    /** Invalid subnet format or mask length */
    INVALID_FORMAT
  }

  private final Severity _severity;
  private final Category _category;
  private final String _bridgeDomain1;
  private final String _vrf1;
  private final String _subnet1;
  private final @Nullable String _bridgeDomain2;
  private final @Nullable String _vrf2;
  private final @Nullable String _subnet2;
  private final String _description;
  private final String _recommendation;

  @JsonCreator
  public SubnetFinding(
      @JsonProperty(PROP_SEVERITY) Severity severity,
      @JsonProperty(PROP_CATEGORY) Category category,
      @JsonProperty(PROP_BRIDGE_DOMAIN_1) String bridgeDomain1,
      @JsonProperty(PROP_VRF_1) String vrf1,
      @JsonProperty(PROP_SUBNET_1) String subnet1,
      @JsonProperty(PROP_BRIDGE_DOMAIN_2) @Nullable String bridgeDomain2,
      @JsonProperty(PROP_VRF_2) @Nullable String vrf2,
      @JsonProperty(PROP_SUBNET_2) @Nullable String subnet2,
      @JsonProperty(PROP_DESCRIPTION) String description,
      @JsonProperty(PROP_RECOMMENDATION) String recommendation) {
    _severity = severity;
    _category = category;
    _bridgeDomain1 = bridgeDomain1;
    _vrf1 = vrf1;
    _subnet1 = subnet1;
    _bridgeDomain2 = bridgeDomain2;
    _vrf2 = vrf2;
    _subnet2 = subnet2;
    _description = description;
    _recommendation = recommendation;
  }

  @JsonProperty(PROP_SEVERITY)
  public @Nonnull Severity getSeverity() {
    return _severity;
  }

  @JsonProperty(PROP_CATEGORY)
  public @Nonnull Category getCategory() {
    return _category;
  }

  @JsonProperty(PROP_BRIDGE_DOMAIN_1)
  public @Nonnull String getBridgeDomain1() {
    return _bridgeDomain1;
  }

  @JsonProperty(PROP_VRF_1)
  public @Nonnull String getVrf1() {
    return _vrf1;
  }

  @JsonProperty(PROP_SUBNET_1)
  public @Nonnull String getSubnet1() {
    return _subnet1;
  }

  @JsonProperty(PROP_BRIDGE_DOMAIN_2)
  public @Nullable String getBridgeDomain2() {
    return _bridgeDomain2;
  }

  @JsonProperty(PROP_VRF_2)
  public @Nullable String getVrf2() {
    return _vrf2;
  }

  @JsonProperty(PROP_SUBNET_2)
  public @Nullable String getSubnet2() {
    return _subnet2;
  }

  @JsonProperty(PROP_DESCRIPTION)
  public @Nonnull String getDescription() {
    return _description;
  }

  @JsonProperty(PROP_RECOMMENDATION)
  public @Nonnull String getRecommendation() {
    return _recommendation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SubnetFinding)) {
      return false;
    }
    SubnetFinding that = (SubnetFinding) o;
    return _severity == that._severity
        && _category == that._category
        && Objects.equals(_bridgeDomain1, that._bridgeDomain1)
        && Objects.equals(_vrf1, that._vrf1)
        && Objects.equals(_subnet1, that._subnet1)
        && Objects.equals(_bridgeDomain2, that._bridgeDomain2)
        && Objects.equals(_vrf2, that._vrf2)
        && Objects.equals(_subnet2, that._subnet2)
        && Objects.equals(_description, that._description)
        && Objects.equals(_recommendation, that._recommendation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _severity,
        _category,
        _bridgeDomain1,
        _vrf1,
        _subnet1,
        _bridgeDomain2,
        _vrf2,
        _subnet2,
        _description,
        _recommendation);
  }

  @Override
  public String toString() {
    return String.format(
        "SubnetFinding{severity=%s, category=%s, bd1=%s, vrf1=%s, subnet1=%s, "
            + "bd2=%s, vrf2=%s, subnet2=%s, description='%s'}",
        _severity,
        _category,
        _bridgeDomain1,
        _vrf1,
        _subnet1,
        _bridgeDomain2,
        _vrf2,
        _subnet2,
        _description);
  }
}
