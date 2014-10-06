package batfish.grammar.juniper.bgp;

import batfish.grammar.juniper.StanzaStatusType;
import batfish.representation.juniper.BGPLocalAddress;

public class BGGRN_LocalAddressStanza extends BGGR_NStanza {
   
   private BGPLocalAddress _la;
   
   /* ------------------------------ Constructor ----------------------------*/
   public BGGRN_LocalAddressStanza(BGPLocalAddress l){
      _la = l;
      if (l.get_isIPV6()) {
    	  set_stanzaStatus(StanzaStatusType.IPV6);
      }
      set_postProcessTitle("BGP Group Neighbor Local Address " + l);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public String GetLocalAddress () {
      return _la.get_localAddress();
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/  
   @Override
   public BGGR_NType getType() {      
      return BGGR_NType.LOCAL_ADDRESS;
   }

}
