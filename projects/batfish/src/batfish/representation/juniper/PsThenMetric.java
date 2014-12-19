package batfish.representation.juniper;

import batfish.representation.PolicyMapSetLine;

public final class PsThenMetric extends PsThen {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _metric;

   public PsThenMetric(int metric) {
      _metric = metric;
   }

   public int getMetric() {
      return _metric;
   }

   @Override
   public PolicyMapSetLine toPolicyStatmentSetLine() {
      // TODO Auto-generated method stub
      return null;
   }

}
