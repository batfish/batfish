package org.batfish.vendor.huawei.representation;

import javax.annotation.Nonnull;

/** ACL rule for Huawei device. */
public class HuaweiAclRule {

  public enum Action {
    PERMIT,
    DENY
  }

  private final int _number;
  private final @Nonnull Action _action;

  public HuaweiAclRule(int number, @Nonnull Action action) {
    _number = number;
    _action = action;
  }

  public int getNumber() {
    return _number;
  }

  public @Nonnull Action getAction() {
    return _action;
  }
}
