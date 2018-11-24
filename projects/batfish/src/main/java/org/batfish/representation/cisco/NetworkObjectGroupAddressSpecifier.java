package org.batfish.representation.cisco;

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

  /** */
  private static final long serialVersionUID = 1L;

  private final String _name;

  public NetworkObjectGroupAddressSpecifier(String name) {
    _name = name;
  }

  @Override
  @Nonnull
  public IpSpace toIpSpace() {
    return new IpSpaceReference(_name, String.format("Match network object-group: '%s'", _name));
  }
}
