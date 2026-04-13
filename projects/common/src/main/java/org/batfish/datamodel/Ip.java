package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** An IPv4 address */
public class Ip implements Comparable<Ip>, Serializable {

  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^20: Just some upper bound on cache size, well less than GiB.
  //   (4 bytes seems smallest possible entry (int), would be 4 MiB total).
  private static final LoadingCache<Ip, Ip> CACHE =
      CacheBuilder.newBuilder().softValues().maximumSize(1 << 20).build(CacheLoader.from(x -> x));

  public static final Ip FIRST_CLASS_A_PRIVATE_IP = parse("10.0.0.0");

  public static final Ip FIRST_CLASS_B_PRIVATE_IP = parse("172.16.0.0");

  public static final Ip FIRST_CLASS_C_PRIVATE_IP = parse("192.168.0.0");

  public static final Ip FIRST_CLASS_E_EXPERIMENTAL_IP = parse("240.0.0.0");

  public static final Ip FIRST_MULTICAST_IP = parse("224.0.0.0");

  public static final Ip MAX = create(0xFFFFFFFFL);

  public static final Ip ZERO = create(0L);

  /**
   * Return the boolean value of a bit at the given position.
   *
   * @param bits the representation of an IP address as an int (unsigned 32-bit value)
   * @param position bit position (0 means most significant, 31 least significant)
   */
  public static boolean getBitAtPosition(int bits, int position) {
    checkArgument(
        position >= 0 && position < Prefix.MAX_PREFIX_LENGTH, "Invalid bit position %s", position);
    return (bits & (1 << (Prefix.MAX_PREFIX_LENGTH - 1 - position))) != 0;
  }

  /**
   * Return the boolean value of a bit at the given position.
   *
   * @param bits the representation of an IP address as a long (unsigned 32-bit value)
   * @param position bit position (0 means most significant, 31 least significant)
   */
  public static boolean getBitAtPosition(long bits, int position) {
    return getBitAtPosition((int) bits, position);
  }

  /** Return the boolean value of the bit at the given position (0 = most significant). */
  public boolean getBitAtPosition(int position) {
    return getBitAtPosition(_ip, position);
  }

  private static int ipStrToInt(String addr) {
    String[] addrArray = addr.split("\\.");
    if (addrArray.length != 4) {
      if (addr.startsWith("INVALID_IP") || addr.startsWith("AUTO/NONE")) {
        String[] tail = addr.split("\\(");
        if (tail.length == 2) {
          String[] longStrParts = tail[1].split("l");
          if (longStrParts.length == 2) {
            String longStr = longStrParts[0];
            return (int) Long.parseLong(longStr);
          }
        }
      }
      throw new IllegalArgumentException("Invalid IPv4 address: " + addr);
    }
    int num = 0;
    try {
      for (int i = 0; i < 4; i++) {
        int segment = Integer.parseInt(addrArray[i]);
        checkArgument(
            0 <= segment && segment <= 255,
            "Invalid IPv4 address: %s. %s is an invalid octet",
            addr,
            addrArray[i]);
        num = (num << 8) + segment;
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid IPv4 address: " + addr, e);
    }
    return num;
  }

  /**
   * Return an {@code int} that represents the {@code /numBits} network mask in a IPv4 subnet.
   *
   * <p>For example, for {@code numBits = 8}, should return {@code 0xFF000000} representing a mask
   * of the top 8 bits.
   */
  // Visible for testing only.
  static int numSubnetBitsToSubnetInt(int numBits) {
    // Shifts are mod 32 in Java, so -1 >>> 32 is a no-op. Handle 32 explicitly.
    return numBits == Prefix.MAX_PREFIX_LENGTH ? -1 : ~(-1 >>> numBits);
  }

  public static Ip numSubnetBitsToSubnetMask(int numBits) {
    return create(numSubnetBitsToSubnetInt(numBits));
  }

  /**
   * Return an {@link Optional} {@link Ip} from a string, or {@link Optional#empty} if the string
   * does not represent an {@link Ip}.
   */
  public static @Nonnull Optional<Ip> tryParse(@Nonnull String text) {
    try {
      return Optional.of(parse(text));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  private final int _ip;

  private Ip(int ipAsInt) {
    _ip = ipAsInt;
  }

  @JsonCreator
  public static Ip parse(String ipAsString) {
    return create(ipStrToInt(ipAsString));
  }

  /** Create an {@link Ip} from the unsigned 32-bit representation stored in a {@code long}. */
  public static Ip create(long ipAsLong) {
    checkArgument(ipAsLong >= 0L && ipAsLong <= 0xFFFFFFFFL, "Invalid IP value: %s", ipAsLong);
    return create((int) ipAsLong);
  }

  /** Create an {@link Ip} from the unsigned 32-bit representation stored in an {@code int}. */
  public static Ip create(int ipAsInt) {
    Ip ip = new Ip(ipAsInt);
    return CACHE.getUnchecked(ip);
  }

  public long asLong() {
    return Integer.toUnsignedLong(_ip);
  }

  @Override
  public int compareTo(Ip rhs) {
    return Integer.compareUnsigned(_ip, rhs._ip);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof Ip)) {
      return false;
    }
    Ip rhs = (Ip) o;
    return _ip == rhs._ip;
  }

  public Ip getClassMask() {
    // Use unsigned right shift to get first octet as unsigned value.
    int firstOctet = _ip >>> 24;
    if (firstOctet <= 127) {
      return create(0xFF000000);
    } else if (firstOctet <= 191) {
      return create(0xFFFF0000);
    } else if (firstOctet <= 223) {
      return create(0xFFFFFF00);
    } else {
      throw new IllegalArgumentException("Cannot compute classmask");
    }
  }

  /**
   * Returns the size of an IPv4 network in IPv4 Address Class of this {@link Ip}. Returns {@code
   * -1} if the class does not have a defined subnet size, e.g. the experimental subnet.
   */
  public int getClassNetworkSize() {
    int firstOctet = _ip >>> 24;
    if (firstOctet <= 127) {
      return 8;
    } else if (firstOctet <= 191) {
      return 16;
    } else if (firstOctet <= 223) {
      return 24;
    } else {
      return -1;
    }
  }

  public Ip getNetworkAddress(int subnetBits) {
    int masked = _ip & numSubnetBitsToSubnetInt(subnetBits);
    if (masked == _ip) {
      return this;
    }
    return create(masked);
  }

  @Override
  public int hashCode() {
    return _ip;
  }

  public Ip inverted() {
    return create(~_ip);
  }

  public int numSubnetBits() {
    return Prefix.MAX_PREFIX_LENGTH - Integer.numberOfTrailingZeros(_ip);
  }

  /**
   * Whether this IP is a valid 1s-leading netmask, e.g. 255.0.0.0. IPs 0.0.0.0 and 255.255.255.255
   * are considered valid 1s-leading netmasks.
   */
  public boolean isValidNetmask1sLeading() {
    return (_ip | (_ip - 1)) == -1;
  }

  public @Nonnull IpIpSpace toIpSpace() {
    return IpIpSpace.create(this);
  }

  @Override
  @JsonValue
  public String toString() {
    return ((_ip >>> 24) & 0xFF)
        + "."
        + ((_ip >>> 16) & 0xFF)
        + "."
        + ((_ip >>> 8) & 0xFF)
        + "."
        + (_ip & 0xFF);
  }

  public Prefix toPrefix() {
    return Prefix.create(this, Prefix.MAX_PREFIX_LENGTH);
  }

  /** Cache after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return CACHE.getUnchecked(this);
  }
}
