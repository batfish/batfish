package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An IP wildcard consisting of a IP address and a wildcard (also expressed as an IP address) */
@ParametersAreNonnullByDefault
public final class IpWildcard implements Serializable, Comparable<IpWildcard> {
  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^20: Just some upper bound on cache size, well less than GiB.
  //   (16 bytes seems smallest possible entry (long), would be 16 MiB total).
  private static final LoadingCache<IpWildcard, IpWildcard> CACHE =
      CacheBuilder.newBuilder().softValues().maximumSize(1 << 20).build(CacheLoader.from(x -> x));

  private final @Nonnull Ip _ip;
  // Set bits are "don't care" bits
  private final long _wildcardMask;

  private static final long ALL_BITS_MASKED = 0xFFFFFFFFL;
  public static final IpWildcard ANY = ipWithWildcardMask(Ip.ZERO, ALL_BITS_MASKED);

  private static Ip parseAddress(String str) {
    if (str.contains(":")) {
      String[] parts = str.split(":");
      checkArgument(parts.length == 2, "Invalid IpWildcard string: '%s'");
      return Ip.parse(parts[0]);
    } else if (str.contains("/")) {
      String[] parts = str.split("/");
      checkArgument(parts.length == 2, "Invalid IpWildcard string: '%s'");
      return Ip.parse(parts[0]);
    } else {
      return Ip.parse(str);
    }
  }

  private static Ip parseMask(String str) {
    if (str.contains(":")) {
      String[] parts = str.split(":");
      checkArgument(parts.length == 2, "Invalid IpWildcard string: '%s'");
      return Ip.parse(parts[1]);
    } else if (str.contains("/")) {
      String[] parts = str.split("/");
      checkArgument(parts.length == 2, "Invalid IpWildcard string: '%s'");
      int prefixLength = Integer.parseInt(parts[1]);
      return Ip.numSubnetBitsToSubnetMask(prefixLength).inverted();
    } else {
      return Ip.ZERO;
    }
  }

  /**
   * Return an {@link IpWildcard} from the given {@link Ip IP address} and {@code wildcardMask}.
   *
   * <p>Bits that are set in the {@code wildcardMask} are "don't care" bits.
   */
  public static IpWildcard ipWithWildcardMask(Ip address, long wildcardMask) {
    IpWildcard wildcard = new IpWildcard(address, wildcardMask);
    return CACHE.getUnchecked(wildcard);
  }

  /**
   * Return an {@link IpWildcard} from the given {@link Ip IP address} and {@link Ip wildcardMask}.
   *
   * <p>Bits that are set in the {@code wildcardMask} are "don't care" bits.
   *
   * @see #ipWithWildcardMask(Ip, long) for a version that directly accesses the mask.
   */
  public static IpWildcard ipWithWildcardMask(Ip address, Ip wildcardMask) {
    return ipWithWildcardMask(address, wildcardMask.asLong());
  }

  public static IpWildcard create(Prefix prefix) {
    int wildcardBits = Prefix.MAX_PREFIX_LENGTH - prefix.getPrefixLength();
    long wildcardMask = (1L << wildcardBits) - 1L;
    return ipWithWildcardMask(prefix.getStartIp(), wildcardMask);
  }

  public static IpWildcard create(Ip ip) {
    return ipWithWildcardMask(ip, 0L);
  }

  @JsonCreator
  public static IpWildcard parse(String str) {
    return ipWithWildcardMask(parseAddress(str), parseMask(str).asLong());
  }

  public boolean containsIp(Ip ip) {
    checkArgument(ip.valid(), "Invalid IP address %s", ip);
    long thisMasked = _ip.asLong() | _wildcardMask;
    long ipMasked = ip.asLong() | _wildcardMask;
    return thisMasked == ipMasked;
  }

  @Override
  public int compareTo(IpWildcard o) {
    if (this == o) {
      return 0;
    }
    return Comparator.comparing(IpWildcard::getIp)
        .thenComparing(IpWildcard::getWildcardMask)
        .compare(this, o);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof IpWildcard)) {
      return false;
    }
    IpWildcard other = (IpWildcard) o;
    return _ip.equals(other._ip) && _wildcardMask == other._wildcardMask;
  }

  @Override
  public int hashCode() {
    return 31 * _ip.hashCode() + Long.hashCode(_wildcardMask);
  }

  public @Nonnull Ip getIp() {
    return _ip;
  }

  public @Nonnull Ip getWildcardMaskAsIp() {
    return Ip.create(_wildcardMask);
  }

  public long getWildcardMask() {
    return _wildcardMask;
  }

  public long getMask() {
    return ALL_BITS_MASKED ^ _wildcardMask;
  }

  /**
   * @param other another IpWildcard
   * @return whether the set of IPs matched by this intersects the set of those matched by other.
   */
  public boolean intersects(IpWildcard other) {
    long differentIpBits = _ip.asLong() ^ other._ip.asLong();
    long canDiffer = _wildcardMask | other._wildcardMask;

    /*
     * All IP bits that different must be wild in either this or other.
     */
    return (differentIpBits & canDiffer) == differentIpBits;
  }

  public boolean isPrefix() {
    return (_wildcardMask & (_wildcardMask + 1L)) == 0L;
  }

  /**
   * @param other another IpWildcard
   * @return whether the set of IPs matched by this is a subset of those matched by other.
   */
  public boolean subsetOf(IpWildcard other) {
    return other.supersetOf(this);
  }

  /**
   * @param other another IpWildcard
   * @return whether the set of IPs matched by this is a superset of those matched by other.
   */
  public boolean supersetOf(IpWildcard other) {
    long wildToThis = _wildcardMask;
    long wildToOther = other._wildcardMask;
    long differentIpBits = _ip.asLong() ^ other._ip.asLong();

    /*
     * 1. Any bits wild in other must be wild in this (i.e. other's wild bits must be a subset
     *    of this' wild bits).
     * 2. Any IP bits that differ must be wild in this.
     */
    return (wildToThis & wildToOther) == wildToOther
        && (wildToThis & differentIpBits) == differentIpBits;
  }

  public @Nonnull IpWildcardIpSpace toIpSpace() {
    return IpWildcardIpSpace.create(this);
  }

  public Prefix toPrefix() {
    checkState(isPrefix(), "Invalid wildcard format for conversion to prefix: %s", _wildcardMask);
    return Prefix.create(_ip, Integer.numberOfLeadingZeros((int) _wildcardMask));
  }

  @JsonValue
  @Override
  public String toString() {
    if (_wildcardMask == 0L) {
      return _ip.toString();
    } else if (isPrefix()) {
      return toPrefix().toString();
    } else {
      return _ip + ":" + getWildcardMaskAsIp();
    }
  }

  private IpWildcard(Ip address, long wildcardMask) {
    checkArgument(address.valid(), "Invalid IP address %s", address);
    checkArgument(
        (wildcardMask & ALL_BITS_MASKED) == wildcardMask, "Invalid mask %s", wildcardMask);

    long inputIp = address.asLong();
    long canonicalIp = inputIp & (ALL_BITS_MASKED ^ wildcardMask);
    _ip = (canonicalIp == inputIp) ? address : Ip.create(canonicalIp);
    _wildcardMask = wildcardMask;
  }

  /** Cache after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return CACHE.getUnchecked(this);
  }
}
