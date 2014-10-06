package batfish.grammar.juniper.bgp;

import batfish.representation.juniper.BGPLocalAddress;

public class BGGR_LocalAddressStanza extends BG_GRStanza {
   
   private BGPLocalAddress _la;
   
   /* ------------------------------ Constructor ----------------------------*/
   public BGGR_LocalAddressStanza(BGPLocalAddress l){
      _la = l;
      //set_stanzaStatus(l.get_stanzaStatus());
      set_postProcessTitle("BGP Group Local Address " + l);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public String GetLocalAddress () {
      return _la.get_localAddress();
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/  
   @Override
   public BG_GRType getType() {      
      return BG_GRType.LOCAL_ADDRESS;
   }

}
