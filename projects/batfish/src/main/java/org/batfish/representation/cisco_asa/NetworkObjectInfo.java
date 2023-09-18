package org.batfish.representation.cisco_asa;

import java.io.Serializable;
import javax.annotation.Nonnull;

public final class NetworkObjectInfo implements Serializable {

  private final @Nonnull String _name;
  private String _description;

  public NetworkObjectInfo(@Nonnull String name) {
    _name = name;
  }

  public String getDescription() {
    return _description;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public void setDescription(String description) {
    _description = description;
  }
}
