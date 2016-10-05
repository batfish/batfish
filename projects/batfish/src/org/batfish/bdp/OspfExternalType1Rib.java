package org.batfish.bdp;

import org.batfish.datamodel.OspfExternalType1Route;

public class OspfExternalType1Rib extends AbstractRib<OspfExternalType1Route> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   @Override
   public int comparePreference(OspfExternalType1Route lhs,
         OspfExternalType1Route rhs) {
      // reversed on purpose
      return Integer.compare(rhs.getMetric(), lhs.getMetric());
   }

}
