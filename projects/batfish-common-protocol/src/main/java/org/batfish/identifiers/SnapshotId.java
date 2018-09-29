package org.batfish.identifiers;

import javax.annotation.Nullable;

public class SnapshotId extends Id {

  private final String _name;

  public SnapshotId(String id, @Nullable String name) {
    super(id);
    _name = name;
  }

  public @Nullable String getName() {
    return _name;
  }
}
