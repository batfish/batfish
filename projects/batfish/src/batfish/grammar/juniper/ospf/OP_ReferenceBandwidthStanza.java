package batfish.grammar.juniper.ospf;

public class OP_ReferenceBandwidthStanza extends OPStanza {
   
   private double _referenceBandwidth;
   
   /* ------------------------------ Constructor ----------------------------*/
   public OP_ReferenceBandwidthStanza(double rb) {
      _referenceBandwidth = rb;
      set_postProcessTitle("Reference Bandwidth " + rb);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public double getReferenceBandwidth(){
      return _referenceBandwidth;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/ 
	@Override
	public OPType getType() {
		return OPType.REFERENCE_BANDWIDTH;
	}

}
