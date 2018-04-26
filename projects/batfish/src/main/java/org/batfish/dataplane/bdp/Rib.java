package org.batfish.dataplane.bdp;

import org.batfish.datamodel.AbstractRoute;

public class Rib extends AbstractRib<AbstractRoute> {

  /** */
  private static final long serialVersionUID = 1L;

  public Rib(VirtualRouter owner) {
    super(owner);
  }

  @Override
  public int comparePreference(AbstractRoute lhs, AbstractRoute rhs) {
    int lhsAdmin = lhs.getAdministrativeCost();
    int rhsAdmin = rhs.getAdministrativeCost();
    if (lhsAdmin < rhsAdmin) {
      return 1;
    } else if (lhsAdmin == rhsAdmin) {
      // same administrative distance, so check protocol-specific cost
      // further down
      long lhsMetric = lhs.getMetric();
      long rhsMetric = rhs.getMetric();
      if (lhsMetric < rhsMetric) {
        return 1;
      } else if (lhsMetric == rhsMetric) {
        // same cost, so equally preferable
        return 0;
      } else {
        // higher cost, so less preferable
        return -1;
      }
    } else {
      // higher administartive distance, so less preferable
      return -1;
    }
  }

  @Override
  public boolean mergeRoute(AbstractRoute route) {
    if (!route.getNonRouting()) {
      return super.mergeRoute(route);
    } else {
      return false;
    }
  }
}
