package org.batfish.datamodel;

import java.util.Set;
import javax.annotation.Nonnull;

/**
 * A generic route builder of type {@code B} for routes of type {@code R} with a BGP cluster list
 * that may be written.
 */
public interface HasWritableClusterList<
        B extends AbstractRouteBuilder<B, R>, R extends AbstractRoute>
    extends HasReadableClusterList {

  @Nonnull
  B setClusterList(Set<Long> clusterList);
}
