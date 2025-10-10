package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.FibActionVisitor;

/**
 * A {@link FibAction} directing the device to ARP for an IP on a given interface, then forward a
 * packet given a successful reply.
 */
@ParametersAreNonnullByDefault
public final class FibForward implements FibAction {
  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^20: Just some upper bound on cache size, well less than GiB.
  //   (16 bytes data, would be 16 MiB total ignoring overhead).
  private static final LoadingCache<FibForward, FibForward> CACHE =
      Caffeine.newBuilder().softValues().maximumSize(1 << 16).build(x -> x);

  private final @Nullable Ip _arpIp;
  private final @Nonnull String _interfaceName;

  private FibForward(@Nullable Ip arpIp, String interfaceName) {
    // TODO: remove once Route.UNSET_NEXT_HOP_IP and Ip.AUTO are killed
    assert !Route.UNSET_ROUTE_NEXT_HOP_IP.equals(arpIp);
    _arpIp = arpIp;
    _interfaceName = interfaceName;
  }

  public static FibForward of(@Nullable Ip arpIp, String interfaceName) {
    return CACHE.get(new FibForward(arpIp, interfaceName));
  }

  /**
   * IP that a router would ARP for to send the packet. If {@link Optional#empty()}, then router
   * should use ARP IP of an earlier entry in the resolution chain, or the destination IP if this is
   * the only entry in the chain.
   */
  public @Nonnull Optional<Ip> getArpIp() {
    return Optional.ofNullable(_arpIp);
  }

  /** Name of the interface to be used to send the packet out */
  public @Nonnull String getInterfaceName() {
    return _interfaceName;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof FibForward)) {
      return false;
    }
    FibForward rhs = (FibForward) o;
    return Objects.equals(_arpIp, rhs._arpIp) && _interfaceName.equals(rhs._interfaceName);
  }

  @Override
  public int hashCode() {
    return (_arpIp != null ? _arpIp.hashCode() : 0) * 31 + _interfaceName.hashCode();
  }

  @Override
  public @Nonnull String toString() {
    return toStringHelper(FibForward.class)
        .omitNullValues()
        .add("arpIp", _arpIp)
        .add("interfaceName", _interfaceName)
        .toString();
  }

  @Override
  public <T> T accept(FibActionVisitor<T> visitor) {
    return visitor.visitFibForward(this);
  }

  /** Re-intern after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return CACHE.get(this);
  }
}
