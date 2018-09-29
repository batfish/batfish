package org.batfish.identifiers;

import javax.annotation.Nullable;

public class NetworkId extends Id {

  private final String _name;

  public NetworkId(String id, @Nullable String name) {
    super(id);
    _name = name;
  }

  public @Nullable String getName() {
    return _name;
  }
}
