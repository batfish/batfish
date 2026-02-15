package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Finding from ACI VRF isolation analysis.
 *
 * <p>Represents a potential issue or recommendation found during VRF isolation validation, such as
 * overlapping subnets across VRFs, contracts crossing VRF boundaries, unused VRFs, or bridge
 * domains associated with multiple VRFs.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VrfIsolationFinding implements Serializable {

  /** Severity levels for VRF isolation findings. */
  public enum Severity {
    /** Critical issue that could cause network disruption or security breach */
    CRITICAL,
    /** High priority issue that should be addressed soon */
    HIGH,
    /** Medium priority issue that could cause problems in certain scenarios */
    MEDIUM,
    /** Low priority issue or informational finding */
    LOW,
    /** Informational finding for awareness */
    INFO
  }

  /** Categories of VRF isolation findings. */
  public enum Category {
    /** Subnets overlap across different VRFs (potential routing conflict) */
    SUBNET_OVERLAP,
    /** Contract references EPGs in different VRFs */
    CROSS_VRF_CONTRACT,
    /** VRF has no bridge domains or L3Outs (unused configuration) */
    UNUSED_VRF,
    /** Bridge domain is associated with multiple VRFs */
    BD_MULTI_VRF,
    /** L3Out external connectivity scope validation issue */
    L3OUT_SCOPE,
    /** EPGs in different VRFs share the same bridge domain */
    BD_CROSS_VRF_EPG
  }

  @JsonProperty("severity")
  private Severity _severity;

  @JsonProperty("category")
  private Category _category;

  @JsonProperty("vrfName1")
  private String _vrfName1;

  @JsonProperty("vrfName2")
  private String _vrfName2;

  @JsonProperty("tenantName")
  private String _tenantName;

  @JsonProperty("bridgeDomain")
  private String _bridgeDomain;

  @JsonProperty("subnet1")
  private String _subnet1;

  @JsonProperty("subnet2")
  private String _subnet2;

  @JsonProperty("contractName")
  private String _contractName;

  @JsonProperty("description")
  private String _description;

  @JsonProperty("impact")
  private String _impact;

  @JsonProperty("recommendation")
  private String _recommendation;

  public VrfIsolationFinding() {}

  /**
   * Creates a new VRF isolation finding with basic information.
   *
   * @param severity The severity level of the finding
   * @param category The category of the finding
   * @param vrfName1 The primary VRF name
   * @param description Description of the issue
   * @param impact Impact of the issue
   * @param recommendation Recommendation for remediation
   */
  public VrfIsolationFinding(
      @Nonnull Severity severity,
      @Nonnull Category category,
      @Nonnull String vrfName1,
      @Nonnull String description,
      @Nonnull String impact,
      @Nonnull String recommendation) {
    _severity = severity;
    _category = category;
    _vrfName1 = vrfName1;
    _description = description;
    _impact = impact;
    _recommendation = recommendation;
  }

  public @Nonnull Severity getSeverity() {
    return _severity;
  }

  public void setSeverity(@Nonnull Severity severity) {
    _severity = severity;
  }

  public @Nonnull Category getCategory() {
    return _category;
  }

  public void setCategory(@Nonnull Category category) {
    _category = category;
  }

  public @Nonnull String getVrfName1() {
    return _vrfName1;
  }

  public void setVrfName1(@Nonnull String vrfName1) {
    _vrfName1 = vrfName1;
  }

  public @Nullable String getVrfName2() {
    return _vrfName2;
  }

  public void setVrfName2(@Nullable String vrfName2) {
    _vrfName2 = vrfName2;
  }

  public @Nullable String getTenantName() {
    return _tenantName;
  }

  public void setTenantName(@Nullable String tenantName) {
    _tenantName = tenantName;
  }

  public @Nullable String getBridgeDomain() {
    return _bridgeDomain;
  }

  public void setBridgeDomain(@Nullable String bridgeDomain) {
    _bridgeDomain = bridgeDomain;
  }

  public @Nullable String getSubnet1() {
    return _subnet1;
  }

  public void setSubnet1(@Nullable String subnet1) {
    _subnet1 = subnet1;
  }

  public @Nullable String getSubnet2() {
    return _subnet2;
  }

  public void setSubnet2(@Nullable String subnet2) {
    _subnet2 = subnet2;
  }

  public @Nullable String getContractName() {
    return _contractName;
  }

  public void setContractName(@Nullable String contractName) {
    _contractName = contractName;
  }

  public @Nonnull String getDescription() {
    return _description;
  }

  public void setDescription(@Nonnull String description) {
    _description = description;
  }

  public @Nonnull String getImpact() {
    return _impact;
  }

  public void setImpact(@Nonnull String impact) {
    _impact = impact;
  }

  public @Nonnull String getRecommendation() {
    return _recommendation;
  }

  public void setRecommendation(@Nonnull String recommendation) {
    _recommendation = recommendation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VrfIsolationFinding)) {
      return false;
    }
    VrfIsolationFinding that = (VrfIsolationFinding) o;
    return _severity == that._severity
        && _category == that._category
        && Objects.equals(_vrfName1, that._vrfName1)
        && Objects.equals(_vrfName2, that._vrfName2)
        && Objects.equals(_description, that._description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_severity, _category, _vrfName1, _vrfName2, _description);
  }

  @Override
  public String toString() {
    return String.format(
        "[%s] %s - VRF %s%s: %s",
        _severity,
        _category,
        _vrfName1,
        (_vrfName2 != null ? " & " + _vrfName2 : ""),
        _description);
  }
}
