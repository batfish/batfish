package batfish.grammar.juniper.interfaces;

import java.util.ArrayList;

import batfish.representation.SwitchportMode;
import batfish.util.SubRange;

public class UnitIFStanza extends IFStanza {
   private int _num;
   private FamilyType _type;
   private String _address;
   private String _subnetMask;
   private SwitchportMode _interfaceMode;
   private int _accessVlan;
   private int _nativeVlan;
   private ArrayList<SubRange> _vlanID;
   private boolean _active;
   private String _incomingFilter;
   private String _outgoingFilter;

   public UnitIFStanza(int num) {
      _num = num;
      // default switch port mode is access in Juniper
      _interfaceMode = SwitchportMode.ACCESS;
      _active = true;
   }

   public void processFamily(FamilyUIFStanza fuifs) {
      switch (fuifs.getType()) {
      case BRIDGE:
         _type = fuifs.getType();
         _interfaceMode = fuifs.getInterfaceMode();
         _accessVlan = fuifs.getAccessVlan();
         _nativeVlan = fuifs.getNativeVlan();
         _vlanID = fuifs.getVlanIDList();
         break;

      case INET:
         _type = fuifs.getType();
         String tmpAdd = fuifs.getAddress();
         if (tmpAdd != null) {
            _address = tmpAdd;
            _subnetMask = convertSubnet(fuifs.getSubnetMask());
         }
         else {
            _address = "";
            _subnetMask = null;
         }
         _accessVlan = fuifs.getAccessVlan();
         _incomingFilter = fuifs.getIncomingFilter();
         _outgoingFilter = fuifs.getOutgoingFilter();
         break;

      case INET6:
         break;

      default:
         System.out.println("bad family type");
         break;
      }
   }

   private String convertSubnet(String s) {
      String result = "";
      int sval = Integer.parseInt(s);
      if ((sval >= 0) && (sval <= 8)) {

         if (sval == 0) {
            result += "0";
         }
         else if (sval == 1) {
            result += "128";
         }
         else if (sval == 2) {
            result += "192";
         }
         else if (sval == 3) {
            result += "224";
         }
         else if (sval == 4) {
            result += "240";
         }
         else if (sval == 5) {
            result += "248";
         }
         else if (sval == 6) {
            result += "252";
         }
         else if (sval == 7) {
            result += "254";
         }
         else if (sval == 8) {
            result += "255";
         }
         result += ".0.0.0";

      }
      else if ((sval >= 9) && (sval <= 16)) {

         result += "255.";
         if (sval == 9) {
            result += "128";
         }
         else if (sval == 10) {
            result += "192";
         }
         else if (sval == 11) {
            result += "224";
         }
         else if (sval == 12) {
            result += "240";
         }
         else if (sval == 13) {
            result += "248";
         }
         else if (sval == 14) {
            result += "252";
         }
         else if (sval == 15) {
            result += "254";
         }
         else if (sval == 16) {
            result += "255";
         }
         result += ".0.0";

      }
      else if ((sval >= 17) && (sval <= 24)) {

         result += "255.255.";
         if (sval == 17) {
            result += "128";
         }
         else if (sval == 18) {
            result += "192";
         }
         else if (sval == 19) {
            result += "224";
         }
         else if (sval == 20) {
            result += "240";
         }
         else if (sval == 21) {
            result += "248";
         }
         else if (sval == 22) {
            result += "252";
         }
         else if (sval == 23) {
            result += "254";
         }
         else if (sval == 24) {
            result += "255";
         }
         result += ".0";

      }
      else if ((sval >= 25) && (sval <= 32)) {
         result += "255.255.255.";
         if (sval == 25) {
            result += "128";
         }
         else if (sval == 26) {
            result += "192";
         }
         else if (sval == 27) {
            result += "224";
         }
         else if (sval == 28) {
            result += "240";
         }
         else if (sval == 29) {
            result += "248";
         }
         else if (sval == 30) {
            result += "252";
         }
         else if (sval == 31) {
            result += "254";
         }
         else if (sval == 32) {
            result += "255";
         }

      }
      else {
         System.out.println("bad subnet value");
      }

      return result;
   }

   public void setAccessVlan(int vlan) {
      _accessVlan = vlan;
   }

   public int getUnitNum() {
      return _num;
   }

   public FamilyType getFamilyType() {
      return _type;
   }

   public String getAddress() {
      return _address;
   }

   public String getSubnetMask() {
      return _subnetMask;
   }

   public SwitchportMode getInterfaceMode() {
      return _interfaceMode;
   }

   public int getAccessVlan() {
      return _accessVlan;
   }

   public ArrayList<SubRange> getVlanIDList() {
      return _vlanID;
   }

   public int getNativeVlan() {
      return _nativeVlan;
   }
   
   public void setDisable(){
      _active = false;
   }
   
   public boolean getActive(){
      return _active;
   }
   
   public String getIncomingFilter(){
      return _incomingFilter;
   }
   
   public String getOutgoingFilter(){
      return _outgoingFilter;
   }

   @Override
   public IFType getType() {
      return IFType.UNIT;
   }

}
