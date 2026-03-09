package org.batfish.vendor.huawei.representation;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/** ACL configuration for Huawei device. */
public class HuaweiAcl {

  private final @Nonnull String _name;
  private final @Nonnull List<HuaweiAclRule> _rules;

  public HuaweiAcl(@Nonnull String name) {
    _name = name;
    _rules = new ArrayList<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull List<HuaweiAclRule> getRules() {
    return _rules;
  }
}
