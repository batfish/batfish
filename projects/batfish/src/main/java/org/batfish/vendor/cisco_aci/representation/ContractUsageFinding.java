package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Finding from ACI contract usage analysis.
 *
 * <p>Represents a potential issue or recommendation found during contract usage analysis, such as
 * unused contracts, duplicate contracts, or contracts with broken references.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractUsageFinding implements Serializable {

  public enum Severity {
    HIGH,
    MEDIUM,
    LOW,
    INFO
  }

  public enum Category {
    /** Contract is defined but not consumed by any EPG */
    UNUSED,
    /** Contract is defined but not provided by any EPG */
    UNPROVIDED,
    /** Contract is consumed but not provided by any EPG */
    ORPHANED_CONSUMER,
    /** Contract is provided but not consumed by any EPG */
    ORPHANED_PROVIDER,
    /** Contract name is duplicated in different tenants */
    DUPLICATE,
    /** Contract has identical filter rules as another contract */
    REDUNDANT,
    /** Contract references a non-existent filter */
    BROKEN_REFERENCE
  }

  @JsonProperty("severity")
  private Severity _severity;

  @JsonProperty("category")
  private Category _category;

  @JsonProperty("contractName")
  private String _contractName;

  @JsonProperty("tenantName")
  private String _tenantName;

  @JsonProperty("description")
  private String _description;

  @JsonProperty("recommendation")
  private String _recommendation;

  @JsonProperty("relatedContracts")
  private String _relatedContracts;

  public ContractUsageFinding() {}

  public ContractUsageFinding(
      @Nonnull Severity severity,
      @Nonnull Category category,
      @Nonnull String contractName,
      @Nonnull String tenantName,
      @Nonnull String description,
      @Nonnull String recommendation) {
    _severity = severity;
    _category = category;
    _contractName = contractName;
    _tenantName = tenantName;
    _description = description;
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

  public @Nonnull String getContractName() {
    return _contractName;
  }

  public void setContractName(@Nonnull String contractName) {
    _contractName = contractName;
  }

  public @Nonnull String getTenantName() {
    return _tenantName;
  }

  public void setTenantName(@Nonnull String tenantName) {
    _tenantName = tenantName;
  }

  public @Nonnull String getDescription() {
    return _description;
  }

  public void setDescription(@Nonnull String description) {
    _description = description;
  }

  public @Nonnull String getRecommendation() {
    return _recommendation;
  }

  public void setRecommendation(@Nonnull String recommendation) {
    _recommendation = recommendation;
  }

  public @Nullable String getRelatedContracts() {
    return _relatedContracts;
  }

  public void setRelatedContracts(@Nullable String relatedContracts) {
    _relatedContracts = relatedContracts;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ContractUsageFinding)) {
      return false;
    }
    ContractUsageFinding that = (ContractUsageFinding) o;
    return _severity == that._severity
        && _category == that._category
        && Objects.equals(_contractName, that._contractName)
        && Objects.equals(_tenantName, that._tenantName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_severity, _category, _contractName, _tenantName);
  }

  @Override
  public String toString() {
    return String.format("%s:%s - %s: %s", _tenantName, _contractName, _severity, _category);
  }
}
