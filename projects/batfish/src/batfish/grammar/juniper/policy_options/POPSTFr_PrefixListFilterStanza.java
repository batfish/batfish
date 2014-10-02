package batfish.grammar.juniper.policy_options;


import java.util.ArrayList;

import batfish.grammar.juniper.StanzaStatusType;
import batfish.representation.juniper.ASPathAccessList;
import batfish.representation.juniper.FilterMatch;

public class POPSTFr_PrefixListFilterStanza extends POPST_FromStanza {
   
   private String _listName;
   private FilterMatch _fms;
   private ArrayList<POPST_ThenStanza> _fasl;
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTFr_PrefixListFilterStanza(String i) {
      _listName = i;
      _fasl = new ArrayList<POPST_ThenStanza>();
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addFas(POPST_ThenStanza f) {
      _fasl.add(f);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_listName() {
      return _listName;
   }
   public void set_fms(POPSTTh_FilterMatchStanza fms) {
      _fms = fms.get_filterMatch();
      if (fms.get_stanzaStatus()==StanzaStatusType.IPV6) {
         set_stanzaStatus(StanzaStatusType.IPV6);
      }
   }
   public FilterMatch get_fms() {
      return _fms;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
  @Override
   public POPST_FromType getType() {
      return POPST_FromType.PREFIX_LIST_FILTER;
   }

}
