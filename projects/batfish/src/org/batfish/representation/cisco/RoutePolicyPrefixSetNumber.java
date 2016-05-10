package org.batfish.representation.cisco;

import org.batfish.common.datamodel.Prefix;

public class RoutePolicyPrefixSetNumber extends RoutePolicyPrefixSetInline {

   private static final long serialVersionUID = 1L;

   private Prefix _prefix;

   public RoutePolicyPrefixSetNumber(Prefix prefix, Integer lower, Integer upper) {
      super(lower, upper);
      _prefix = prefix;
   }

   public Prefix getPrefix() {
      return _prefix;
   }

   @Override
   public RoutePolicyPrefixType getPrefixType() {
      return RoutePolicyPrefixType.NUMBER;
   }

}
