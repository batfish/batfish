package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import java.math.BigInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;

@ParametersAreNonnullByDefault
public class Ip6Wildcard implements Serializable, Comparable<Ip6Wildcard> {

  public static final Ip6Wildcard ANY = new Ip6Wildcard(Ip6.ZERO, Ip6.MAX);

  private static Ip6 parseAddress(String str) {
    if (str.contains(";")) {
      String[] parts = str.split(";");
      if (parts.length != 2) {
        throw new BatfishException("Invalid Ip6Wildcard string: '" + str + "'");
      } else {
        return Ip6.parse(parts[0]);
      }
    } else if (str.contains("/")) {
      String[] parts = str.split("/");
      if (parts.length != 2) {
        throw new BatfishException("Invalid Ip6Wildcard string: '" + str + "'");
      } else {
        return Ip6.parse(parts[0]);
      }
    } else {
      return Ip6.parse(str);
    }
  }

  private static Ip6 parseMask(String str) {
    if (str.contains(";")) {
      String[] parts = str.split(";");
      if (parts.length != 2) {
        throw new BatfishException("Invalid Ip6Wildcard string: '" + str + "'");
      } else {
        return Ip6.parse(parts[1]);
      }
    } else if (str.contains("/")) {
      String[] parts = str.split("/");
      if (parts.length != 2) {
        throw new BatfishException("Invalid Ip6Wildcard string: '" + str + "'");
      } else {
        int prefixLength = Integer.parseInt(parts[1]);
        return Ip6.numSubnetBitsToSubnetMask(prefixLength).inverted();
      }
    } else {
      return Ip6.ZERO;
    }
  }

  public Ip6Wildcard(Ip6 ip) {
    this(Prefix6.create(ip, Prefix6.MAX_PREFIX_LENGTH));
  }

  public Ip6Wildcard(Ip6 address, Ip6 wildcardMask) {
    _ip = address;
    _wildcardMask = wildcardMask;
    if (!wildcardMask.valid()) {
      throw new BatfishException("Invalid wildcard: " + wildcardMask);
    }
  }

  public Ip6Wildcard(Prefix6 prefix) {
    this(prefix.getAddress(), prefix.getPrefixWildcard());
  }

  public static Ip6Wildcard parse(String str) {
    return new Ip6Wildcard(parseAddress(str), parseMask(str));
  }

  @JsonCreator
  private static Ip6Wildcard jsonCreator(@Nullable String str) {
    checkArgument(str != null, "Ip6Wildcard cannot be null");
    return parse(str);
  }

  public boolean contains(Ip6 ip) {
    BigInteger wildcardIpAsBigInteger = getIp().asBigInteger();
    BigInteger wildcardMask = getWildcard().asBigInteger();
    BigInteger ipAsBigInteger = ip.asBigInteger();
    BigInteger maskedIpAsBigInteger = ipAsBigInteger.or(wildcardMask);
    BigInteger maskedWildcard = wildcardIpAsBigInteger.or(wildcardMask);
    return maskedIpAsBigInteger.equals(maskedWildcard);
  }

  public @Nonnull Ip6 getIp() {
    return _ip;
  }

  public @Nonnull Ip6 getWildcard() {
    return _wildcardMask;
  }

  public boolean isPrefix() {
    BigInteger w = _wildcardMask.asBigInteger();
    BigInteger wp = w.add(BigInteger.ONE);
    int numTrailingZeros = wp.getLowestSetBit();
    BigInteger check = BigInteger.ONE.shiftLeft(numTrailingZeros);
    return wp.equals(check);
  }

  public Prefix6 toPrefix() {
    if (isPrefix()) {
      return Prefix6.create(_ip, _wildcardMask.inverted().numSubnetBits());
    } else {
      throw new BatfishException(
          "Invalid wildcard format for conversion to prefix: " + _wildcardMask);
    }
  }

  private final @Nonnull Ip6 _ip;
  private final @Nonnull Ip6 _wildcardMask;

  @Override
  public int compareTo(Ip6Wildcard o) {
    if (o == this) {
      return 0;
    }
    int ips = _ip.compareTo(o._ip);
    if (ips != 0) {
      return ips;
    }
    return _wildcardMask.compareTo(o._wildcardMask);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof Ip6Wildcard)) {
      return false;
    }
    Ip6Wildcard other = (Ip6Wildcard) o;
    return _ip.equals(other._ip) && _wildcardMask.equals(other._wildcardMask);
  }

  @Override
  public int hashCode() {
    return 31 * _ip.hashCode() + _wildcardMask.hashCode();
  }

  @JsonValue
  @Override
  public String toString() {
    if (_wildcardMask.equals(Ip6.ZERO)) {
      return _ip.toString();
    } else if (isPrefix()) {
      return toPrefix().toString();
    } else {
      return _ip + ";" + _wildcardMask;
    }
  }
}
