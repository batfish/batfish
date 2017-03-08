package org.batfish.bdp;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.RoutingProtocol;

public class BgpRib extends AbstractRib<BgpRoute> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public BgpRib(VirtualRouter owner) {
      super(owner);
   }

   @Override
   public int comparePreference(BgpRoute lhs, BgpRoute rhs) {
      // first compare local preference
      int res;
      res = Integer.compare(lhs.getLocalPreference(), rhs.getLocalPreference());
      if (res != 0) {
         return res;
      }

      // on non-juniper, prefer aggregates (these routes won't appear on
      // juniper)
      res = Integer.compare(getAggregatePreference(rhs.getProtocol()),
            getAggregatePreference(lhs.getProtocol()));
      if (res != 0) {
         return res;
      }

      // then compare as path size (shorter is better, hence reversal)
      res = Integer.compare(rhs.getAsPath().size(), lhs.getAsPath().size());
      if (res != 0) {
         return res;
      }
      // TODO: origin type (IGP better than EGP, which is better than
      // INCOMPLETE)
      // then compare MED
      // TODO: handle presence/absence of always-compare-med, noting that
      // normally we only do this comparison if the first AS is the same in the
      // paths for both routes
      res = Integer.compare(rhs.getMetric(), lhs.getMetric());
      if (res != 0) {
         return res;
      }
      // next prefer eBGP over iBGP
      res = Integer.compare(getTypeCost(rhs.getProtocol()),
            getTypeCost(lhs.getProtocol()));
      if (res != 0) {
         return res;
      }
      // The remaining criteria only apply in non-multipath (or limited
      // multipath) environments, which we do not yet support. So we end here.
      return res;
   }

   private int getAggregatePreference(RoutingProtocol protocol) {
      if (protocol == RoutingProtocol.AGGREGATE) {
         return 0;
      }
      else {
         return 1;
      }
   }

   private int getTypeCost(RoutingProtocol protocol) {
      switch (protocol) {
      case AGGREGATE:
         return 0;
      case BGP: // eBGP
         return 1;
      case IBGP:
         return 2;
      // $CASES-OMITTED$
      default:
         throw new BatfishException(
               "Invalid BGP protocol: '" + protocol.toString() + "'");
      }
   }

}
