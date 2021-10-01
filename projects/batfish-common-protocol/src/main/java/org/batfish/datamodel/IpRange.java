package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Utility class to create an {@link IpSpace} including all IPs between two given IPs. */
@ParametersAreNonnullByDefault
public final class IpRange {

  /**
   * Adds all prefixes contained in the IP range between {@code lowIp} and {@code highIp}
   * (inclusive) to the given {@link Prefix} list.
   */
  private static void collectPrefixes(Ip lowIp, Ip highIp, ImmutableList.Builder<Prefix> prefixes) {
    long low = lowIp.asLong();
    long high = highIp.asLong();

    // If IPs are the same, just add /32 prefix
    if (low == high) {
      prefixes.add(lowIp.toPrefix());
      return;
    }

    // Create mask with one 1 at the most significant bit at which the IPs differ
    long cursorMask = Long.highestOneBit(low ^ high);
    int cursor = Long.numberOfTrailingZeros(cursorMask);

    // If everything starting from the bit that differs is 0 in low and 1 in high, add the prefix
    // shared by lowIp and highIp. Otherwise recurse on subranges. mask is all 1s starting at cursor
    long mask = (cursorMask << 1) - 1;
    long ip1BitsAfterCursor = low & mask;
    long ip2BitsAfterCursor = high & mask;
    if (ip1BitsAfterCursor == 0 && ip2BitsAfterCursor == mask) {
      prefixes.add(Prefix.create(lowIp, Prefix.MAX_PREFIX_LENGTH - 1 - cursor));
    } else {
      // Upper bound in first recursion is lowIp with everything after the cursor replaced with 1s;
      // lower bound in second recursion is highIp with everything after the cursor replaced with 0s
      collectPrefixes(lowIp, Ip.create(low | (mask >> 1)), prefixes);
      collectPrefixes(Ip.create((high >> cursor) << cursor), highIp, prefixes);
    }
  }

  /**
   * Returns an {@link IpSpace} representing the space of all IPs whose magnitude is between that of
   * {@code low} and {@code high} (inclusive).
   */
  public static @Nonnull IpSpace range(Ip low, Ip high) {
    checkArgument(
        low.asLong() <= high.asLong(),
        "Invalid range: low IP must be <= high IP, but received low=%s and high=%s",
        low,
        high);
    ImmutableList.Builder<Prefix> prefixes = ImmutableList.builder();
    collectPrefixes(low, high, prefixes);
    List<Prefix> prefixList = prefixes.build();
    if (prefixList.size() == 1) {
      return prefixList.get(0).toIpSpace();
    }
    return IpWildcardSetIpSpace.create(
        ImmutableSet.of(),
        prefixList.stream().map(IpWildcard::create).collect(ImmutableSet.toImmutableSet()));
  }

  private IpRange() {}
}
