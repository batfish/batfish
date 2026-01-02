package org.batfish.specifier;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpSpace;

/** Information about whether/how to treat a location as a source or sink of traffic. */
public final class LocationInfo implements Serializable {
  public static final LocationInfo NOTHING =
      new LocationInfo(false, EmptyIpSpace.INSTANCE, EmptyIpSpace.INSTANCE);

  private final boolean _isSource;
  private final IpSpace _sourceIps;
  private final IpSpace _arpIps;

  public LocationInfo(boolean isSource, IpSpace sourceIps, IpSpace arpIps) {
    _isSource = isSource;
    _sourceIps = sourceIps;
    _arpIps = arpIps;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LocationInfo)) {
      return false;
    }
    LocationInfo other = (LocationInfo) o;
    return _isSource == other._isSource
        && _sourceIps.equals(other._sourceIps)
        && _arpIps.equals(other._arpIps);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_isSource, _sourceIps, _arpIps);
  }

  /**
   * Whether a location should be considered a source of traffic in batfish analyses. When false,
   * the user can still explicitly specify to use the location as a source.
   *
   * <p>For example, this might be false for an infrastructure interface, since most of the time the
   * user wants to analyze end-to-end network behavior. However, they can still explicitly source
   * traffic from that location, which can be useful for debugging in some cases.
   */
  public boolean isSource() {
    return _isSource;
  }

  /**
   * The set of IP addresses to use as source IPs for flows originating from the location by
   * default, when the location is used as a source of traffic. Users can override this via question
   * parameters. For non-sources, these IPs will be used when the user explicitly specifies to use
   * the location as a source but does not explicitly specify the source IPs to use.
   */
  public IpSpace getSourceIps() {
    return _sourceIps;
  }

  /**
   * For {@link InterfaceLinkLocation} only. Used for disposition assignment when a flow terminates
   * at an {@link InterfaceLinkLocation}. Batfish will give a successful disposition for these IPs,
   * as if ARP resolution indicates the presence of a device listening on that address at that
   * location, without modeling the device itself.
   */
  public IpSpace getArpIps() {
    return _arpIps;
  }
}
