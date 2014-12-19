package batfish.representation.juniper;

import batfish.representation.Ip;
import batfish.representation.PolicyMapSetLine;

public final class ThenNextHopIp extends Then {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Ip _nextHopIp;

   public ThenNextHopIp(Ip nextHopIp) {
      _nextHopIp = nextHopIp;
   }

   public Ip getNextHopIp() {
      return _nextHopIp;
   }

   @Override
   public PolicyMapSetLine toPolicyStatmentSetLine() {
      // TODO Auto-generated method stub
      return null;
   }

}
