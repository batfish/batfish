package org.batfish.specifier;

import java.util.Set;
import org.batfish.datamodel.Protocol;

/** An abstract specification of a set of application protocols. */
public interface ApplicationSpecifier {
  /** Returns the application protocols that match this specifier. */
  Set<Protocol> resolve();
}
