package org.batfish.representation.cisco;

import org.batfish.datamodel.Prefix6;

public class RoutePolicyPrefixSetNumberV6 extends RoutePolicyPrefixSetInline {

   private static final long serialVersionUID = 1L;

   private Prefix6 _prefix;

   public RoutePolicyPrefixSetNumberV6(Prefix6 prefix, Integer lower,
         Integer upper) {
      super(lower, upper);
      _prefix = prefix;
   }

   public Prefix6 getPrefix() {
      return _prefix;
   }

   @Override
   public RoutePolicyPrefixType getPrefixType() {
      return RoutePolicyPrefixType.NUMBER_V6;
   }

}
