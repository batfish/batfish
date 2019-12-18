package org.batfish.representation.cisco_xr;

import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;

/**
 * Represents a reference to a named network object-group.
 *
 * @see <a
 *     href="https://www.cisco.com/c/en/us/td/docs/security/asa/asa82/configuration/guide/config/objectgroups.html">ASA
 *     documentation</a>
 * @see NetworkObjectAddressSpecifier
 */
public class NetworkObjectGroupAddressSpecifier implements AccessListAddressSpecifier {

  private final String _name;

  public NetworkObjectGroupAddressSpecifier(String name) {
    _name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NetworkObjectGroupAddressSpecifier)) {
      return false;
    }
    NetworkObjectGroupAddressSpecifier that = (NetworkObjectGroupAddressSpecifier) o;
    return Objects.equals(_name, that._name);
  }

  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_name);
  }

  @Override
  @Nonnull
  public IpSpace toIpSpace() {
    return new IpSpaceReference(_name, String.format("Match network object-group: '%s'", _name));
  }
}
