package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Ordering;
import com.google.common.primitives.UnsignedInts;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An IPv4 Prefix */
@ParametersAreNonnullByDefault
public final class Prefix implements Comparable<Prefix>, Serializable {

  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^20: Just some upper bound on cache size, well less than GiB.
  //   (12 bytes seems smallest possible entry (long + int), would be 12 MiB total).
  private static final LoadingCache<Prefix, Prefix> CACHE =
      Caffeine.newBuilder().softValues().maximumSize(1 << 20).build(x -> x);

  /** Maximum prefix length (number of bits) for a IPv4 address, which is 32 */
  public static final int MAX_PREFIX_LENGTH = 32;

  /** A "0.0.0.0/0" prefix */
  public static final Prefix ZERO = create(Ip.ZERO, 0);

  /** The "127.0.0.0/8" host prefix for loopback addresses */
  public static final Prefix LOOPBACKS = Prefix.parse("127.0.0.0/8");

  /** Multicast IPs are in "244.0.0.0/4". */
  public static final Prefix MULTICAST = create(Ip.parse("224.0.0.0"), 4);

  /**
   * /32s are loopback interfaces -- no hosts are connected.
   *
   * <p>/31s are point-to-point connections between nodes -- again, no hosts.
   *
   * <p>/30s could have hosts, but usually do not. Historically, each subnet was required to reserve
   * two addresses: one identifying the network itself, and a broadcast address. This made /31s
   * invalid, since there were no usable IPs left over. A /30 had 2 usable IPs, so was used for
   * point-to-point connections. Eventually /31s were allowed, but we assume here that any /30s are
   * hold-over point-to-point connections in the legacy model.
   */
  public static final int HOST_SUBNET_MAX_PREFIX_LENGTH = 29;

  private static long wildcardMaskForPrefixLength(int prefixLength) {
    assert 0 <= prefixLength && prefixLength <= Prefix.MAX_PREFIX_LENGTH;
    return (1L << (Prefix.MAX_PREFIX_LENGTH - prefixLength)) - 1;
  }

  /** Parse a {@link Prefix} from a string. */
  @JsonCreator
  public static @Nonnull Prefix parse(@Nullable String text) {
    checkArgument(text != null, "Invalid IPv4 prefix %s", text);
    String[] parts = text.split("/");
    checkArgument(parts.length == 2, "Invalid prefix string: \"%s\"", text);
    Ip ip = Ip.parse(parts[0]);
    int prefixLength;
    try {
      prefixLength = Integer.parseInt(parts[1]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid prefix length: \"" + parts[1] + "\"", e);
    }
    return create(ip, prefixLength);
  }

