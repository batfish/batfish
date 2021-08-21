package org.batfish.vendor.check_point_management;

/**
 * When assigned to {@code translated-destination},{@code translated-service}, or {@code
 * translated-source} field of a {@link NatRule}, indicates that the original value should be
 * retained for that field when applying the rule.
 */
public final class Original extends Global {

  Original(Uid uid) {
    super(NAME_ORIGINAL, uid);
  }
}
