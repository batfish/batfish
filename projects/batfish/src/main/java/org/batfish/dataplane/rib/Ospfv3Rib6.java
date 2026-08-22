package org.batfish.dataplane.rib;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.Ospfv3ExternalType2Route6;
import org.batfish.datamodel.Ospfv3IntraAreaRoute6;

/**
 * OSPFv3 IPv6 RIB.
 *
 * <p>Internal intra-area routes are preferred to external routes before
 * metric comparison.
 */
@ParametersAreNonnullByDefault
public final class Ospfv3Rib6
    extends AbstractRib6<AbstractRoute6> {

  private static int routeTypePreference(
      AbstractRoute6 route) {
    if (route instanceof Ospfv3IntraAreaRoute6) {
      return 2;
    }
    if (route instanceof Ospfv3ExternalType2Route6) {
      return 1;
    }
    return 0;
  }

  @Override
  public int comparePreference(
      AbstractRoute6 lhs, AbstractRoute6 rhs) {
    int typeComparison =
        Integer.compare(
            routeTypePreference(lhs),
            routeTypePreference(rhs));

    if (typeComparison != 0) {
      return typeComparison;
    }

    int adminComparison =
        Long.compare(
            rhs.getAdministrativeCost(),
            lhs.getAdministrativeCost());

    if (adminComparison != 0) {
      return adminComparison;
    }

    // Lower OSPF metric is preferable.
    int metricComparison =
        Long.compare(
            rhs.getMetric(),
            lhs.getMetric());

    if (metricComparison != 0) {
      return metricComparison;
    }

    // For equal E2 metrics, prefer the lower internal cost to the ASBR.
    if (lhs instanceof Ospfv3ExternalType2Route6
        && rhs instanceof Ospfv3ExternalType2Route6) {
      Ospfv3ExternalType2Route6 lhsExternal =
          (Ospfv3ExternalType2Route6) lhs;
      Ospfv3ExternalType2Route6 rhsExternal =
          (Ospfv3ExternalType2Route6) rhs;

      return Long.compare(
          rhsExternal.getCostToAdvertiser(),
          lhsExternal.getCostToAdvertiser());
    }

    return 0;
  }
}
