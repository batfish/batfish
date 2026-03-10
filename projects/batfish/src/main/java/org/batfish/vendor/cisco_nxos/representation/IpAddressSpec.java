package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;

public interface IpAddressSpec extends Serializable {

  <T> T accept(IpAddressSpecVisitor<T> visitor);
}
