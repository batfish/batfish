package org.batfish.representation.cisco;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Track whether an interface is up, and optionally whether both of:
 *
 * <ul>
 *   <li>The interface has an IP address, either manual or via DHCP
 *   <li>ip routing is enabled (globally)
 * </ul>
 */
@ParametersAreNonnullByDefault
public class TrackInterface implements Track {

  public TrackInterface(String interfaceName) {
    _interfaceName = interfaceName;
  }

  @Override
  public <T> T accept(TrackVisitor<T> visitor) {
    return visitor.visitTrackInterface(this);
  }

  public @Nonnull String getInterfaceName() {
    return _interfaceName;
  }

  public void setInterfaceName(String interfaceName) {
    _interfaceName = interfaceName;
  }

  /**
   * If true, then also check that the interface has an IP address manually or via DHCP, and ip
   * routing is globally enabled.
   */
  public boolean getIpRouting() {
    return _ipRouting;
  }

  public void setIpRouting(boolean ipRouting) {
    _ipRouting = ipRouting;
  }

  private boolean _ipRouting;
  private @Nonnull String _interfaceName;
}
