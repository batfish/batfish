package org.batfish.datamodel;

import javax.annotation.ParametersAreNonnullByDefault;

/** A route builder with writable local preference */
@ParametersAreNonnullByDefault
public interface HasWritableLocalPreference<
        B extends AbstractRouteBuilder<B, R>, R extends AbstractRoute>
    extends HasReadableLocalPreference {

  B setLocalPreference(long localPreference);
}
