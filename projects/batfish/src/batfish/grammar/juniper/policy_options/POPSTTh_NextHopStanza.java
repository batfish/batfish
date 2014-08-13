package batfish.grammar.juniper.policy_options;

import batfish.representation.juniper.PolicyStatement_HopType;

public class POPSTTh_NextHopStanza extends POPST_ThenStanza {
   
   private String _hopName;
   PolicyStatement_HopType _hopType;
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTTh_NextHopStanza() {
      _hopType = PolicyStatement_HopType.NEXTHOP_NAME;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public void set_hopType(PolicyStatement_HopType ht) {
      _hopType = ht;
   }
   public PolicyStatement_HopType get_hopType() {
      return _hopType;
   }
   public String get_hopName() {
      return _hopName;
   }
   public void set_hopName(String hn) {
      _hopName = hn;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public POPST_ThenType getType() {
		return POPST_ThenType.NEXT_HOP;
	}

}