  /**
   * Return an {@link Optional} {@link Prefix} from a string, or {@link Optional#empty} if the
   * string does not represent a {@link Prefix}.
   */
  public static @Nonnull Optional<Prefix> tryParse(@Nonnull String text) {
    try {
      return Optional.of(parse(text));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
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

  private Prefix(Ip ip, int prefixLength) {
    checkArgument(
        prefixLength >= 0 && prefixLength <= MAX_PREFIX_LENGTH,
        "Invalid prefix length %s",
        prefixLength);
    if (ip.valid()) {
      // TODO: stop using Ip as a holder for invalid values.
      _ip = ip.getNetworkAddress(prefixLength);
    } else {
      _ip = ip;
    }
    _prefixLength = prefixLength;
  }

  public static Prefix create(Ip ip, int prefixLength) {
    Prefix p = new Prefix(ip, prefixLength);
    return CACHE.get(p);
  }

  public static Prefix create(Ip address, Ip mask) {
    return create(address, mask.numSubnetBits());
  }

  /** Return the longest prefix that contains both input prefixes. */
  public static Prefix longestCommonPrefix(Prefix p1, Prefix p2) {
    if (p1.containsPrefix(p2)) {
      return p1;
    }
    if (p2.containsPrefix(p1)) {
      return p2;
    }
    long l1 = p1.getStartIp().asLong();
    long l2 = p2.getStartIp().asLong();
    long oneAtFirstDifferentBit = Long.highestOneBit(l1 ^ l2);
    int lengthInCommon = MAX_PREFIX_LENGTH - 1 - Long.numberOfTrailingZeros(oneAtFirstDifferentBit);
    return Prefix.create(Ordering.natural().min(p1.getStartIp(), p2.getStartIp()), lengthInCommon);
  }

  @Override
  public int compareTo(Prefix rhs) {
    if (this == rhs) {
      return 0;
    }
    int ret = _ip.compareTo(rhs._ip);
    if (ret != 0) {
      return ret;
    }
    return Integer.compare(_prefixLength, rhs._prefixLength);
  }

  public boolean containsIp(Ip ip) {
    long masked = ip.asLong() & ~wildcardMaskForPrefixLength(_prefixLength);
    return masked == _ip.asLong();
  }

  /** Returns {@code true} if {@code this} contains every {@link Ip} in the given {@link Prefix}. */
  public boolean containsPrefix(Prefix prefix) {
    return _prefixLength <= prefix._prefixLength && containsIp(prefix._ip);
  }

  /**
   * Returns {@code true} if {@code this} {@link Prefix} contains the prefix represented by the
   * {@link Ip} and length are in separate parts. The given {@link Ip} does not need to be
   * normalized as the lowest address in the prefix.
   *
   * <p>This is a zero-allocation alternative to calling {@link #containsPrefix(Prefix)} with the
   * result of {@link Prefix#create(Ip, int)}.
   */
  public boolean containsPrefix(Ip ip, int prefixLength) {
    return _prefixLength <= prefixLength && containsIp(ip);
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

  public @Nonnull Ip getEndIp() {
    long networkEnd = _ip.asLong() | wildcardMaskForPrefixLength(_prefixLength);
    return Ip.create(networkEnd);
  }

  public int getPrefixLength() {
    return _prefixLength;
  }

  public @Nonnull Ip getPrefixWildcard() {
    long wildcardLong = wildcardMaskForPrefixLength(_prefixLength);
    return Ip.create(wildcardLong);
  }

  public @Nonnull Ip getStartIp() {
    return _ip;
  }

  @Override
  public int hashCode() {
    // We want a custom quick implementation, so don't call Objects.hash()
    return 31 + 31 * Long.hashCode(_ip.asLong()) + _prefixLength;
  }

  public @Nonnull IpSpace toIpSpace() {
    if (_prefixLength == 0) {
      return UniverseIpSpace.INSTANCE;
    } else if (_prefixLength == MAX_PREFIX_LENGTH) {
      return _ip.toIpSpace();
    }
    return PrefixIpSpace.create(this);
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
    return IpWildcardSetIpSpace.builder()
        .excluding(IpWildcard.create(getStartIp()))
        .excluding(IpWildcard.create(getEndIp()))
        .including(IpWildcard.create(this))
        .build();
  }

  /**
   * Returns the first ip in the prefix that is not a subnet address. When the prefix is /32 or /31,
   * returns the getStartIp(), otherwise, returns the ip after getStartIp().
   */
  public Ip getFirstHostIp() {
    if (_prefixLength >= Prefix.MAX_PREFIX_LENGTH - 1) {
      return getStartIp();
    } else {
      Ip subnetIp = getStartIp();
      return Ip.create(subnetIp.asLong() + 1);
    }
  }

  /**
   * Returns the last ip in the prefix that is not a broadcast address. When the prefix is /32 or
   * /31, returns the getEndIp(), otherwise, returns the ip before getEndIp().
   */
  public Ip getLastHostIp() {
    if (_prefixLength >= Prefix.MAX_PREFIX_LENGTH - 1) {
      return getEndIp();
    } else {
      Ip broadcastIp = getEndIp();
      return Ip.create(broadcastIp.asLong() - 1);
    }
  }

  @Override
  @JsonValue
  public String toString() {
    return _ip + "/" + _prefixLength;
  }

  @Serial
  private Object writeReplace() throws ObjectStreamException {
    return new SerializedForm(_ip, _prefixLength);
  }

  /** Optimize serialized form and re-cache after deserialization. */
  private static class SerializedForm implements Serializable {
    private final int _ip;
    private final byte _length;

    public SerializedForm(Ip ip, int prefixLength) {
      _ip = (int) ip.asLong();
      _length = (byte) prefixLength;
    }

    @Serial
    private Object readResolve() throws ObjectStreamException {
      return CACHE.get(new Prefix(Ip.create(UnsignedInts.toLong(_ip)), _length));
    }
  }
}
