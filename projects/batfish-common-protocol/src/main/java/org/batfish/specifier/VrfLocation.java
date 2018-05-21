package org.batfish.specifier;

import java.util.Objects;

/** Identifies the {@link Location} of a VRF in the network. */
public class VrfLocation implements Location {
  private final String _hostname;

  private final String _vrf;

  public VrfLocation(String hostname, String vrf) {
    _hostname = hostname;
    _vrf = vrf;
  }

  @Override
  public <T> T accept(LocationVisitor<T> visitor) {
    return visitor.visitVrfLocation(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VrfLocation that = (VrfLocation) o;
    return Objects.equals(_hostname, that._hostname) && Objects.equals(_vrf, that._vrf);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _vrf);
  }

  public String getHostname() {
    return _hostname;
  }

  public String getVrf() {
    return _vrf;
  }
}
