package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import java.util.Comparator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An IP wildcard consisting of a IP address and a wildcard (also expressed as an IP address) */
@ParametersAreNonnullByDefault
public final class IpWildcard implements Serializable, Comparable<IpWildcard> {

  @Nonnull private final Ip _ip;
  @Nonnull private final Ip _mask;

  public static final IpWildcard ANY = new IpWildcard(Ip.ZERO, Ip.MAX);

  private static final long serialVersionUID = 1L;

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

  public IpWildcard(Ip ip) {
    this(Prefix.create(ip, Prefix.MAX_PREFIX_LENGTH));
  }

  public IpWildcard(Ip address, Ip wildcardMask) {
    // Canonicalize the address before passing it to parent, so that #equals works.
    _ip = Ip.create(address.asLong() & ~wildcardMask.asLong());
    _mask = wildcardMask;
    checkArgument(wildcardMask.valid(), "Invalid wildcard: %s", wildcardMask);
  }

  public IpWildcard(Prefix prefix) {
    this(prefix.getStartIp(), prefix.getPrefixWildcard());
  }

  @JsonCreator
  public IpWildcard(String str) {
    this(parseAddress(str), parseMask(str));
  }

  public boolean containsIp(Ip ip) {
    long wildcardIpAsLong = getIp().asLong();
    long wildcardMask = getWildcard().asLong();
    long ipAsLong = ip.asLong();
    long maskedIpAsLong = ipAsLong | wildcardMask;
    long maskedWildcard = wildcardIpAsLong | wildcardMask;
    return maskedIpAsLong == maskedWildcard;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof IpWildcard)) {
      return false;
    }
    IpWildcard other = (IpWildcard) o;
    return this._ip.equals(other._ip) && this._mask.equals(other._mask);
  }

  @Override
  public int hashCode() {
    return 31 * _ip.hashCode() + _mask.hashCode();
  }

  @Override
  public int compareTo(IpWildcard o) {
    return Comparator.comparing(IpWildcard::getIp)
        .thenComparing(IpWildcard::getWildcard)
        .compare(this, o);
  }

  @Nonnull
  public Ip getIp() {
    return _ip;
  }

  @Nonnull
  public Ip getWildcard() {
    return _mask;
  }

  /**
   * @param other another IpWildcard
   * @return whether the set of IPs matched by this intersects the set of those matched by other.
   */
  public boolean intersects(IpWildcard other) {
    long differentIpBits = getIp().asLong() ^ other.getIp().asLong();
    long canDiffer = getWildcard().asLong() | other.getWildcard().asLong();

    /*
     * All IP bits that different must be wild in either this or other.
     */
    return (differentIpBits & canDiffer) == differentIpBits;
  }

  public boolean isPrefix() {
    long w = _mask.asLong();
    long wp = w + 1L;
    int numTrailingZeros = Long.numberOfTrailingZeros(wp);
    long check = 1L << numTrailingZeros;
    return wp == check;
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
    long wildToThis = getWildcard().asLong();
    long wildToOther = other.getWildcard().asLong();
    long differentIpBits = getIp().asLong() ^ other.getIp().asLong();

    /*
     * 1. Any bits wild in other must be wild in this (i.e. other's wild bits must be a subset
     *    of this' wild bits).
     * 2. Any IP bits that differ must be wild in this.
     */
    return (wildToThis & wildToOther) == wildToOther
        && (wildToThis & differentIpBits) == differentIpBits;
  }

  @Nonnull
  public IpWildcardIpSpace toIpSpace() {
    return new IpWildcardIpSpace(this);
  }

  public Prefix toPrefix() {
    checkState(isPrefix(), "Invalid wildcard format for conversion to prefix: %s", _mask);
    return Prefix.create(_ip, _mask.inverted());
  }

  @JsonValue
  @Override
  public String toString() {
    if (_mask.equals(Ip.ZERO)) {
      return _ip.toString();
    } else if (isPrefix()) {
      return toPrefix().toString();
    } else {
      return _ip + ":" + _mask;
    }
  }
}
