package batfish.representation.juniper;

public class StaticOptions_Metric extends StaticOptions {

   private int _metric;

   public StaticOptions_Metric(int i) {
      _metric = i;
   }

   public int getMetric() {
      return _metric;
   }

   @Override
   public StaticOptionsType getType() {
      return StaticOptionsType.METRIC;
   }

}
