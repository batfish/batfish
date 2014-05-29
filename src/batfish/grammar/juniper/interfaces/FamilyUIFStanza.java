package batfish.grammar.juniper.interfaces;

import java.util.ArrayList;

import batfish.util.SubRange;
import batfish.representation.SwitchportMode;

public class FamilyUIFStanza {
   private FamilyType _type;
   private String _address;
   private String _subnetMask;
   private SwitchportMode _interfaceMode;
   private int _accessVlan;
   private int _nativeVlan;
   private ArrayList<SubRange> _vlanID;
   private String _incomingFilter;
   private String _outgoingFilter;

   public FamilyUIFStanza(FamilyType t) {
      _type = t;

      // default switch port mode is access in Juniper
      _interfaceMode = SwitchportMode.ACCESS;
   }

   public void processFamilyUStanza(FamilyUStanza fus) {
      switch (fus.getType()) {
      case ADDRESS:
         AddressFamilyUStanza afus = (AddressFamilyUStanza) fus;
         _address = afus.getAddress();
         _subnetMask = afus.getSubnetMask();
         break;

      case INTERFACE_MODE:
         InterfaceModeFamilyUStanza imfus = (InterfaceModeFamilyUStanza) fus;
         _interfaceMode = imfus.getMode();
         break;

      case NULL:
         break;

      case VLAN_ID:
         VlanIdFamilyUStanza vfus = (VlanIdFamilyUStanza) fus;
         _accessVlan = vfus.getVlanID();
         break;

      case VLAN_ID_LIST:
         VlanIdListFamilyUStanza vlfus = (VlanIdListFamilyUStanza) fus;
         _vlanID = vlfus.getVlanList();
         break;

      case NATIVE_VLAN:
         NativeVlanIdFamilyUStanza nvlfus = (NativeVlanIdFamilyUStanza) fus;
         _nativeVlan = nvlfus.getNativeVlan();
         break;
         
      case FILTER:
         FilterFamilyUStanza ffus = (FilterFamilyUStanza) fus;
         _incomingFilter = ffus.getIncomingFilterName();
         _outgoingFilter = ffus.getOutgoingFilterName();
         break;

      default:
         System.out.println("bad family stanza type");
         break;
      }

   }

   public FamilyType getType() {
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
   
   public String getIncomingFilter(){
      return _incomingFilter;
   }
   
   public String getOutgoingFilter(){
      return _outgoingFilter;
   }
}
