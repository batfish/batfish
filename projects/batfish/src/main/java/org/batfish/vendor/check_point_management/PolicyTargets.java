package org.batfish.vendor.check_point_management;

/** Indicates that a rule should be applied to all installation targets of the package. */
public final class PolicyTargets extends Global {

  PolicyTargets(Uid uid) {
    super(NAME_POLICY_TARGETS, uid);
  }
}
