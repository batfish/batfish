package batfish.grammar.juniper.policy_options;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.StanzaStatusType;
import batfish.representation.juniper.FilterMatch;

public class POPSTFr_SourceAddressFilterStanza extends POPST_FromStanza {
   
   private String _prefix;
   private int _prefixLength;
   private FilterMatch _fms;
   private List<POPST_ThenStanza> _fasl;

   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTFr_SourceAddressFilterStanza() {
      _fasl = new ArrayList<POPST_ThenStanza>();
      set_postProcessTitle("Source Address Filter " + _prefix);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addFas(POPST_ThenStanza f) {
      _fasl.add(f);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public void set_prefix(String ip_with_mask) {
      
      String[] split_prefix = ip_with_mask.split("/");
      _prefix = split_prefix[0];
      _prefixLength = Integer.parseInt(split_prefix[1]);
      
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
   public String get_prefix () {
      return _prefix;
   }
   public int get_prefixLenght() {
      return _prefixLength;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public POPST_FromType getType() {
      return POPST_FromType.SOURCE_ADDRESS_FILTER;
   }

}
