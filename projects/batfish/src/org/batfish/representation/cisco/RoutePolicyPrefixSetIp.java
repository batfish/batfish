package org.batfish.representation.cisco;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.SubRange;

public class RoutePolicyPrefixSetIp extends RoutePolicyPrefixSetInline {

   private static final long serialVersionUID = 1L;
   private Ip _addr;

   private SubRange _range;

   public RoutePolicyPrefixSetIp(Ip addr, Integer lower, Integer upper) {
      super(lower, upper);
      _addr = addr;
   }

   public Ip getAddress() {
      return _addr;
   }

   @Override
   public RoutePolicyPrefixType getPrefixType() {
      return RoutePolicyPrefixType.IP;
   }

   public SubRange getRange() {
      return _range;
   }

}
