package org.batfish.vendor.check_point_management;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Container for all data corresponding to a given package. */
public final class ManagementPackage implements Serializable {

  public ManagementPackage(
      @Nullable List<AccessRulebase> accessRulebase,
      @Nullable NatRulebase natRulebase,
      Package pakij) {
    _accessRulebases =
        accessRulebase == null ? ImmutableList.of() : ImmutableList.copyOf(accessRulebase);
    _natRulebase = natRulebase;
    _package = pakij;
  }

  public @Nonnull List<AccessRulebase> getAccessRulebases() {
    return _accessRulebases;
  }

  public @Nullable NatRulebase getNatRulebase() {
    return _natRulebase;
  }

  public @Nonnull Package getPackage() {
    return _package;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof ManagementPackage)) {
      return false;
    }
    ManagementPackage that = (ManagementPackage) o;
    return Objects.equals(_accessRulebases, that._accessRulebases)
        && Objects.equals(_natRulebase, that._natRulebase)
        && _package.equals(that._package);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_accessRulebases, _natRulebase, _package);
  }

  @Override
  public String toString() {
    return toStringHelper(this)
        .add("_accessRulebases", _accessRulebases)
        .add("_natRulebase", _natRulebase)
        .add("_package", _package)
        .toString();
  }

  private final @Nonnull List<AccessRulebase> _accessRulebases;
  private final @Nullable NatRulebase _natRulebase;
  private final @Nonnull Package _package;
}
