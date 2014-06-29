package batfish.representation.cisco;

import batfish.representation.Ip;

public class StaticRoute {
   private int _distance;
   private Ip _mask;
   private String _nextHopInterface;
   private Ip _nextHopIp;
   private boolean _permanent;
   private Ip _prefix;
   private Integer _tag;
   private Integer _track;

   public StaticRoute(Ip prefix, Ip mask, Ip nextHopIp,
         String nextHopInterface, int distance, Integer tag, Integer track,
         boolean permanent) {
      _prefix = prefix;
      _mask = mask;
      _nextHopIp = nextHopIp;
      _nextHopInterface = nextHopInterface;
      _distance = distance;
      _tag = tag;
      _track = track;
      _permanent = permanent;
   }

   public int getDistance() {
      return _distance;
   }

   public Ip getMask() {
      return _mask;
   }

   public String getNextHopInterface() {
      return _nextHopInterface;
   }

   public Ip getNextHopIp() {
      return _nextHopIp;
   }

   public boolean getPermanent() {
      return _permanent;
   }

   public Ip getPrefix() {
      return _prefix;
   }

   public Integer getTag() {
      return _tag;
   }

   public Integer getTrack() {
      return _track;
   }

}
