package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;

public class Prefix6 implements Comparable<Prefix6>, Serializable {

  public static final int MAX_PREFIX_LENGTH = 128;

  public static final Prefix6 ZERO = new Prefix6(Ip6.ZERO, 0);

  private static BigInteger getNetworkEnd(BigInteger networkStart, int prefixLength) {
    BigInteger networkEnd = networkStart;
    int onesLength = MAX_PREFIX_LENGTH - prefixLength;
    for (int i = 0; i < onesLength; i++) {
      networkEnd = networkEnd.or(BigInteger.ONE.shiftLeft(i));
    }
    return networkEnd;
  }

  private static BigInteger numWildcardBitsToWildcardBigInteger(int numBits) {
    BigInteger wildcard = BigInteger.ZERO;
    for (int i = 0; i < numBits; i++) {
      wildcard = wildcard.or(BigInteger.ONE.shiftLeft(i));
    }
    return wildcard;
  }

  private Ip6 _address;

  private int _prefixLength;

  public Prefix6(Ip6 network, int prefixLength) {
    _address = network;
    _prefixLength = prefixLength;
  }

  public Prefix6(Ip6 address, Ip6 mask) {
    if (address == null) {
      throw new BatfishException("Cannot create prefix6 with null network");
    }
    if (mask == null) {
      throw new BatfishException("Cannot create prefix6 with null mask");
    }
    _address = address;
    _prefixLength = mask.numSubnetBits();
  }

  @JsonCreator
  public static @Nonnull Prefix6 parse(String text) {
    String[] parts = text.split("/");
    if (parts.length != 2) {
      throw new BatfishException("Invalid Prefix6 string: \"" + text + "\"");
    }
    Ip6 address6 = Ip6.parse(parts[0]);
    try {
      int prefixLength = Integer.parseInt(parts[1]);
      return new Prefix6(address6, prefixLength);
    } catch (NumberFormatException e) {
      throw new BatfishException("Invalid Prefix6 length: \"" + parts[1] + "\"", e);
    }
  }

  /**
   * Return an {@link Optional} {@link Prefix6} from a string, or {@link Optional#empty} if the
   * string does not represent a {@link Prefix6}.
   */
  @Nonnull
  public static Optional<Prefix6> tryParse(@Nonnull String text) {
    try {
      return Optional.of(parse(text));
    } catch (BatfishException e) {
      return Optional.empty();
    }
  }

  @Override
  public int compareTo(Prefix6 rhs) {
    int ret = _address.compareTo(rhs._address);
    if (ret != 0) {
      return ret;
    }
    return Integer.compare(_prefixLength, rhs._prefixLength);
  }

  public boolean contains(Ip6 ip6) {
    BigInteger start = getNetworkAddress().asBigInteger();
    BigInteger end = getEndAddress().asBigInteger();
    BigInteger ipAsLong = ip6.asBigInteger();
    return start.compareTo(ipAsLong) <= 0 && ipAsLong.compareTo(end) <= 0;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof Prefix6)) {
      return false;
    }
    Prefix6 rhs = (Prefix6) o;
    return _address.equals(rhs._address) && _prefixLength == rhs._prefixLength;
  }

  public Ip6 getAddress() {
    return _address;
  }

  public Ip6 getEndAddress() {
    return new Ip6(getNetworkEnd(_address.asBigInteger(), _prefixLength));
  }

  public Ip6 getNetworkAddress() {
    return _address.getNetworkAddress(_prefixLength);
  }

  public Prefix6 getNetworkPrefix() {
    return new Prefix6(getNetworkAddress(), _prefixLength);
  }

  public int getPrefixLength() {
    return _prefixLength;
  }

  public Ip6 getPrefixWildcard() {
    int numWildcardBits = MAX_PREFIX_LENGTH - _prefixLength;
    BigInteger wildcardBigInteger = numWildcardBitsToWildcardBigInteger(numWildcardBits);
    return new Ip6(wildcardBigInteger);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _address.hashCode();
    result = prime * result + _prefixLength;
    return result;
  }

  @Override
  @JsonValue
  public String toString() {
    return _address + "/" + _prefixLength;
  }
}
