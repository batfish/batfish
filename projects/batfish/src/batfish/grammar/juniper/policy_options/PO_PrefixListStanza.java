package batfish.grammar.juniper.policy_options;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.StanzaStatusType;
import batfish.representation.juniper.PrefixList;
import batfish.representation.juniper.PrefixListLine;


public class PO_PrefixListStanza extends POStanza {

   private String _applyPathStr;
   private String _name;
   private List<String> _ipAddresses;
   private PrefixList _prefixList;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PO_PrefixListStanza(String n) {
      _name = n;
      _applyPathStr = "";
      _ipAddresses = new ArrayList<String> ();
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addAddress(String ipmask) {
      _ipAddresses.add(ipmask);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public PrefixList get_prefixList () {
      return _prefixList;
   }
   public void set_applyPathStr (String s) {
      _applyPathStr = s;
   }

   /* --------------------------- Inherited Methods -------------------------*/
   public void postProcessStanza () {
      if (get_stanzaStatus()==StanzaStatusType.IPV6) {
         addIgnoredStatement("prefix list " + _name + "(IPV6)");
      }
      else {
         
         if (!_applyPathStr.isEmpty()) {
         // TODO [P0]: This is where the apply-path thing gets expanded
         }
         
         
         List<PrefixListLine> _prefixes = new ArrayList<PrefixListLine> ();
         for (String ip : _ipAddresses) {
            _prefixes.add(new PrefixListLine(ip));
         }
         _prefixList = new PrefixList(_name, _prefixes);
      }
      
   }
   
   @Override
   public POType getType() {
      return POType.PREFIX_LIST;
   }

}
