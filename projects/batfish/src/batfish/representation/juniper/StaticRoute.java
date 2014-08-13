package batfish.representation.juniper;

public class StaticRoute {
   private String _prefix;
   private String _mask;
   private String _nextHopIp;
   private int    _distance;
   private String _nextHopInterface;

   public StaticRoute(String prefix, String mask, String nextHopIp,
         String nextHopInterface, int distance) {
      _prefix = prefix;
      _mask = mask;
      _nextHopIp = nextHopIp;
      _nextHopInterface = nextHopInterface;
      _distance = distance;
   }

   public String getPrefix() {
      return _prefix;
   }

   public String getMask() {
      return _mask;
   }

   public String getNextHopIp() {
      return _nextHopIp;
   }

   public String getNextHopInterface() {
      return _nextHopInterface;
   }

   public int getDistance() {
      return _distance;
   }

}
