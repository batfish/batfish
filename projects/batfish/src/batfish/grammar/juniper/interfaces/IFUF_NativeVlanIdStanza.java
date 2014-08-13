package batfish.grammar.juniper.interfaces;

public class IFUF_NativeVlanIdStanza extends IFU_FamStanza {
   
   private int _vlanId;
   
   /* ------------------------------ Constructor ----------------------------*/
   public IFUF_NativeVlanIdStanza(int i) {
      _vlanId = i;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public int get_vlanId() {
      return _vlanId;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public IFU_FamType getType() {
		return IFU_FamType.NATIVE_VLAN_ID;
	}
}
