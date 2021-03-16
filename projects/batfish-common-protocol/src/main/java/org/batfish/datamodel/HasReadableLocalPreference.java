package org.batfish.datamodel;

import javax.annotation.ParametersAreNonnullByDefault;

/** A route or route builder with readable local preference. */
@ParametersAreNonnullByDefault
public interface HasReadableLocalPreference {

  long getLocalPreference();
}
