package org.batfish.representation.juniper;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.math.LongMath;
import java.math.RoundingMode;
import java.util.SortedMap;
import java.util.SortedSet;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

public final class AddressRangeAddressBookEntry extends AddressBookEntry {

  private final Ip _lowerLimit;
  private final Ip _upperLimit;

  public AddressRangeAddressBookEntry(String name, Ip lowerLimit, Ip upperLimit) {
    super(name);
    _lowerLimit = lowerLimit;
    _upperLimit = upperLimit;
  }

  @Override
  public SortedMap<String, AddressSetEntry> getEntries() {
    return ImmutableSortedMap.of();
  }

  @Override
  public SortedSet<IpWildcard> getIpWildcards(Warnings w) {
    return rangeToWildcards(_lowerLimit, _upperLimit);
  }

  /**
   * Converts a range of {@link Ip} addresses to a minimal set of {@link Prefix prefixes} that
   * exactly cover the range.
   */
  @VisibleForTesting
  static SortedSet<IpWildcard> rangeToWildcards(Ip low, Ip high) {
    checkArgument(low.valid(), "Illegal range: %s is not a valid IP", low);
    checkArgument(high.valid(), "Illegal range: %s is not a valid IP", high);
    checkArgument(low.compareTo(high) <= 0, "Illegal range: %s is larger than %s", low, high);

    long lo = low.asLong();
    long hi = high.asLong();

    ImmutableSortedSet.Builder<IpWildcard> ret = ImmutableSortedSet.naturalOrder();
    long start = lo;
    while (start <= hi) {
      long numIps = hi - start + 1;
      int minPrefixLengthThatStartsAtStart = 32 - Math.min(Long.numberOfTrailingZeros(start), 32);
      int minPrefixLengthContainedInRange = 32 - LongMath.log2(numIps, RoundingMode.DOWN);
      int prefixLengthToUse =
          Math.max(minPrefixLengthThatStartsAtStart, minPrefixLengthContainedInRange);
      ret.add(IpWildcard.create(Prefix.create(Ip.create(start), prefixLengthToUse)));
      start += 1L << (32 - prefixLengthToUse);
    }
    return ret.build();
  }
}
