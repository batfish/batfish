package org.batfish.vendor.check_point_management;

import javax.annotation.Nonnull;

public interface INamedManagementObject {
  @Nonnull
  String getName();

  @Nonnull
  Uid getUid();
}
