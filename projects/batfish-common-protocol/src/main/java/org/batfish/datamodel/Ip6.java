package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.net.InetAddresses;
import java.io.Serializable;
import java.math.BigInteger;
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

  private static String asIpv6AddressString(BigInteger ipv6AddressAsBigInteger) {
    // Cut the address into the 8 /16s that are used for strings.
    int[] segments = new int[8];
    BigInteger remainder = ipv6AddressAsBigInteger;
    for (int i = 0; i < 8; i++) {
      segments[7 - i] = remainder.shortValue() & 0xFFFF;
      remainder = remainder.shiftRight(16);
    }

    /* **********************************************************************************
     * ZERO compression: The left-most, longest string of 2+ consecutive zeros should be
     * compressed to :: from 0:...:0
     * **********************************************************************************/
    // Find the longest sequence of consecutive zeros
    int longestStart = -1;
    int longestLength = 0;
    int currentStart = -1;
    int currentLength = 0;

    for (int i = 0; i < segments.length; i++) {
      if (segments[i] == 0) {
        if (currentStart == -1) {
          currentStart = i;
        }
        currentLength++;
        if (currentLength >= 2 && currentLength > longestLength) {
          // Only collapse at least 2 zeros in a row in ::
          longestStart = currentStart;
          longestLength = currentLength;
        }
      } else {
        currentStart = -1;
        currentLength = 0;
      }
    }

    /* **********************************************************
     * Compute the final String by skipping zeros as appropriate.
     * **********************************************************/
    StringBuilder sb = new StringBuilder();
    if (longestStart == 0) {
      // Edge case: need a preceding ':' since no prior segment put one in.
      sb.append(':');
    }
    // Core: put in <Segment><:>, skipping last segment's :
    for (int i = 0; i < segments.length; i++) {
      if (i >= longestStart && i < longestStart + longestLength) {
        // Skip this zero, putting in a single : which will go with the preceding one to make ::
        assert segments[i] == 0;
        if (i == longestStart) {
          sb.append(':');
        }
      } else {
        sb.append(Integer.toHexString(segments[i]));
        if (i != 7) {
          sb.append(':');
        }
      }
    }

    return sb.toString();
  }

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
    byte[] ip6AsByteArray = InetAddresses.forString(ipAsString).getAddress();
    return create(new BigInteger(ip6AsByteArray));
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

  @Override
  @JsonValue
  public String toString() {
    return asIpv6AddressString(_ip6);
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
