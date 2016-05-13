package org.batfish.representation.cisco;

import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.SubRange;

public class RoutePolicyPrefixSetIpV6 extends RoutePolicyPrefixSetInline {

   private static final long serialVersionUID = 1L;
   private Ip6 _addr;

   private SubRange _range;

   public RoutePolicyPrefixSetIpV6(Ip6 addr, Integer lower, Integer upper) {
      super(lower, upper);
      _addr = addr;
   }

   public Ip6 getAddress() {
      return _addr;
   }

   @Override
   public RoutePolicyPrefixType getPrefixType() {
      return RoutePolicyPrefixType.IP_V6;
   }

   public SubRange getRange() {
      return _range;
   }

}
