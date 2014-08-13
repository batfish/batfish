package batfish.grammar.juniper.policy_options;

public class POPSTTh_MetricStanza extends POPST_ThenStanza {

   private int _metric;
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTTh_MetricStanza(int m) {
      _metric = m;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public int get_metric() {
      return _metric;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public POPST_ThenType getType() {
		return POPST_ThenType.METRIC;
	}
}
