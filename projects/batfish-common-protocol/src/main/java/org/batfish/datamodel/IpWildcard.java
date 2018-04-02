package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.batfish.common.BatfishException;
import org.batfish.common.Pair;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class IpWildcard extends Pair<Ip, Ip> implements IpSpace {

  public static final IpWildcard ANY = new IpWildcard(Ip.ZERO, Ip.MAX);

  /** */
  private static final long serialVersionUID = 1L;

  private static Ip parseAddress(String str) {
    if (str.contains(":")) {
      String[] parts = str.split(":");
      if (parts.length != 2) {
        throw new BatfishException("Invalid IpWildcard string: '" + str + "'");
      } else {
        return new Ip(parts[0]);
      }
    } else if (str.contains("/")) {
      String[] parts = str.split("/");
      if (parts.length != 2) {
        throw new BatfishException("Invalid IpWildcard string: '" + str + "'");
      } else {
        return new Ip(parts[0]);
      }
    } else {
      return new Ip(str);
    }
  }

  private static Ip parseMask(String str) {
    if (str.contains(":")) {
      String[] parts = str.split(":");
      if (parts.length != 2) {
        throw new BatfishException("Invalid IpWildcard string: '" + str + "'");
      } else {
        return new Ip(parts[1]);
      }
    } else if (str.contains("/")) {
      String[] parts = str.split("/");
      if (parts.length != 2) {
        throw new BatfishException("Invalid IpWildcard string: '" + str + "'");
      } else {
        int prefixLength = Integer.parseInt(parts[1]);
        return Ip.numSubnetBitsToSubnetMask(prefixLength).inverted();
      }
    } else {
      return Ip.ZERO;
    }
  }

  public IpWildcard(Ip ip) {
    this(new Prefix(ip, Prefix.MAX_PREFIX_LENGTH));
  }

  public IpWildcard(Ip address, Ip wildcardMask) {
    super(address, wildcardMask);
    if (!wildcardMask.valid()) {
      throw new BatfishException("Invalid wildcard: " + wildcardMask);
    }
  }

  public IpWildcard(Prefix prefix) {
    this(prefix.getStartIp(), prefix.getPrefixWildcard());
  }

  @JsonCreator
  public IpWildcard(String str) {
    super(parseAddress(str), parseMask(str));
  }

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> visitor) {
    return visitor.visitIpWildcard(this);
  }

  @Override
  public boolean containsIp(Ip ip) {
    long wildcardIpAsLong = getIp().asLong();
    long wildcardMask = getWildcard().asLong();
    long ipAsLong = ip.asLong();
    long maskedIpAsLong = ipAsLong | wildcardMask;
    long maskedWildcard = wildcardIpAsLong | wildcardMask;
    return maskedIpAsLong == maskedWildcard;
  }

  @Override
  public IpSpace complement() {
    return AclIpSpace.builder()
        .thenRejecting(this)
        .thenPermitting(UniverseIpSpace.INSTANCE)
        .build();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof IpWildcard)) {
      return false;
    }
    IpWildcard other = (IpWildcard) o;
    if (other.getFirst().equals(this.getFirst()) && other.getSecond().equals(this.getSecond())) {
      return true;
    } else {
      return false;
    }
  }

  public Ip getIp() {
    return _first;
  }

  public Ip getWildcard() {
    return _second;
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
    // mark which bits are wild
    long wildToThis = getWildcard().asLong();
    long wildToOther = other.getWildcard().asLong();

    // 1. Any bits wild in other are wild in this
    if ((wildToThis | wildToOther) != wildToThis) {
      // other has a wild bit that this doesn't
      return false;
    }

    // 2. Any IP bits that differ are wild in this
    long thisIp = getIp().asLong();
    long otherIp = other.getIp().asLong();
    long bitsThatDiffer = thisIp ^ otherIp;

    // mark which bits are significant (non-wild).
    long significantToThis = getWildcard().inverted().asLong();

    return (significantToThis & bitsThatDiffer) == 0;
  }

  /**
   * @param other another IpWildcard
   * @return whether the set of IPs matched by this intersects the set of those matched by other.
   */
  public boolean intersects(IpWildcard other) {
    long thisIp = getIp().asLong();
    long otherIp = other.getIp().asLong();

    // mark which bits are significant (non-wild)
    long significantToThis = getWildcard().inverted().asLong();
    long significantToOther = other.getWildcard().inverted().asLong();

    long differentBits = thisIp ^ otherIp;

    // this and other must agree at any position that is significant to both
    long bitsThatMustAgree = significantToThis & significantToOther;

    return (differentBits & bitsThatMustAgree) == 0;
  }

  public boolean isPrefix() {
    long w = _second.asLong();
    long wp = w + 1L;
    int numTrailingZeros = Long.numberOfTrailingZeros(wp);
    long check = 1L << numTrailingZeros;
    return wp == check;
  }

  public Prefix toPrefix() {
    if (isPrefix()) {
      return new Prefix(_first, _second.inverted());
    } else {
      throw new BatfishException("Invalid wildcard format for conversion to prefix: " + _second);
    }
  }

  @JsonValue
  @Override
  public String toString() {
    if (_second.equals(Ip.ZERO)) {
      return _first.toString();
    } else if (isPrefix()) {
      return toPrefix().toString();
    } else {
      return _first + ":" + _second;
    }
  }
}
