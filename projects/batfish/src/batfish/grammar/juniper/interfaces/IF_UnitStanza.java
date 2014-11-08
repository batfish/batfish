package batfish.grammar.juniper.interfaces;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.StanzaStatusType;
import batfish.representation.SwitchportMode;
import static batfish.representation.juniper.SubnetOps.*;

public class IF_UnitStanza extends IFStanza {

   private int _num;
   private boolean _wildcard;
   private int _accessVlan; // TODO [P0]: does this get used
   private String _address;
   private String _subnetMask;
   private SwitchportMode _interfaceMode;  // TODO [P0]: does this get used
   private List<IF_UStanza> _ifuStanzas;

   /* ------------------------------ Constructor ----------------------------*/
   public IF_UnitStanza () {
      _num = 0;
      _wildcard = false;
      _address = "";
      _subnetMask = null;
      _interfaceMode = SwitchportMode.ACCESS;
      _ifuStanzas = new ArrayList<IF_UStanza>();
   }

   /* ----------------------------- Other Methods ---------------------------*/
   public void addIFUstanza(IF_UStanza ifu) {
      _ifuStanzas.add(ifu);
   }

   /* ---------------------------- Getters/Setters --------------------------*/
   public int get_num() {
      return _num;
   }

   public String get_address() {
      return _address;
   }

   public String get_subnetMask() {
      return _subnetMask;
   }

   public SwitchportMode get_interfaceMode() {
      return _interfaceMode;
   }

   public void set_num (int i) {
      _num = i;
      this.set_postProcessTitle("Unit " + i);
   }

   public void set_wildcard (boolean b) {
      _wildcard = b;
      this.set_postProcessTitle("Unit <*>");
   }

   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public void postProcessStanza() {

      for (IF_UStanza ifus : _ifuStanzas) {

         ifus.postProcessStanza();

         if (ifus.get_stanzaStatus() == StanzaStatusType.ACTIVE) {

            switch (ifus.getType()) {
            case APPLY_GROUPS:
               // TODO: [P0] WHAT TO DO
               break;

            case DISABLE:
               // TODO: [P0] WHAT TO DO
               break;
            case FAMILY: // TODO [P0]: This doesn't work with multiple family stanzas
               IFU_FamilyStanza ifufs = (IFU_FamilyStanza) ifus;
               if (ifufs.get_stanzaStatus()==StanzaStatusType.ACTIVE) {// if it's IGNORED/INACTIVE/IPV6 already noted
                  switch (ifufs.get_famType()) {                   // ignored family types already handled
                  case ETHERNET_SWITCHING:
                     // TODO: [P0]: What do these look like
                     break;
                  case INET:
                     String tmpAdd = ifufs.get_address();
                     if (tmpAdd != null) {
                        _address = tmpAdd;
                        _subnetMask = convertSubnet(ifufs.get_subnetMask());
                     }
                     // TODO [P0]: what to do with filter
                     break;
                  case BRIDGE:
                  case CCC:
                  case INET6:
                  case ISO:
                  case MPLS:
                     break;
                  case INET6_VPN:
                  case INET_VPN:
                  case L2_VPN:
                  case VPLS:

                  default:
                     throw new Error ("bad family type");
                  }
               }
               break;
            case VLAN_ID:
               IFU_VlanIdStanza ifuvs = (IFU_VlanIdStanza) ifus;
               _accessVlan = ifuvs.get_vlanid(); // TODO [ask Ari] verify difference between native and access
               break;
            case NULL:
               break;
            case ENABLE:
            default:
               throw new Error ("Bad Unit Stanza Type");
            }
         }
         addIgnoredStatements(ifus.get_ignoredStatements());
      }
      set_alreadyAggregated(false);
      super.postProcessStanza();
   }

   @Override
   public IFType getType() {
      return IFType.UNIT;
   }

}
