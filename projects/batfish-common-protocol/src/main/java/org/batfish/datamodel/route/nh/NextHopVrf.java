package org.batfish.datamodel.route.nh;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/**
 * Indicates that destinations matching this route must be <em>re-resolved</em> in a different VRF.
 *
 * <p>The optional IP specifies which IP to look up in the target VRF's FIB. If null, the packet's
 * destination IP is used.
 */
public final class NextHopVrf implements NextHop {

  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^20: Just some upper bound on cache size, well less than GiB.
  //   (8 bytes seems smallest possible entry (long), would be 8 MiB total).
  private static final LoadingCache<NextHopVrf, NextHopVrf> CACHE =
      CacheBuilder.newBuilder().softValues().maximumSize(1 << 20).build(CacheLoader.from(x -> x));

  /**
   * Create new next hop, pointing to a given VRF name
   *
   * @param vrfName name of the VRF in which to resolve the destination
   */
  public static NextHopVrf of(String vrfName) {
    return CACHE.getUnchecked(new NextHopVrf(vrfName, null));
  }

  /**
   * Create new next hop, pointing to a given VRF name with a specific IP to resolve
   *
   * @param vrfName name of the VRF in which to resolve the destination
   * @param ip the IP to look up in the target VRF's FIB (if null, packet destination is used)
   * @throws IllegalArgumentException if the IP is invalid, e.g., {@link Ip#AUTO}
   */
  public static NextHopVrf of(String vrfName, @Nullable Ip ip) {
    return CACHE.getUnchecked(new NextHopVrf(vrfName, ip));
  }

  /** VRF in which to resolve the destination */
  @JsonProperty(PROP_VRF)
  public @Nonnull String getVrfName() {
    return _vrfName;
  }

  /** Optional IP to look up in the target VRF's FIB (if null, packet destination is used) */
  @JsonProperty(PROP_IP)
  public @Nullable Ip getIp() {
    return _ip;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NextHopVrf)) {
      return false;
    }
    NextHopVrf that = (NextHopVrf) o;
    return _vrfName.equals(that._vrfName) && Objects.equals(_ip, that._ip);
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      // This hashcode could be hot, so compute manually instead of Objects.hash(...)
      h = _vrfName.hashCode() + (_ip == null ? 0 : 31 * _ip.hashCode());
      _hashCode = h;
    }
    return h;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(NextHopVrf.class)
        .omitNullValues()
        .add("vrfName", _vrfName)
        .add("ip", _ip)
        .toString();
  }

  @Override
  public <T> T accept(NextHopVisitor<T> visitor) {
    return visitor.visitNextHopVrf(this);
  }

  private static final String PROP_VRF = "vrf";
  private static final String PROP_IP = "ip";

  private final @Nonnull String _vrfName;
  private final @Nullable Ip _ip;
  private transient int _hashCode;

  @JsonCreator
  private static @Nonnull NextHopVrf create(
      @JsonProperty(PROP_VRF) @Nullable String vrfName, @JsonProperty(PROP_IP) @Nullable Ip ip) {
    checkArgument(vrfName != null, "Missing %s", PROP_VRF);
    return of(vrfName, ip);
  }

  private NextHopVrf(String vrfName, @Nullable Ip ip) {
    checkArgument(
        ip == null || (!ip.equals(Ip.AUTO) && !ip.equals(Ip.ZERO) && !ip.equals(Ip.MAX)),
        "NextHopVrf IP must be a valid concrete IP address. Received %s",
        ip);
    _vrfName = vrfName;
    _ip = ip;
  }

  /** Re-intern after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return CACHE.getUnchecked(this);
  }
}
