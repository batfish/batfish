package batfish.representation.juniper;

import java.io.Serializable;

public class StaticRoute implements Serializable {

   private static final long serialVersionUID = 1L;

   private int _distance;
   private String _mask;
   private String _nextHopInterface;
   private String _nextHopIp;
   private String _prefix;
   private int _tag;

   public StaticRoute(String prefix, String mask, String nextHopIp,
         String nextHopInterface, int distance) {
      _prefix = prefix;
      _mask = mask;
      _nextHopIp = nextHopIp;
      _nextHopInterface = nextHopInterface;
      _distance = distance;
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
