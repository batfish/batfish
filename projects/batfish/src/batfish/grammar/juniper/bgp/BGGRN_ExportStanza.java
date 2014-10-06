package batfish.grammar.juniper.bgp;

import java.util.List;

import batfish.representation.juniper.BGPExportList;


public class BGGRN_ExportStanza extends BGGR_NStanza {
   
   BGPExportList _eList;
   
   /* ------------------------------ Constructor ----------------------------*/
   public BGGRN_ExportStanza(BGPExportList e) {
      _eList = e;
     // set_stanzaStatus(e.get_stanzaStatus());
      set_postProcessTitle("BGP Group Neighbor Export List");
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public List<String> GetExportNames () {
      return _eList.get_policyNames();
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/   
   @Override
   public BGGR_NType getType() {
      return BGGR_NType.EXPORT;
   }

}
