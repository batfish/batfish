package org.batfish.vendor.check_point_management;

import com.google.common.annotations.VisibleForTesting;

/** Indicates that a rule should be applied to all installation targets of the package. */
public final class PolicyTargets extends Global {

  @VisibleForTesting
  public PolicyTargets(Uid uid) {
    super(NAME_POLICY_TARGETS, uid);
  }
}
