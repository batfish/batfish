package batfish.grammar.juniper.policy_options;

import java.util.ArrayList;
import java.util.List;

import batfish.representation.juniper.ASPathAccessList;

public class POPSTFr_InterfaceStanza extends POPST_FromStanza {
   
	private List<String> _interfaceNames;
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTFr_InterfaceStanza() {
      _interfaceNames = new ArrayList<String>(); 
      set_postProcessTitle("Interface ");
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addInterface (String s) {
      _interfaceNames.add(s);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public List<String> get_interfaceNames () {
      return _interfaceNames;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public POPST_FromType getType() {
		return POPST_FromType.INTERFACE;
	}

}
