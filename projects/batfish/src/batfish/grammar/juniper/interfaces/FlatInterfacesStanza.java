package batfish.grammar.juniper.interfaces;

import batfish.grammar.juniper.JStanza;
import batfish.grammar.juniper.JStanzaType;
import batfish.representation.juniper.Interface;

public class FlatInterfacesStanza extends JStanza {
   private IFType _type1;
   private FamilyType _type2;
   private FamilyUType _type3;
   private Interface _interface;
   private String _name;

   public FlatInterfacesStanza(String name) {
      _name = name;
   }

   public void processStanza(IFStanza ifs) {
      _type1 = ifs.getType();
      switch (ifs.getType()) {
      case DISABLE:
         Interface i = new Interface(_name);
         i.setActive(false);
         i.setBandwidth(getDefaultBandwidth(_name));
         _interface = i;
         break;

      case NULL:
         break;

      case UNIT:
         FlatUnitIFStanza uifs = (FlatUnitIFStanza) ifs;
         _type2 = uifs.getType1();
         _type3 = uifs.getType2();
         String u = Integer.toString(uifs.getUnitNum());
         Interface ui = new Interface(_name + "." + u);
         if (uifs.getFamilyType() != null) {
            switch (uifs.getFamilyType()) {
            case BRIDGE:
               ui.setSwitchportMode(uifs.getInterfaceMode());
               ui.setAccessVlan(uifs.getAccessVlan());
               ui.setNativeVlan(0);
               if (uifs.getVlanIDList() != null) {
                  ui.addAllowedRanges(uifs.getVlanIDList());
               }
               break;

            case INET:
               ui.setIP(uifs.getAddress());
               ui.setSubnetMask(uifs.getSubnetMask());
               break;

            case INET6:
               break;

            default:
               System.out.println("bad family type");
               break;
            }
         }
         ui.setBandwidth(getDefaultBandwidth(_name));
         _interface = ui;
         break;
      case NATIVE_VLAN_ID:
         throw new Error("to implement");

      default:
         System.out.println("bad interface stanza type");
         break;
      }

   }

   private static Double getDefaultBandwidth(String name) {
      Double bandwidth = null;
      if (name.startsWith("fe")) {
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

   public Interface getInterface() {
      return _interface;
   }

   public IFType getType1() {
      return _type1;
   }

   public FamilyType getType2() {
      return _type2;
   }

   public FamilyUType getType3() {
      return _type3;
   }

   @Override
   public JStanzaType getType() {
      return JStanzaType.INTERFACES;
   }

}