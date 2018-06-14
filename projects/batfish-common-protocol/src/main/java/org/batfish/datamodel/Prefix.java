package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;

public final class Prefix implements Comparable<Prefix>, Serializable {

  public static final int MAX_PREFIX_LENGTH = 32;

  /** */
  private static final long serialVersionUID = 1L;

  public static final Prefix ZERO = new Prefix(Ip.ZERO, 0);

  private static long getNetworkEnd(long networkStart, int prefixLength) {
    return networkStart | ((1L << (32 - prefixLength)) - 1);
  }

  private static long numWildcardBitsToWildcardLong(int numBits) {
    long wildcard = 0;
    for (int i = 0; i < numBits; i++) {
      wildcard |= (1L << i);
    }
    return wildcard;
  }

  @JsonCreator
  public static Prefix parse(String text) {
    String[] parts = text.split("/");
    if (parts.length != 2) {
      throw new BatfishException("Invalid prefix string: \"" + text + "\"");
    }
    Ip ip = new Ip(parts[0]);
    int prefixLength;
    try {
      prefixLength = Integer.parseInt(parts[1]);
    } catch (NumberFormatException e) {
      throw new BatfishException("Invalid prefix length: \"" + parts[1] + "\"", e);
    }
    return new Prefix(ip, prefixLength);
  }

  private Ip _ip;

  private int _prefixLength;

  public Prefix(@Nonnull Ip ip, int prefixLength) {
    if (ip == null) {
      throw new BatfishException("Cannot create prefix with null network");
    }
    if (ip.valid()) {
      // TODO: stop using Ip as a holder for invalid values.
      _ip = ip.getNetworkAddress(prefixLength);
    } else {
      _ip = ip;
    }
    _prefixLength = prefixLength;
  }

  public Prefix(@Nonnull Ip address, @Nonnull Ip mask) {
    this(address, mask.numSubnetBits());
  }

  @Override
  public int compareTo(@Nonnull Prefix rhs) {
    int ret = _ip.compareTo(rhs._ip);
    if (ret != 0) {
      return ret;
    }
    return Integer.compare(_prefixLength, rhs._prefixLength);
  }

  public boolean containsIp(@Nonnull Ip ip) {
    long start = _ip.asLong();
    long end = getNetworkEnd(start, _prefixLength);
    long ipAsLong = ip.asLong();
    return start <= ipAsLong && ipAsLong <= end;
  }

  public boolean containsPrefix(@Nonnull Prefix prefix) {
    return containsIp(prefix._ip) && _prefixLength <= prefix._prefixLength;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof Prefix)) {
      return false;
    }
    Prefix rhs = (Prefix) o;
    return _ip.equals(rhs._ip) && _prefixLength == rhs._prefixLength;
  }

  public Ip getEndIp() {
    return new Ip(getNetworkEnd(_ip.asLong(), _prefixLength));
  }

  public int getPrefixLength() {
    return _prefixLength;
  }

  public Ip getPrefixWildcard() {
    int numWildcardBits = MAX_PREFIX_LENGTH - _prefixLength;
    long wildcardLong = numWildcardBitsToWildcardLong(numWildcardBits);
    return new Ip(wildcardLong);
  }

  public Ip getStartIp() {
    return _ip;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _ip.hashCode();
    result = prime * result + _prefixLength;
    return result;
  }

  public PrefixIpSpace toIpSpace() {
    return new PrefixIpSpace(this);
  }

  @Override
  @JsonValue
  public String toString() {
    return _ip + "/" + _prefixLength;
  }
}
