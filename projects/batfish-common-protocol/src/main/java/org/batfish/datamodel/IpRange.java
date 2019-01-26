package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.AclIpSpace.intersection;
import static org.batfish.datamodel.AclIpSpace.union;

import com.google.common.collect.ImmutableMap;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.ipspace.IpSpaceSimplifier;

@ParametersAreNonnullByDefault
public final class IpRange {

  /**
   * Returns an {@link IpSpace} representing the space of all IPs whose magnitude is larger than
   * that of {@@code operaand}.
   */
  public static @Nonnull IpSpace greaterThanOrEqualTo(Ip operand) {
    return new IpSpaceSimplifier(ImmutableMap.of())
        .simplify(greaterThanOrEqualTo(operand.asLong(), Prefix.MAX_PREFIX_LENGTH - 1));
  }

  private static @Nonnull IpSpace greaterThanOrEqualTo(long operand, int cursor) {
    // cursor begins at MSB and computation ends after passing LSB
    if (cursor < 0) {
      return UniverseIpSpace.INSTANCE;
    }
    // cursorMask contains a single 1 at cursor position
    long cursorMask = 1L << cursor;
    Ip cursorIp = Ip.create(cursorMask);
    Ip cursorMaskIp = Ip.create(0xFFFFFFFFL & ~cursorMask);
    // oneAtCursor is the space of all IPs with a 1 at cursor position
    IpSpace oneAtCursor = new IpWildcard(cursorIp, cursorMaskIp).toIpSpace();
    if ((operand & cursorMask) != 0L) {
      // The operand has a 1 at cursor position.
      // To be GE, must have a 1 at cursor position AND be GE from next cursor position
      return intersection(oneAtCursor, greaterThanOrEqualTo(operand, cursor - 1));
    } else {
      // The operand has a 0 at cursor position.
      // To be GE, must have a 1 at cursor position OR be GE from next cursor position
      return union(oneAtCursor, greaterThanOrEqualTo(operand, cursor - 1));
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
    return intersection(
        greaterThanOrEqualTo(low),
        union(high.toIpSpace(), greaterThanOrEqualTo(high).complement()));
  }

  private IpRange() {}
}
