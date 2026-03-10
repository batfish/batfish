package org.batfish.datamodel;

import javax.annotation.ParametersAreNonnullByDefault;

/** A route builder with writable weight */
@ParametersAreNonnullByDefault
public interface HasWritableWeight<B extends AbstractRouteBuilder<B, R>, R extends AbstractRoute>
    extends HasReadableWeight {

  B setWeight(int weight);
}
