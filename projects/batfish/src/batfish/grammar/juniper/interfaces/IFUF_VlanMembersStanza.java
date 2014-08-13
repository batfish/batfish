package batfish.grammar.juniper.interfaces;

import java.util.ArrayList;
import java.util.List;

public class IFUF_VlanMembersStanza extends IFU_FamStanza {
   
	private List<String> _members;
   
   /* ------------------------------ Constructor ----------------------------*/
   public IFUF_VlanMembersStanza(ArrayList<String> s) {
      _members = new ArrayList<String>();
      _members.addAll(s);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public List<String> get_members() {
      return _members;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public IFU_FamType getType() {
		return IFU_FamType.VLAN_MEMBERS;
	}

}
