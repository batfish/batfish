package org.batfish.specifier;

import java.util.Set;
import org.batfish.datamodel.IpProtocol;

/** An abstract specification of a set of IP protocols. */
public interface IpProtocolSpecifier {
  /** Returns the IP protocols that match this specifier. */
  Set<IpProtocol> resolve();
}
