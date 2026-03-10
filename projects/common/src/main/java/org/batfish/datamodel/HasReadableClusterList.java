package org.batfish.datamodel;

import java.util.Set;
import javax.annotation.Nonnull;

/** A route or route builder with readable BGP cluster-list. */
public interface HasReadableClusterList {

  @Nonnull
  Set<Long> getClusterList();
}
