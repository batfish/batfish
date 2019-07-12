package org.batfish.representation.cisco_nxos;

import java.io.Serializable;

public interface IpAddressSpec extends Serializable {

  <T> T accept(IpAddressSpecVisitor<T> visitor);
}
