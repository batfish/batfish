package org.batfish.main;

public enum SrcNattedConstraint {
  REQUIRE_SRC_NATTED,
  REQUIRE_NOT_SRC_NATTED,
  UNCONSTRAINED;

  public static SrcNattedConstraint fromBoolean(Boolean srcNatted) {
    return srcNatted == null
        ? UNCONSTRAINED
        : srcNatted ? REQUIRE_SRC_NATTED : REQUIRE_NOT_SRC_NATTED;
  }
}
