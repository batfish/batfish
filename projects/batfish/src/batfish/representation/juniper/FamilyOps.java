package batfish.representation.juniper;

import batfish.main.BatfishException;

public class FamilyOps  {
   
   public static boolean FamilyTypeIgnored (FamilyType ft) {
      switch (ft) {
      case BRIDGE: 
      case ETHERNET_SWITCHING:
      case INET:
         return false;
      case CCC:
      case INET_VPN:
      case INET6:
      case INET6_VPN:
      case ISO:
      case L2_VPN:
      case MPLS:
      case VPLS:
         return true; 
      }
      throw new BatfishException("Invalid family type");
   }
  
   public static String FamilyTypeToString (FamilyType ft) {
      switch (ft) {
      case BRIDGE: 
         return "BRIDGE";
      case CCC:
         return "CCC";
      case ETHERNET_SWITCHING:
         return "ETHERNET_SWITCHING";
      case INET:
         return "INET";
      case INET_VPN:
         return "INET_VPN";
      case INET6:
         return "INET6";
      case INET6_VPN:
         return "INET6_VPN";
      case ISO:
         return "ISO";
      case L2_VPN:
         return "L2_VPN";
      case MPLS:
         return "MPLS";
      case VPLS:
         return "VPLS";
      }
      throw new BatfishException("Invalid family string");
   }
  
   public static FamilyType FamilyTypeFromString (String s) {
      switch (s.toUpperCase()) {
      case "BRIDGE": 
         return FamilyType.BRIDGE;
      case "CCC":
         return FamilyType.CCC;
      case "ETHERNET_SWITCHING":
         return FamilyType.ETHERNET_SWITCHING;
      case "INET":
         return FamilyType.INET;
      case "INET_VPN":
         return FamilyType.INET_VPN;
      case "INET6":
         return FamilyType.INET6;
      case "INET6_VPN":
         return FamilyType.INET6_VPN;
      case "ISO":
         return FamilyType.ISO;
      case "L2_VPN":
         return FamilyType.L2_VPN;
      case "MPLS":
         return FamilyType.MPLS;
      case "VPLS":
         return FamilyType.VPLS;
      }
      throw new BatfishException("Invalid family type string");
   }
}