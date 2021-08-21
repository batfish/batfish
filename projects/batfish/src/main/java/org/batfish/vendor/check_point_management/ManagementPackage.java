package org.batfish.vendor.check_point_management;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Container for all data corresponding to a given package. */
public final class ManagementPackage implements Serializable {

  public ManagementPackage(@Nullable NatRulebase natRulebase, Package pakij) {
    _natRulebase = natRulebase;
    _package = pakij;
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
    return Objects.equals(_natRulebase, that._natRulebase) && _package.equals(that._package);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_natRulebase, _package);
  }

  @Override
  public String toString() {
    return toStringHelper(this)
        .add("_natRulebase", _natRulebase)
        .add("_package", _package)
        .toString();
  }

  private final @Nullable NatRulebase _natRulebase;
  private final @Nonnull Package _package;
}
