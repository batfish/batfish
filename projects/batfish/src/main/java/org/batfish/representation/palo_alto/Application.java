package org.batfish.representation.palo_alto;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a Palo Alto application object */
@ParametersAreNonnullByDefault
public final class Application implements Serializable {

  public static final class Builder {
    private String _name;
    private String _description;

    private Builder() {}

    public @Nonnull Application build() {
      checkArgument(_name != null, "Application is missing name");
      Application app = new Application(_name);
      if (_description != null) {
        app.setDescription(_description);
      }
      return app;
    }

    public Builder setDescription(String description) {
      _description = description;
      return this;
    }

    public Builder setName(String name) {
      _name = name;
      return this;
    }
  }

  private static final long serialVersionUID = 1L;

  @Nullable private String _description;

  @Nonnull private final String _name;

  public Application(String name) {
    _name = name;
  }

  public static Builder builder() {
    return new Builder();
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
