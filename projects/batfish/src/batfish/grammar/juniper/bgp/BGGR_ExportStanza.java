package batfish.grammar.juniper.bgp;

import java.util.List;

import batfish.representation.juniper.BGPExportList;

public class BGGR_ExportStanza extends BG_GRStanza {
   
   private BGPExportList _eList;
   
   /* ------------------------------ Constructor ----------------------------*/
   public BGGR_ExportStanza(BGPExportList e) {
      _eList = e;
      set_stanzaStatus(e.get_stanzaStatus());
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public List<String> GetExportNames () {
      return _eList.get_policyNames();
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/  
   @Override
   public BG_GRType getType() {
      return BG_GRType.EXPORT;
   }

}
