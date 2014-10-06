package batfish.representation.juniper;

public class PolicyStatementSetMetricLine extends PolicyStatement_SetLine {

   private static final long serialVersionUID = 1L;
   
   private int _metric;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PolicyStatementSetMetricLine(int m) {
      _metric = m;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public int get_metric() {
      return _metric;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
    @Override
   public PolicyStatement_SetType getType() {
      return PolicyStatement_SetType.METRIC;
   }   
   
}
