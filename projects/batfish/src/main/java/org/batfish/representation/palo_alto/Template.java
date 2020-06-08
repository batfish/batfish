package org.batfish.representation.palo_alto;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a Palo Alto configuration template */
@ParametersAreNonnullByDefault
public class Template extends PaloAltoConfiguration {
  private final String _name;
  private @Nullable String _description;

  public Template(String name) {
    _name = name;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public String getName() {
    return _name;
  }

  public void setDescription(String description) {
    _description = description;
  }
}
