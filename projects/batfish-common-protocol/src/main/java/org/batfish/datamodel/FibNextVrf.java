package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.FibActionVisitor;

/**
 * A {@link FibAction} indicating that lookup should be delegated to another VRF.
 *
 * <p>The optional IP specifies which IP to look up in the target VRF's FIB. If null, the packet's
 * destination IP is used.
 */
@ParametersAreNonnullByDefault
public class FibNextVrf implements FibAction {
  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^20: Just some upper bound on cache size, well less than GiB.
  //   (8 bytes data, would be 8 MiB total ignoring overhead).
  private static final LoadingCache<FibNextVrf, FibNextVrf> CACHE =
      Caffeine.newBuilder().softValues().maximumSize(1 << 16).build(key -> key);

  private final @Nonnull String _nextVrf;
  private final @Nullable Ip _ip;
  private transient int _hashCode;

  private FibNextVrf(String nextVrf, @Nullable Ip ip) {
    checkArgument(
        ip == null || (!ip.equals(Ip.AUTO) && !ip.equals(Ip.ZERO) && !ip.equals(Ip.MAX)),
        "FibNextVrf IP must be a valid concrete IP address. Received %s",
        ip);
    _nextVrf = nextVrf;
    _ip = ip;
  }

  /**
   * Create a FibNextVrf with only a VRF name (packet destination IP will be used for lookup)
   *
   * @param nextVrf the target VRF name
   */
  public static FibNextVrf of(String nextVrf) {
    return CACHE.get(new FibNextVrf(nextVrf, null));
  }

  /**
   * Create a FibNextVrf with a VRF name and optional IP
   *
   * @param nextVrf the target VRF name
   * @param ip the IP to look up in the target VRF's FIB (if null, packet destination is used)
   * @throws IllegalArgumentException if the IP is invalid, e.g., {@link Ip#AUTO}
   */
  public static FibNextVrf of(String nextVrf, @Nullable Ip ip) {
    return CACHE.get(new FibNextVrf(nextVrf, ip));
  }

  @Override
  public <T> T accept(FibActionVisitor<T> visitor) {
    return visitor.visitFibNextVrf(this);
  }

  public @Nonnull String getNextVrf() {
    return _nextVrf;
  }

  /** Optional IP to look up in the target VRF's FIB (if null, packet destination is used) */
  public @Nullable Ip getIp() {
    return _ip;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FibNextVrf)) {
      return false;
    }
    FibNextVrf that = (FibNextVrf) obj;
    return _nextVrf.equals(that._nextVrf) && Objects.equals(_ip, that._ip);
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      // This hashcode could be hot, so compute manually instead of Objects.hash(...)
      h = _nextVrf.hashCode() + (_ip == null ? 0 : 31 * _ip.hashCode());
      _hashCode = h;
    }
    return h;
  }

  /** Re-intern after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return CACHE.get(this);
  }
}
