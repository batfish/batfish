package batfish.representation;

import batfish.util.Util;

public class PolicyMapSetMetricLine extends PolicyMapSetLine {

   private int _metric;

   public PolicyMapSetMetricLine(int metric) {
      _metric = metric;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.METRIC;
   }

   public int getMetric() {
      return _metric;
   }

   @Override
   public boolean sameParseTree(PolicyMapSetLine line, String prefix) {
      boolean res = (line.getType() == PolicyMapSetType.METRIC);
      if(res == false){
         System.out.println("PoliMapSetMetricLine:Type "+prefix);
         return res;
      }
      
      PolicyMapSetMetricLine metLine = (PolicyMapSetMetricLine) line;
         
      res = (_metric == metLine._metric);
      
      if(res == false){
         System.out.println("PoliMapSetMetricLine "+prefix);
         
      }
      
      return res;
   }
   
   @Override
   public String getIFString(int indentLevel) {
	   return Util.getIndentString(indentLevel) + "Metric " + _metric;
   }
}
