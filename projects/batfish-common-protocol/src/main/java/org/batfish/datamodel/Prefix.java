package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;

/** An IPv4 Prefix */
@ParametersAreNonnullByDefault
public final class Prefix implements Comparable<Prefix>, Serializable {

  /** Maximum prefix length (number of bits) for a IPv4 address, which is 32 */
  public static final int MAX_PREFIX_LENGTH = 32;

  private static final long serialVersionUID = 1L;

  /** A "0.0.0.0/0" prefix */
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

  /** Parse a {@link Prefix} from a string. */
  @Nonnull
  @JsonCreator
  public static Prefix parse(@Nullable String text) {
    checkArgument(text != null, "Invalid IPv4 prefix %s", text);
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

  /**
   * Parse a {@link Prefix} from a string.
   *
   * @throws IllegalArgumentException if the string does not represent a prefix in canonical form.
   */
  public static @Nonnull Prefix strict(String prefixStr) {
    Prefix prefix = parse(prefixStr);
    checkArgument(prefix.toString().equals(prefixStr), "Non-canonical prefix: %s", prefixStr);
    return prefix;
  }

  private final Ip _ip;

  private final int _prefixLength;

  public Prefix(Ip ip, int prefixLength) {
    if (ip.valid()) {
      // TODO: stop using Ip as a holder for invalid values.
      _ip = ip.getNetworkAddress(prefixLength);
    } else {
      _ip = ip;
    }
    _prefixLength = prefixLength;
  }

  public Prefix(Ip address, Ip mask) {
    this(address, mask.numSubnetBits());
  }

  @Override
  public int compareTo(Prefix rhs) {
    int ret = _ip.compareTo(rhs._ip);
    if (ret != 0) {
      return ret;
    }
    return Integer.compare(_prefixLength, rhs._prefixLength);
  }

  public boolean containsIp(Ip ip) {
    long start = _ip.asLong();
    long end = getNetworkEnd(start, _prefixLength);
    long ipAsLong = ip.asLong();
    return start <= ipAsLong && ipAsLong <= end;
  }

  public boolean containsPrefix(Prefix prefix) {
    return containsIp(prefix._ip) && _prefixLength <= prefix._prefixLength;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof Prefix)) {
      return false;
    }
    Prefix rhs = (Prefix) o;
    return _ip.equals(rhs._ip) && _prefixLength == rhs._prefixLength;
  }

  @Nonnull
  public Ip getEndIp() {
    return new Ip(getNetworkEnd(_ip.asLong(), _prefixLength));
  }

  public int getPrefixLength() {
    return _prefixLength;
  }

  @Nonnull
  public Ip getPrefixWildcard() {
    int numWildcardBits = MAX_PREFIX_LENGTH - _prefixLength;
    long wildcardLong = numWildcardBitsToWildcardLong(numWildcardBits);
    return new Ip(wildcardLong);
  }

  @Nonnull
  public Ip getStartIp() {
    return _ip;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ip, _prefixLength);
  }

  public PrefixIpSpace toIpSpace() {
    return new PrefixIpSpace(this);
  }

  /**
   * Returns an {@link IpSpace} that contains this prefix except for the network and broadcast
   * addresses, if applicable. Following RFC 3021, the entire {@code /31} is treated as host IP
   * space. A prefix of length {@code /32} is preserved, though may be a degenerate case.
   */
  public IpSpace toHostIpSpace() {
    if (_prefixLength >= Prefix.MAX_PREFIX_LENGTH - 1) {
      return toIpSpace();
    }
    return AclIpSpace.builder()
        .thenRejecting(getStartIp().toIpSpace())
        .thenRejecting(getEndIp().toIpSpace())
        .thenPermitting(toIpSpace())
        .build();
  }

  @Override
  @JsonValue
  public String toString() {
    return _ip + "/" + _prefixLength;
  }
}
