package org.batfish.bdp;

import org.batfish.datamodel.OspfExternalType2Route;

public class OspfExternalType2Rib extends AbstractRib<OspfExternalType2Route> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   @Override
   public int comparePreference(OspfExternalType2Route lhs,
         OspfExternalType2Route rhs) {
      // reversed on purpose
      int costComparison = Integer.compare(rhs.getMetric(), lhs.getMetric());
      if (costComparison != 0) {
         return costComparison;
      }
      return Integer.compare(rhs.getCostToAdvertiser(),
            lhs.getCostToAdvertiser());
   }

}
