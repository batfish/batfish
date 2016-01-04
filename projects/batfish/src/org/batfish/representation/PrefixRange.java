package org.batfish.representation;

import org.batfish.common.Pair;
import org.batfish.util.SubRange;

public class PrefixRange extends Pair<Prefix, SubRange> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public static PrefixRange fromPrefix(Prefix prefix) {
      int prefixLength = prefix.getPrefixLength();
      return new PrefixRange(prefix, new SubRange(prefixLength, prefixLength));
   }

   public PrefixRange(Prefix prefix, SubRange lengthRange) {
      super(prefix, lengthRange);
   }

   public SubRange getLengthRange() {
      return _second;
   }

   public Prefix getPrefix() {
      return _first;
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

   public boolean includesPrefixRange(PrefixRange argPrefixRange) {
      Prefix prefix = getPrefix();
      SubRange lengthRange = getLengthRange();
      int prefixLength = prefix.getPrefixLength();
      long maskedPrefixAsLong = prefix.getAddress()
            .getNetworkAddress(prefixLength).asLong();
      Prefix argPrefix = argPrefixRange.getPrefix();
      SubRange argLengthRange = argPrefixRange.getLengthRange();
      long argMaskedPrefixAsLong = argPrefix.getAddress()
            .getNetworkAddress(prefixLength).asLong();
      return maskedPrefixAsLong == argMaskedPrefixAsLong
            && lengthRange.getStart() <= argLengthRange.getStart()
            && lengthRange.getEnd() >= argLengthRange.getEnd();
   }

}
