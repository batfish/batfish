package batfish.grammar.juniper.policy_options;

import java.util.ArrayList;
import java.util.List;

import batfish.representation.juniper.ASPathAccessList;
import batfish.representation.juniper.ProtocolType;

public class POPSTFr_ProtocolStanza extends POPST_FromStanza {
   
	private List<ProtocolType> _protocols;
   
   /* ------------------------------ Constructor ----------------------------*/
	public POPSTFr_ProtocolStanza() {
      _protocols = new ArrayList<ProtocolType>();
   }
	
   /* ----------------------------- Other Methods ---------------------------*/
	public void addProtocol(ProtocolType p) {
      _protocols.add(p);
   }
	
   /* ---------------------------- Getters/Setters --------------------------*/
   public List<ProtocolType> get_protocols() {
      return _protocols;
   }
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public POPST_FromType getType() {
		return POPST_FromType.PROTOCOL;
	}

}
