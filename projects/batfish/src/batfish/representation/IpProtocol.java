package batfish.representation;

import java.util.HashMap;
import java.util.Map;

public enum IpProtocol {
   AHP(51),

   EIGRP(88),

   ESP(50),

   GRE(47),

   ICMP(1),

   IGMP(2),

   IP(0),

   IPINIP(4),

   OSPF(89),

   PIM(103),

   SCTP(132),

   TCP(6),

   UDP(17),

   VRRP(112);

   private static final Map<Integer, IpProtocol> NUMBER_TO_PROTOCOL_MAP = buildNumberToProtocolMap();

   private static Map<Integer, IpProtocol> buildNumberToProtocolMap() {
      Map<Integer, IpProtocol> map = new HashMap<Integer, IpProtocol>();
      for (IpProtocol protocol : values()) {
         map.put(protocol._number, protocol);
      }
      return map;
   }

   public static IpProtocol fromNumber(int number) {
      return NUMBER_TO_PROTOCOL_MAP.get(number);
   }

   private int _number;

   private IpProtocol(int number) {
      _number = number;
   }

   public int number() {
      return _number;
   }

}
