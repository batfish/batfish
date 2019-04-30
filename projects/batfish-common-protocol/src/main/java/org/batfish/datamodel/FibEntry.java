package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Iterables;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class FibEntry implements Serializable {
  private static final long serialVersionUID = 1L;
  @Nonnull private final Ip _arpIP;
  @Nonnull private final String _interfaceName;
  @Nonnull private final List<AbstractRoute> _resolutionSteps;
  private transient int _hashCode;

  /**
   * Create a new FIB entry with the given nextHop/ARP IP, interface name, and resolution steps.
   *
   * <p>Note that at least one resolution step is required (even if it is the route itself, e.g. a
   * connected route)
   */
  public FibEntry(Ip arpIP, String interfaceName, List<AbstractRoute> resolutionSteps) {
    checkArgument(
        !resolutionSteps.isEmpty(), "FIB resolution steps must contain at least one route");
    _arpIP = arpIP;
    _interfaceName = interfaceName;
    _resolutionSteps = resolutionSteps;
  }

  /** IP that a router would ARP for to send the packet */
  @Nonnull
  public Ip getArpIP() {
    return _arpIP;
  }

  /** Name of the interface to be used to send the packet out */
  @Nonnull
  public String getInterfaceName() {
    return _interfaceName;
  }

  /** A chain of routes that explains how the top route was resolved */
  @Nonnull
  public List<AbstractRoute> getResolutionSteps() {
    return _resolutionSteps;
  }

  /** Return the top level route for this entry (before recursive resolution) */
  @Nonnull
  public AbstractRoute getTopLevelRoute() {
    return _resolutionSteps.get(0);
  }

  /** Return the final resolved route */
  public AbstractRoute getResolvedToRoute() {
    return Iterables.getLast(_resolutionSteps);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FibEntry fibEntry = (FibEntry) o;
    return Objects.equals(getArpIP(), fibEntry.getArpIP())
        && Objects.equals(getInterfaceName(), fibEntry.getInterfaceName())
        && Objects.equals(getResolutionSteps(), fibEntry.getResolutionSteps());
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h = _arpIP.hashCode();
      h = 31 * h + _interfaceName.hashCode();
      h = 31 * h + _resolutionSteps.hashCode();
      _hashCode = h;
    }
    return h;
  }
}
