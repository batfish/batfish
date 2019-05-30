package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a Palo Alto application object */
@ParametersAreNonnullByDefault
public final class Application implements Serializable {
  private static final long serialVersionUID = 1L;

  @Nullable private String _description;

  @Nonnull private final String _name;

  public Application(String name) {
    _name = name;
  }

  @Nullable
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
