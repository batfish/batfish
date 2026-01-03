package org.batfish.datamodel;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A generic route builder of type {@code B} for routes of type {@code R} with a BGP cluster list
 * that may be written.
 */
@ParametersAreNonnullByDefault
public interface HasWritableOriginatorIp<
        B extends AbstractRouteBuilder<B, R>, R extends AbstractRoute>
    extends HasReadableOriginatorIp {

  @Nonnull
  B setOriginatorIp(Ip originatorIp);
}
