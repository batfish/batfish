package batfish.representation.cisco;

import java.io.Serializable;

import batfish.representation.Ip;

public class StaticRoute implements Serializable {

   private static final long serialVersionUID = 1L;

   private int _distance;
   private Ip _mask;
   private String _nextHopInterface;
   private Ip _nextHopIp;
   private boolean _permanent;
   private Ip _prefix;
   private Integer _tag;
   private Integer _track;
   private Ip _nextHopPrefix;
   private Ip _nextHopMask;

   public StaticRoute(Ip prefix, Ip mask, Ip nextHopIp, Ip nextHopPrefix, Ip nextHopMask,
         String nextHopInterface, int distance, Integer tag, Integer track,
         boolean permanent) {
      _prefix = prefix;
      _mask = mask;
      _nextHopIp = nextHopIp;
      _nextHopPrefix = nextHopPrefix;
      _nextHopMask = nextHopMask;
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

   public Ip getNextHopPrefix() {
      return _nextHopPrefix;
   }

   public Ip getNextHopMask() {
      return _nextHopMask;
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
