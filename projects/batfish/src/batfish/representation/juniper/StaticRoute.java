package batfish.representation.juniper;

public class StaticRoute {
   private int _distance;
   private String _mask;
   private String _nextHopInterface;
   private String _nextHopIp;
   private String _prefix;
   private int _tag;

   public StaticRoute(String prefix, String mask, String nextHopIp,
         String nextHopInterface, int distance, int tag) {
      _prefix = prefix;
      _mask = mask;
      _nextHopIp = nextHopIp;
      _nextHopInterface = nextHopInterface;
      _distance = distance;
      _tag = tag;
   }

   public int getDistance() {
      return _distance;
   }

   public String getMask() {
      return _mask;
   }

   public String getNextHopInterface() {
      return _nextHopInterface;
   }

   public String getNextHopIp() {
      return _nextHopIp;
   }

   public String getPrefix() {
      return _prefix;
   }

   public int getTag() {
      return _tag;
   }

}
