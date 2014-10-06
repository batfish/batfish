package batfish.grammar.juniper.interfaces;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.StanzaStatusType;
import batfish.representation.SwitchportMode;
import batfish.representation.juniper.FamilyType;
import static batfish.representation.juniper.FamilyOps.*;

public class IFU_FamilyStanza extends IF_UStanza{
   
   private FamilyType _famType;
   private List<IFU_FamStanza> _ifuFamStanzas;
   
   private String _address;
   private String _subnetMask;
   private SwitchportMode _interfaceMode;
   private int _nativeVlan;
   
   /* ------------------------------ Constructor ----------------------------*/
   public IFU_FamilyStanza(FamilyType ft) {
      // default switch port mode is access in Juniper
      _interfaceMode = SwitchportMode.ACCESS;   // TODO [P0]: i dont see this getting used
      _ifuFamStanzas = new ArrayList<IFU_FamStanza> ();
      set_famType(ft);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addIFU_FamStanza (IFU_FamStanza f) {
      _ifuFamStanzas.add(f);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public void set_famType(FamilyType ft) {
      _famType = ft;
      set_postProcessTitle("family " + FamilyTypeToString(_famType));
       if (_famType==FamilyType.INET6 ) { 
          this.set_stanzaStatus(StanzaStatusType.IPV6);
       }
   }
   public SwitchportMode get_interfaceMode() {
      return _interfaceMode;
   }   
   public int get_nativeVlan() {
      return _nativeVlan;
   }
   public String get_subnetMask() {
      return _subnetMask;
   }
   public String get_address() {
      return _address;
   }
   public FamilyType get_famType() {
      return _famType;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public void postProcessStanza() {
     
      if (FamilyTypeIgnored(_famType)) {                        // don't post-process if it's not a family type we care about
         addIgnoredStatement("Family " + FamilyTypeToString(_famType) + "{...}");
         set_alreadyAggregated(true);
      }
      else {
         for (IFU_FamStanza ifufam: _ifuFamStanzas) {
                        
            ifufam.postProcessStanza();
            
            if (ifufam.get_stanzaStatus() == StanzaStatusType.ACTIVE) {
            
               switch (ifufam.getType()) {
               case ADDRESS:
                  if (_famType != FamilyType.INET && _famType != FamilyType.INET6 &&_famType != FamilyType.ISO) {
                     throw new Error("Unexpected Family Substanza!");
                  }
               
                  IFUF_AddressStanza ifufa = (IFUF_AddressStanza) ifufam;
                  _address = ifufa.get_address();
                  _subnetMask = ifufa.get_subnetMask();
                  
                  if (ifufa.get_stanzaStatus() == StanzaStatusType.IPV6) {
                     this.set_stanzaStatus(StanzaStatusType.IPV6);
                  }
                  break;
               
               case FILTER:
                  if (_famType != FamilyType.INET && _famType != FamilyType.INET6) {
                     throw new Error("Unexpected Family Substanza!");
                  }
                  // TODO [P0]: what to do with filters!
                  break;
      
               case NATIVE_VLAN_ID:
                  IFUF_NativeVlanIdStanza ifufn = (IFUF_NativeVlanIdStanza) ifufam;
                  _nativeVlan = ifufn.get_vlanId();
                  break;
                  
               case VLAN_MEMBERS:
                  //IFUF_VlanMembersStanza ifufv = (IFUF_VlanMembersStanza) ifufam;
                  // TODO [P0}: what to do wtih this!
                  break;
      
               case NULL:
                  break;
               
               default:
                  throw new Error("bad family stanza type");
               }
            }
            addIgnoredStatements(ifufam.get_ignoredStatements());
         }
         
         if (get_stanzaStatus()==StanzaStatusType.IPV6) {       // if we ran into an IPV6 address, cut short
            clearIgnoredStatements();
            addIgnoredStatement("Family " + FamilyTypeToString(FamilyType.INET6) + "{...}");
            set_alreadyAggregated(true);
         }
         else {
            set_alreadyAggregated(false);
         }
      }
      super.postProcessStanza();
              
   }

   

   @Override
   public IF_UType getType() {
      return IF_UType.FAMILY;
   }
}
