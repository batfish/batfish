package batfish.grammar.juniper.interfaces;

import java.util.ArrayList;

import batfish.representation.SwitchportEncapsulationType;
import batfish.representation.juniper.Interface;

public class InterfaceStanza {
   private ArrayList<Interface> _interfaceList;
   private String _name;
   private int _nativeVlan;

   public InterfaceStanza(String name) {
      _name = name;
      _interfaceList = new ArrayList<Interface>();
      _nativeVlan = 0;
   }

   public void processStanza(IFStanza ifs) {
      switch (ifs.getType()) {
      case DISABLE:
         Interface i = new Interface(_name);
         i.setActive(false);
         i.setBandwidth(getDefaultBandwidth(_name));
         _interfaceList.add(i);
         break;

      case NULL:
         break;

      case UNIT:
         UnitIFStanza uifs = (UnitIFStanza) ifs;
         String u = Integer.toString(uifs.getUnitNum());
         Interface ui = new Interface(_name + "." + u);
         if (!uifs.getActive()) {
            ui.setActive(false);
         }
         else {
            switch (uifs.getFamilyType()) {
            case BRIDGE:
               ui.setSwitchportMode(uifs.getInterfaceMode());
               switch (uifs.getInterfaceMode()) {
               case ACCESS:
                  ui.setAccessVlan(uifs.getAccessVlan());
                  break;

               case TRUNK:
                  if (uifs.getNativeVlan() == 0) {
                     ui.setNativeVlan(_nativeVlan);
                  }
                  else {
                     ui.setNativeVlan(uifs.getNativeVlan());
                  }
                  ui.addAllowedRanges(uifs.getVlanIDList());
                  ui.setSwitchportTrunkEncapsulation(SwitchportEncapsulationType.DOT1Q);
                  break;

               // TODO: Stanley, you should check if these dynamic modes are
               // supported
               // in Juniper, and make adjustments accordingly
               case DYNAMIC_AUTO:
               case DYNAMIC_DESIRABLE:
               case NONE:
                  throw new Error("not implemented");

               default:
                  System.out.println("bad switchport mode");
                  break;
               }
               break;

            case INET:
               ui.setIP(uifs.getAddress());
               ui.setSubnetMask(uifs.getSubnetMask());
               ui.setAccessVlan(uifs.getAccessVlan());
               ui.setIncomingFilter(uifs.getIncomingFilter());
               ui.setOutgoingFilter(uifs.getOutgoingFilter());
               break;

            case INET6:
               throw new Error("not implemented");

            default:
               System.out.println("bad family type");
               break;
            }
         }
         ui.setBandwidth(getDefaultBandwidth(_name));
         _interfaceList.add(ui);
         break;

      case NATIVE_VLAN_ID:
         NativeVlanIdIFStanza nviifs = (NativeVlanIdIFStanza) ifs;
         for (Interface inf : _interfaceList) {
            if (inf.getNativeVlan() == 0) {
               inf.setNativeVlan(nviifs.getNativeVlan());
            }
         }
         break;

      default:
         System.out.println("bad interface stanza type");
         break;
      }

   }

   private static Double getDefaultBandwidth(String name) {
      Double bandwidth = null;
      if ((name.startsWith("fe")) || (name.startsWith("em"))) {
         bandwidth = 100E6;
      }
      else if (name.startsWith("lo")) {
         bandwidth = 1E12;
      }
      else if (name.startsWith("ge")) {
         bandwidth = 1E9;
      }
      else if (name.startsWith("xe")) {
         bandwidth = 10E9;
      }
      if (bandwidth == null) {
         bandwidth = 1.0;
      }
      return bandwidth;
   }

   public ArrayList<Interface> getInterfaceList() {
      return _interfaceList;
   }

}
