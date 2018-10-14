package org.batfish.identifiers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class SnapshotId extends Id {
  public SnapshotId(String id) {
    super(id);
  }

  @JsonCreator
  private static SnapshotId jsonCreator(@Nullable String id) {
    checkArgument(!isNullOrEmpty(id), "Id must be provided");
    return new SnapshotId(id);
  }
}
