package org.batfish.bdp;

import org.batfish.datamodel.AbstractRoute;

public class Rib extends AbstractRib<AbstractRoute> {

   /**
    *
    */
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
      }
      // same administrative distance, so check protocol-specific cost
      // further down
      else if (lhsAdmin == rhsAdmin) {
         int lhsMetric = lhs.getMetric();
         int rhsMetric = rhs.getMetric();
         if (lhsMetric < rhsMetric) {
            return 1;
         }
         // same cost, so equally preferable
         else if (lhsMetric == rhsMetric) {
            return 0;
         }
         // higher cost, so less preferable
         else {
            return -1;
         }
      }
      else {
         return -1;
      }
   }

   @Override
   public boolean mergeRoute(AbstractRoute route) {
      if (!route.getNonRouting()) {
         return super.mergeRoute(route);
      }
      else {
         return false;
      }
   }

}
