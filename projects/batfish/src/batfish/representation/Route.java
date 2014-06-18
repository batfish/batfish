package batfish.representation;

import batfish.util.Util;

public abstract class Route {

   protected Ip _nextHopIp;
   protected Ip _prefix;
   protected int _prefixLength;
   protected Ip _prefixMask;

   public Route(Ip prefix, int prefixLength, Ip nextHopIp) {
      _prefix = prefix;
      _prefixLength = prefixLength;
      _nextHopIp = nextHopIp;
      long maskLong = Util.numSubnetBitsToSubnetInt(_prefixLength);
      _prefixMask = new Ip(Util.longToIp(maskLong));
   }

   public abstract int getAdministrativeCost();
   
   public Ip getNextHopIp() {
      return _nextHopIp;
   }
   
   public Ip getPrefix() {
      return _prefix;
   }

   public int getPrefixLength() {
      return _prefixLength;
   }

   public Ip getPrefixMask() {
      return _prefixMask;
   }

   public abstract RouteType getRouteType();
   
   @Override
   public abstract boolean equals(Object o); 
   
   public String getRouteString() {
	   return String.format("Prefix %s PrefixLength %s PrefixMask %s NextHop %s", 
			  				_prefix, _prefixLength, _prefixMask, _nextHopIp);			   													
   }

   public abstract String getIFString(int indentLevel);

}
