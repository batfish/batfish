package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class IpRange {

  /**
   * Adds all prefixes contained in the IP range between {@code lowIp} and {@code highIp}
   * (inclusive) to the given {@link Prefix} list.
   */
  private static void collectPrefixes(Ip lowIp, Ip highIp, List<Prefix> prefixes) {
    long low = lowIp.asLong();
    long high = highIp.asLong();

    // If IPs are the same, just add /32 prefix
    if (low == high) {
      prefixes.add(Prefix.create(lowIp, 32));
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
      prefixes.add(Prefix.create(lowIp, 31 - cursor));
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

    List<Prefix> prefixes = new ArrayList<>();
    collectPrefixes(low, high, prefixes);
    AclIpSpace.Builder ipSpaceBuilder = AclIpSpace.builder();
    prefixes.forEach(p -> ipSpaceBuilder.thenPermitting(new PrefixIpSpace(p)));
    return ipSpaceBuilder.build();
  }

  private IpRange() {}
}
