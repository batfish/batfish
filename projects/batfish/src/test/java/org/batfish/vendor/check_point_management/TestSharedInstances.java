package org.batfish.vendor.check_point_management;

/** Test instances shared with other modules. */
public final class TestSharedInstances {
  /**
   * Instance of this class populated with arbitrary values. Useful for generating a valid object
   * for use in tests.
   */
  public static final NatSettings NAT_SETTINGS_TEST_INSTANCE =
      new NatSettings(true, NatHideBehindGateway.INSTANCE, "All", null, NatMethod.HIDE);
}
