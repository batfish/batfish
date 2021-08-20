package org.batfish.vendor.check_point_management;

import java.util.Objects;
import javax.annotation.Nullable;

/** Container for all data corresponding to a given package. */
public final class ManagementPackage extends NamedManagementObject {

  public ManagementPackage(@Nullable NatRulebase natRulebase, Package pakij) {
    super(pakij.getName(), pakij.getUid());
    _natRulebase = natRulebase;
  }

  public @Nullable NatRulebase getNatRulebase() {
    return _natRulebase;
  }

  @Override
  public boolean equals(Object o) {
    if (!baseEquals(o)) {
      return false;
    }
    ManagementPackage that = (ManagementPackage) o;
    return Objects.equals(_natRulebase, that._natRulebase);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _natRulebase);
  }

  private final @Nullable NatRulebase _natRulebase;
}
