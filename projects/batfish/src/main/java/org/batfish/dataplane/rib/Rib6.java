package org.batfish.dataplane.rib;

import java.util.Comparator;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute6;

/**
 * General IPv6 RIB capable of storing routes from multiple protocols.
 *
 * <p>Lower administrative cost is preferred, followed by lower metric,
 * matching the preference ordering of the IPv4 main RIB.
 */
@ParametersAreNonnullByDefault
public final class Rib6 extends AbstractRib6<AbstractRoute6> {

  private static final Comparator<AbstractRoute6> COMPARE_PREFERENCE =
      Comparator.comparingLong(
              AbstractRoute6::getAdministrativeCost)
          .thenComparingLong(AbstractRoute6::getMetric);

  @Override
  public int comparePreference(
      AbstractRoute6 lhs, AbstractRoute6 rhs) {
    // Lower values are preferable.
    return COMPARE_PREFERENCE.compare(rhs, lhs);
  }
}
