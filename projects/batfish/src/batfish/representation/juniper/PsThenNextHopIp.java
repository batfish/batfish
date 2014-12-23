package batfish.representation.juniper;

import batfish.representation.Ip;
import batfish.representation.PolicyMapClause;
import batfish.representation.PolicyMapSetLine;

public final class PsThenNextHopIp extends PsThen {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Ip _nextHopIp;

   public PsThenNextHopIp(Ip nextHopIp) {
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

   @Override
   public void applyTo(PolicyMapClause clause) {
      throw new UnsupportedOperationException("no implementation for generated method"); // TODO Auto-generated method stub
   }

}
