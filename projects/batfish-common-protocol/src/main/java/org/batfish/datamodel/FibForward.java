package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.toStringHelper;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.FibActionVisitor;

/**
 * A {@link FibAction} directing the device to ARP for a given IP on a given interface, then forward
 * a packet given a successful reply.
 */
@ParametersAreNonnullByDefault
public final class FibForward implements FibAction {

  private final @Nonnull Ip _arpIp;
  private final @Nonnull String _interfaceName;

  public FibForward(Ip arpIp, String interfaceName) {
    _arpIp = arpIp;
    _interfaceName = interfaceName;
  }

  /** IP that a router would ARP for to send the packet */
  public @Nonnull Ip getArpIp() {
    return _arpIp;
  }

  /** Name of the interface to be used to send the packet out */
  public @Nonnull String getInterfaceName() {
    return _interfaceName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FibForward)) {
      return false;
    }
    FibForward rhs = (FibForward) o;
    return _arpIp.equals(rhs._arpIp) && _interfaceName.equals(rhs._interfaceName);
  }

  @Override
  public int hashCode() {
    return _arpIp.hashCode() * 31 + _interfaceName.hashCode();
  }

  @Override
  public @Nonnull String toString() {
    return toStringHelper(FibForward.class)
        .add("arpIp", _arpIp)
        .add("interfaceName", _interfaceName)
        .toString();
  }

  @Override
  public <T> T accept(FibActionVisitor<T> visitor) {
    return visitor.visitFibForward(this);
  }
}
