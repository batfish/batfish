package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;

public class Ip implements Comparable<Ip>, Serializable {

  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^20: Just some upper bound on cache size, well less than GiB.
  //   (8 bytes seems smallest possible entry (long), would be 8 MiB total).
  private static final Cache<Long, Ip> CACHE =
      CacheBuilder.newBuilder().softValues().maximumSize(1 << 20).build();

  public static final Ip AUTO = create(-1L);

  public static final Ip FIRST_CLASS_A_PRIVATE_IP = parse("10.0.0.0");

  public static final Ip FIRST_CLASS_B_PRIVATE_IP = parse("172.16.0.0");

  public static final Ip FIRST_CLASS_C_PRIVATE_IP = parse("192.168.0.0");

  public static final Ip FIRST_CLASS_E_EXPERIMENTAL_IP = parse("240.0.0.0");

  public static final Ip FIRST_MULTICAST_IP = parse("224.0.0.0");

  public static final Ip MAX = create(0xFFFFFFFFL);

  private static final long serialVersionUID = 1L;

  public static final Ip ZERO = create(0L);

  /**
   * See {@link #getBitAtPosition(long, int)}. Equivalent to {@code getBitAtPosition(ip.asLong(),
   * position)}
   */
  public static boolean getBitAtPosition(Ip ip, int position) {
    return getBitAtPosition(ip.asLong(), position);
  }

  /**
   * Return the boolean value of a bit at the given position.
   *
   * @param bits the representation of an IP address as a long
   * @param position bit position (0 means most significant, 31 least significant)
   * @return a boolean representation of the bit value
   */
  public static boolean getBitAtPosition(long bits, int position) {
    checkArgument(
        position >= 0 && position < Prefix.MAX_PREFIX_LENGTH, "Invalid bit position %s", position);
    return (bits & (1 << (Prefix.MAX_PREFIX_LENGTH - 1 - position))) != 0;
  }

  private static long ipStrToLong(String addr) {
    String[] addrArray = addr.split("\\.");
    if (addrArray.length != 4) {
      if (addr.startsWith("INVALID_IP") || addr.startsWith("AUTO/NONE")) {
        String[] tail = addr.split("\\(");
        if (tail.length == 2) {
          String[] longStrParts = tail[1].split("l");
          if (longStrParts.length == 2) {
            String longStr = longStrParts[0];
            return Long.parseLong(longStr);
          }
        }
      }
      throw new IllegalArgumentException("Invalid IPv4 address: " + addr);
    }
    long num = 0;
    try {
      for (int i = 0; i < 4; i++) {
        long segment = Long.parseLong(addrArray[i]);
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
   * Return a {@code long} that represents the {@code /numBits} network mask in a IPv4 subnet.
   *
   * <p>For example, for {@code numBits = 8}, should return {@code 0xFF000000L} representing a mask
   * of the top 8 bits.
   */
  // Visible for testing only.
  static long numSubnetBitsToSubnetLong(int numBits) {
    return ~(0xFFFFFFFFL >> numBits) & 0xFFFFFFFFL;
  }

  public static Ip numSubnetBitsToSubnetMask(int numBits) {
    long mask = numSubnetBitsToSubnetLong(numBits);
    return create(mask);
  }

  private final long _ip;

  private Ip(long ipAsLong) {
    _ip = ipAsLong;
  }

  @JsonCreator
  public static Ip parse(String ipAsString) {
    return create(ipStrToLong(ipAsString));
  }

  public static Ip create(long ipAsLong) {
    try {
      return CACHE.get(ipAsLong, () -> new Ip(ipAsLong));
    } catch (ExecutionException e) {
      // This shouldn't happen, but handle anyway.
      return new Ip(ipAsLong);
    }
  }

  public long asLong() {
    return _ip;
  }

  @Override
  public int compareTo(Ip rhs) {
    return Long.compare(_ip, rhs._ip);
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
    long firstOctet = _ip >> 24;
    if (firstOctet <= 127) {
      return create(0xFF000000L);
    } else if (firstOctet <= 191) {
      return create(0XFFFF0000L);
    } else if (firstOctet <= 223) {
      return create(0xFFFFFF00L);
    } else {
      throw new BatfishException("Cannot compute classmask");
    }
  }

  /**
   * Returns the size of an IPv4 network in IPv4 Address Class of this {@link Ip}. Returns {@code
   * -1} if the class does not have a defined subnet size, e.g. the experimental subnet.
   */
  public int getClassNetworkSize() {
    long firstOctet = _ip >> 24;
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
    long mask = numSubnetBitsToSubnetLong(subnetBits);
    return create(_ip & mask);
  }

  public Ip getNetworkAddress(Ip mask) {
    return create(_ip & mask.asLong());
  }

  public Ip getSubnetEnd(Ip mask) {
    return create(_ip | mask.inverted().asLong());
  }

  public Ip getWildcardEndIp(Ip wildcard) {
    return create(_ip | wildcard.asLong());
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_ip);
  }

  public Ip inverted() {
    long invertedLong = (~_ip) & 0xFFFFFFFFL;
    return create(invertedLong);
  }

  public String networkString(int prefixLength) {
    return toString() + "/" + prefixLength;
  }

  public String networkString(Ip mask) {
    return toString() + "/" + mask.numSubnetBits();
  }

  public int numSubnetBits() {
    int numTrailingZeros = Long.numberOfTrailingZeros(_ip);
    if (numTrailingZeros > Prefix.MAX_PREFIX_LENGTH) {
      return 0;
    } else {
      return Prefix.MAX_PREFIX_LENGTH - numTrailingZeros;
    }
  }

  public IpIpSpace toIpSpace() {
    return new IpIpSpace(this);
  }

  @Override
  @JsonValue
  public String toString() {
    if (!valid()) {
      if (_ip == -1L) {
        return "AUTO/NONE(-1l)";
      } else {
        return "INVALID_IP(" + _ip + "l)";
      }
    } else {
      return ((_ip >> 24) & 0xFF)
          + "."
          + ((_ip >> 16) & 0xFF)
          + "."
          + ((_ip >> 8) & 0xFF)
          + "."
          + (_ip & 0xFF);
    }
  }

  public boolean valid() {
    return 0L <= _ip && _ip <= 0xFFFFFFFFL;
  }
}
