package org.batfish.representation.cisco_asa;

import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;

/**
 * Represents a reference to a named network object.
 *
 * @see <a
 *     href="https://www.cisco.com/c/en/us/td/docs/security/asa/asa90/configuration/guide/asa_90_cli_config/acl_objects.html">ASA
 *     documentation</a>
 * @see NetworkObjectGroupAddressSpecifier
 */
public class NetworkObjectAddressSpecifier implements AccessListAddressSpecifier {

  private final String _name;

  public NetworkObjectAddressSpecifier(String name) {
    _name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NetworkObjectAddressSpecifier)) {
      return false;
    }
    NetworkObjectAddressSpecifier that = (NetworkObjectAddressSpecifier) o;
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
    return new IpSpaceReference(_name, String.format("Match network object: '%s'", _name));
  }
}
