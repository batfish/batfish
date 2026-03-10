package org.batfish.specifier;

import java.util.Set;
import org.batfish.datamodel.applications.Application;

/** An abstract specification of a set of {@link Application}s. */
public interface ApplicationSpecifier {
  /** Returns the applications that match this specifier. */
  Set<Application> resolve();
}
