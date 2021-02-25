package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class Condition implements Serializable {

  public Condition(String name) {
    _name = name;
  }

  public @Nonnull IfRouteExists getOrCreateIfRouteExists() {
    if (_ifRouteExists == null) {
      _ifRouteExists = new IfRouteExists();
    }
    return _ifRouteExists;
  }

  public @Nullable IfRouteExists getIfRouteExists() {
    return _ifRouteExists;
  }

  public @Nonnull String getName() {
    return _name;
  }

  private final @Nonnull String _name;
  private @Nullable IfRouteExists _ifRouteExists;
}
