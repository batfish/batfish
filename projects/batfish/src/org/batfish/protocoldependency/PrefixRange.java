package org.batfish.protocoldependency;

import org.batfish.common.Pair;
import org.batfish.representation.Prefix;
import org.batfish.util.SubRange;

public class PrefixRange extends Pair<Prefix, SubRange> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public PrefixRange(Prefix prefix, SubRange lengthRange) {
      super(prefix, lengthRange);
   }

   public SubRange getLengthRange() {
      return _t2;
   }

   public Prefix getPrefix() {
      return _t1;
   }

   public boolean includesPrefix(Prefix argPrefix) {
      Prefix prefix = getPrefix();
      SubRange lengthRange = getLengthRange();
      int prefixLength = prefix.getPrefixLength();
      int minPrefixLength = lengthRange.getStart();
      int maxPrefixLength = lengthRange.getEnd();
      int argPrefixLength = argPrefix.getPrefixLength();
      if (minPrefixLength > argPrefixLength
            || maxPrefixLength < argPrefixLength) {
         return false;
      }
      long maskedPrefixAsLong = prefix.getAddress()
            .getNetworkAddress(prefixLength).asLong();
      long argMaskedPrefixAsLong = argPrefix.getAddress()
            .getNetworkAddress(prefixLength).asLong();
      return maskedPrefixAsLong == argMaskedPrefixAsLong;
   }

}
