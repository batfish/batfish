package org.batfish.dataplane.rib;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.Ospfv3ExternalType1Route6;
import org.batfish.datamodel.Ospfv3ExternalType2Route6;
import org.batfish.datamodel.Ospfv3InterAreaRoute6;
import org.batfish.datamodel.Ospfv3NssaExternalType2Route6;
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
      return 4;
    }
    if (route instanceof Ospfv3InterAreaRoute6) {
      return 3;
    }
    if (route instanceof Ospfv3ExternalType1Route6) {
      return 2;
    }
    if (route instanceof Ospfv3ExternalType2Route6
        || route
            instanceof Ospfv3NssaExternalType2Route6) {
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

    // For equal external metrics, prefer lower internal cost to the ASBR.
    Long lhsCost =
        externalCostToAdvertiser(lhs);
    Long rhsCost =
        externalCostToAdvertiser(rhs);

    if (lhsCost != null && rhsCost != null) {
      return Long.compare(
          rhsCost,
          lhsCost);
    }

    return 0;
  }

  private static Long externalCostToAdvertiser(
      AbstractRoute6 route) {
    if (route instanceof Ospfv3ExternalType2Route6) {
      return ((Ospfv3ExternalType2Route6) route)
          .getCostToAdvertiser();
    }

    if (route
        instanceof Ospfv3NssaExternalType2Route6) {
      return ((Ospfv3NssaExternalType2Route6) route)
          .getCostToAdvertiser();
    }

    return null;
  }
}
