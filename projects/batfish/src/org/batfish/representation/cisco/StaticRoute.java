package org.batfish.representation.cisco;

import java.io.Serializable;

import org.batfish.representation.Ip;
import org.batfish.representation.Prefix;

public class StaticRoute implements Serializable {

   private static final long serialVersionUID = 1L;

   private int _distance;
   private String _nextHopInterface;
   private Ip _nextHopIp;
   private boolean _permanent;
   private Prefix _prefix;
   private Integer _tag;
   private Integer _track;

   public StaticRoute(Prefix prefix, Ip nextHopIp, String nextHopInterface,
         int distance, Integer tag, Integer track, boolean permanent) {
      _prefix = prefix;
      _nextHopIp = nextHopIp;
      _nextHopInterface = nextHopInterface;
      _distance = distance;
      _tag = tag;
      _track = track;
      _permanent = permanent;
   }

   @Override
   public boolean equals(Object o) {
      StaticRoute rhs = (StaticRoute) o;
      boolean res = _prefix.equals(rhs._prefix);
      if (_nextHopIp != null) {
         res = res && _nextHopIp.equals(rhs._nextHopIp);
      }
      else {
         res = res && rhs._nextHopIp == null;
      }
      if (_nextHopInterface != null) {
         return res && _nextHopInterface.equals(rhs._nextHopInterface);
      }
      else {
         return res && rhs._nextHopInterface == null;
      }
   }

   public int getDistance() {
      return _distance;
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

   public Prefix getPrefix() {
      return _prefix;
   }

   public Integer getTag() {
      return _tag;
   }

   public Integer getTrack() {
      return _track;
   }

   @Override
   public int hashCode() {
      int code = _prefix.hashCode();
      if (_nextHopInterface != null) {
         code = code * 31 + _nextHopInterface.hashCode();
      }
      if (_nextHopIp != null) {
         code = code * 31 + _nextHopIp.hashCode();
      }
      return code;
   }

}
