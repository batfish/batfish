package batfish.representation.juniper;

public class FamilyOps  {
   
   public static boolean FamilyTypeIgnored (FamilyType ft) {
      switch (ft) {
      case BRIDGE: 
      case ETHERNET_SWITCHING:
      case INET:
         return false;
      case CCC:
      case ISO:
      case MPLS:
      case INET6:
         return true; 
      default:
         return false;
      }
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
      case INET6:
         return "INET6";
      case ISO:
         return "ISO";
      case MPLS:
         return "MPLS";
      default:
         return "SOMETHING WENT WRONG";
      }
   }
  
   public static FamilyType FamilyTypeFromString (String s) {
      switch (s) {
      case "BRIDGE": 
         return FamilyType.BRIDGE;
      case "CCC":
         return FamilyType.CCC;
      case "ETHERNET_SWITCHING":
         return FamilyType.ETHERNET_SWITCHING;
      case "INET":
         return FamilyType.INET;
      case "INET6":
         return FamilyType.INET6;
      case "ISO":
         return FamilyType.ISO;
      case "MPLS":
         return FamilyType.MPLS;
      }
      return FamilyType.UNKNOWN;
   }
}