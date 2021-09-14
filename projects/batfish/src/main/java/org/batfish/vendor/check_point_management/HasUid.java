package org.batfish.vendor.check_point_management;

import javax.annotation.Nonnull;

/** An object that has a {@link Uid}. */
public interface HasUid {
  @Nonnull
  Uid getUid();
}
