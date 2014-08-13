package batfish.grammar.juniper.interfaces;

public class IFU_VlanIdStanza extends IF_UStanza {
   
   private int _vlanid;
   
   /* ------------------------------ Constructor ----------------------------*/
   public IFU_VlanIdStanza (int i) {
      _vlanid = i;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public int get_vlanid () {
      return _vlanid;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
 	@Override
	public IF_UType getType() {
		return IF_UType.VLAN_ID;
	}

}
