package org.batfish.datamodel;

import javax.annotation.ParametersAreNonnullByDefault;

/** Provides helper methods to auto-generate structure names. */
@ParametersAreNonnullByDefault
public final class Names {
  /**
   * Return the Batfish canonical name for a filter between zones.
   *
   * <p>This should only be used for filters that are defined by the user but unnamed in the vendor
   * language, rather than filters that are "generated" by Batfish combining multiple user-defined
   * structures.
   */
  public static String zoneToZoneFilter(String fromZone, String toZone) {
    return String.format("zone~%s~to~zone~%s", fromZone, toZone);
  }

  private Names() {} // prevent instantiation by default.
}
