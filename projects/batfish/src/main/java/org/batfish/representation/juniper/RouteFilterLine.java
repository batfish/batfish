package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;

public abstract class RouteFilterLine implements Serializable {

  private final @Nonnull PsThens _thens;

  public RouteFilterLine() {
    _thens = new PsThens();
  }

  @Override
  public abstract boolean equals(Object o);

  public final @Nonnull PsThens getThens() {
    return _thens;
  }

  @Override
  public abstract int hashCode();
}
