package batfish.grammar.juniper.interfaces;

public class IFUF_AddressStanza extends IFU_FamStanza {
   
	private String _address;
	private String _subnetMask;
   
   /* ------------------------------ Constructor ----------------------------*/
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public void set_address(String a) {
      String[] temp = a.split("/");
      _address = temp[0];
      _subnetMask = temp[1];
      set_postProcessTitle("Address " + a);
   }
   
   public String get_address() {
      return _address;
   }

   public String get_subnetMask() {
      return _subnetMask;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public IFU_FamType getType() {
		return IFU_FamType.ADDRESS;
	}

}
