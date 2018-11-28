package org.batfish.representation.cisco;

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

  /** */
  private static final long serialVersionUID = 1L;

  private final String _name;

  public NetworkObjectAddressSpecifier(String name) {
    _name = name;
  }

  @Override
  @Nonnull
  public IpSpace toIpSpace() {
    return new IpSpaceReference(_name, String.format("Match network object: '%s'", _name));
  }
}
