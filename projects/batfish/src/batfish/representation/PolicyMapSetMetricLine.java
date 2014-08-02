package batfish.representation;

public class PolicyMapSetMetricLine extends PolicyMapSetLine {

   private static final long serialVersionUID = 1L;

   private int _metric;

   public PolicyMapSetMetricLine(int metric) {
      _metric = metric;
   }

   public int getMetric() {
      return _metric;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.METRIC;
   }

   @Override
   public boolean sameParseTree(PolicyMapSetLine line, String prefix) {
      boolean res = (line.getType() == PolicyMapSetType.METRIC);
      if (res == false) {
         System.out.println("PoliMapSetMetricLine:Type " + prefix);
         return res;
      }

      PolicyMapSetMetricLine metLine = (PolicyMapSetMetricLine) line;

      res = (_metric == metLine._metric);

      if (res == false) {
         System.out.println("PoliMapSetMetricLine " + prefix);

      }

      return res;
   }
}
