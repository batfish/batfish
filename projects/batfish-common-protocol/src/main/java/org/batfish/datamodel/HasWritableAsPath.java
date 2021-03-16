package org.batfish.datamodel;

import javax.annotation.ParametersAreNonnullByDefault;

/** A route builder with writable as-path */
@ParametersAreNonnullByDefault
public interface HasWritableAsPath<B extends AbstractRouteBuilder<B, R>, R extends AbstractRoute>
    extends HasReadableAsPath {

  B setAsPath(AsPath asPath);
}
