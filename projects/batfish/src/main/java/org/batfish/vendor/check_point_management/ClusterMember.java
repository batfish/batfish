package org.batfish.vendor.check_point_management;

import javax.annotation.Nonnull;

/** A gateway that is a member of a {@link Cluster}. */
public interface ClusterMember {

  /** The specific class type of the {@link Cluster} of which this gateway may be a member. */
  @Nonnull
  Class<? extends Cluster> getClusterClass();
}
