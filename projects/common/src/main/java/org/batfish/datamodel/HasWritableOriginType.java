package org.batfish.datamodel;

import javax.annotation.ParametersAreNonnullByDefault;

/** A route builder with writable origin type */
@ParametersAreNonnullByDefault
public interface HasWritableOriginType<
        B extends AbstractRouteBuilder<B, R>, R extends AbstractRoute>
    extends HasReadableOriginType {

  B setOriginType(OriginType originType);
}
