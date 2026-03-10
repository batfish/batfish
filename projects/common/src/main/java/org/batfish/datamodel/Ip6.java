package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.net.InetAddresses.fromIPv4BigInteger;
import static com.google.common.net.InetAddresses.fromIPv6BigInteger;
import static com.google.common.net.InetAddresses.getCompatIPv4Address;
import static com.google.common.net.InetAddresses.isCompatIPv4Address;
import static com.google.common.net.InetAddresses.isMappedIPv4Address;
import static com.google.common.net.InetAddresses.toAddrString;
import static com.google.common.net.InetAddresses.toBigInteger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.net.InetAddresses;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Ip6 implements Comparable<Ip6>, Serializable {

  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^20: Just some upper bound on cache size, well less than GiB.
  private static final LoadingCache<BigInteger, Ip6> CACHE =
      Caffeine.newBuilder().softValues().maximumSize(1 << 20).build(Ip6::new);

  public static @Nonnull Ip6 create(BigInteger value) {
    return CACHE.get(value);
  }

  public static final Ip6 MAX = create(new BigInteger("+FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16));

  public static final Ip6 ZERO = create(BigInteger.ZERO);

  private static final BigInteger MAPPED_IPV4_OFFSET = BigInteger.valueOf(0xffff).shiftLeft(32);

  private static BigInteger numSubnetBitsToSubnetBigInteger(int numBits) {
    BigInteger val = BigInteger.ZERO;
    for (int i = Prefix6.MAX_PREFIX_LENGTH - 1; i > Prefix6.MAX_PREFIX_LENGTH - 1 - numBits; i--) {
      val = val.or(BigInteger.ONE.shiftLeft(i));
    }
    return val;
  }

  public static Ip6 numSubnetBitsToSubnetMask(int numBits) {
    BigInteger mask = numSubnetBitsToSubnetBigInteger(numBits);
    return create(mask);
  }

  /**
   * Return an {@link Optional} {@link Ip6} from a string, or {@link Optional#empty} if the string
   * does not represent an {@link Ip6}.
   */
  public static @Nonnull Optional<Ip6> tryParse(@Nonnull String text) {
    try {
      return Optional.of(parse(text));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public static @Nonnull Ip6 parse(@Nonnull String ipAsString) {
    checkArgument(ipAsString.contains(":"), "Invalid IPv6 address literal '%s'", ipAsString);
    // Note we need to special case mapped addresses because Java's InetAddress loses this info and
    // only keeps the v4 bits
    // https://guava.dev/releases/snapshot-jre/api/docs/com/google/common/net/InetAddresses.html#isMappedIPv4Address(java.lang.String)
    // explains it in more detail
    if (isMappedIPv4Address(ipAsString)) {
      return create(MAPPED_IPV4_OFFSET.or(toBigInteger(InetAddresses.forString(ipAsString))));
    }
    return create(toBigInteger(InetAddresses.forString(ipAsString)));
  }

  public BigInteger asBigInteger() {
    return _ip6;
  }

  @Override
  public int compareTo(Ip6 rhs) {
    return _ip6.compareTo(rhs._ip6);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof Ip6)) {
      return false;
    }
    Ip6 rhs = (Ip6) o;
    return _ip6.equals(rhs._ip6);
  }

  public Ip6 getNetworkAddress(int subnetBits) {
    BigInteger mask = numSubnetBitsToSubnetBigInteger(subnetBits);
    return create(_ip6.and(mask));
  }

  @Override
  public int hashCode() {
    return _ip6.hashCode();
  }

  public Ip6 inverted() {
    BigInteger mask = new BigInteger("+FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
    BigInteger invertedBigInteger = mask.andNot(_ip6);
    return create(invertedBigInteger);
  }

  public int numSubnetBits() {
    int numTrailingZeros = _ip6.getLowestSetBit();
    if (numTrailingZeros == -1) {
      return 0;
    } else {
      return Prefix6.MAX_PREFIX_LENGTH - numTrailingZeros;
    }
  }

  /** Returns true if this is an IPv4-mapped IPv6 address */
  private boolean isMappedIpV4() {
    // keep 96 of most significant bits and check that we get 0xffff
    return new BigInteger("+FFFFFFFFFFFFFFFFFFFFFFFF", 16)
        .shiftLeft(32)
        .and(_ip6)
        .equals(MAPPED_IPV4_OFFSET);
  }

  @Override
  @JsonValue
  public String toString() {
    if (isMappedIpV4()) {
      // leave only lowest 32 bits
      Inet4Address inet4 = fromIPv4BigInteger(_ip6.and(new BigInteger("+FFFFFFFF", 16)));
      return "::ffff:" + toAddrString(inet4);
    } else {
      Inet6Address inetAddr = fromIPv6BigInteger(_ip6);
      if (isCompatIPv4Address(inetAddr)) {
        return "::" + toAddrString(getCompatIPv4Address(inetAddr));
      } else {
        // Fall back to regular hex representation
        return toAddrString(inetAddr);
      }
    }
  }

  public Prefix6 toPrefix6() {
    return Prefix6.create(this, Prefix6.MAX_PREFIX_LENGTH);
  }

  public boolean valid() {
    return _ip6.compareTo(BigInteger.ZERO) >= 0
        && _ip6.compareTo(new BigInteger("+FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16)) <= 0;
  }

  @JsonCreator
  private static @Nonnull Ip6 jsonCreator(@Nullable String ipAsString) {
    if (ipAsString == null) {
      throw new IllegalArgumentException("Missing value for IPv6 address");
    }
    return parse(ipAsString);
  }

  private final BigInteger _ip6;

  /** Private constructor, use {@link #create} instead */
  private Ip6(BigInteger ip6AsBigInteger) {
    _ip6 = ip6AsBigInteger;
  }
}
