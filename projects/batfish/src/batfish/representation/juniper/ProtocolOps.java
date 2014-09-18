package batfish.representation.juniper;

public class ProtocolOps  {
   
   public static String ProtocolTypeToString (ProtocolType pt) {
      switch (pt) {
      case AGGREGATE: 
         return "AGGREGATE";
      case BGP:
         return "BGP";
      case DIRECT:
         return "DIRECT";
      case ISIS:
         return "ISIS";
      case MSDP:
         return "MSDP";
      case OSPF:
         return "OSPF";
      case STATIC:
         return "STATIC";
      default:
         return "SOMETHING WENT WRONG";
      }
   }
  
   public static ProtocolType ProtocolTypeFromString (String s) {
      switch (s) {
      case "AGGREGATE": 
         return ProtocolType.AGGREGATE;
      case "BGP":
         return ProtocolType.BGP;
      case "DIRECT":
         return ProtocolType.DIRECT;
      case "ISIS":
         return ProtocolType.ISIS;
      case "MSDP":
         return ProtocolType.MSDP;
      case "OSPF":
         return ProtocolType.OSPF;
      case "STATIC":
         return ProtocolType.STATIC;
      }
      return ProtocolType.UNKNOWN;
   }
}