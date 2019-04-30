package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.Nonnull;

public final class NetworkObjectInfo implements Serializable {

  private static final long serialVersionUID = 1L;

  @Nonnull private final String _name;
  private String _description;

  public NetworkObjectInfo(@Nonnull String name) {
    _name = name;
  }

  public String getDescription() {
    return _description;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  public void setDescription(String description) {
    _description = description;
  }
}
